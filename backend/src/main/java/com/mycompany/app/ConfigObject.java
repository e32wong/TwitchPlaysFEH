package com.mycompany.app;

public class ConfigObject {

    String gameName;
    String gameID;
    String deviceAddress;
    String twitchOAuth;
    String twitchChannel;
    int websocketPort;
    long rebootPeriodMs;
    boolean debug;
    String twitchClientID;
    String twitchExtensionSecret;

    public ConfigObject(String gameName, String gameID, String deviceAddress,
            String twitchOAuth, String twitchChannel, int websocketPort,
            long rebootPeriodMs, boolean debug, String twitchClientID,
            String twitchExtensionSecret) {

        this.gameName = gameName;
        this.gameID = gameID;
        this.deviceAddress = deviceAddress;
        this.twitchOAuth = twitchOAuth;
        this.twitchChannel = twitchChannel;
        this.websocketPort = websocketPort;
        this.rebootPeriodMs = rebootPeriodMs;
        this.debug = debug;
        this.twitchClientID = twitchClientID;
        this.twitchExtensionSecret = twitchExtensionSecret;
    }

    public String getGameName() {
        return gameName;
    }

    public String getGameID() {
        return gameID;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public String getTwitchOAuth() {
        return twitchOAuth;
    }

    public String getTwitchChannel() {
        return twitchChannel;
    }

    public int getWebsocketPort() {
        return websocketPort;
    }

    public long getRebootPeriodMs() {
        return rebootPeriodMs;
    }

    public boolean getDebugFlag() {
        return debug;
    }

    public String getTwitchClientID() {
        return twitchClientID;
    }

    public String getTwitchExtensionSecret() {
        return twitchExtensionSecret;
    }

}
