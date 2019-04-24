"use strict";

var msgIndex = 0;
var msgList = [];
var maxNumChatEntries = 2;
var listUnitID = [];
var ws = null;
var connected = false;

var mode = "feh";

window.onload = function() {
    // startTime();

	connectServer();

	//printCrossHair(500, 500, 15000);

    if (mode != "feh") {
        var element = document.getElementById('middle');
        element.style.width = "90.2%";

        element = document.getElementById('middleTop');
        element.style.height = "5%";
        element.style.backgroundColor = "red";

        element = document.getElementById('middleMid');
        element.style.height = "90%";

        element = document.getElementById('middleBottom');
        element.style.height = "90%";
		element.style.backgroundColor = "blue";	

        element = document.getElementById('left');
        element.style.width = "9.8%";
        element.style.backgroundColor = "red";

        //element = document.getElementById('helpDiv');
        //element.style.visibility = "hidden";

    } else {
        drawFullGrid();
    }

	keepAlive();

};

function keepAlive() {

	if (connected == true) {
		console.log("keepalive..");
		sendPing();
	}

	setTimeout(function () {
		keepAlive();
	}, 5000);
}

function drawFullGrid() {

    var numHorizontal = 12;
    if (mode != "feh") {
        numHorizontal = 20;
    }

    for (var i = 1; i <= 240; i++) {
        if (i <= 24 || i >= 217) {
            var red = 0;
            var green = 0;
            var blue = 0;
            addTag(i, 1, red, green, blue);
        } else {
            var remain = i % 12;
            if (remain == 0) {
                addTag(i, 1, red, green, blue);
            }
        }
    }

    /*
    var useEven = false;
    for (var i = 1; i <= 240;) {
        var red = 0;
        var green = 0;
        var blue = 0;

        if (useEven) {
            if (i % 2 == 1) {
                if (!(i >= 24 && i <= 216)) {
                    addTag(i, 1, red, green, blue);
                }
            } else {
                //addTag(i, 0.3, red, green, blue);   
            }
        } else {
            if (i % 2 == 0) {
                if (!(i >= 24 && i <= 216)) {
                    addTag(i, 1, red, green, blue);
                }
            } else {
                //addTag(i, 0.3, red, green, blue);
            }
        }

        if (i % numHorizontal == 0) {
            if (useEven) {
                useEven = false;
            } else {
                useEven = true;
            }
        }

        i = i + 1;
    }*/

}

function startTime() {
	var today = new Date();
	var h = today.getHours();
	var m = today.getMinutes();
	var s = today.getSeconds();
	m = checkTime(m);
	s = checkTime(s);
	document.getElementById('clockDiv').innerHTML =
		"Local Time: " + h + ":" + m + ":" + s;
	var t = setTimeout(startTime, 500);
}

function checkTime(i) {
	if (i < 10) {i = "0" + i};  // add zero in front of numbers < 10
	return i;
}

function connectServer() {
    console.log("trying to connect to socket server");

	var element = document.getElementById("lastCommandDiv");
	element.innerHTML = "Trying to connect to server..";

    ws = new WebSocket('wss://kyojin.me:10000/');
    ws.onerror = function (event) {
        console.log("connection error occured, closing");
        ws.close();
        //addText("Connection error");
    };
    ws.onopen = function (event) {
        console.log("established websocket connection");
        //addText("Connected to server!")
		var element = document.getElementById("lastCommandDiv");
		element.innerHTML = "Connected to server!";

        sendHello();

		connected = true;
    };
    ws.onmessage = function (event) {
        var obj;
        var validJson = false;
        try {
            //console.log(event.data);
            obj = JSON.parse(event.data);
            validJson = true;
        }
        catch (e) {
            // The JSON was invalid, `e` has some further information
            console.log("invalid jaon");
        }
        if (validJson) {
            if (obj.type == "battleGrid") {
				var element = document.getElementById("screenOverlay");
                var targetVisibility = obj.value;
                toggleOverlay("battleGrid", targetVisibility);
            } else if (obj.type == "busyIcon") {
                console.log(event.data);
                var element = document.getElementById("floatingImage");
				var text = obj.value;
				if (text == true) {
					element.style.visibility = "visible";
				} else {
                	element.style.visibility = "hidden";
				}
            } else if (obj.type == "changeWallpaper") {
                /*
                var targetWallpaperName = obj.value;
                console.log(targetWallpaperName);
                var element = document.getElementById("wallpaperDiv");
                element.style.backgroundImage = "url('images/wallpaper/" + targetWallpaperName + "')";
                */
            } else if (obj.type == "remoteMessage") {
                var text = obj.value;
                var element = document.getElementById("remoteMessage");
                element.innerHTML = text;
            } else if (obj.type == "security") {
                /*
                var text = obj.value;
                var element = document.getElementById("securityDiv");
                if (text == true) {
                    element.style.visibility = "visible";
                } else {
                    element.style.visibility = "hidden";
                }*/
            } else if (obj.type == "helpText") {
                var text = obj.value;
                var element = document.getElementById("helpTextPre");
				element.innerHTML = text;
            } else if (obj.type == "uptime") {
                var text = obj.value;
                var element = document.getElementById("uptimeDiv");
                element.innerHTML = "Uptime: " + text;
            } else if (obj.type == "lastCommand") {
                var text = obj.value;
                var element = document.getElementById("lastCommandDiv");
                element.innerHTML = text;
			} else if (obj.type == "toggleGrid") {
				console.log("modifying grid..");
				toggle_visibility_class('numberTags');
            } else if (obj.type == "toggleOverlay") {
                var targetStatus = obj.value;
				var overlayName = obj.overlayName;
				toggleOverlay(overlayName, targetStatus);
            } else if (obj.type == "markScreen") {
                var x = obj.x;
                var y = obj.y;
                var duration = obj.value;
                printCrossHair(x, y, duration);
            } else if (obj.type == "tempMessage") {
                var message = obj.value;
				var tileNumber = obj.tileNumber;
                drawTempDiv(message, tileNumber);
            } else {
                console.log("onMessage unknown command: " + obj.type);
                // console.log(event.data);
            }
		}
	}
    ws.onclose = function (e) {
        // websocket is closed.
        console.log("Connection is closed.", e.reason);
        ws.close();

		//toggleOverlay("", false);

		var element = document.getElementById("helpTextPre");
		element.innerHTML = "INIT TEXT";

        element = document.getElementById("lastCommandDiv");
        element.innerHTML = "Disconnected";

		element = document.getElementById("remoteMessage");
		element.innerHTML = "n/a";

		element = document.getElementById("floatingImage");
		element.style.visibility = "hidden";

        element = document.getElementById("securityDiv");
        element.style.visibility = "hidden";

        ws = null;
		connected = false;

        //addText("Disconnected from server, will attempt to reconnect..");
        setTimeout(function () {
            console.log("reconnecting..");
            connectServer();
        }, 1000);
    };
}

function sendPing() {
    var msg = new Object();
    msg.type = "ping";
    var jsonStr = JSON.stringify(msg);
    ws.send(jsonStr);
}

function sendHello() {
	var msg = new Object();
	msg.type = "helloOverlay";
	var jsonStr = JSON.stringify(msg);
	ws.send(jsonStr);
}

function printCrossHair(x, y, durationMs) {

    console.log("printing crosshair");

    var elem = document.getElementById("middle");
    var screenWidth = elem.offsetWidth;
    var screenHeight = elem.offsetHeight;
    var projectedX = x / 1080 * screenWidth - 25;
    var projectedY = y / 1920 * screenHeight - 25;

    console.log(projectedX + ", " + projectedY);

	var img = document.createElement("img");
	img.setAttribute('width', 50);
	img.setAttribute('height', 50);
    img.style.left = projectedX;
    img.style.top = projectedY;
    img.style.position = "relative";
	img.style.border= 'medium solid blue';
    img.setAttribute("src", "./images/crossHair.png");
	document.getElementById("middle").appendChild(img);

    setTimeout(function() {
        console.log("removed");
        img.parentNode.removeChild(img);
    }, durationMs);

}

function toggle_visibility_class(className) {
    var elements = document.getElementsByClassName(className);
    for (var i = 0; i < elements.length; i++) {
        elements[i].style.display = elements[i].style.display == 'inline' ? 'none' : 'inline';
    }
}

function toggleOverlay(overlayName, turnOn) {

	var visibility = "";
	if (turnOn == true) {
		visibility = "visible";
	} else {
		visibility = "hidden";
	}

	var element = document.getElementById("screenOverlay");
	element.style.visibility = visibility;
    element.style.opacity = "1";
    element.style.height = "";
    element.style.width = "";
	if (overlayName == "version") {
		element.src="./images/sadLucina.jpg";
        element.style.width = "31%";
    } else if (overlayName == "battleGrid") {
        element.src="./images/overlayTiles.png";
        element.style.height = "100%";
    } else if (overlayName == "pixelGrid") {
        element.src="./images/pixelOverlay.png";
        element.style.height = "100%";
        element.style.opacity = "0.4";
	} else {
		console.log("no target name");
	}
}

function prependChild(parentEle, newFirstChildEle) {
    parentEle.insertBefore(newFirstChildEle, parentEle.firstChild)
}

function drawTempDiv(message, gridNumber) {

    var numHorizontal = 12;
    var numVertical = 20;
    if (mode != "feh") {
        numHorizontal = 20;
        numVertical = 12;
    }

    var offsetTop = document.getElementById("middleTop").clientHeight;

    var element = document.getElementById("middle");
    var screenWidth = element.offsetWidth;
    var screenHeight = element.offsetHeight;

    var xPixel = (gridNumber - 1) % numHorizontal * screenWidth / numHorizontal + screenWidth / numHorizontal / 2;
    var yPixel = Math.floor((gridNumber - 1) / numHorizontal) * screenHeight / numVertical + screenHeight / numVertical / 2 - 30 / 2 + offsetTop;

    var tagDiv = document.createElement('div');
    tagDiv.innerHTML = message;
    tagDiv.style.textAlign = "center";
    tagDiv.style.height = '50px';
    tagDiv.style.fontSize = "2vw";
    //tagDiv.style.backgroundColor = "rgba(0, 0, 0, 0.1)";
    tagDiv.style.left = xPixel;
    tagDiv.style.top = yPixel;
    tagDiv.style.color = "rgba(0, 0, 0, 0.8)";
    tagDiv.style.backgroundColor = "rgba(234, 236, 239, 0.8)";
    tagDiv.style.position = "absolute";

    element.appendChild(tagDiv);

    setTimeout(function(){
        tagDiv.remove();
    }, 10000);
}

function addTag(gridNumber, transparencyValue, red, green, blue) {

	//console.log("add tag for " + gridNumber);
    
    var numHorizontal = 12;
    var numVertical = 20;
    if (mode != "feh") {
        numHorizontal = 20;
        numVertical = 12;
    }

    var offsetTop = document.getElementById("middleTop").clientHeight;

	var element = document.getElementById("middle");
    var screenWidth = element.offsetWidth;
    var screenHeight = element.offsetHeight;

	var xPixel = (gridNumber - 1) % numHorizontal * screenWidth / numHorizontal + screenWidth / numHorizontal / 2 - 50 / 2;
	var yPixel = Math.floor((gridNumber - 1) / numHorizontal) * screenHeight / numVertical + screenHeight / numVertical / 2 - 30 / 2 + offsetTop;

    var tagDiv = document.createElement('div');
    tagDiv.innerHTML = gridNumber.toString();
    tagDiv.setAttribute("class", "numberTags");
	tagDiv.style.textAlign = "center";
    tagDiv.style.width = '50px';
	tagDiv.style.height = '30px';
    tagDiv.style.fontFamily = 'sans-serif';
	tagDiv.style.fontSize = "1.0vw";
    tagDiv.style.fontWeight = "bolder";
	//tagDiv.style.backgroundColor = "rgba(0, 0, 0, " + str(transparencyValue) + ")";
	tagDiv.style.left = xPixel;
	tagDiv.style.top = yPixel;
    tagDiv.style.color = "rgba(" + red.toString() + "," + green.toString() + "," +
        blue.toString() + "," + transparencyValue.toString() + ")";
	tagDiv.style.position = "absolute";

    element.appendChild(tagDiv);

}

