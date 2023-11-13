var stompClient;

/* Chart Configuration */
var config = {
    type : 'line',
    data : {
        labels : [],
        datasets : [ {
            label : 'Temperature',
            backgroudColor : 'rgb(255, 99, 132)',
            borderColor : 'rgb(255, 99, 132)',
            data : [],
            fill : false

        } ]
    },
    options : {
        responsive : true,
        title : {
            display : true,
            text : 'Temperature'
        },
        tooltips : {
            mode : 'index',
            intersect : false
        },
        hover : {
            mode : 'nearest',
            intersect : true
        },
        scales : {
            xAxes : [ {
                display : true,
                type : 'time',
                time : {
                    displayFormats : {
                        quarter : 'h:mm:ss a'
                    }
                },
                scaleLabel : {
                    display : true,
                    labelString : 'Time'
                }
            } ],
            yAxes : [ {
                display : true,
                scaleLabel : {
                    display : true,
                    labelString : 'Value'
                }
            } ]
        }
    }
};

/* Document Ready Event */
$(document).ready(function() {

    var ctx = document.getElementById('lineChart').getContext('2d');
    window.myLine = new Chart(ctx, config);

    /* Configuring WebSocket on Client Side */
    var socket = new SockJS(getServerPath() + '/websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {

        stompClient.subscribe('/topic/temperature', function(temperature) {
            console.log(temperature);
            $('#temperature').text(temperature.body);
            /* Push new data On X-Axis of Chart */
            config.data.labels.push(new Date());
            /* Push new data on Y-Axis of chart */
            config.data.datasets.forEach(function(dataset) {
                dataset.data.push(temperature.body);
            });
            window.myLine.update();
        });

        stompClient.subscribe('/topic/listen', function(payload) {
            let data = JSON.parse(payload.body);
            let tableBody = $("#topTable").find("tbody");
            //tableBody.empty();

            // todo convert to jquery dataTable and allow for export directly
            let row = $("<tr>").append(
                $("<td>").text(data["topic"]),
                $("<td>").text(data["key"]),
                $("<td>").text(data["value"]),
                $("<td>").text(data["time"])
            );
            tableBody.append(row);
        });
    });

});

function getServerPath() {
    return window.location.pathname.substring(0, window.location.pathname.indexOf("/",2));
}