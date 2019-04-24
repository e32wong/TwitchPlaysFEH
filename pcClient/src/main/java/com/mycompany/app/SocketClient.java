package com.mycompany.app;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketException;
import javax.json.JsonObject;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonReader;
import javax.json.Json;
import javax.net.ssl.SSLContext;
import java.io.StringReader;

public class SocketClient
{
    private WebSocket connection = null;

    private final int connectionRetryTimeMs = 5000;

    public SocketClient() {
    }

    /**
     * Get JSON object of a string.
     * @param jsonObjectStr JSON string.
     * @return A JSON object.
     */
    private static JsonObject jsonFromString(final String jsonObjectStr) {

        JsonReader jsonReader = Json.createReader(new StringReader(jsonObjectStr));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();

        return object;
    }

    private static void processClick(int x, int y) {
		try {
			Device.click(x, y);
		} catch (Exception e) {
			System.out.println("Error in clicking");
		}
    }


    /**
     * Defines the websocket server's callback bahaviours.
     */
    private WebSocketAdapter adapter = new WebSocketAdapter() {
        @Override
        public void onTextMessage(final WebSocket ws, final String message) {
            // Received a response. Print the received message.
            System.out.println(message);

            JsonObject jsonObject = jsonFromString(message);
            String messageType = jsonObject.getString("type");

            if (messageType.equals("MouseInput")) {

                int x = jsonObject.getJsonNumber("x").intValue();
                int y = jsonObject.getJsonNumber("y").intValue();

                processClick(x, y);

            } else if (messageType.equals("KeyInput")) {

                String key = jsonObject.getString("value");

                try
                {
                    Device.keyboard(key);
                }
                catch (Exception exp)
                {
                    exp.printStackTrace();
                }
            }

        }

        @Override
        public void onError(WebSocket websocket, WebSocketException cause) throws Exception {

            System.out.println("Fatal error occured!!!!");
            System.out.println(cause);

        }

        @Override
        public void onDisconnected(final WebSocket ws, final WebSocketFrame serverCloseFrame,
                final WebSocketFrame clientCloseFrame, final boolean closedByServer) {

            boolean connectionDown = true;
            while (connectionDown) {

                try {
                    System.out.println("disconnected.. reconnecting to websocket server...");
                    WebSocketFactory factory = new WebSocketFactory();
                    SSLContext context = NaiveSSLContext.getInstance("TLS");
                    factory.setSSLContext(context);
                    factory.setVerifyHostname(false);
                    connection = factory.createSocket("wss://machinedoll.me:5002");
                    connection.addListener(adapter);
                    connection.connect();

                    System.out.println("reconnected!");

                    connectionDown = false;
                } catch (Exception e) {
                    System.out.println("Error at connecting, trying again in 5 seconds");
                    e.printStackTrace(System.out);
                }

                if (connectionDown) {
                    try {
                        Thread.sleep(connectionRetryTimeMs);
                    } catch (Exception e) {
                        System.out.println("Error at sleeping");
                        e.printStackTrace(System.out);
                    }
                }
            }
        }
    };

    /**
     * Connect to the websocket server.
     * @return Status of the connection.
     */
    public final boolean connectWebsocket() {

        boolean status = true;

        // Connect to "wss://echo.websocket.org" and send "Hello." to it.
        // When a response from the WebSocket server is received, the
        // WebSocket connection is closed.
        try {
            WebSocketFactory factory = new WebSocketFactory();
            SSLContext context = NaiveSSLContext.getInstance("TLS");
            factory.setSSLContext(context);
            factory.setVerifyHostname(false);
            connection = factory.createSocket("wss://kyojin.me:5002");
            connection.addListener(adapter);
            connection.setPingInterval(1000 * 30);
            connection.connect();

        } catch (Exception e) {
            System.out.println("Error at connecting");
            e.printStackTrace(System.out);
            status = false;
        }

        return status;
    }

}

