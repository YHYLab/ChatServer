CREATE TABLE `chat_log` (
  `insert_d` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `msg` varchar(500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
  
CREATE TABLE `concurrent_user_number`(
  `user_number`     int NOT NULL,
  `inserted_at`  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
 );
 
CREATE TABLE `block_user`(
  `player_id`     varchar(200),
  `inserted_at`  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
 );