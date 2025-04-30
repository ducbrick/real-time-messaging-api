DROP TABLE IF EXISTS app_user;

CREATE TABLE app_user (
	id SERIAL PRIMARY KEY,
	name VARCHAR,
	email VARCHAR,
	id_provider_url VARCHAR,
	id_provider_id VARCHAR
);