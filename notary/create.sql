---
--- Users
---
CREATE TABLE IF NOT EXISTS `users` (
	`uid` INTEGER PRIMARY KEY,
	`username` TEXT UNIQUE NOT NULL
);

---
--- Goods
---
CREATE TABLE IF NOT EXISTS `goods` (
	`gid` INTEGER PRIMARY KEY,
	`name` TEXT UNIQUE NOT NULL,
	`owner_id` INTEGER,
	FOREIGN KEY(`owner_id`) REFERENCES `users`(`uid`)
);