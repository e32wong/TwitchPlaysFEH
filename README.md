# TwitchPlaysFEH

This project allows a user to control any game through Twitch using a mouse.
Unlike traditional TwitchPlays engines where user have to type in the commands through chat, 
we propose a much more natural solution which is to use a mouse to control the game! 
This is done by adding an invisible HTML layer on top of the video to capture viewers' mouse clicks.

The code officially supports Fire Emblem Heroes and unofficially supports Stardew Valley.

# Overview

The idea is to allow user to control any game using a mouse by clicking directly on the video itself. 
Our code currently works out of the box for Android games but it can be ported for PC games easily. 
The provided code assumes there is an Android TV box hosting the game and we control the game using Android ADB.

# Demo

We had been streaming this project for Fire Emblem Heroes 24/7 since the summer of 2017 for almost two years: twitch.tv/twitchplaysfeh

# Source Layout

*  **Extension** (``extension``) - Hosts the Twitch extension HTML overlay to capture users' mouse clicks. 
   You have to submit this code to Twitch developer portal for approval before it can be deployed to your Twitch channel.
*  **Backend Server** (``backend``) - Hosts the backend Java server code. 
   It is a websocket server that communicates against the Twitch extension and the dynamic OBS overlay.
*  **Dynamic OBS Overlay** (``overlay``) - Hosts the dynamic OBS HTML overlay for displaying interactive content on screen. 
   For example, if a person performs a mouse click on the screen, the HTML overlay will display the name tag of the user on the location that he/she clicked on.
*  **Helper Scripts** - (``scripts``) - Scripts for installing/uninstalling APKs on the Android device.

# Requirements

Accounts:

*  Twitch account. Remember to enable low-latency streaming mode.
*  Twitch developer account (https://dev.twitch.tv/) and create a new Twitch Extension project(https://dev.twitch.tv/docs/extensions/). 

Passwords/IDs:

*  Twitch Chat OAuth Password for sending/receiving Twitch IRC chat commands (https://twitchapps.com/tmi/).
*  Twitch client ID for performing API requests to Twitch (https://dev.twitch.tv/docs/v5/).
*  Twitch extension secret for validating the JWT tokens for authentication (https://dev.twitch.tv/docs/extensions/building/#managing-extension-secrets).

Hardware Setup (if not using an emulator):

*  A Nvidia Shield/Android phone with developer mode enabled.
*  A PC with an HDMI capture card. You don't need this if you can mirror the Android screen wirelessly.

Server Setup:

*  Backend code tested on Ubuntu 18.04 LTS and OpenJDK 8 (JDK 11 is known to have websocket issues).

Network Setup:

*  Public server port - The backend server hosts a websocket server, which requires a port for communicating to the dynamic OBS overlay and Twitch extension.
*  SSL Certificate - Self sign a certificate (https://letsencrypt.org/).
*  Domain Name - Twitch extension will use the DNS name for SSL websocket connection.

Software Setup:

*  Tesseract 4.0.0-beta.1 & tess4j-4.0.0 - Make sure Tesseract is compatible with your tess4j version (https://github.com/nguyenq/tess4j). 
   This is used for image recognition purposes. You may use a newer version if you wish.
*  ADB 1.0.39 or newer - For controlling the Android device through the network.

# Getting things to run - Twitch Extension

The Twitch extension is used to intercept user's mouse clicks and send the information back to the backend server. 
It is a websocket client and connects back to the backend server through SSL. 

Setting up a Twitch extension is going to take some time to learn.
You have to read and figure out how to create a new extension project
and upload it for approval by Twitch which takes 2-3 business days.

``execute.sh`` will create a temporary web server that allows you to test the frontend extension locally.

Once the frontend extension files are ready to go for submission to Twitch, 
``zip.sh`` will create ``archieve.zip`` which will be uploaded to the Twitch developer portal (https://dev.twitch.tv/).

In order to authenticate users we utilize JSON Web Tokens (JWTs) that is provided by Twitch from the helper library 
(https://dev.twitch.tv/docs/extensions/building/). 
Please read the documentation carefully on how to set up the authentication token.
You also have to request permission from your 
viewer to obtain their Twitch username if you would like to know who is controlling the game.

``viewer.js`` contains a few parameters that you have to change including the:

* Twitch channel ID
* Public port number of your backend server
* DNS name of your backend server

# Getting things to run - Backend Server

The backend code can be installed by typing ``make`` and Maven will sort out the dependencies.
You have to manually install Tesseract since it is binded by the library Tess4J.
The code can be executed using ``execute.sh``, where
the code accepts a command line argument such as ``./config/fireemblem/``
that points to the configuration folder.

An overview of what you need inside the configuration folder:

* ``config.xml`` - master configuration file
* ``pem/`` - stores the SSL certificates for the websocket server

**Configuration XML**

The XML file must be named ``config.xml``. It stores the configuration
of the backend server.
Please see the following example:

```xml
<config>
    <gameName>fireEmblem</gameName>
    <gameID>com.nintendo.zaba</gameID>
    <deviceAddress>192.168.0.12:5555</deviceAddress>
    <twitchOAuth>oauth:xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx</twitchOAuth>
    <twitchChannel>twitchplaysfeh</twitchChannel>
    <websocketPort>10000</websocketPort>
    <rebootPeriodMs>3600000</rebootPeriodMs>
    <debug>false</debug>
    <twitchClientID>xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx</twitchClientID>
    <twitchExtensionSecret>xxxxxxxxxxxxxxxxxxx</twitchExtensionSecret>
</config>
```
* ``gameName`` - Supports ``fireEmblem`` or ``stardewValley``.
* ``gameID`` - Refers to the game ID in Android. It is used for rebooting the game.
* ``deviceAddress`` - Refers to the IP address and port of the Android device.
* ``twitchOAuth`` - Twitch Chat OAuth Password.
* ``twitchChannel`` - Name of the Twitch channel.
* ``websocketPort`` - The public port of your backend server.
* ``rebootPeriodMs`` - Configures how often the game should be rebooted if it is idle.
* ``debug`` - Debug flag.
* ``twitchClientID`` - Client ID of your twitch account. Used for performing API calls to Twitch.
* ``twitchExtensionSecret`` - The JWT secret for your Twitch extension.

**PEM Files**

Within the configuration folder you should place your SSL certificates under a folder called ``pem``.
Since the backend code hosts a websocket server over SSL you need to generate a self signed certificate
with the following files from Let's Encrypt using Certbot (https://certbot.eff.org/)
which will provide you with these two files:

* ``fullchain.pem``
* ``privkey.pem``

After that you will have to generate the der file (``my-ca.der``) using the following command 
to make it compatible for the websocket server library:

``openssl x509 -in fullchain.pem -inform pem -out my-ca.der -outform der``

Place the three files including ``fullchain.pem``, ``privkey.pem`` and ``my-ca.der`` under the ``pem`` folder.

To check the expirary date of the certificate:

``openssl x509 -enddate -noout -in fullchain.pem``

# Getting things to run - Dynamic OBS Overlay

Simply run ``execute.sh`` and it will automatically host a web server for you.
You can then use that webpage as a browser source in OBS.
Remember to adjust the DNS name and port number.

