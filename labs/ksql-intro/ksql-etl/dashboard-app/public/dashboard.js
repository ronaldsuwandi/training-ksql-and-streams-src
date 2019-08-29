axios.defaults.baseURL = 'http://localhost:3000';

function run(){
    poll();
    setInterval(poll, 2000);
}

async function poll(){
    var result = await axios.get('/api/temperatures');
    var stations = result.data;
    var arrayLength = stations.length;
    var list = document.getElementById('stations');
    var mustAdd = (list.getAttribute('data-must-init'));
    let stationElem;
    if(mustAdd){
        list.removeAttribute('data-must-init');
        stationElem = list.children[0];
        list.innerHTML = '';
        for (var i = 0; i < arrayLength; i++) {
            var node = stationElem.cloneNode(true);
            var stationId = stations[i].STATION_ID;
            node.id = 'station-' + stationId;
            node.className = 'station';
            replaceValue(node, '.id', stationId);
            replaceValue(node, '.name', stations[i].NAME);
            list.appendChild(node);
        }
    }
    let station;
    for (var i = 0; i < arrayLength; i++) {
        var stationId = stations[i].STATION_ID;
        stationElem = document.getElementById('station-' + stationId);
        updateStation(stationElem, stations[i]);
    }
}

function updateStation(element, station){
    replaceValue(element, '.max_temp', station.MAX_TEMP);
    replaceValue(element, '.min_temp', station.MIN_TEMP);
    replaceValue(element, '.count', station.COUNT);
}

function replaceValue(element, key, value){
    var element = element.querySelector(key);
    element.innerHTML = value;
}

run();