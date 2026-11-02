"""Host SQLite checks using the actual Android asset schema. Does not access a device.
Tests SQL constraints/transactions; Android SQLiteOpenHelper/Keystore/UI are not executed here.
"""
from contextlib import closing
import sqlite3
import tempfile
import threading
import unittest
from pathlib import Path
from concurrent.futures import ThreadPoolExecutor

SCHEMA=Path(__file__).resolve().parents[1]/'app/src/main/assets/local_gems/schema.sql'
class LocalGemsSQLiteTest(unittest.TestCase):
    def setUp(self):
        self.temp=tempfile.TemporaryDirectory();self.path=Path(self.temp.name)/'local.sqlite'
        with closing(self.db()) as db:
            db.executescript(SCHEMA.read_text(encoding='utf-8'))
            for owner in ('a@test','b@test'):
                db.execute('INSERT INTO gem_accounts VALUES(?,?,?)',(owner,1000,40))
                for i in range(4):db.execute('INSERT INTO gem_rewards VALUES(?,?,?,?,?,?,?)',(owner,'LESSON:'+str(i),'LESSON',10,1000,'2026-10-20','2026-10-19'))
    def tearDown(self):self.temp.cleanup()
    def db(self):
        db=sqlite3.connect(self.path,timeout=15,isolation_level=None);db.execute('PRAGMA foreign_keys=ON');return db
    def spend(self,owner='a@test',item='limits-guide-v1',fail=False):
        db=self.db()
        try:
            db.execute('BEGIN IMMEDIATE')
            if db.execute('SELECT 1 FROM gem_unlocks WHERE owner=? AND item=?',(owner,item)).fetchone():db.commit();return 'OWNED'
            balance=db.execute('SELECT balance FROM gem_accounts WHERE owner=?',(owner,)).fetchone()[0]
            if balance<40:db.commit();return 'INSUFFICIENT'
            db.execute('INSERT INTO gem_unlocks VALUES(?,?,?,?)',(owner,item,40,2000))
            if fail:raise RuntimeError('crash before balance update')
            db.execute('UPDATE gem_accounts SET balance=balance-40 WHERE owner=?',(owner,));db.commit();return 'UNLOCKED'
        except Exception:db.rollback();raise
        finally:db.close()
    def test_debit_and_grant_rollback_together(self):
        with self.assertRaises(RuntimeError):self.spend(fail=True)
        with closing(self.db()) as db:
            self.assertEqual(40,db.execute('SELECT balance FROM gem_accounts WHERE owner=?',('a@test',)).fetchone()[0])
            self.assertEqual(0,db.execute('SELECT count(*) FROM gem_unlocks').fetchone()[0])
    def test_duplicate_confirmation_and_reopen_are_free(self):
        self.assertEqual('UNLOCKED',self.spend())
        for _ in range(20):self.assertEqual('OWNED',self.spend())
        with closing(self.db()) as db:self.assertEqual((0,),db.execute('SELECT balance FROM gem_accounts WHERE owner=?',('a@test',)).fetchone())
    def test_concurrent_spend_cannot_make_negative_balance(self):
        gate=threading.Barrier(12)
        def work(i):gate.wait();return self.spend(item='item-'+str(i%2))
        with ThreadPoolExecutor(max_workers=12) as executor:results=list(executor.map(work,range(12)))
        self.assertEqual(1,results.count('UNLOCKED'))
        with closing(self.db()) as db:
            self.assertEqual(1,db.execute('SELECT count(*) FROM gem_unlocks').fetchone()[0])
            self.assertEqual(0,db.execute('SELECT balance FROM gem_accounts WHERE owner=?',('a@test',)).fetchone()[0])
            self.assertEqual('ok',db.execute('PRAGMA integrity_check').fetchone()[0]);self.assertEqual([],db.execute('PRAGMA foreign_key_check').fetchall())
    def test_owner_isolation_and_persistence_after_reopen(self):
        self.spend()
        with closing(self.db()) as db:
            self.assertEqual(40,db.execute('SELECT balance FROM gem_accounts WHERE owner=?',('b@test',)).fetchone()[0])
            self.assertEqual(0,db.execute('SELECT count(*) FROM gem_unlocks WHERE owner=?',('b@test',)).fetchone()[0])
        self.assertEqual('UNLOCKED',self.spend(owner='b@test'))
        with closing(self.db()) as db:self.assertEqual(2,db.execute('SELECT count(*) FROM gem_unlocks').fetchone()[0])
    def test_reward_identity_is_unique_per_owner(self):
        with closing(self.db()) as db:
            with self.assertRaises(sqlite3.IntegrityError):db.execute('INSERT INTO gem_rewards VALUES(?,?,?,?,?,?,?)',('a@test','LESSON:0','LESSON',10,1001,'2026-10-20','2026-10-19'))
    def test_balance_constraint_and_foreign_keys_fail_closed(self):
        with closing(self.db()) as db:
            with self.assertRaises(sqlite3.IntegrityError):db.execute('UPDATE gem_accounts SET balance=-1 WHERE owner=?',('a@test',))
            with self.assertRaises(sqlite3.IntegrityError):db.execute('INSERT INTO gem_unlocks VALUES(?,?,?,?)',('missing','x',40,1000))
    def test_unlock_price_and_reward_units_are_constrained(self):
        with closing(self.db()) as db:
            with self.assertRaises(sqlite3.IntegrityError):db.execute('INSERT INTO gem_unlocks VALUES(?,?,?,?)',('a@test','x',-40,1000))
            with self.assertRaises(sqlite3.IntegrityError):db.execute('INSERT INTO gem_rewards VALUES(?,?,?,?,?,?,?)',('a@test','bad','LESSON',-1,1000,'d','w'))
    def test_no_legacy_learning_schema_is_modified(self):
        with closing(self.db()) as db:
            tables={r[0] for r in db.execute("SELECT name FROM sqlite_master WHERE type='table'")}
            self.assertEqual({'gem_accounts','gem_exclusions','gem_rewards','gem_unlocks','gem_lessons','gem_assessments'},tables)
    def test_legacy_owner_activation_and_owned_access_preserve_all_six_tables(self):
        owner='legacy-owner@example.test'
        tables=('gem_accounts','gem_exclusions','gem_rewards','gem_unlocks','gem_lessons','gem_assessments')
        with closing(self.db()) as db:
            db.execute('INSERT INTO gem_accounts VALUES(?,?,?)',(owner,1000,40))
            for i in range(4):
                db.execute('INSERT INTO gem_rewards VALUES(?,?,?,?,?,?,?)',
                    (owner,'LESSON:legacy-'+str(i),'LESSON',10,1000,'2026-10-20','2026-10-19'))
            db.execute('INSERT INTO gem_exclusions VALUES(?,?)',(owner,'LESSON:baseline'))
            db.execute('INSERT INTO gem_lessons VALUES(?,?)',(owner,'legacy-lesson'))
            db.execute('INSERT INTO gem_assessments VALUES(?,?,?,?,?)',(owner,'legacy-attempt','limits',0,1.0))
        self.assertEqual('UNLOCKED',self.spend(owner=owner))
        def snapshot(db):
            return {table:db.execute('SELECT * FROM '+table+' ORDER BY rowid').fetchall() for table in tables}
        with closing(self.db()) as db:before=snapshot(db)
        with closing(self.db()) as db:
            # Same INSERT IGNORE activation contract as Android; existing ownership is unchanged.
            db.execute('INSERT OR IGNORE INTO gem_accounts VALUES(?,?,?)',(owner,9999,0))
            self.assertEqual((0,),db.execute('SELECT balance FROM gem_accounts WHERE owner=?',(owner,)).fetchone())
            self.assertEqual(('limits-guide-v1',40),db.execute('SELECT item,price FROM gem_unlocks WHERE owner=?',(owner,)).fetchone())
            self.assertEqual(before,snapshot(db))
        self.assertEqual('OWNED',self.spend(owner=owner))
        with closing(self.db()) as db:self.assertEqual(before,snapshot(db))
    def test_historical_weekly_reward_and_balance_survive_reopen_unchanged(self):
        owner='historical-weekly@example.test'
        with closing(self.db()) as db:
            db.execute('INSERT INTO gem_accounts VALUES(?,?,?)',(owner,1000,15))
            db.execute('INSERT INTO gem_rewards VALUES(?,?,?,?,?,?,?)',
                (owner,'WEEKLY:2026-10-19','WEEKLY',15,1000,'2026-10-20','2026-10-19'))
        with closing(self.db()) as db:
            self.assertEqual((15,),db.execute('SELECT balance FROM gem_accounts WHERE owner=?',(owner,)).fetchone())
            self.assertEqual(('WEEKLY:2026-10-19','WEEKLY',15),
                db.execute('SELECT achievement,kind,units FROM gem_rewards WHERE owner=?',(owner,)).fetchone())
if __name__=='__main__':unittest.main(verbosity=2)
