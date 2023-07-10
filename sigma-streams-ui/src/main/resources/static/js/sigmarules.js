let dropdown = document.getElementById('sigmarule-dropdown');
let publishButton = document.getElementById('publish-rule');

function updateOptions() {
    dropdown.options.length = 0;

    let defaultOption = document.createElement('option');
    defaultOption.text = 'Choose Sigma Rule';

    dropdown.add(defaultOption);
    dropdown.selectedIndex = 0;

    const url = '/sigmaTitles';
    const request = new XMLHttpRequest();
    request.open('GET', url, true);

    request.onload = function () {
        if (request.status === 200) {
            const data = JSON.parse(request.responseText);
            let option;
            for (let i = 0; i < data.length; i++) {
                option = document.createElement('option');
                option.text = data[i];
                dropdown.add(option);
            }
        } else {
            // Reached the server, but it returned an error
        }
    }

    request.onerror = function () {
        console.error('An error occurred fetching the JSON from ' + url);
    };

    request.send();
}

function handleSelect() {
    const url = '/sigmaRule/' + dropdown[dropdown.selectedIndex].value;
    const request = new XMLHttpRequest();
    request.open('GET', url, true);

    request.onload = function () {
        if (request.status === 200) {
            editor.session.setValue(YAML.stringify(YAML.parse(request.responseText)));
            editor.resize();
         } else {
            // Reached the server, but it returned an error
        }
    }

    request.onerror = function () {
        console.error('An error occurred fetching the JSON from ' + url);
    };

    request.send();
}

function onPublish() {
    console.log("onPublish");
    const url = '/addSigmaRule/';
    const request = new XMLHttpRequest();
    request.open('POST', url, true);

    request.onreadystatechange = function() { // Call a function when the state changes.
        if (this.readyState === XMLHttpRequest.DONE && this.status === 200) {
            // Request finished. Do processing here.
        }
    }
    request.send(editor.session.getValue());
    console.log(editor.session.getValue());
}
