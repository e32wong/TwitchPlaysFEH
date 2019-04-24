package com.mycompany.app;

import java.util.concurrent.ThreadLocalRandom;

import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.util.List;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class TipsThread implements Runnable {

	//SocketServerSystem socketSystem;
    SocketInitializer initializer;
    String messageFile;

    ArrayList<String> listMessages = new ArrayList<String>();

	public TipsThread(SocketInitializer initializer, String baseDirectoryFullStr) {

		this.initializer = initializer;
        this.messageFile = baseDirectoryFullStr + "/messages.txt";

        try {
            File f = new File(this.messageFile);
            if (!f.exists()) {
                FileWriter fileWriter = new FileWriter(f);
                PrintWriter printWriter = new PrintWriter(fileWriter);
                printWriter.println("Welcome to the system!");
                printWriter.println("Have fun!");
                printWriter.close();
            }
        } catch (Exception e) {
            System.out.println("Error at creating text file for tips thread");
        }


        loadMessages();

	}

    private void loadMessages() {

        try {
            Charset charset = Charset.forName("ISO-8859-1");
            Path filePath = Paths.get(messageFile);
            List<String> allLines = Files.readAllLines(filePath, charset);

            ArrayList<String> newList = new ArrayList<String>();
            for (String line : allLines) {
                newList.add(line);
                //System.out.println("added: " + line);
            }

            listMessages = newList;

        } catch (Exception e) {
            System.out.println("error at loading messages");
            e.printStackTrace();
        }

    }

    private void sendRemoteMessage(String message) {
        initializer.getSocketSystem().sendRemoteMessage(message);
    }

    @Override
	public void run() {

		while (true) {

            loadMessages();

            int numMessages = listMessages.size();
            if (numMessages > 0) {
                int selectedNumber = ThreadLocalRandom.current().nextInt(0, numMessages); 
                String selectedMessage = listMessages.get(selectedNumber);
                sendRemoteMessage(selectedMessage);
            } else {
                sendRemoteMessage("no messages available");
            }

			try {
				Thread.sleep(1000 * 120);
			} catch (Exception e) {
				System.out.println("error in sleep");
			}
		}
	}
}
