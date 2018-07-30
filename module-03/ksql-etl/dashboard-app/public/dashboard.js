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
            var stationId = stations[i].station_id;
            node.id = 'station-' + stationId;
            node.className = 'station';
            replaceValue(node, '.id', stationId);
            replaceValue(node, '.name', stations[i].name);
            list.appendChild(node);
        }
    }
    let station;
    for (var i = 0; i < arrayLength; i++) {
        var stationId = stations[i].station_id;
        stationElem = document.getElementById('station-' + stationId);
        updateStation(stationElem, stations[i]);
    }
}

function updateStation(element, station){
    replaceValue(element, '.max_temp', station.max_temp);
    replaceValue(element, '.min_temp', station.min_temp);
    replaceValue(element, '.count', station.count);
}

function replaceValue(element, key, value){
    var element = element.querySelector(key);
    element.innerHTML = value;
}

run();