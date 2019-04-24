"use strict";

var last_auth = null;
var isOpen = false;
var port = 0;
var hostName1 = "kyojin.me";
var hostName2 = "";
var hostNameCurrent = hostName1;
var debug = false;

function loadAuthentication() {
	if (window.Twitch.ext) {
		window.Twitch.ext.onAuthorized(function(auth) {
            /*
			console.log('JWT token', auth.token);
			console.log('The channel ID is', auth.channelId);
            console.log('The opaque user ID is', auth.opaque_user_id);
            console.log('The provided user ID is', auth.user_id);
            */
            if (auth.channelId == "168045796") {
                // twitchplaysfeh
                port = 5002;
            } else if (auth.channelId == "62614017") {
                // bet_100_blue
                port = 5001;
            } else if (auth.channelId == "270072762") {
                // twitchplaysbo4
                port = 5003;
            } else {
                port = 5004;
            }

			last_auth = auth;

			// https://dev.twitch.tv/docs/extensions/reference
		});
	}

    if (debug == true) {
        port = 5002;
        last_auth = "";
    }
}

window.onload = function() {

    //addButton(20, 20, "asdf");
    loadEventListener();
    loadAuthentication();
    connectServer();
};

window.onresize = function() {
};

function addText(msg, displayTime) {
    var mainElement = document.getElementById("messageBox");
    messageBox.innerHTML = msg;
    setTimeout(clearMessage, displayTime);
}


function clearMessage() {
    var mainElement = document.getElementById("messageBox");
    messageBox.innerHTML = "";
}

function changeHost() {
    if (hostNameCurrent == hostName1) {
        if (hostName2 != "") {
            hostNameCurrent = hostName2;
        }
    } else {
        hostNameCurrent = hostName1;
    }
}

function connectServer() {

    if (port == 0) {
        setTimeout(function() { connectServer() }, 1000);
        return;
    }

    ws = new WebSocket("wss://" + hostNameCurrent + ":" + port + "/");
    ws.onmessage = function (event) {

        var messageStr = event.data;
        var jsonObject = JSON.parse(messageStr);
    
        // console.log(messageStr);

        if (jsonObject.type == "addMessage") {
            var message = jsonObject.value;
            var timeMS = jsonObject.timeMS;
            addText(message, timeMS);
        } else if (jsonObject.type == "addButton") {
            var xPercent = jsonObject.xPercent;
            var yPercent = jsonObject.yPercent;
            var name = jsonObject.name;
            addButton(xPercent, yPercent, name);
        }
    }

	ws.onopen = function(event) {
		//console.log("Opened connection!");
        isOpen = true;

        sendHello();
	}

	ws.onclose = function(event) {
		//console.log("Closed connection trying again in 5 seconds!");
        isOpen = false;
        setTimeout(connectServer, 5000);

        changeHost();
        destroyButtons();
	}
}   

function destroyButtons() {
    
	var list = document.getElementsByClassName("dynamicButton");
	for (var i = list.length - 1; 0 <= i; i--) {
		if(list[i] && list[i].parentElement)
			list[i].parentElement.removeChild(list[i]);
	}

}

function loadEventListener() {
    var el = document.getElementById("mainbody");
    el.addEventListener("touchstart", touchStart, false);
    el.addEventListener("touchend", touchEnd, false);
    el.addEventListener("touchcancel", touchCancel, false);
    el.addEventListener("mousedown", mouseDown, false);
    el.addEventListener("mouseup", mouseUp, false);
    el.addEventListener("click", click, false);
    el.addEventListener("dblclick", dblclick, false);

    //console.log("initialized.");
}

function dblclick(event) {
	event.stopPropagation();
	sendNetwork("dblClick", event.clientX, event.clientY);
}

function sendHello() {

    if (last_auth == null) {
        setTimeout(function() {sendHello();}, 1000);
    } else {

        var msg = new Object();
        msg.type = "helloClient";
        if (last_auth != null) {
            msg.auth = last_auth;
            var jsonStr = JSON.stringify(msg);
            ws.send(jsonStr);
        }
    }
}

function sendNetwork(msgType, x, y) {

    if (isOpen) {

        var msg = new Object();
        msg.type = msgType;
        msg.x = x;
        msg.y = y;
        msg.clientWidth = document.body.clientWidth;
        msg.clientHeight = document.body.clientHeight;
        if (last_auth != null) {
            msg.auth = last_auth;
            var jsonStr = JSON.stringify(msg);
            ws.send(jsonStr);
        }

    }

	//console.log(jsonStr);
}

function click(event) {
    //console.log("click: " + event.clientX + "," + event.clientY);
    event.stopPropagation();
    sendNetwork("click", event.clientX, event.clientY);
}

function mouseUp(event) {
    //console.log("mouse up: " + event.clientX + "," + event.clientY);
    event.stopPropagation();
	sendNetwork("mouseUp", event.clientX, event.clientY);
}

function mouseDown(event) {
    //console.log("mouse down: " + event.clientX + "," + event.clientY);
    event.stopPropagation();
	sendNetwork("mouseDown", event.clientX, event.clientY);
}

function touchStart(event) {
    //console.log("touch start");
    event.stopPropagation();
	sendNetwork("touchStart", event.pageX, event.pageY);
}

function touchEnd(event) {
	//console.log("touch end");
    event.stopPropagation();
	sendNetwork("touchEnd", event.pageX, event.pageY);
}

function touchCancel() {
    //console.log("touch cancel");
    event.stopPropagation();
	sendNetwork("touchCancel", event.pageX, event.pageY);
}

function getX(rawX) {
    return rawX / document.body.clientWidth;
}

function getY(rawY) {
    return rawY / document.body.clientHeight;
}

function addCallback(name) {

    var msg = new Object();
    msg.type = "buttonPress";
	msg.buttonName = name;
    if (last_auth != null) {
        msg.auth = last_auth;
        var jsonStr = JSON.stringify(msg);
        ws.send(jsonStr);
    }
}

function addButton(x, y, name) {
    
    var div = document.createElement("div");
    div.className = "dynamicButton";
    div.style.left = x + "%";
    div.style.top = y + "%";
    div.style.position = "absolute";

    var btn = document.createElement("BUTTON");
	btn.textContent = name;
    btn.addEventListener('click', function(event) {
        event.stopPropagation();
        addCallback(name);
    }, false);
    btn.addEventListener('mouseup', function(event) {
        event.stopPropagation();
    }, false);
    btn.addEventListener('mousedown', function(event) {
        event.stopPropagation();
    }, false);

    div.appendChild(btn);
    
    var body = document.getElementById("mainbody");
    body.appendChild(div);


}

var ws = null;
var canvasWidth = null;
var canvasHeight = null;


