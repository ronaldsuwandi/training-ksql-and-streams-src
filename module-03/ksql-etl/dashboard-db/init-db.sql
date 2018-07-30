create table temperatures (
    station_id BIGINT NOT NULL PRIMARY KEY,
    count BIGINT NOT NULL,
    max_temp DOUBLE NOT NULL,
    min_temp DOUBLE NOT NULL
);
