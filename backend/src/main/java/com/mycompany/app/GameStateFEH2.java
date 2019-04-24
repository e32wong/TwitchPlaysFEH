package com.mycompany.app;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;

public class GameStateFEH2 extends GameBase {

	public enum AvailableStateDragalia {
		LOADING, LOADED, SPECIAL
	}

    private ArrayList<Integer> customSequence = new ArrayList<Integer>();

    AvailableStateDragalia currentState = AvailableStateDragalia.LOADED;
    //AvailableStateDragalia lastStateBeforeBattle = AvailableStateDragalia.LOADING;

    @Override
    public void resetVariables() {
        currentState = AvailableStateDragalia.LOADED;
    }

	private static boolean isNumeric(String str)  
	{  
		try  
		{  
			double d = Double.parseDouble(str);  
		}  
		catch(NumberFormatException nfe)  
		{  
			return false;  
		}  
		return true;  
	}

    public GameStateFEH2(IRCConnection connection, SocketInitializer initializer, 
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
		int xPixel = (gridNumber - 1) % 12 * 1080 / 12 + 1080 / 12 / 2;
		return xPixel;
	}	

	private int mapToY(int gridNumber) {
		int yPixel = (gridNumber - 1) / 12 * 1920 / 20 + 1920 / 20 / 2;
		return yPixel;
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

            device.sendSwipeCommand(xPixel1, yPixel1, xPixel2, yPixel2, 200);

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

					sendTempMessageOnTile(userName, Integer.parseInt(listTerms[i]));
					boolean executed = sendTap(Integer.parseInt(listTerms[i]), userName);
					if (executed) {
						securityCheck(userName);
					}

				}

            } else if (baseCommand.equals("end")) {

                if (numArguments == 1) {
                    sendTempMessageOnTile(userName, 234);
                	sendTap(234, userName);   
                    securityCheck(userName);

                    Thread.sleep(500);

					sendTempMessageOnTile(userName, 116);
					sendTap(116, userName);
                    securityCheck(userName);
                } else {
                    connection.sendText("@" + userName + " The end command doens't require any arguments VoHiYo");
                }

            } else if (baseCommand.equals("extensiontouch")) {

                if (numArguments == 7) {

                    double startXInput = Double.parseDouble(listTerms[1]);
                    double startYInput = Double.parseDouble(listTerms[2]);
                    double endXInput = Double.parseDouble(listTerms[3]);
                    double endYInput = Double.parseDouble(listTerms[4]);
					double clientWidth = Double.parseDouble(listTerms[5]);
					double clientHeight = Double.parseDouble(listTerms[6]);


					// normalize to 1080
					double overlayStartX = 882d / 2560d * clientWidth;
					double overlayGameWidth = 806d / 2560d * clientWidth;
					int endXScaled = (int)((endXInput - overlayStartX) / overlayGameWidth * 1080);
					int endYScaled = (int)(endYInput / clientHeight * 1920);
					int startXScaled = (int)((startXInput - overlayStartX) / overlayGameWidth * 1080);
					int startYScaled = (int)(startYInput / clientHeight * 1920);

                    int xTile = startXScaled / (1080 / 12) + 1;
                    int yTile = startYScaled / (1920 / 20);

                    int xTileEnd = endXScaled / (1080 / 12) + 1;
                    int yTileEnd = endYScaled / (1920 / 20);


                    int targetTileNumber = xTile + yTile * 12;
                    int targetTileNumberEnd = xTileEnd + yTileEnd * 12;

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

            } else if (baseCommand.equals("grid")) {

                SocketServerSystem sys = initializer.getSocketSystem();
                try {
                    System.out.println("Toggling gird..");
                    sys.toggleGrid();
                } catch (Exception e) {
                    System.out.println("error at toggle grid");
                }

            } else if (baseCommand.equals("record")) {

                try {
                    customSequence = new ArrayList<Integer>();
                    for (int i = 1; i < numArguments; i++) {
                        int gridNumber = Integer.parseInt(listTerms[i]);
                        if (gridNumber > 240 || gridNumber < 1) {
                            connection.sendText("Invalid grid number: " + gridNumber);
                            break;
                        }
                        customSequence.add(gridNumber);
                    }
                } catch (Exception e) {
                    System.out.println("Error at recording sequence");
                }

            } else if (baseCommand.equals("play")) {

                if (numArguments == 1) {
                    if (customSequence.size() == 0) {
                        connection.sendText("There are no recorded sequence!");
                    } else {
                        for (int tileNum: customSequence) {
                            sendTempMessageOnTile(userName, tileNum);
                            sendTap(tileNum, userName);
                            securityCheck(userName);
                        }
                    }
                }

            } else if (baseCommand.equals("check")) {

                if (numArguments == 1) {
                    securityCheck("console");
                }

            } else if (baseCommand.equals("auto")) {

                if (numArguments == 1) {
                    sendTempMessageOnTile(userName, 226);
                    sendTap(226, userName);
                    securityCheck(userName);

                    Thread.sleep(500);

                    sendTempMessageOnTile(userName, 116);
                    sendTap(116, userName);
                    securityCheck(userName);
                } else {
                    connection.sendText("@" + userName + " The auto command doens't require any arguments VoHiYo");
                }

			} else if (baseCommand.equals("down") || baseCommand.equals("up")) {

                if (numArguments == 1 || numArguments == 2) {

                    try {
                        int numberTimes = 1;
                        if (numArguments == 2) {
                            try {
                                numberTimes = Integer.parseInt(listTerms[1]);
                            } catch (Exception e) {
                                connection.sendText("@" + userName + " The argument for the up/down command " + 
                                        "must be a value between 1-20 VoHiYo");
                            }
                            if (numberTimes > 20) {
                                numberTimes = 10;
                            }
                        }

                        boolean executed = false;
                        for (int i = 0; i < numberTimes; i++) {
                            if (listTerms[0].equals("up")) {
                                sendTempMessageOnTile(userName, 154);
                                sendTempMessageOnTile(userName, 178);
                                executed = sendSwipe(154, 178, userName);
                            } else if (listTerms[0].equals("down")) {
                                sendTempMessageOnTile(userName, 178);
                                sendTempMessageOnTile(userName, 154);
                                executed = sendSwipe(178, 154, userName);
                            }
                        }
                        if (executed) {
                            securityCheck(userName);
                        }
                    } catch (Exception e) {
                        connection.sendText("@" + userName + " Invalid up/down command please double check the usage examples VoHiYo");
                    }
                } else {
                    connection.sendText("@" + userName + " The up/down command accepts one argument VoHiYo");
                }

            } else if (baseCommand.equals("close") || baseCommand.equals("c")) {
                
				if (numArguments == 1) {
					closeRewards();
				}

            } else if (baseCommand.equals("button")) {

				String buttonName = listTerms[1];

				if (buttonName.equals("auto")) { 
					connection.sendText(userName + " \"auto\""); 
					eventSystem.addMessage("auto", userName); 
				} else if (buttonName.equals("end")) { 
					connection.sendText(userName + " \"end\""); 
					eventSystem.addMessage("end", userName); 
				} else if (buttonName.equals("up")) { 
					connection.sendText(userName + " \"up\""); 
					eventSystem.addMessage("up", userName); 
				} else if (buttonName.equals("down")) { 
					connection.sendText(userName + " \"down\""); 
					eventSystem.addMessage("down", userName); 
                } else if (isNumeric(buttonName)) {
                    connection.sendText(userName + " \"" + buttonName + "\"");
                    eventSystem.addMessage(buttonName, userName);
				} else {
                    System.out.println("Unknown button");
                }

            } else if (baseCommand.equals("hellooverlay")) {

                System.out.println("hello overlay");
                initializer.getSocketSystem().toggleSecurity(enableSecurityChecks);

            } else if (baseCommand.equals("helloclient")) {

                System.out.println("FEH got hello client");
                SocketServerSystem sys = initializer.getSocketSystem();
                try {
                    sys.addButton(67, 70, "Auto", userName);
                    sys.addButton(74, 70, "End", userName);
                    sys.addButton(81, 70, "Up", userName);
                    sys.addButton(88, 70, "Down", userName);

                    sys.addButton(67, 77, "10", userName);
                    sys.addButton(73, 77, "13", userName);

                    sys.addButton(67, 84, "218", userName);
                    sys.addButton(73, 84, "220", userName);
                    sys.addButton(79, 84, "222", userName);
                    sys.addButton(85, 84, "224", userName);

                } catch (Exception e) {
                    System.out.println("error at adding buttons");
                }

            } else if (baseCommand.equals("spam")) {

                if (numArguments == 2) {
                    try {
                        int numberTimes = Integer.parseInt(listTerms[1]);
                        if (numberTimes < 1 || numberTimes > 200) {
                            connection.sendText("'spam' command only accepts a value between 1 to 200");
                        } else {
                            for (int i = 0; i < numberTimes; i++) {
                                boolean executed = sendTap(222, userName);
                            }
                            securityCheck(userName);
                        }
                    } catch (Exception e) {
                        System.out.println("error at spamming");
                    }
                }

            } else if (baseCommand.equals("s")) {

                if (numArguments == 3) {
                    try {
                        int grid1 = Integer.parseInt(listTerms[1]);
                        int grid2 = Integer.parseInt(listTerms[2]);

						sendTempMessageOnTile(userName, grid1);
						sendTempMessageOnTile(userName, grid2);

                        boolean executed = sendSwipe(grid1, grid2, userName);
                        if (executed) {
                            securityCheck(userName);
                        }
                    } catch (Exception e) {
                        connection.sendText("@" + userName + " Invalid swipe command please double check the usage examples VoHiYo");
                    }
                } else {
                    connection.sendText("@" + userName + " The swipe command accepts two arguments VoHiYo");
                }
            } else if (baseCommand.equals("analyze")) {

				String[] gameDataTerms = {
					"asdf"
				};

                int freeX = 940;
                int freeY = 294;

                int baseX = 92;
				int baseY = 1272;
                int width = 391;
                int height = 146;

                int offsetY = 74;

                int columnBaseX = 274;
                int columnBaseY = 1226;

                // HP
                device.sendTouchCommand(freeX, freeY);
                Thread.sleep(100);
                device.sendTouchCommand(columnBaseX, columnBaseY);
                Thread.sleep(500);
                screenshot();
				String detectedText = ImageAnalysis.getTextPosition(gameDataTerms, width, height, baseX, baseY);
                //System.out.println("HP: " + detectedText);
       

                // Atk
                columnBaseY = columnBaseY + offsetY;
				baseY = baseY + offsetY;
                device.sendTouchCommand(freeX, freeY);
                Thread.sleep(100);
                device.sendTouchCommand(columnBaseX, columnBaseY);
                Thread.sleep(500);
                screenshot();
                detectedText = ImageAnalysis.getTextPosition(gameDataTerms, width, height, baseX, baseY);
                //System.out.println("Atk: " + detectedText);
         

				// spd
                columnBaseY = columnBaseY + offsetY;
                baseY = baseY + offsetY;
                device.sendTouchCommand(freeX, freeY);
                Thread.sleep(100);
                device.sendTouchCommand(columnBaseX, columnBaseY);
                Thread.sleep(500);
                screenshot();
                detectedText = ImageAnalysis.getTextPosition(gameDataTerms, width, height, baseX, baseY);
                //System.out.println("Atk: " + detectedText);
           

				// def
                columnBaseY = columnBaseY + offsetY;
                baseY = baseY + offsetY;
                device.sendTouchCommand(freeX, freeY);
                Thread.sleep(100);
                device.sendTouchCommand(columnBaseX, columnBaseY);
                Thread.sleep(500);
                screenshot();
                detectedText = ImageAnalysis.getTextPosition(gameDataTerms, width, height, baseX, baseY);
                //System.out.println("Atk: " + detectedText);
               

				// res
                columnBaseY = columnBaseY + offsetY;
                baseY = baseY + offsetY;
                device.sendTouchCommand(freeX, freeY);
                Thread.sleep(100);
                device.sendTouchCommand(columnBaseX, columnBaseY);
                Thread.sleep(500);
                screenshot();
                detectedText = ImageAnalysis.getTextPosition(gameDataTerms, width, height, baseX, baseY);
                //System.out.println("Atk: " + detectedText);
             



            }
            /*
               } else if (baseCommand.equals("hold") || baseCommand.equals("h")) {

                sendTempMessageOnTile(userName, Integer.parseInt(listTerms[1]));

                if (numArguments == 2) {
                    boolean executed = sendHold(Integer.parseInt(listTerms[1]), 3, userName);
                    if (executed) {
                        securityCheck(userName);
                    }
                } else if (numArguments == 3) {
                    int numberSeconds = Integer.parseInt(listTerms[2]);
					boolean executed = sendHold(Integer.parseInt(listTerms[1]), Integer.parseInt(listTerms[2]), userName);
                    if (executed) {
                        securityCheck(userName);
                    }
                }
                */
            /*
            } else if (baseCommand.equals("a")) {

                if (numArguments == 1) {
                    boolean executed = sendTap(142, userName);
                } else if (numArguments == 2) {

					int numberTimes = Integer.parseInt(listTerms[1]);
					if (numberTimes < 1 || numberTimes > 10) {
						numberTimes = 5;
					}

					for (int i = 0; i < numberTimes; i++) {
                        boolean executed = sendTap(142, userName);
                        Thread.sleep(1000);
					}

				}
			}*/
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

    private void processDirection(String direction, String userName) {

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
            connection.sendText("@" + userName + " Invalid command, must be either n, e, s, w, ne, nw, se or sw.");
		}

    }

    /*
    private boolean advancedCheck(int targetTileNumber, String detectedText) {

        boolean ignoreClick = false;

        try {
            String[] gameDataTerms = {
                "Inherit", "InheniteSkill"
            };
            //ArrayList<String> foundTerms = HelpTools.containsText(detectedText, gameDataTerms);
            ArrayList<String> foundTerms = HelpTools.containsText(detectedText, gameDataTerms);
            if (foundTerms.size() > 0) {

                for (String term : foundTerms) {
                    if ((term.equals("Inherit") || term.equals("InheniteSkill")) 
                            && (targetTileNumber >= 85 && targetTileNumber <= 108)) {
                        System.out.println("Ignored inheritance button click");
                        return true;
                    }
                }

            }

        } catch (Exception e) {
            System.out.println("Error in security check");
            e.printStackTrace();
        }

        return ignoreClick;

    }*/


    public void securityCheck(String userName) {

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

            Thread.sleep(30000);
            device.sendTouchCommand(100, 1640);
            Thread.sleep(5000);
            device.sendTouchCommand(100, 1640);
            Thread.sleep(5000);

            boolean needStopBot = checkNewGameDataAndNewVersion();
            if (needStopBot) {
				System.exit(0);
                return;
            }

            checkResumeDialog();


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
                sendTap(222, userName);

                Thread.sleep(1000);

                sendTempMessageOnTile(userName, 116);
                sendTap(116, userName);

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
