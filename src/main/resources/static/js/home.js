/* Chart Configuration */
const config = {
    type: 'line',
    data: {
        labels: [],
        datasets: [{
            label: 'Offset',
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
            text: 'Offset'
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

const h2DB = function() {
    const currentUrl = window.location.href;
    document.location.href = currentUrl.replace("/home","/h2-console");
}
/* Document Ready Event */
$(document).ready(function() {
    $("#load").on('click', function () {
        // Create request object
        var request = new Request(location.origin + '/kafka/landing',
            { method: 'POST',
                body: JSON.stringify({
                    "manifest":"&sometimes$#*^;'09()",
                    "note": {
                        "special": "&sometimes$#*^;'09()",
                        "to": "Tove",
                        "from": "Jani",
                        "heading": "Reminder",
                        "body": "Don't forget me this weekend!"
                    }
                }),
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8',
                    'Authorization' : 'Bearer test',
                },
            });
        // Now use it!

        fetch(request)
            .then(resp => {
                if (resp.redirected) {
                    // resp.url contains the string URL to redirect to
                    console.log(resp.url)
                    window.location.href = resp.url;
                } else {
                    // data.form contains the HTML for the replacement form
                    return resp.text()
                }
            })
            .then (async html => {
                document.body.innerHTML = await html;
                let scripts = document.getElementsByTagName("script");
                for (let i = 0, l = scripts.length; i < l; i++) eval(scripts[i].innerText);
            })
            .catch(err => {
                console.log(err)
            });
    });

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

    $("#h2-console").on('click', function () {
        h2DB();
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