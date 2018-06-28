const moment = require('moment');
const { Client } = require('pg');
const client = new Client({
    user: 'weatherapp',
    password: 'weatherapp123',
    host: 'weather-db',
    port: 5432,
    database: 'weather'
});

client.connect().then(()=>{
    console.log('Connected to weather database...');
}).catch(error => {
    console.log('Cannot connect to database!');
    throw error;
});

exports.addHistoricData = async function(){
    console.log('Adding historical data');
    const sql = "INSERT INTO readings(station_id, reading_date, temperature, wind_speed, wind_direction) VALUES($1,$2,$3,$4,$5);";
    var result = await client.query("SELECT id FROM stations");
    var stations = result.rows;
    var now = moment();
    var dt0 = now.subtract(1000, "minutes");
    for(var s=0; s<stations.length; s++){
        var stationId = stations[s].id;
        console.log('Adding data for station ' + stationId);
        var t0 = Math.random() * 20 - 10;
        var ws0 = Math.random() * 100;
        var wd0 = Math.random() * 20 - 10;
        for(var i=0; i<1000; i++){
            if(i%100==0) console.log("...."+i);
            var readingDate = dt0.add(1, "minutes");
            var temperature = t0 + Math.random();
            var windSpeed = ws0 + Math.random();
            var windDirection = Math.floor(wd0 + Math.random()*2) % 360;
            values = [
                stationId,
                readingDate,
                temperature,
                windSpeed,
                windDirection
            ];
            await client.query(sql, values);
        }
    }
}