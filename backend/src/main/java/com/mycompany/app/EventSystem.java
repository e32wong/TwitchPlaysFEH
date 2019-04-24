package com.mycompany.app;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Arrays;
import java.util.ArrayList;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

import java.util.HashMap;
import java.time.Instant;

public class EventSystem extends Thread {

    BlockingQueue<MessageInstance> messageQueue = new ArrayBlockingQueue<MessageInstance>(100);
    BanSystem banSystem;
    IRCConnection con = null;
    SocketInitializer initializer;
    GameBase gameState;
    String gameName;
    String gameID;
    String configFolder;
    String address;
    Scheduler scheduler;

    protected EventSystem(String gameName, String gameID, String configFolder, String address) {

        this.gameName = gameName;
        this.gameID = gameID;
        this.configFolder = configFolder;
        this.address = address;

        banSystem = new BanSystem(configFolder);
    }

	private void processCommand(ArrayList<String> listTerms) {

        System.out.println("\n\nProcessing command...");
        for (String term : listTerms) {
            System.out.print(term + " ");
        }
        System.out.println("");

        gameState.process(listTerms);
	}

    private String formatTerms(ArrayList<String> listTerms) {
        String formattedString = listTerms.get(0) + ": ";
        listTerms.remove(0);
        for (String term : listTerms) {
            formattedString = formattedString + term + " ";
        }
        return formattedString;
    }

    private void processMessage(MessageInstance messageInstance) {

        try {

            String message = messageInstance.getMessage();
            String userName = messageInstance.getUsername();

            if (message.contains(":tmi.twitch.tv")) {
                System.out.println("skipped msg");
            } else if (message.contains("PRIVMSG")) {

                //ArrayList<String> listTerms = breakdownCommand(message);
                message = message.toLowerCase();
                ArrayList<String> listTerms = splitMessage(message, userName);
                processCommand(listTerms);

                scheduler.commandTriggered();

            } else {

                // command from command line
                message = message.toLowerCase();
                ArrayList<String> listTerms = splitMessage(message, userName);
                processCommand(listTerms);

                scheduler.commandTriggered();

            }

        } catch (Exception e) {
            System.out.println("Error at process message function");
            e.printStackTrace();
        }

    }

    private ArrayList<String> splitMessage(String message, String userName) {
		String[] seperatedList = message.split(" ");
		ArrayList<String> listTerms = new ArrayList<String>();
		listTerms.add(0, userName);

		for (String term : seperatedList) {
			listTerms.add(term);
		}

        return listTerms;

    }

    public void addResources(IRCConnection con, SocketInitializer initializer, Scheduler scheduler) { 
        this.con = con;
        this.initializer = initializer;
        this.scheduler = scheduler;
    } 

    public void run() {

        if (initializer == null || con == null) {
            System.out.println("Failed: variables not initialized");
            return;
        }

        ReportSystem reportSystem = new ReportSystem(configFolder);
        AdminSystem adminSystem = new AdminSystem(configFolder);
        Device device = new Device(address);

        if (gameName.equals("fireEmblem")) {
            gameState = new GameStateFEH2(con, initializer, reportSystem, banSystem, adminSystem, gameID, device, address, this);
        } else if (gameName.equals("stardewValley")) {
            gameState = new GameStateStardew(con, initializer, reportSystem, banSystem, adminSystem, gameID, device, address, this);
        } else {
            System.out.println("Game name not found: " + gameName);
            System.exit(0);
        }

        MessageInstance msg;
        while (true) {

            try {
                msg = messageQueue.take();

                // process the command
                processMessage(msg);

            } catch (Exception e) {
                System.out.println("Error at taking out of message queue");
                e.printStackTrace();
            }


        }

    }

    public void sendChatMsg(String message) {
        con.sendText(message);
    }

    HashMap<String, Long> lastInputTimeMap = new HashMap<String, Long>();

    public void addMessage(String message, String userName) {

        try {

            if (userName.equals("ping")) {
                con.sendTextRaw("PONG :tmi.twitch.tv\r\n");
                return;
            }

            if (!banSystem.isWhitelisted(userName) && messageQueue.size() >= 100) {
                System.out.println("Message queue is full");
                con.sendText(userName + " Slow down on the commands PunOko");
            } else {
                MessageInstance instance = new MessageInstance(userName, message);
                try {
                    messageQueue.put(instance);
                } catch (Exception e) {
                    System.out.println("Error at inserting into message queue");
                    e.printStackTrace();
                }
			}

            /*
            if (!banSystem.isWhitelisted(userName)) {
                // first check if the user is typing too fast
                Long lastInputTime = lastInputTimeMap.get(userName);
                if (lastInputTime != null) {
                    long gapSecond = 1;
                    long now = Instant.now().toEpochMilli();
                    if (now - lastInputTime < gapSecond * 1000) {
                        // too fast so discard the message
                        con.sendText("@" + userName + " You can only enter one command every " + gapSecond + " second please slow down VoHiYo");
                        return;
                    }

                } else {
                    // create new entry for him
                    long now = Instant.now().toEpochMilli();
                    lastInputTimeMap.put(userName, new Long(now));
                }
            }*/

        } catch (Exception e) {
            System.out.println("Error within addMessage for the following:");
            System.out.println(message);
            e.printStackTrace();
        }
    }

}

