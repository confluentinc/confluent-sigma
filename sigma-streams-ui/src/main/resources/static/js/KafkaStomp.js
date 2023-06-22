var stompClient = null;
//let dashboardList = new Map();
let dnsCols = [];
let detectionCols = [];

connect();

function setConnected(connected) {
    //$("#dashboard-data").html("");
}

function connect() {
    var socket = new SockJS('/confluent-sigma-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/dns-data', function (dns) {
            handleDNSData(JSON.parse(dns.body));
        });
        stompClient.subscribe('/topic/dns-detection', function (detection) {
            handleDNSDetectionData(JSON.parse(detection.body));
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function convertEpochDateTime(epochTime) {
    return convertEpochDate(epochTime) + " " + convertEpochTime(epochTime);
}

function convertEpochDate(epochTime) {
    var time = new Date(epochTime * 1000);

    let formatTime;
    formatTime = (time.getMonth() + 1).toString().padStart(2,'0');
    formatTime += "/";
    formatTime += time.getDate().toString().padStart(2,'0');
    formatTime += "/";
    formatTime += time.getFullYear();

    return formatTime;
}

function convertEpochTime(epochTime) {
    var time = new Date(epochTime * 1000);

    let formatTime;
    formatTime = time.getHours().toString().padStart(2,'0');
    formatTime += ":";
    formatTime += time.getMinutes().toString().padStart(2,'0');
    formatTime += ":";
    formatTime += time.getSeconds().toString().padStart(2,'0');

    return formatTime;
}

function handleDNSData(dns_data) {
    var table = document.getElementById("dns-data-table");
    dns_data.forEach(dns => {
        var row = table.insertRow(1);
        var timeCell = row.insertCell(0);
        var dataCell = row.insertCell(1);
        timeCell.style = "width:150px";
        timeCell.innerHTML = convertEpochDateTime(dns.ts);
        dataCell.style = "white-space:pre;"
        dataCell.innerHTML = JSON.stringify(dns, undefined, 4);

    });

    updateDataCounter(dns_data.length);
}

function handleDNSDetectionData(dns_data) {
    var table = document.getElementById("dns-detection-table");
    dns_data.forEach(dns => {
        var row = table.insertRow(1);
        var timeCell = row.insertCell(0);
        var titleCell = row.insertCell(1);
        var dataCell = row.insertCell(2);
        timeCell.style = "width:150px";
        timeCell.innerHTML = convertEpochDateTime(dns.timeStamp);
        titleCell.style = "width:150px";
        titleCell.innerHTML = dns.sigmaMetaData.title;
        dataCell.style = "white-space:pre;"
        dataCell.innerHTML = JSON.stringify(dns, undefined, 4);

    });

    updateDetectionCounter(dns_data.length);
}


