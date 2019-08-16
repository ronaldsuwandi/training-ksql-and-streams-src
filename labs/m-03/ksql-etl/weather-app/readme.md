# Weather App

## Schema

Table **Station**
    station_id: serial,
    name: varchar(50),
    lattitude: float,       # in degrees
    longitude: float,       # in degrees
    elevation: float        # in meters above sea level

Table **Reading**
    reading_id: serial,
    reading_datetime: datetime,
    temperature: float,             # degrees centigrade
    wind_speed: float,              # in m/s
    wind_direction: int,            # in degrees (0 - 359, where 0 == north)