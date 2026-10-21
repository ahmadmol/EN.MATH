CREATE TABLE gem_accounts(owner TEXT PRIMARY KEY, activated INTEGER NOT NULL, balance INTEGER NOT NULL DEFAULT 0 CHECK(balance>=0));
CREATE TABLE gem_exclusions(owner TEXT NOT NULL REFERENCES gem_accounts(owner), achievement TEXT NOT NULL, PRIMARY KEY(owner,achievement));
CREATE TABLE gem_rewards(owner TEXT NOT NULL REFERENCES gem_accounts(owner), achievement TEXT NOT NULL, kind TEXT NOT NULL CHECK(kind IN ('LESSON','MASTERY','PRACTICE','ASSIGNMENT','WEEKLY')), units INTEGER NOT NULL CHECK(units BETWEEN 0 AND 20), occurred INTEGER NOT NULL, day TEXT NOT NULL, week TEXT NOT NULL, PRIMARY KEY(owner,achievement));
CREATE TABLE gem_unlocks(owner TEXT NOT NULL REFERENCES gem_accounts(owner), item TEXT NOT NULL, price INTEGER NOT NULL CHECK(price IN (40,120)), opened INTEGER NOT NULL, PRIMARY KEY(owner,item));
CREATE TABLE gem_lessons(owner TEXT NOT NULL REFERENCES gem_accounts(owner), lesson TEXT NOT NULL, PRIMARY KEY(owner,lesson));
CREATE TABLE gem_assessments(owner TEXT NOT NULL REFERENCES gem_accounts(owner), activity TEXT NOT NULL, topic TEXT NOT NULL, quiz INTEGER NOT NULL CHECK(quiz IN (0,1)), accuracy REAL NOT NULL CHECK(accuracy BETWEEN 0 AND 1), PRIMARY KEY(owner,activity));
