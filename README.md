# chatServer

Third party dependencies:

HikariCP 3.4.1 (JDBC Connection Pool)
slf4j-api-1.7.25 (Logging comonent for HikiraCP)
slf4j-jdk14-1.7.25 (Logging comonent for HikiraCP)



Database table:

CREATE TABLE users ( id int(11) NOT NULL AUTO_INCREMENT, name varchar(45) CHARACTER SET latin1 NOT NULL, password varchar(45) CHARACTER SET latin1 NOT NULL, PRIMARY KEY (id), UNIQUE KEY name_UNIQUE (name) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE user_relations ( requester_id int(10) unsigned NOT NULL, recipient_id int(10) unsigned NOT NULL, status tinyint(3) unsigned NOT NULL DEFAULT '0', PRIMARY KEY (requester_id,recipient_id) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE unsent_decisions ( requester_id int(10) unsigned NOT NULL, recipient_id int(10) unsigned NOT NULL, status tinyint(3) unsigned NOT NULL, send_status tinyint(3) unsigned NOT NULL, PRIMARY KEY (requester_id,recipient_id) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE pending_messages ( message_id int(10) unsigned NOT NULL AUTO_INCREMENT, sender_id int(10) unsigned NOT NULL, recipient_id int(10) unsigned NOT NULL, message longtext NOT NULL, PRIMARY KEY (message_id) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
