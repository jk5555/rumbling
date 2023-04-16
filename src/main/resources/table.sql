CREATE TABLE IF NOT EXISTS chatgptdb.chatgpt_token
(
    token_id INT auto_increment NOT NULL primary key,
    token    varchar(200) NULL,
    enable   TINYINT NULL
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_general_ci;



CREATE TABLE IF NOT EXISTS chatgptdb.chatgpt_message
(
    msg_id INT auto_increment NOT NULL primary key,
    batch_num    varchar(200) NULL,
    user    varchar(200) NULL,
    send_time    timestamp NOT NULL default CURRENT_TIMESTAMP,
    message    varchar(2000) NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_general_ci;

