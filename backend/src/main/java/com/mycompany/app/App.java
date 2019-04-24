package com.mycompany.app;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import org.apache.commons.io.FileUtils;
import java.util.Scanner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.PrintWriter;
import java.io.FileWriter;

/**
 * Hello world!
 *
 */
public class App 
{

    private static String[] getStartupCommands(String baseDirectoryFullStr) {

        String[] data = null;
        String startupTextFile = baseDirectoryFullStr + "/autoExecute.txt";

        try {
            Path p = Paths.get(startupTextFile);
            boolean exists = Files.exists(p);

            if (exists) {
                BufferedReader abc = new BufferedReader(new FileReader(startupTextFile));
                List<String> lines = new ArrayList<String>();

                String line;
                while((line = abc.readLine()) != null) {
					if (!(line.charAt(0) == '#')) {
						lines.add(line);
					}
                }
                abc.close();

                // If you want to convert to a String[]
                data = lines.toArray(new String[]{});
            } else {
				File f = new File(startupTextFile);
				FileWriter fileWriter = new FileWriter(f);
				PrintWriter printWriter = new PrintWriter(fileWriter);
				printWriter.println("reboot");
				printWriter.close();
            }
        } catch (Exception e) {
            System.out.println("Error while trying to process startup commands");
        }

		return data;
    }

    public static void main( String[] args )
    {


        if (args.length != 1) {
            System.out.println("Wrong number of arguments, given: " + args.length);
            System.exit(0);
        }

        String baseDirectoryRelative = args[0];
		Path path = Paths.get(baseDirectoryRelative);
		Path baseDirectoryFull = path.toAbsolutePath();
        String baseDirectoryFullStr = baseDirectoryFull.toString();

		// create config folder
        File directory = new File("./config/");
        if (!directory.exists()){
            directory.mkdir();
        }

        ConfigObject config = ConfigLoader.getConfig(baseDirectoryFullStr);
        if (config == null) {
            System.out.println("Error in fetching config file");
            System.exit(0);
        }

        SocketInitializer initializer = null;
        Thread threadInitializer = null;
        try {
			initializer = new SocketInitializer(config.getWebsocketPort(), baseDirectoryFullStr, 
                    config.getDebugFlag(), config.getTwitchClientID(), config.getTwitchExtensionSecret());
			threadInitializer = new Thread(initializer);

        } catch(Exception e) {
            System.out.println("Failed to start server");
            return;
        }

        // wait to ensure the overlay is connected 
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            System.out.println("Failed to sleep");
        }

        // wallpaper system
    	//WallpaperThread wallThread = new WallpaperThread(socketSystem);
        //Thread threadWall = new Thread(wallThread);
		//threadWall.start();


        // create folders
        try {
            String screenshotFolder = "./screenshots/";
            Path folder = Paths.get(screenshotFolder);
            if (Files.exists(folder)) {
                FileUtils.deleteDirectory(new File(screenshotFolder));
            }
            Files.createDirectories(folder);

        } catch (Exception e) {
            System.out.println("Error while creating folders");
        }

        try {
            boolean connected = Device.connectADB(config.getDeviceAddress());
            if (connected == true) {
                System.out.println("connected to adb");
            } else {
                System.out.println("failed to connect to adb");
                System.exit(0);
            }
        } catch (Exception e) {
            System.out.println("Exception on adb");
            System.exit(0);
        }

        EventSystem eventSystem = new EventSystem(config.getGameName(), 
                config.getGameID(), baseDirectoryFullStr, config.getDeviceAddress()); 
        eventSystem.setDaemon( true ); 
        IRCConnection con = null;
        try {
            con = new IRCConnection( "irc.chat.twitch.tv", 6667, eventSystem, initializer, 
                    baseDirectoryFullStr, config.getTwitchOAuth(), config.getTwitchChannel());
            con.start();
        } catch (Exception e) {
            System.out.println("error at irc connection");
            e.printStackTrace();
            return;
        }

        Scheduler scheduler = new Scheduler(eventSystem, config.getRebootPeriodMs());
        Thread schedulerThread = new Thread(scheduler);
        schedulerThread.start();

        eventSystem.addResources(con, initializer, scheduler);
		eventSystem.start(); 

        String[] listStartCommands = getStartupCommands(baseDirectoryFullStr);
		if (listStartCommands != null) {
            for (String command : listStartCommands) {
                System.out.println("Adding auto msg: " + command);
                
                eventSystem.addMessage(command, "console");
            }
		}

        // make sure to call this for the socket sytem
        initializer.addEventSystem(eventSystem);
        threadInitializer.start();

        UserInput input = new UserInput(eventSystem);
        input.start();

        TipsThread tipsThread = new TipsThread(initializer, baseDirectoryFullStr);
        Thread threadTips = new Thread(tipsThread);
        threadTips.start();

        Uptime uptime = new Uptime(initializer, baseDirectoryFullStr);
        Thread threadUptime = new Thread(uptime);
        threadUptime.start();


    }
}



