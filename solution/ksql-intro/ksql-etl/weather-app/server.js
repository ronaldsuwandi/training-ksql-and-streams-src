const dba = require('./data-access.js');

console.log("Weather App started...");

dba.addHistoricData()
    .then(()=>{
        console.log("Added historical data");
    });

console.log("Ending Weather App...");