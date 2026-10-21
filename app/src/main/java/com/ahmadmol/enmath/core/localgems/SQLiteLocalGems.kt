package com.ahmadmol.enmath.core.localgems

import android.content.ContentValues
import android.content.Context
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabaseCorruptException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/** Additional database only; never reads/writes enmath_demo or the retired server caches. */
class SQLiteLocalGems(context: Context) : LocalGemsStorage {
    private val app = context.applicationContext
    private val helper = object : SQLiteOpenHelper(app, "enmath_local_gems_v1.db", null, 1,
        DatabaseErrorHandler { throw SQLiteDatabaseCorruptException("Gems data preserved; automatic deletion disabled") }) {
        override fun onConfigure(db: SQLiteDatabase) { db.setForeignKeyConstraintsEnabled(true) }
        override fun onCreate(db: SQLiteDatabase) {
            app.assets.open("local_gems/schema.sql").bufferedReader().use { it.readText() }
                .split(';').map(String::trim).filter(String::isNotEmpty).forEach(db::execSQL)
        }
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            error("Unsupported Gems schema: preserve existing data")
        }
    }
    private fun values(vararg pairs: Pair<String, Any>) = ContentValues().apply {
        pairs.forEach { (k,v) -> when(v) { is String -> put(k,v); is Int -> put(k,v); is Long -> put(k,v); is Float -> put(k,v); else -> error("Unsupported value") } }
    }
    private fun rows(db: SQLiteDatabase, table: String, owner: String, visit: (android.database.Cursor) -> Unit) {
        db.query(table, null, "owner=?", arrayOf(owner), null, null, "rowid ASC").use { c -> while(c.moveToNext()) visit(c) }
    }
    private fun load(db: SQLiteDatabase, owner: String): GemAccount {
        var at: Long? = null; var balance = 0
        rows(db,"gem_accounts",owner) { at=it.getLong(it.getColumnIndexOrThrow("activated")); balance=it.getInt(it.getColumnIndexOrThrow("balance")) }
        val excluded=mutableSetOf<String>();val rewards=mutableListOf<GemRewardEntry>();val unlocks=mutableListOf<GemUnlock>();val lessons=mutableSetOf<String>();val assessments=mutableListOf<GemAssessment>()
        rows(db,"gem_exclusions",owner) { excluded+=it.getString(it.getColumnIndexOrThrow("achievement")) }
        rows(db,"gem_rewards",owner) { c ->
            fun s(k:String)=c.getString(c.getColumnIndexOrThrow(k))
            rewards+=GemRewardEntry(s("achievement"),GemReward.valueOf(s("kind")),c.getInt(c.getColumnIndexOrThrow("units")),c.getLong(c.getColumnIndexOrThrow("occurred")),s("day"),s("week"))
        }
        rows(db,"gem_unlocks",owner) { unlocks+=GemUnlock(it.getString(it.getColumnIndexOrThrow("item")),it.getInt(it.getColumnIndexOrThrow("price")),it.getLong(it.getColumnIndexOrThrow("opened"))) }
        rows(db,"gem_lessons",owner) { lessons+=it.getString(it.getColumnIndexOrThrow("lesson")) }
        rows(db,"gem_assessments",owner) { assessments+=GemAssessment(it.getString(it.getColumnIndexOrThrow("activity")),it.getString(it.getColumnIndexOrThrow("topic")),it.getInt(it.getColumnIndexOrThrow("quiz"))==1,it.getFloat(it.getColumnIndexOrThrow("accuracy"))) }
        return GemAccount(owner,checkNotNull(at),balance,excluded,rewards,unlocks,lessons,assessments).also(LocalGemsPolicy::validate)
    }
    private fun <T> transaction(block: (SQLiteDatabase)->T): T {
        val db=helper.writableDatabase;db.beginTransaction()
        try { return block(db).also { db.setTransactionSuccessful() } } finally { db.endTransaction() }
    }
    @Synchronized override fun activate(owner: String, excluded: Set<String>, at: Long): GemAccount = transaction { db ->
        val inserted=db.insertWithOnConflict("gem_accounts",null,values("owner" to owner,"activated" to at,"balance" to 0),SQLiteDatabase.CONFLICT_IGNORE)
        if(inserted!=-1L) excluded.forEach { db.insertOrThrow("gem_exclusions",null,values("owner" to owner,"achievement" to it)) }
        load(db,owner)
    }
    @Synchronized override fun read(owner: String): GemAccount = transaction { load(it,owner) }
    @Synchronized override fun change(owner: String, transform: (GemAccount)->GemAccount): GemAccount = transaction { db ->
        val old=load(db,owner);val next=transform(old)
        check(next.owner==owner && next.activatedAt==old.activatedAt && next.excluded==old.excluded)
        check(next.rewards.containsAll(old.rewards) && next.unlocks.containsAll(old.unlocks) && next.lessons.containsAll(old.lessons))
        LocalGemsPolicy.validate(next)
        next.rewards.filter { reward -> old.rewards.none { it.key==reward.key } }.forEach {
            db.insertOrThrow("gem_rewards",null,values("owner" to owner,"achievement" to it.key,"kind" to it.kind.name,"units" to it.units,"occurred" to it.at,"day" to it.day,"week" to it.week))
        }
        next.unlocks.filter { grant -> old.unlocks.none { it.itemId==grant.itemId } }.forEach {
            db.insertOrThrow("gem_unlocks",null,values("owner" to owner,"item" to it.itemId,"price" to it.price,"opened" to it.at))
        }
        (next.lessons-old.lessons).forEach { db.insertOrThrow("gem_lessons",null,values("owner" to owner,"lesson" to it)) }
        next.assessments.forEach {
            db.insertWithOnConflict("gem_assessments",null,values("owner" to owner,"activity" to it.id,"topic" to it.topic,"quiz" to if(it.quiz) 1 else 0,"accuracy" to it.accuracy),SQLiteDatabase.CONFLICT_REPLACE)
        }
        check(db.update("gem_accounts",values("balance" to next.balance),"owner=?",arrayOf(owner))==1)
        next
    }
}
