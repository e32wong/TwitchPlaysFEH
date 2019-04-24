package com.mycompany.app;

import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.nio.file.Files;

public class BanSystem  {

    HashSet<String> banList = new HashSet<String>();
	HashSet<String> whiteList = new HashSet<String>();
    String bannedUserFile;
    String whitelistUserFile;

    public BanSystem(String configFolder) {
        bannedUserFile = configFolder + "/banned.txt";
        whitelistUserFile = configFolder + "/whitelist.txt";

        try {
            System.out.println("Loading ban list");
            File banFile = new File(bannedUserFile);
            banFile.createNewFile(); // if file already exists will do nothing 
            Scanner sc = new Scanner(banFile);
            while (sc.hasNextLine()) {
                String userName = sc.nextLine();
                // do whatever you need with current line
                banList.add(userName);
                System.out.println("banned: " + userName);
            }
            sc.close();
        } catch (Exception e) {
            System.out.println("error in ban list");
        }

        try {
            System.out.println("Loading white list");
            File whitelistFile = new File(whitelistUserFile);
            whitelistFile.createNewFile(); // if file already exists will do nothing 
            Scanner sc = new Scanner(whitelistFile);
            while (sc.hasNextLine()) {
                String userName = sc.nextLine();
                // do whatever you need with current line
                whiteList.add(userName);
                System.out.println("whitelisted: " + userName);
            }
            sc.close();
        } catch (Exception e) {
            System.out.println("error in white list");
        }

    }

	public boolean isWhitelisted(String userName) {
        userName = userName.toLowerCase();
        if (whiteList.contains(userName)) {
            return true;
        } else {
            return false;
        }
	}

    public boolean isBanned(String userName) {
        userName = userName.toLowerCase();
        if (banList.contains(userName)) {
            return true;
        } else {
            return false;
        }
    }

    private void flushBanList() {

        try {
			File banFile = new File(bannedUserFile);
			Files.deleteIfExists(banFile.toPath());
	
			FileWriter fw = new FileWriter(bannedUserFile);
			for (String userName : banList) {
				fw.write(userName + "\n");
			}
			fw.close();

		
        } catch (Exception e) {
            System.out.println("error while flushing ban list");
        }

    }

    private void flushWhiteList() {
        
        try {
            File whitelistFile = new File(whitelistUserFile);
            Files.deleteIfExists(whitelistFile.toPath());
            
            FileWriter fw = new FileWriter(whitelistFile);
            for (String userName : whiteList) {
                fw.write(userName + "\n");
            }
            fw.close();
        
        
        } catch (Exception e) {
            System.out.println("error while flushing white list");
        }
    
    }

    public void unwhiteListUser(String userName) {

        userName = userName.toLowerCase();

        System.out.println("unwhite listing user: " + userName);
        if (whiteList.contains(userName)) {
            System.out.println("removed user from white list");
            whiteList.remove(userName);
            flushWhiteList();
        } else {
            System.out.println("user was never whitelisted, no action needed");
        }
    }

    public void whitelistUser(String userName) {

        userName = userName.toLowerCase();

        System.out.println("whitelisting user: " + userName);
        if (whiteList.contains(userName)) {
            System.out.println("user is already whitelisted, no action needed");
        } else {
            System.out.println("added user to white list");
            whiteList.add(userName);
            flushWhiteList();
        }
    }

    public void unbanUser(String userName) {

        userName = userName.toLowerCase();

        System.out.println("unbanning user: " + userName);
        if (banList.contains(userName)) {
            System.out.println("removed user from ban list");
			banList.remove(userName);
			flushBanList();
        } else {
            System.out.println("user was never banned, no action needed");
        }
    }

    public void banUser(String userName) {

        userName = userName.toLowerCase();

        System.out.println("banning user: " + userName);
        if (banList.contains(userName)) {
            System.out.println("user is already banned, no action needed");
        } else {
            System.out.println("added user to ban list");
            banList.add(userName);
            flushBanList();
        }
    }

}
