const express = require('express');
const mariadb = require('mariadb');
const mustacheExpress = require('mustache-express');

const app = express();
app.engine('html', mustacheExpress());
app.set('view engine', 'html');
app.set('views', __dirname + '/public');
app.use(express.static('public'));

const pool = mariadb.createPool({
    // host: 'localhost', 
    host: 'dashboard-db', 
    user:'dashboard', 
    password: 'dashboard123',
    database: 'MyDashboard'
});

app.listen(3000, host='0.0.0.0', ()=>{
    console.log("Dashboard App started...");
    console.log("Listening at port 3000...");
})

app.get("/dashboard", async (req,res)=>{
    res.render("dashboard");
})

app.get('/api/temperatures', async (req,res)=>{
    const rows = await getTemperatureAggregates();
    res.send(rows);
})

async function getTemperatureAggregates(){
    let conn;
    try {
      conn = await pool.getConnection();
      const rows = await conn.query("SELECT * FROM `EXTREME_TEMPERATURES`");
      return rows;
    } catch (err) {
        throw err;
    } finally {
        if (conn) conn.end();
    } 
}
