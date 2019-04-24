package com.mycompany.app;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.methods.HttpGet;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;
import java.util.Iterator;

import java.io.IOException;

import java.util.HashMap;

public class TwitchAPI {

    String twitchClientID;

    HashMap<String, StreamerInfo> mapID2Info = new HashMap<String, StreamerInfo>();

    public TwitchAPI(String twitchClientID) {
        this.twitchClientID = twitchClientID;

        System.out.println("asdf: " + twitchClientID);
    }

    private StreamerInfo createStreamerInfo(String streamerTwitchID) {

        System.out.println("Calling twitch API for twitch id: " + streamerTwitchID);

        StreamerInfo streamerInfo = null;

        try {

            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet("https://api.twitch.tv/helix/users?id=" + streamerTwitchID);

            httpGet.addHeader("Client-ID", twitchClientID);

            CloseableHttpResponse response = client.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println("error on response");
            } else {

                HttpEntity responseEntity = response.getEntity();
                JSONObject result = new JSONObject(EntityUtils.toString(responseEntity));

                JSONArray jsonArray = result.getJSONArray("data");
                JSONObject content = jsonArray.getJSONObject(0); // there is only one object within the array

                System.out.println(content);

                EntityUtils.consume(responseEntity);
                response.close();
                client.close();

                streamerInfo = new StreamerInfo(
                        content.getString("broadcaster_type"), content.getString("login"));

                mapID2Info.put(streamerTwitchID, streamerInfo);
            }

        } catch (IOException e) {
            System.out.println("Exception at translating the user id");
            e.printStackTrace();
        }

        // save it for the future
        mapID2Info.put(streamerTwitchID, streamerInfo);

        return streamerInfo;

	}

    public String getUsernameFromID(String streamerTwitchID) {

        StreamerInfo streamerInfo = mapID2Info.get(streamerTwitchID);
        String userName = null;
        if (streamerInfo == null) {
            System.out.println("Need to create streamer object");
            streamerInfo = createStreamerInfo(streamerTwitchID);
            if (streamerInfo == null)  {
                System.out.println("Failed to create streamer info object!");
                return null;
            }
        }

        userName = streamerInfo.getUserName();

        System.out.println("Username Translation: " + userName);

        return userName;
    }

    class StreamerInfo {
        public String broadcaster_type;
        public String userName;

        public String getUserName() {
            return userName;
        }

        /**
         * Constructor for recording streamer's information.
         * @param broadcasterTypeIN Type of a broadcaster.
         * @param userNameIN Twitch user name.
         */
        StreamerInfo(final String broadcasterTypeIN,
                final String userNameIN) {
            broadcaster_type = broadcasterTypeIN;
            userName = userNameIN;
        }
    }
}
