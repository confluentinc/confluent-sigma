initChart();

let dataCounter = 0;
let detectionCounter = 0;

function initChart() {
    var count = 0;
    var data = {
        labels : [],
        datasets : [
            {
                fillColor : "rgba(220,220,220,0.5)",
                strokeColor : "rgba(220,220,220,1)",
                pointColor : "rgba(220,220,220,1)",
                pointStrokeColor : "#fff",
                data : []
            },
            {
                fillColor : "rgba(255,0,0,0.5)",
                strokeColor : "rgba(255,0,0,1)",
                pointColor : "rgba(225,0,0,1)",
                pointStrokeColor : "#ff0000",
                data : []
            }
        ]
    }

    var event = new Date();
    for (var i=0; i<10; i++) {
        event.setSeconds(event.getSeconds() - 2);
        data["labels"][i] = formatTime(event);
        data["datasets"][0]["data"][i] = 0;
        data["datasets"][1]["data"][i] = 0;
    }

    // this is ugly, don't judge me
    var updateData = function(oldData){
        var labels = oldData["labels"];
        var dataSetA = oldData["datasets"][0]["data"];
        var dataSetB = oldData["datasets"][1]["data"];

        labels.push(formatTime(new Date()));
        dataSetA.push(getDataCounter());
        dataSetB.push(getDetectionCounter());

        labels.shift();
        dataSetA.shift();
        dataSetB.shift();
    };

    var optionsAnimation = {
        //Boolean - If we want to override with a hard coded scale
        scaleOverride : true,
        //** Required if scaleOverride is true **
        //Number - The number of steps in a hard coded scale
        scaleSteps : 10,
        //Number - The value jump in the hard coded scale
        scaleStepWidth : 10,
        //Number - The scale starting value
        scaleStartValue : 0
    }

    // Not sure why the scaleOverride isn't working...
    var optionsNoAnimation = {
        animation : false,
        //Boolean - If we want to override with a hard coded scale
        scaleOverride : true,
        //** Required if scaleOverride is true **
        //Number - The number of steps in a hard coded scale
        scaleSteps : 20,
        //Number - The value jump in the hard coded scale
        scaleStepWidth : 10,
        //Number - The scale starting value
        scaleStartValue : 0
    }

    //Get the context of the canvas element we want to select
    var ctx = document.getElementById("myChart").getContext("2d");
    var optionsNoAnimation = {animation : false}
    var myNewChart = new Chart(ctx);
    myNewChart.Line(data, optionsAnimation);

    setInterval(function(){
        updateData(data);
        myNewChart.Line(data, optionsNoAnimation)
        ;}, 2000
    );

};

function updateDataCounter(dCount) {
    dataCounter += dCount;
}

function getDataCounter() {
    let currentCount = dataCounter;
    dataCounter = 0;
    return currentCount;
}

function updateDetectionCounter(dCount) {
    detectionCounter += dCount;
}

function getDetectionCounter() {
    let currentCount = detectionCounter;
    detectionCounter = 0;
    return currentCount;
}

function formatTime(event) {
    return event.getHours() + ":" + ('0' + event.getMinutes()).slice(-2) + ":" + ('0' + event.getSeconds()).slice(-2);
}
