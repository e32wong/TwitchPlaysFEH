package com.mycompany.app;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;

public class GameStateStardew extends GameBase {

	public enum AvailableStateDragalia {
		LOADING, LOADED, SPECIAL
	}

    AvailableStateDragalia currentState = AvailableStateDragalia.LOADED;
    //AvailableStateDragalia lastStateBeforeBattle = AvailableStateDragalia.LOADING;

    @Override
    public void resetVariables() {
        currentState = AvailableStateDragalia.LOADED;
    }

    public GameStateStardew(IRCConnection connection, SocketInitializer initializer, 
            ReportSystem reportSystem, BanSystem banSystem,
            AdminSystem adminSystem, String gameID, Device device,
            String deviceAddress, EventSystem eventSystem) {
        this.connection = connection;
        this.initializer = initializer;
        this.reportSystem = reportSystem;
        this.banSystem = banSystem;
        this.adminSystem = adminSystem;
        this.gameID = gameID;
        this.device = device;
        this.deviceAddress = deviceAddress;
        this.eventSystem = eventSystem;

        initializer.getSocketSystem().toggleSecurity(true);

        updateOverlayMessage(currentState);
    }

	private int mapToX(int gridNumber) {
		int xPixel = (gridNumber - 1) % 20 * 1080 / 20 + 1080 / 20 / 2;
		return xPixel;
	}	

	private int mapToY(int gridNumber) {
		int yPixel = (gridNumber - 1) / 20 * 1920 / 12 + 1920 / 12 / 2;
		return yPixel;
	}

    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

	private boolean sendHold(int gridNumber, int numberSeconds, String userName) {

        if (isValidNumber(gridNumber, userName)) {

            if (numberSeconds < 1 || numberSeconds > 5) {
                numberSeconds = 1;
            }  

            int xPixel = mapToX(gridNumber);
            int yPixel = mapToY(gridNumber);

            device.sendSwipeCommand(xPixel, yPixel, xPixel, yPixel, numberSeconds * 1000);

            return true;
        } else {
            return false;
        }
    }

    private boolean sendSwipe(int gridNumber1, int gridNumber2, String userName) {

        if (isValidNumber(gridNumber1, userName) && isValidNumber(gridNumber2, userName)) {

            int xPixel1 = mapToX(gridNumber1);
            int yPixel1 = mapToY(gridNumber1);

            int xPixel2 = mapToX(gridNumber2);
            int yPixel2 = mapToY(gridNumber2);

            device.sendSwipeCommand(xPixel1, yPixel1, xPixel2, yPixel2, 1000);

            return true;
        } else {
            return false;
        }

    }

    private static boolean isValidNumber(int gridNumber, String userName) {

        if (userName.equals("console")) {
            return true;
        }

        if (gridNumber < 1 && gridNumber > 240) {
            return false;
        }

        System.out.println("approved");
        return true;

    }


    private boolean sendClick(int gridNumber, String userName) {

        if (isValidNumber(gridNumber, userName)) {

            int xPixel = mapToX(gridNumber);
            int yPixel = mapToY(gridNumber);

            device.sendTouchCommand(xPixel, yPixel);

            return true;
        } else {
            return false;
        }
    }

    private boolean sendTap(int gridNumber, String userName) {

        if (isValidNumber(gridNumber, userName)) {

            int xPixel = mapToX(gridNumber);
            int yPixel = mapToY(gridNumber);

            device.sendTouchCommand(xPixel, yPixel);

            return true;
        } else {
            return false;
        }
    }

    private void processLoaded(String[] listTerms, String userName) {
        System.out.println("inside loaded");

        try {
            String baseCommand = listTerms[0].toLowerCase();
            int numArguments = listTerms.length;

            boolean isPureTap = true;
            for (String term : listTerms) {
                if (!isInteger(term)) {
                    isPureTap = false;
                    break;
                }
            }

            if (isPureTap) {

                for (int i = 0; i < numArguments; i++) {

                    if (i >= 20) {
                        break;
                    }

                    int targetTileNumber = Integer.parseInt(listTerms[i]);
                    boolean executed = sendTap(targetTileNumber, userName);
                    if (executed) {
                        sendTempMessageOnTile(userName, targetTileNumber);
                        securityCheck(userName);
                    }

                }
                //} else if (processDirection(baseCommand, userName)) {
                // processed within the function

            } else if (baseCommand.equals("extensiontouch")) {

                if (numArguments == 7) {


                    double startXInput = Double.parseDouble(listTerms[1]);
                    double startYInput = Double.parseDouble(listTerms[2]);
                    double endXInput = Double.parseDouble(listTerms[3]);
                    double endYInput = Double.parseDouble(listTerms[4]);
                    double clientWidth = Double.parseDouble(listTerms[5]);
                    double clientHeight = Double.parseDouble(listTerms[6]);


                    // normalize to 1080
                    double overlayStartX =  171d / 1717d * clientWidth;
                    double overlayGameWidth = 1548d / 1717d * clientWidth;

                    double overlayStartY = 48d / 964d * clientHeight;
                    double overlayGameHeight = 869d / 964d * clientHeight;

                    int endXScaled = (int)((endXInput - overlayStartX) / overlayGameWidth * 1920);
                    int endYScaled = (int)((endYInput - overlayStartY)/ overlayGameHeight * 1080);
                    int startXScaled = (int)((startXInput - overlayStartX) / overlayGameWidth * 1920);
                    int startYScaled = (int)((startYInput - overlayStartY) / overlayGameHeight * 1080);

                    int xTile = startXScaled / (1920 / 20) + 1;
                    int yTile = startYScaled / (1080 / 12);

                    int xTileEnd = endXScaled / (1920 / 20) + 1;
                    int yTileEnd = endYScaled / (1080 / 12);


                    int targetTileNumber = xTile + yTile * 20;
                    int targetTileNumberEnd = xTileEnd + yTileEnd * 20;

                    if (Math.abs(endXScaled - startXScaled) < 50 && Math.abs(endYScaled - startYScaled) < 50) {
                        connection.sendText(userName + " " + targetTileNumber);
                        sendTempMessageOnTile(userName, targetTileNumber);
                        device.sendTouchCommand(startXScaled, startYScaled);
                    } else {
                        connection.sendText(userName + " s " + targetTileNumber + " " + targetTileNumberEnd);
                        sendTempMessageOnTile(userName, targetTileNumber);
                        sendTempMessageOnTile(userName, targetTileNumberEnd);
                        device.sendSwipeCommand(startXScaled, startYScaled, endXScaled, endYScaled, 200);
                    }

                    securityCheck(userName);

                }

            }
        } catch (Exception e) {
            System.out.println("Error at processing loaded");
            e.printStackTrace();
        }
    }

    private void closeRewards() throws InterruptedException {

        while (true) {

            screenshot();
            // new game data
            String detectedText = device.detectText(1080, 1920, 0, 0);
            String[] gameDataTerms = {"You got Hero Feather", "for reward", "Claim your reward",
                    "Details", "Quest Cleared", "Sent to", "Close",
                    "mission list", "New difficulty", "All rewards", "You got a",
                    "New quests have been added", "This reward has been added", "to your inventory", "Orb",
                    "You got", "Claim your feward"};
            ArrayList<String> foundTerms = HelpTools.containsText(detectedText, gameDataTerms);
            if (foundTerms.size() > 0) {
                device.sendKeyEvent(4);
                Thread.sleep(1000);
            } else {
                break;
            }
        }
    }

    private boolean processDirection(String direction, String userName) {

        if (direction.equals("n")) {
            sendSwipe(152, 116, "console");
        } else if (direction.equals("e")) {
            sendSwipe(152, 156, "console");
        } else if (direction.equals("s")) {
            sendSwipe(152, 176, "console");
        } else if (direction.equals("w")) {
            sendSwipe(152, 148, "console");
        } else if (direction.equals("ne")) {
            sendSwipe(152, 130, "console");
        } else if (direction.equals("nw")) {
            sendSwipe(152, 126, "console");
        } else if (direction.equals("se")) {
            sendSwipe(152, 180, "console");
        } else if (direction.equals("sw")) {
            sendSwipe(152, 172, "console");
		} else {
            return false;
		}

        return true;

    }

    public void securityCheck(String userName) {

        try {
            device.focusApp(gameID);
        } catch (Exception e) {
            System.out.println("Error on app focus");
        }

        if (enableSecurityChecks == false) {
            return;
        }

        if (banSystem.isWhitelisted(userName)) {
            return;
        }

        try {

            /*

            device.focusApp(gameID);

            Thread.sleep(1000);
            screenshot();

            String detectedText = device.detectText(1080, 1720, 0, 0);
            String[] gameDataTerms = {"Selectlaisourcejand", "Selectiajsourceyand", 
                "foriskilllinheritance", "vanish", "Mergejsame", "potentially/stat boosts", "skill inheritance", "skillssto",
                "CA$", "you want to purchase", "Event Recap", "Google", "Inventory",
                "Purchase", "Menge Allies", "stat boosts", "Combat Manuals", "Your Manuals", 
                "xchange Menus", "Create Combat", 
                "Delete All Data", "delete your data",
                "Thanks for playing", "Open with", "YouTube", "Chrome", "Level and stats will", "return to initial",
                " rating?", "Return selected characters", "to obtain Hero Feathers", "sentahome", "ExchangeManuals",
                "Youn Manuals", "Send Home", "Play Achievements", "Data Quality", "Inventony",
                "access this data on any device", "Data version", "sound will decrease",
                "Your data quality", "Account Management", "Customer Support",
                "User Agreement", "Change Nickname", "Change Language", "characters or fewer", "Title Screen"};
            ArrayList<String> foundTerms = HelpTools.containsText(detectedText, gameDataTerms);

            if (foundTerms.size() > 0) {
                System.out.println("Found unaccepted words");
                connection.sendText("@" + userName + " We had detected attempts to SI/sending home/5* a unit" + 
                        " and will reboot the game! Sorry if it is a false positive, the " + 
                        "bot will resume after rebooting :)");

				if (userName.equals("console")) {
					return;
				}

                //banSystem.banUser(userName);

                processReboot();

			}
            */
        } catch (Exception e) {
            System.out.println("Error in security check");
            e.printStackTrace();
        }

    }

    @Override
    public void updateAfterState() {
        System.out.println("\nAfter state: " + currentState);
        updateOverlayMessage(currentState);
    }

    private boolean checkNewGameDataAndNewVersion() throws InterruptedException {

        boolean needStopBot = false;

        screenshot();

        // new game data
        String detectedText = device.detectText(828, 669, 135, 633);
        String[] gameDataTermsNewData = {"New game", "data is available", "Start download"};
        ArrayList<String> foundTerms = HelpTools.containsText(detectedText, gameDataTermsNewData);
        if (foundTerms.size() > 0) {
            System.out.println("Found new game data dialog");
            device.sendTouchCommand(564, 1173);
            Thread.sleep(60000);
        } else {
            System.out.println("No new game data dialog found");
        }

        String[] gameDataTermsUpdate = {"A new", "Update"};
        foundTerms = HelpTools.containsText(detectedText, gameDataTermsUpdate);
        if (foundTerms.size() > 0) {
            System.out.println("Found new update, stopping the bot");
            needStopBot = true;
        } else {
            System.out.println("No new update, continuing execution");
        }

        return needStopBot;

    }

    private void checkResumeDialog() throws InterruptedException {

        screenshot();

         // new game data
         String detectedTextResume = device.detectText(780, 1270, 207, 456);
         String[] gameDataTermsResume = {"Resume"};
         ArrayList<String> foundTermsResume = HelpTools.containsText(detectedTextResume, gameDataTermsResume);
         if (foundTermsResume.size() > 0) {
            System.out.println("Found resume dialog");
            device.sendTouchCommand(570, 1116);
            device.sendTouchCommand(570, 945);
             Thread.sleep(5000);
        } else {
            System.out.println("Resume not found");
        }

        /*
        // bookmark (from new installation)
        String detectedText = device.detectText(780, 1270, 207, 456);
        String[] gameDataTerms = {"Bookmark"};
        ArrayList<String> foundTerms = HelpTools.containsText(detectedText, gameDataTerms);
        if (foundTerms.size() > 0) {
            System.out.println("Found bookmark dialog");
            device.sendTouchCommand(536, 992);
            Thread.sleep(5000);
        } else {
            System.out.println("Bookmark not found");
        }*/

    }

    @Override
    public void processReboot() {
        try {

            resetVariables();

            device.connectADB(deviceAddress);

            device.rebootApp(gameID);

            System.out.println("Executing intitial set of macros");

            currentState = AvailableStateDragalia.LOADED;

            Thread.sleep(10000);
            //device.sendTouchCommand(100, 1640);
            //Thread.sleep(5000);
            //

            sendTap(131, "console");

            /*
            boolean needStopBot = checkNewGameDataAndNewVersion();
            if (needStopBot) {
				System.exit(0);
                return;
            }*/

            // checkResumeDialog();

            /*
            int delay = 1000;
            int index = 0;
            while (index < 8) {
                Thread.sleep(delay);
                device.sendKeyEvent(4);
                index = index + 1;
            }

            Thread.sleep(2000);

            // check quit because of the back button
            checkQuitDialog();
            */

        } catch (Exception e) {
            System.out.println("Error while process loading");
        }

    }

    private void checkQuitDialog() throws InterruptedException {

        screenshot();

        // new game data
        String detectedText = device.detectText(1080, 1920, 0, 0);
        String[] gameDataTerms = {"Quit"};
        ArrayList<String> foundTerms = HelpTools.containsText(detectedText, gameDataTerms);
        if (foundTerms.size() > 0) {
            System.out.println("Found new quit dialog");
            device.sendKeyEvent(4);
            Thread.sleep(1000);
        }

    }

    @Override
    public boolean executeGameModCommand(String[] listTerms, String userName) {

        boolean executedCommand = true;

		if (!userName.equals("console")) {
			return false;
		}

		try {

            String baseCommand = listTerms[0].toLowerCase();
            int numArguments = listTerms.length;

            if (baseCommand.equals("endturn")) {

                sendTempMessageOnTile(userName, 222);
                sendClick(222, userName);

                Thread.sleep(1000);

                sendTempMessageOnTile(userName, 116);
                sendClick(116, userName);

                Thread.sleep(1000);

            } else if (baseCommand.equals("switch")) {

                currentState = AvailableStateDragalia.SPECIAL;

            } else {
                executedCommand = false;
			}
		} catch (Exception e) {
            System.out.println("Failure to execute game specific mod command");
		}

        return executedCommand;
    }

    @Override
    public boolean executeStateCommand(String[] listTerms, String userName) {

		boolean executedCommand = true;

        // state commands
		if (currentState == AvailableStateDragalia.LOADING) {
			// should never be in this state
        } else if (currentState == AvailableStateDragalia.LOADED) {
            processLoaded(listTerms, userName);
		} else {
			System.out.println("unknown state"); 
			executedCommand = false; 
		}
    
		return executedCommand;
    }

	private void updateOverlayMessage(AvailableStateDragalia currentState) {
		if (currentState == AvailableStateDragalia.LOADING) {
		} else if (currentState == AvailableStateDragalia.LOADED) {
            initializer.getSocketSystem().sendHelpText("loaded.txt");
        } else if (currentState == AvailableStateDragalia.SPECIAL) {
            initializer.getSocketSystem().sendHelpText("loaded-special.txt");
        }
	}

}
