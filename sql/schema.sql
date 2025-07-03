DROP TABLE IF EXISTS message_receiver;
DROP TABLE IF EXISTS message;
DROP TABLE IF EXISTS app_user;

CREATE TABLE app_user (
	id SERIAL PRIMARY KEY,
	name VARCHAR NOT NULL,
	email VARCHAR NOT NULL,
	id_provider_url VARCHAR NOT NULL,
	id_provider_id VARCHAR NOT NULL
);

CREATE TABLE message (
	id SERIAL PRIMARY KEY,
	content VARCHAR NOT NULL,
	sender_id INT NOT NULL REFERENCES app_user
);

CREATE TABLE message_receiver (
	message_id INT NOT NULL REFERENCES message,
	receiver_id INT NOT NULL REFERENCES app_user,
	PRIMARY KEY (message_id, receiver_id)
);
