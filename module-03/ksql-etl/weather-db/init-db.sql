CREATE TABLE stations
(
  id SERIAL NOT NULL,
  station_id SERIAL UNIQUE PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  lattitude NUMERIC NOT NULL,
  longitude NUMERIC NOT NULL,
  elevation NUMERIC NOT NULL
);

ALTER TABLE stations
  OWNER TO weatherapp;

CREATE TABLE readings
(
  id SERIAL NOT NULL,
  reading_id serial UNIQUE PRIMARY KEY,
  station_id INTEGER NOT NULL REFERENCES stations(station_id),
  reading_date TIMESTAMP NOT NULL,
  temperature NUMERIC NOT NULL,
  wind_speed NUMERIC NOT NULL,
  wind_direction INTEGER NOT NULL
);

ALTER TABLE readings
  OWNER TO weatherapp;

ALTER ROLE weatherapp CONNECTION LIMIT -1;

-- add stations
INSERT INTO stations (name, lattitude, longitude, elevation) VALUES('Antarctica 1', 0, 85, 2240);
INSERT INTO stations (name, lattitude, longitude, elevation) VALUES('Antarctica 2', 0, 85, 2240);
INSERT INTO stations (name, lattitude, longitude, elevation) VALUES('Antarctica 3', 0, 85, 2240);
