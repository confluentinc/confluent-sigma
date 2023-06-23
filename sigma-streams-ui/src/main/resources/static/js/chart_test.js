initChart();

function initChart() {
    var count = 10;
    var data = {
        labels : ["1","2","3","4","5", "6", "7", "8", "9", "10"],
        datasets : [
            {
                fillColor : "rgba(220,220,220,0.5)",
                strokeColor : "rgba(220,220,220,1)",
                pointColor : "rgba(220,220,220,1)",
                pointStrokeColor : "#fff",
                data : [65,59,90,81,56,45,30,20,3,37]
            },
            {
                fillColor : "rgba(151,187,205,0.5)",
                strokeColor : "rgba(151,187,205,1)",
                pointColor : "rgba(151,187,205,1)",
                pointStrokeColor : "#fff",
                data : [28,48,40,19,96,87,66,97,92,85]
            }
        ]
    }
    // this is ugly, don't judge me
    var updateData = function(oldData){
        var labels = oldData["labels"];
        var dataSetA = oldData["datasets"][0]["data"];
        var dataSetB = oldData["datasets"][1]["data"];

        labels.shift();
        count++;
        labels.push(count.toString());
        var newDataA = dataSetA[9] + (20 - Math.floor(Math.random() * (41)));
        var newDataB = dataSetB[9] + (20 - Math.floor(Math.random() * (41)));
        dataSetA.push(newDataA);
        dataSetB.push(newDataB);
        dataSetA.shift();
        dataSetB.shift();    };

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
