package com.mycompany.app;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.*;
import org.w3c.dom.*;

import java.util.ArrayList;
import java.io.IOException;
import java.io.File;

public class ConfigLoader {

    public static ConfigObject getConfig(String configFolder) {

        System.out.println("Config folder: " + configFolder);
		File directory = new File(configFolder);
		if (!directory.exists()){
			directory.mkdir();
		}

		String configFile = configFolder + "/config.xml";

		File f = new File(configFile);
		if (!f.exists()) {
			System.out.println("File does not exist: " + configFile);
			return null;
		}

        ConfigObject config = readXML(configFile);

        return config;

	}

	private static String getTextValue(String def, Element doc, String tag) {
		String value = def;
		NodeList nl;
		nl = doc.getElementsByTagName(tag);
		if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
			value = nl.item(0).getFirstChild().getNodeValue();
		}
		return value;
	}

	private static ConfigObject readXML(String xml) {

        String gameName = null;
        String gameID = null;
		String deviceAddress = null;
        String twitchOAuth = null;
        String twitchChannel = null;
        String websocketPort = null;
        String rebootPeriodMs = null;
        String debug = null;
        String twitchClientID = null;
        String twitchExtensionSecret = null;

		Document dom;
		// Make an  instance of the DocumentBuilderFactory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			// use the factory to take an instance of the document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			// parse using the builder to get the DOM mapping of the    
			// XML file
			dom = db.parse(xml);

			Element doc = dom.getDocumentElement();

			gameName = getTextValue(gameName, doc, "gameName");
			if (gameName == null) {
                System.out.println("Error at obtaining config folder value");
                return null;
			}

            gameID = getTextValue(gameID, doc, "gameID");
            if (gameID == null) {
                System.out.println("Error at obtaining config folder value for gameID");
                return null;
            }

            deviceAddress = getTextValue(deviceAddress, doc, "deviceAddress");
            if (deviceAddress == null) {
                System.out.println("Error at obtaining config folder value for device address");
                return null;
            }

            twitchOAuth = getTextValue(twitchOAuth, doc, "twitchOAuth");
            if (twitchOAuth == null) {
                System.out.println("Error at obtaining config folder value for twitch OAuth");
                return null;
            }

            twitchChannel = getTextValue(twitchChannel, doc, "twitchChannel");
            if (twitchOAuth == null) {
                System.out.println("Error at obtaining config folder value for twitch channel name");
                return null;
            }

            websocketPort = getTextValue(websocketPort, doc, "websocketPort");
            if (websocketPort == null) {
                System.out.println("Error at obtaining config folder value for websocket port");
                return null;
            }

            rebootPeriodMs = getTextValue(rebootPeriodMs, doc, "rebootPeriodMs");
            if (rebootPeriodMs == null) {
                System.out.println("Error at obtaining config folder value for reboot time");
                return null;
            }

            debug = getTextValue(debug, doc, "debug");
            if (debug == null) {
                System.out.println("Error at obtaining config folder value for debug flag");
                return null;
            }

            twitchClientID = getTextValue(twitchClientID, doc, "twitchClientID");
            if (twitchClientID == null) {
                System.out.println("Error at obtaining config folder value for Twitch extension client ID");
                return null;
            }

            twitchExtensionSecret = getTextValue(twitchExtensionSecret, doc, "twitchExtensionSecret");
            if (twitchExtensionSecret == null) {
                System.out.println("Error at obtaining config folder value for Twitch extension secret");
                return null;
            }

		} catch (ParserConfigurationException pce) {
			System.out.println(pce.getMessage());
		} catch (SAXException se) {
			System.out.println(se.getMessage());
		} catch (IOException ioe) {
			System.err.println(ioe.getMessage());
		}

		ConfigObject config = new ConfigObject(gameName, gameID, 
				deviceAddress, twitchOAuth, twitchChannel, Integer.parseInt(websocketPort), 
                Long.parseLong(rebootPeriodMs), Boolean.parseBoolean(debug), twitchClientID,
                twitchExtensionSecret);

		return config;
	}


}

