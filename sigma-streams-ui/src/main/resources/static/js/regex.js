let regexTitle = document.getElementById('regex-list');
let sourceType = document.getElementById('sourcetype');
let regex = document.getElementById('regex');
let outputTopic = document.getElementById('rtopic');
let submit = document.getElementById('submitRegex');
let customLabel = document.getElementById('custom-label');
customLabel.style.visibility = 'hidden';

let regexRules = null;
submit.disabled = true;
let customFieldCount = 0;
let customFieldsList = new Set();

updateRegexRules();

function updateRegexRules() {
    const url = '/regexRules';
    const request = new XMLHttpRequest();
    request.open('GET', url, true);

    request.onload = function () {
        if (request.status === 200) {
            regexTitle.options.length = 0;
            regexRules = JSON.parse(request.responseText);
            let option;
            for (const [key, value] of Object.entries(regexRules)) {
                console.log("key: " + key);
                console.log("sourceType: " + regexRules[key].sourceType);
                console.log("regex: " + regexRules[key].regex);
                console.log("outputTopic: " + regexRules[key].outputTopic);
                console.log("customFields: " + JSON.stringify(regexRules[key].customFields));

                option = document.createElement('option');
                option.text = key;
                regexTitle.add(option);
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

function createCustomFields(field, value) {
    var addList = document.getElementById('custom-fields');
    customLabel.style.visibility = 'visible';

    customFieldCount++;

    var text = document.createElement('div');
    text.className = 'custom-fields';
    text.id = 'custom-fields' + customFieldCount;
    if (field != undefined && value != undefined) {
        text.innerHTML = "<input type='text' id='customField" + customFieldCount + "' placeholder='Enter Name' onKeyUp='verifySubmit()' value='" + field + "' />" +
            "<input type='text' id='customValue" + customFieldCount + "' placeholder='Enter Value' onKeyUp='verifySubmit()' value='" + value + "' />" +
            "<button class='btn' id='deleteCustomFields" + customFieldCount + "' onClick='deleteCustomField(" + customFieldCount + ")'>Delete</button>";
    } else {
        text.innerHTML = "<input type='text' id='customField" + customFieldCount + "' placeholder='Enter Name' onKeyUp='verifySubmit()' />" +
            "<input type='text' id='customValue" + customFieldCount + "' placeholder='Enter Value' onKeyUp='verifySubmit()' />" +
            "<button class='btn' id='deleteCustomFields" + customFieldCount + "' onClick='deleteCustomField(" + customFieldCount + ")'>Delete</button>";
    }
    addList.appendChild(text);

    customFieldsList.add(customFieldCount);

    verifySubmit();
}

function deleteCustomField(customId) {
    if (customId == 'all') {
        document.getElementById("custom-fields").innerHTML = '';
        customFieldsList.clear();
    } else {
        document.getElementById("custom-fields" + customId).remove();
        customFieldsList.delete(customId);
    }

    if (customFieldsList.size == 0) {
        customLabel.style.visibility = 'hidden';
    }

    verifySubmit();
}

function submitRegEx() {
    console.log("in submitRegEx");
    console.log("source type: " + sourceType.value);
    console.log("reg exp: " + JSON.stringify(regex.value));
    console.log("output topic: " + outputTopic.value);

    const url = '/addRegexRule/';
    const request = new XMLHttpRequest();
    request.open('POST', url, true);
    request.setRequestHeader("Accept", "application/json");
    request.setRequestHeader("Content-Type", "application/json");

    request.onreadystatechange = function() { // Call a function when the state changes.
        if (this.readyState === XMLHttpRequest.DONE && this.status === 200) {
            // Request finished. Do processing here.
            updateRegexRules();
        }
    }

    let regexData = {
        "sourceType": sourceType.value,
        "regex": regex.value,
        "outputTopic": outputTopic.value,
    };

    if (customFieldsList.size > 0) {
        let customFields = {};
        for (let i of customFieldsList) {
            customFields[document.getElementById("customField" + i).value] =
                document.getElementById("customValue" + i).value;
        }

        regexData["customFields"] = customFields;
    }


    console.log("sending " + JSON.stringify(regexData));
    request.send(JSON.stringify(regexData));
}

function deleteRegEx() {
    console.log("in deleteRegEx");

    const url = '/deleteRegexRule/';
    const request = new XMLHttpRequest();
    request.open('POST', url, true);

    request.onreadystatechange = function() { // Call a function when the state changes.
        if (this.readyState === XMLHttpRequest.DONE && this.status === 200) {
            updateRegexRules();
            clearRegEx();
        }
    }

    console.log("sending " + sourceType.value);
    request.send(sourceType.value);
}

function clearRegEx() {
    var elements = regexTitle.options;

    for(var i = 0; i < elements.length; i++){
        if(elements[i].selected)
            elements[i].selected = false;
    }

    sourceType.value = '';
    regex.value = '';
    outputTopic.value = '';
    deleteCustomField('all');
    verifySubmit();
}

function handleRegExSelect() {
    console.log("in handleRegExSelect: " + regexTitle.value);

    deleteCustomField('all');

    sourceType.value = regexRules[regexTitle.value].sourceType;
    regex.value = regexRules[regexTitle.value].regex;
    outputTopic.value = regexRules[regexTitle.value].outputTopic;

    console.log("size of custom fields: " + Object.keys(regexRules[regexTitle.value].customFields).length);
    for (const [key, value] of Object.entries(regexRules[regexTitle.value].customFields)) {
        console.log(`${key}: ${value}`);
        createCustomFields(key, value);
    }

    verifySubmit();
}

function verifySubmit() {
    // check custom fields
    let customValid = true;

    for (let i of customFieldsList) {
        if (document.getElementById("customField" + i).value.length == 0 ||
            document.getElementById("customValue" + i).value.length == 0) {
            customValid = false;
            break;
        }
    }

    if (customValid == true &&
        sourceType.value.length > 0 &&
        regex.value.length > 0 &&
        outputTopic.value.length > 0) {
        submit.disabled = false;
    } else {
        submit.disabled = true;
    }
}

