-- Create user "iimes" and associated database "iimesdb"

DO $roleadd$ BEGIN IF NOT EXISTS (
SELECT
FROM
	pg_catalog.pg_roles
WHERE
	rolname = 'iimes' ) THEN CREATE USER  "iimes" WITH ENCRYPTED PASSWORD 'iimesdb';

END IF;

END $roleadd$;

-- Set working schema

CREATE SCHEMA IF NOT EXISTS iimesdb AUTHORIZATION iimes;
SET search_path TO iimesdb;

-- Create tables

-- Machines
CREATE TABLE IF NOT EXISTS "machine" (
	id SERIAL PRIMARY KEY
);

-- Pieces
CREATE TABLE IF NOT EXISTS "piece" (
	type SERIAL PRIMARY KEY
);

-- Delievered Pieces
CREATE TABLE IF NOT EXISTS "delivered_pieces" (
	piecetype INTEGER PRIMARY KEY REFERENCES "piece"("type")
		ON UPDATE CASCADE ON DELETE CASCADE,
	quantity INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS "stocks" (
	piecetype INTEGER PRIMARY KEY REFERENCES "piece"("type")
		ON UPDATE CASCADE ON DELETE CASCADE,
	quantity INTEGER NOT NULL
);

SET intervalstyle TO 'iso_8601';

-- Machines Worktime
CREATE TABLE IF NOT EXISTS "machine_statiscics" (
	machineid INTEGER PRIMARY KEY 
		REFERENCES "machine"("id")
		ON UPDATE CASCADE ON DELETE CASCADE,
	worktime INTERVAL NOT NULL
);

-- Processed Pieces
CREATE TABLE IF NOT EXISTS "processed_pieces" (
	machineid INTEGER REFERENCES "machine"("id")
	 ON UPDATE CASCADE ON DELETE CASCADE,
	piecetype INTEGER REFERENCES "piece"("type")
	 ON UPDATE CASCADE ON DELETE CASCADE,
	quantity INTEGER NOT NULL,
	PRIMARY KEY("machineid", "piecetype")
);

CREATE TABLE IF NOT EXISTS "suborder" (
	id INTEGER PRIMARY KEY,
	ordernumber INTEGER NOT NULL,
	ordertype VARCHAR NOT NULL,
	state VARCHAR NOT NULL DEFAULT 'waiting',
	px INTEGER REFERENCES "piece"("type"),
	py INTEGER REFERENCES "piece"("type"),
	quantity INTEGER NOT NULL,
	entrytime TIMESTAMP NOT NULL,
	starttime TIMESTAMP,
	endtime TIMESTAMP,
	timeframe TIMESTAMP NOT NULL,
	timeleft BIGINT
);

GRANT ALL ON ALL TABLES IN SCHEMA iimesdb TO iimes;
