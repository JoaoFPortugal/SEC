---
--- Users
---
CREATE TABLE IF NOT EXISTS `users` (
	`uid` INTEGER PRIMARY KEY
);

---
--- Goods
---
CREATE TABLE IF NOT EXISTS `goods` (
	`gid` INTEGER PRIMARY KEY,
	`name` TEXT UNIQUE NOT NULL,
	`owner_id` INTEGER,
	`for_sale` INTEGER, -- 0 (false), 1 (true)
	FOREIGN KEY(`owner_id`) REFERENCES `users`(`uid`)
);