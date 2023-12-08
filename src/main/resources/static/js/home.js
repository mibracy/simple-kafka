/* Chart Configuration */
const config = {
    type: 'line',
    data: {
        labels: [],
        datasets: [{
            label: 'Temperature',
            backgroudColor: 'rgb(255, 99, 132)',
            borderColor: 'rgb(255, 99, 132)',
            data: [],
            fill: false

        }]
    },
    options: {
        responsive: true,
        title: {
            display: true,
            text: 'Temperature'
        },
        tooltips: {
            mode: 'index',
            intersect: false
        },
        hover: {
            mode: 'nearest',
            intersect: true
        },
        scales: {
            xAxes: [{
                display: true,
                type: 'time',
                time: {
                    displayFormats: {
                        quarter: 'h:mm:ss a'
                    }
                },
                scaleLabel: {
                    display: true,
                    labelString: 'Time'
                }
            }],
            yAxes: [{
                display: true,
                scaleLabel: {
                    display: true,
                    labelString: 'Value'
                }
            }]
        }
    }
};
let table;
const getCurrentTime = () => new Date()
    .toLocaleString('en-US', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false })
    .replaceAll(`,`,``);
/* Document Ready Event */
$(document).ready(function() {
    table = $('#kafkaTable').DataTable({
        order: [[3, 'desc']],
        dom: 'Blfrtip',
        buttons: [
            'copyHtml5',
            'excelHtml5',
            'csvHtml5',
            'pdfHtml5',
        ]
    });

    const ctx = document.getElementById('lineChart').getContext('2d');
    window.myLine = new Chart(ctx, config);

    /* Configuring WebSocket on Client Side */
    const socket = new SockJS(getServerPath() + '/websocket');
    const stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {

        stompClient.subscribe('/topic/temperature', function(temperature) {
            $('#temperature').text(temperature.body);
            /* Push new date On X-Axis of Chart */
            config.data.labels.push(getCurrentTime());
            /* Push new data on Y-Axis of chart */
            config.data.datasets.forEach(function(dataset) {
                dataset.data.push(temperature.body);
            });
            window.myLine.update();
        });

        stompClient.subscribe('/topic/listen', function(payload) {
            let data = JSON.parse(payload.body);
            const pureKey = DOMPurify.sanitize(data["key"]);
            const pureValue = DOMPurify.sanitize(data["value"]);
            table.row.add([data["topic"], pureKey, pureValue, data["time"]]).draw();
        });
    });

    $("#inject").on('click', function () {
        triggerMe();
    });
});

function triggerMe() {
    const dirty = '<img src=x onerror=prompt(`HelloWorld`)>';
    table.row.add(['XSS dirty', dirty, 1, getCurrentTime()]).draw();

    const cleaned = DOMPurify.sanitize(dirty);
    table.row.add(['XSS clean', cleaned, 0, getCurrentTime()]).draw();
}

function getServerPath() {
    return window.location.pathname.substring(0, window.location.pathname.indexOf("/",2));
}