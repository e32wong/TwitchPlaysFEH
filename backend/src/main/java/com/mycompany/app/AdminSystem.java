package com.mycompany.app;

import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.nio.file.Files;

public class AdminSystem  {

    HashSet<String> adminList = new HashSet<String>();
    String adminUserFile;

    public AdminSystem(String configFolder) {
        adminUserFile = configFolder + "/adminList.txt";

        try {
            System.out.println("Loading admin list");
            File adminFile = new File(adminUserFile);
            adminFile.createNewFile(); // if file already exists will do nothing 
            Scanner sc = new Scanner(adminFile);
            while (sc.hasNextLine()) {
                String userName = sc.nextLine();
                // do whatever you need with current line
                adminList.add(userName);
                System.out.println("loaded admin: " + userName);
            }
            sc.close();
        } catch (Exception e) {
            System.out.println("error in admin list");
        }
    }

    public boolean isMod(String userName) {

        userName = userName.toLowerCase();

        if (adminList.contains(userName)) {
            return true;
        } else {
            return false;
        }
    }

    private void flushAdminList() {

        try {
			File banFile = new File(adminUserFile);
			Files.deleteIfExists(banFile.toPath());
	
			FileWriter fw = new FileWriter(adminUserFile);
			for (String userName : adminList) {
				fw.write(userName + "\n");
			}
			fw.close();

		
        } catch (Exception e) {
            System.out.println("error while flushing admin list");
        }

    }

    public void unmodUser(String userName) {

        userName = userName.toLowerCase();

        System.out.println("unmodding user: " + userName);
        if (adminList.contains(userName)) {
            System.out.println("removed user from mod list");
			adminList.remove(userName);
			flushAdminList();
        } else {
            System.out.println("user was never modded, no action needed");
        }
    }

    public void modUser(String userName) {

        userName = userName.toLowerCase();

        System.out.println("modding user: " + userName);
        if (adminList.contains(userName)) {
            System.out.println("user is already modded, no action needed");
        } else {
            System.out.println("added user to mod list");
            adminList.add(userName);
            flushAdminList();
        }
    }


}
