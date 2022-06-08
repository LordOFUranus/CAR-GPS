CREATE TABLE "accounts"(
	"id" 			INTEGER PRIMARY KEY AUTOINCREMENT,
	"first_name"	TEXT NOT NULL,
	"last_name" 	TEXT NOT NULL,
	"iid" 			INTEGER NOT NULL UNIQUE,
	"phone" 		INTEGER NOT NULL UNIQUE,
	"pass" 			TEXT NOT NULL,
	"created_at" 	INTEGER NOT NULL
);

INSERT INTO "accounts"("first_name", "last_name", "iid", "phone", "pass","created_at")
VALUES("Zhanibek", "Zhumanov", 020517550083, 77022211397, "1234567890",0);

CREATE TABLE "switchers" (
"ID"	INTEGER,
"SWITCHER_UPDATE"	INTEGER NOT NULL,
"SWITCHER_GPS"	INTEGER NOT NULL,
PRIMARY KEY("ID")
)

CREATE TABLE "switchers_settings" (
"account_id"	INTEGER NOT NULL,
"switcher_id"	INTEGER NOT NULL,
"is_active"	INTEGER NOT NULL,
FOREIGN KEY("account_id") REFERENCES "accounts"(_id)
ON UPDATE CASCADE ON DELETE CASCADE,
FOREIGN KEY ("switcher_id") REFERENCES "switchers"(id)
ON UPDATE CASCADE ON DELETE CASCADE,
UNIQUE("account_id","switcher_id")
)
