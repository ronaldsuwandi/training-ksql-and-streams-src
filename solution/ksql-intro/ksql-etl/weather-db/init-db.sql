CREATE TABLE stations
(
  id SERIAL UNIQUE PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  lattitude SMALLINT NOT NULL,
  longitude SMALLINT NOT NULL,
  elevation SMALLINT NOT NULL
);

ALTER TABLE stations
  OWNER TO weatherapp;

CREATE TABLE readings
(
  id serial UNIQUE PRIMARY KEY,
  station_id INTEGER NOT NULL,  -- REFERENCES stations(id),
  reading_date TIMESTAMP NOT NULL,
  temperature REAL NOT NULL,
  wind_speed REAL NOT NULL,
  wind_direction SMALLINT NOT NULL
);

ALTER TABLE readings
  OWNER TO weatherapp;

ALTER ROLE weatherapp CONNECTION LIMIT -1;

-- add stations
INSERT INTO stations (name, lattitude, longitude, elevation) VALUES('Antarctica 1', 0, 85, 2240);
INSERT INTO stations (name, lattitude, longitude, elevation) VALUES('Antarctica 2', 90, 87, 1785);
INSERT INTO stations (name, lattitude, longitude, elevation) VALUES('Antarctica 3', 180, 92, 2550);
