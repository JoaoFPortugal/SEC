---
--- Users
--- Realistically we wouldn't store the IP and port where a user
--- is listening, and would get it during run-time. But for simplicity
--- we do it this way.
---
CREATE TABLE IF NOT EXISTS `users` (
	`uid` INTEGER PRIMARY KEY
);

---
--- Goods
---
CREATE TABLE IF NOT EXISTS `goods` (
	`gid` INTEGER PRIMARY KEY,
	`owner_id` INTEGER,
	`for_sale` INTEGER, -- 0 (false), 1 (true)
	`tag` INTEGER,
	FOREIGN KEY(`owner_id`) REFERENCES `users`(`uid`)
);

---
--- tags
---
CREATE TABLE IF NOT EXISTS `tags` (
	`tag` INTEGER,
	`owner_id` INTEGER,
	FOREIGN KEY(`owner_id`) REFERENCES  `users`(`uid`)
);

---
--- Log
---
CREATE TABLE IF NOT EXISTS 'log' (
	`lid` INTEGER PRIMARY KEY,
	`uid` INTEGER,
	`query` TEXT,
	`result` TEXT,
	`timestamp` INTEGER,
	`error` TEXT,
	`attack` TEXT,
	FOREIGN KEY(`uid`) REFERENCES `users`(`uid`)
);