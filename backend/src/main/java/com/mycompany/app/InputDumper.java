package com.mycompany.app;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.nio.file.Paths;
import java.nio.file.Files;

public class InputDumper extends Thread
{
    private DataInputStream in;
    private EventSystem eventSystem;
    private Logger logger;
    private FileHandler fh;  
    private String chatFolder;

    protected InputDumper( InputStream in, EventSystem eventSystem, String configFolder)
    {
        this.in = new DataInputStream( in );
        this.eventSystem = eventSystem;
        this.chatFolder = configFolder + "/chatLog/";

        createLogFolder();

        logger = Logger.getLogger("ChatLog");
		try {  
			// This block configure the logger with handler and formatter  
			String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
			fh = new FileHandler(chatFolder + date + ".txt", true);  
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();  
			fh.setFormatter(formatter);  

		} catch (SecurityException e) {  
			e.printStackTrace();  
		} catch (IOException e) {  
			e.printStackTrace();  
		}  
    }

    private void createLogFolder() {

        try {
            Files.createDirectories(Paths.get(chatFolder));
        } catch (Exception e) {
            System.out.println("error at creating log folder");
        }

    }

    /*
    private ArrayList<String> breakdownCommand(String message) {

        ArrayList<String> listTerms = null;

        // :machinedoll!machinedoll@machinedoll.tmi.twitch.tv PRIVMSG #twitchplaysfeh :test
        Pattern pattern = Pattern.compile("^:(\\w+)!\\w+@\\w+\\.tmi\\.twitch\\.tv PRIVMSG #\\w+ :(.+)$");
        Matcher matcher = pattern.matcher(message);
        if (matcher.matches()) {
            String userName = matcher.group(1);
            String userMsg = matcher.group(2);

            System.out.println(userMsg);

            if (userMsg.charAt(0) == '!') {
                // remove "!" from string then seperate
                String[] seperatedList = userMsg.substring(1,userMsg.length()).split(" ");
                listTerms = new ArrayList<String>();
                listTerms.add(userName);
                for (String term : seperatedList) {
                    listTerms.add(term);
                }
            }

        } else {
            System.out.println("Error at processing user message: " + message);
            System.exit(0);
        }

        return listTerms;
    }*/

    private MessageInstance parseIRC(String message) {

        //ArrayList<String> listTerms = null;

        // :machinedoll!machinedoll@machinedoll.tmi.twitch.tv PRIVMSG #twitchplaysfeh :test

        Pattern pattern = Pattern.compile("^:(\\w+)!\\w+@\\w+\\.tmi\\.twitch\\.tv PRIVMSG #\\w+ :(.+)$");
        Matcher matcher = pattern.matcher(message);
        if (matcher.matches()) {

            String userName = matcher.group(1);
            String userMsg = matcher.group(2);

            if (userMsg.charAt(0) == '!') {
                // automatically remove "!" from string
                userMsg = userMsg.substring(1,userMsg.length());
            }

			MessageInstance instance = new MessageInstance(userName, userMsg);

            // log the message
            logger.info(userName + ": " + userMsg);

			return instance;

        } else {
            System.out.println("Not a chat message: " + message);
            return null;
        }

    }

    public void run()
    {
        try {
            String msg;
            while ( ( msg = in.readLine() ) != null )
            {
                if (msg.equals("PING :tmi.twitch.tv")) {
                    eventSystem.addMessage(msg, "ping");
                    continue;
                } else if (msg.equals(":tmi.twitch.tv PONG tmi.twitch.tv :hello")) {
                    continue;
                }

                MessageInstance instance = parseIRC(msg);
                if (instance != null) {
                    eventSystem.addMessage(instance.getMessage(), instance.getUsername());
                    System.out.println( msg );
                }

            }
        } catch( IOException e ) {
            e.printStackTrace();
            System.out.println("Error so input dump thread will go down");
        }
    }
}

