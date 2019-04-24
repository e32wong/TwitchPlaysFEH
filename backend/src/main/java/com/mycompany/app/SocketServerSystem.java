package com.mycompany.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

import java.util.Set;
import java.util.HashSet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.nio.file.Files;
import java.io.File;
import java.util.Random;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.Map;

public class SocketServerSystem extends WebSocketServer {

    TwitchAPI twitchAPI;
    Set<WebSocket> clientHandleList = new HashSet<WebSocket>();
    boolean isConnected = true;
    String baseConfigDir;
    EventSystem eventSystem = null;
    HashMap<String,WebSocket> clientConnectionMap = new HashMap<String,WebSocket>();
    HashMap<String, TouchPoint> mapUsernamePoint = new HashMap<String, TouchPoint>();
    boolean debug = false;
    String twitchExtensionSecret;

    public SocketServerSystem( int port, String baseConfigDir, 
            EventSystem eventSystem, boolean debug, String twitchClientID, String twitchExtensionSecret
            ) throws UnknownHostException {
        super( new InetSocketAddress( port ) );
        this.baseConfigDir = baseConfigDir;
        this.eventSystem = eventSystem;
        this.debug = debug;
        this.twitchExtensionSecret = twitchExtensionSecret;
        twitchAPI = new TwitchAPI(twitchClientID);
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void changeWallpaper() {
		try {
			// check if folder exist
			String folderName = "/home/edmund/twitchplaysfeh/overlay/static/images/wallpaper/";
			ArrayList<String> results = new ArrayList<String>();

            File f = new File(folderName);
			if (f.exists()) {

				File[] files = new File(folderName).listFiles();
				//If this pathname does not denote a directory, then listFiles() returns null. 

				for (File file : files) {
					if (file.isFile()) {
						results.add(file.getName());
					}
				}
			} else {
                return;
            }

            if (results.size() > 0) {
                // get random one
                Random rand = new Random();
                int n = rand.nextInt(results.size());
                String targetName = results.get(n);

                JSONObject newObj = new JSONObject()
                    .put("type", "changeWallpaper")
                    .put("value", targetName);
                sendMsgAll(newObj.toString());
            }


		} catch (Exception e) {
			System.out.println("failed to change wallpaper!");
		}
    }

    public void addButton2(int xPercent, int yPercent, String name, WebSocket conn) {
        System.out.println("addButton");
        JSONObject newObj = new JSONObject()
            .put("type", "addButton")
            .put("xPercent", xPercent)
            .put("yPercent", yPercent)
            .put("name", name);
        conn.send(newObj.toString());
    }

    public void addButton(int xPercent, int yPercent, String name, String userName) {

        WebSocket conn = clientConnectionMap.get(userName);

        System.out.println("addButton");
        JSONObject newObj = new JSONObject()
            .put("type", "addButton")
            .put("xPercent", xPercent)
			.put("yPercent", yPercent)
			.put("name", name);
        conn.send(newObj.toString());
    }

    public void sendRemoteMessage(String msg) {
    
        JSONObject newObj = new JSONObject()
            .put("type", "remoteMessage")
            .put("value", msg);
        sendMsgAll(newObj.toString());

    }

    private void sendMsgAll(String message) {
        try {
            for (WebSocket conn : clientHandleList) {
                conn.send(message);
            }
        } catch (Exception e) {
            System.out.println("Error at sending message through socket system");
            isConnected = false;
        }
    }

    public void sendTempMessageOnTile(String message, int tileNumber) {
        JSONObject newObj = new JSONObject()
            .put("type", "tempMessage")
            .put("value", message)
            .put("tileNumber", tileNumber);
        sendMsgAll(newObj.toString());
    }

    public void toggleOverlay(String overlayName, boolean status) {
        JSONObject newObj = new JSONObject()
            .put("type", "toggleOverlay")
            .put("overlayName", overlayName)
            .put("value", status);
        sendMsgAll(newObj.toString());
    }

    public void updateUnitStates(int unitNumber) {

        System.out.println("updating unit states");

        JSONObject newObj = new JSONObject()
            .put("type", "updateUnits")
            .put("value", unitNumber);
        sendMsgAll(newObj.toString());
    }

    public void toggleBusyIcon(boolean status) {
        JSONObject newObj = new JSONObject()
            .put("type", "busyIcon")
            .put("value", status);
        sendMsgAll(newObj.toString());
    }

    public void toggleChat(boolean status) {
        JSONObject newObj = new JSONObject()
            .put("type", "streamLabChat")
            .put("value", status);
        sendMsgAll(newObj.toString());
    }

    public void updateCommand(String commandStr) {
        JSONObject newObj = new JSONObject()
            .put("type", "lastCommand")
            .put("value", commandStr);
        sendMsgAll(newObj.toString());
    }

    public void updateTime(String executionTimeStr) {

		JSONObject newObj = new JSONObject()
			.put("type", "uptime")
			.put("value", executionTimeStr);
		sendMsgAll(newObj.toString());

	}

    public void sendPCKey(String keyStr) {
        JSONObject newObj = new JSONObject()
            .put("type", "KeyInput")
            .put("value", keyStr);
        sendMsgAll(newObj.toString());
    }

    public void toggleSecurity(boolean status) {
		JSONObject newObj = new JSONObject()
			.put("type", "security")
			.put("value", status);
		sendMsgAll(newObj.toString());
    }

    public void sendHelpText(String fileName) {
        fileName = baseConfigDir + "/text/" + fileName;
        try {
            String fileContent = readFile(fileName);
            JSONObject newObj = new JSONObject()
                .put("type", "helpText")
                .put("value", fileContent);
            sendMsgAll(newObj.toString());
        } catch (Exception e) {
            System.out.println("failed to retrieve text file and send it out");
        }

	}

    public void markScreen(int x, int y, int durationMS) {

        JSONObject newObj = new JSONObject()
            .put("type", "markScreen")
            .put("x", x)
            .put("y", y)
            .put("value", durationMS);
        sendMsgAll(newObj.toString());

    }

	private String readFile(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		} finally {
			br.close();
		}
	}

    public void toggleGrid() {
        //broadcast("sent");
        System.out.println("Toggling grid");
        JSONObject newObj = new JSONObject()
            .put("type", "toggleGrid");
        sendMsgAll(newObj.toString());
    }

    @Override
    public void onOpen( WebSocket conn, ClientHandshake handshake ) {
        //conn.send("Welcome to the server!"); //This method sends a message to the new client
        System.out.println( "new connection: " + conn.getRemoteSocketAddress() ); //This method sends a message to all clients connected
        clientHandleList.add(conn);

        /*
		addButton2(0, 70, "Auto", conn);
		addButton2(0, 75, "End", conn);
		addButton2(0, 80, "Up", conn);
		addButton2(0, 85, "Down", conn);
        */
    }

    @Override
    public void onClose( WebSocket conn, int code, String reason, boolean remote ) {

        System.out.println( conn.getRemoteSocketAddress() + " has left the room!" );
        clientHandleList.remove(conn);

        /*
        // find out the username of the connection
        String targetUserName = null;
        for (Map.Entry<String, WebSocket> entry : clientConnectionMap.entrySet()) {
            WebSocket savedConn = entry.getValue();
            String userName = entry.getKey();
            if (conn.equals(savedConn)) {
                targetUserName = userName;
                break;
            }
        }

        if (targetUserName != null) {
            System.out.println("Removing registered client: " + targetUserName);
            clientConnectionMap.remove(targetUserName);
        } else {
            System.out.println("Client not registered so cannot remove");
        }*/

        clientConnectionMap.values().remove(conn);

    }

    @Override
    public void onError( WebSocket conn, Exception ex ) {
        System.out.println("Error occured!");
        ex.printStackTrace();
        /*
        if( conn != null ) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }*/

        isConnected = false;
        //System.exit(0);
    }

    @Override
    public void onStart() {
        System.out.println("Server started!");
    }

    private void addToQueue(double endXInput, double endYInput, double clientWidth,
            double clientHeight, double startXInput, double startYInput,
			String userName) {

        try {
            // video resolution: 2560/1440 = 1.7777778 
            // example display resolution: 972/503 = 1.93240556 
            //double displayRatio = clientWidth / clientHeight; 
            //double streamRatio = 2560d / 1440d; 

            /*
            double blackBar = 0;
            if (displayRatio > streamRatio) {
                // black bars on the left and right side
                double diffRatio = streamRatio - displayRatio;
                double blackBarWidth = diffRatio * clientWidth;
                startXInput = startXInput - blackBarWidth;
                endXInput = endXInput - blackBarWidth;
            }
            */

            eventSystem.addMessage("extensiontouch " + startXInput + " " + startYInput + " " + 
                    endXInput + " " + endYInput + " " + clientWidth + " " + clientHeight, userName);

            //eventSystem.addMessage("extensiontouch " + xIntStart + " " + yIntStart + " " + 
            //        xIntEnd + " " + yIntEnd + " " + (int)clientWidth + " " + (int)clientHeight, userName);

        } catch (Exception e) {
            System.out.println("Error within addToQueue()");
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage( WebSocket conn, String message ) {
        // broadcast( message );
        // System.out.println( "incoming msg: " + message );

        JSONObject decodedJSON = null;
        try {
            decodedJSON = new JSONObject(message);
            System.out.println(decodedJSON.toString());
        } catch (JSONException ex) {
            System.out.println("Error while decoding the json object");
            return;
        }

        if (decodedJSON != null) {
            try {
                String objectType = decodedJSON.getString("type");

                System.out.println(objectType);

                // process the different message types
                if (objectType.equals("click")) {

                } else if (objectType.equals("helloOverlay")) {

                    eventSystem.addMessage("hellooverlay", "console");

                } else if (objectType.equals("buttonPress")) {

                    String user_id = JWTEngine.getUserID(decodedJSON, twitchExtensionSecret, debug);
                    if (user_id != null) {
						String userName = twitchAPI.getUsernameFromID(user_id);

                        String buttonName = decodedJSON.getString("buttonName");
                        eventSystem.addMessage("button " + buttonName, userName);
					}

                } else if (objectType.equals("helloClient")) {

                    String user_id = JWTEngine.getUserID(decodedJSON, twitchExtensionSecret, debug);
                    if (user_id != null) {
                        String userName = twitchAPI.getUsernameFromID(user_id);
                        if (userName == null) {
                            System.out.println("Failed to retrieve user name from ID so discarded the helloClient message");
                        } else {
                            clientConnectionMap.put(userName, conn);

                            eventSystem.addMessage("helloclient", userName);
                        }

					} else {
                        System.out.println("Client is not authenticated");

						sendUserMsg(conn, "<p>You can use your mouse to control the game " +
								"by granting the extension the permission.</p>" +
								"<p>There are dead zones marked on the top and bottom of the screen.</p>", 15000);

                    }

                } else if (objectType.equals("mouseDown")) {

                    String user_id = JWTEngine.getUserID(decodedJSON, twitchExtensionSecret, debug);
                    if (user_id != null) {

                        String userName = twitchAPI.getUsernameFromID(user_id);
                        if (userName == null) {
                            System.out.println("getUsernameFromID had failed to retrieve user name");
                        } else {
                            TouchPoint touchPoint = new TouchPoint(decodedJSON.getDouble("x"),
                                    decodedJSON.getDouble("y"));
                            mapUsernamePoint.put(userName, touchPoint);
                        }

                    } else {
                        System.out.println("Requesting user to give permission..");
                        sendUserMsg(conn, "Please grant permission to the Twitch extension " + 
                                "to enable the touch screen interface.<br>" +
                                "The bottom panel contains further instructions.", 6000);
                    }
                } else if (objectType.equals("mouseUp")) {

                    String user_id = JWTEngine.getUserID(decodedJSON, twitchExtensionSecret, debug);
                    if (user_id != null) {

                        String userName = twitchAPI.getUsernameFromID(user_id);
                        TouchPoint touchPointStart = mapUsernamePoint.get(userName);
                        mapUsernamePoint.remove(userName);

                        addToQueue(decodedJSON.getDouble("x"), decodedJSON.getDouble("y"),
                                decodedJSON.getDouble("clientWidth"),
                                decodedJSON.getDouble("clientHeight"),
                                touchPointStart.getX(), touchPointStart.getY(), userName);
                    }
                } else if (objectType.equals("ping")) {
                    // just ignore for now
                } else {
                    System.out.println("Unknown message type: " + objectType);
                }
            } catch (Exception e) {
                System.out.println("Error within onMessage");
                e.printStackTrace();
            }
        }
    }

    private static void sendUserMsg(WebSocket conn, String msg, int timeMS) {
        JSONObject newObj = new JSONObject()
            .put("type", "addMessage")
            .put("value", msg)
            .put("timeMS", timeMS);
        conn.send(newObj.toString());
    }

	private class TouchPoint {
        double x;
        double y;

        public TouchPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

	}

}
