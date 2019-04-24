package com.mycompany.app;

import java.util.ArrayList;

public abstract class GameBase
{
    BanSystem banSystem;
    AdminSystem adminSystem;
    IRCConnection connection;
    ReportSystem reportSystem;
    boolean insideAutobattle = false;
    String gameID;
	long lastResetRequest = 0;
    Device device;
    boolean enableSecurityChecks = true;
    String deviceAddress = "";
    SocketInitializer initializer;
    EventSystem eventSystem;

    abstract public void processReboot();
	abstract public boolean executeStateCommand(String[] listTerms, String userName);
	abstract public boolean executeGameModCommand(String[] listTerms, String userName);
    abstract public void updateAfterState();
    abstract public void resetVariables();
    abstract public void securityCheck(String userName);

    public void sendTempMessageOnTile(String userName, int tileNumber) {
        initializer.getSocketSystem().sendTempMessageOnTile(userName, tileNumber);
    }

    public void screenshot() {
        device.takeScreenshotRaw();
    }

    private void sendBackButton() {
        try {
            device.sendKeyEvent(4);
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println("Error in back button");
        }
    }

    private boolean executeGlobalCommand(String[] listTerms, String userName, String fullCommand) {

        boolean executedCommand = true;

        String baseCommand = listTerms[0];
        int numArguments = listTerms.length;

        // global commands
        if (baseCommand.equals("report")) {
            if (numArguments >= 3) {
				reportSystem.addReport(fullCommand);
				connection.sendText("@" + userName + " Your report against " + listTerms[1] + 
                        " will be reviewed by an admin shortly VoHiYo");

            }
        } else if (baseCommand.equals("reset")) {
            if (numArguments == 1) {
				long timeSinceLastRequest = System.currentTimeMillis() - lastResetRequest;
				if (timeSinceLastRequest > 120 * 1000) {
                    // there are no current requests
					connection.sendText("@" + userName + " Reboot command received. " +
							"You have to confirm the reboot request by typing " +
							"the 'reset' command one more time to active the reset VoHiYo");
                    lastResetRequest = System.currentTimeMillis();
				} else {
                    connection.sendText("@" + userName + " Rebooting.. it will take 1.5 minute to initialize..");
                    lastResetRequest = 0;

                    processReboot();

                    connection.sendText("@" + userName + " Reboot completed VoHiYo");
				}
            }
        } else {
            executedCommand = false;
        }

        return executedCommand;
    }

    public void process(ArrayList<String> rawList) {

        String userName = rawList.get(0);

        if (banSystem.isBanned(userName) == true) {
            return;
        }

        String[] listTerms = new String[rawList.size() - 1];
        listTerms = rawList.subList(1, rawList.size()).toArray(listTerms);

        String fullCommand = "";
        for (String str : listTerms) {
            fullCommand = fullCommand + " " + str;
        }
        fullCommand = fullCommand.substring(1, fullCommand.length());

        fullCommand = userName + ": " + "\"" + fullCommand + "\"";
        initializer.getSocketSystem().toggleBusyIcon(true);
        initializer.getSocketSystem().updateCommand(fullCommand);

        System.out.println("process..");

        boolean executedCommand = false;
        if (userName.equals("console") || adminSystem.isMod(userName)) {
            executedCommand = executeAdminCommand(listTerms);
        }

        if (executedCommand == false) {
            executedCommand = executeGlobalCommand(listTerms, userName, fullCommand);
        }

        if (executedCommand == false) {
            executedCommand = executeGameModCommand(listTerms, userName);
        }

        if (executedCommand == false) {
            executedCommand = executeStateCommand(listTerms, userName);
        }

        updateAfterState();

        initializer.getSocketSystem().toggleBusyIcon(false);

    }

    private boolean executeAdminCommand(String[] listTerms) {

        int numArguments = listTerms.length;

        boolean executedCommand = true;
        switch (listTerms[0]) {
            case "security":
                if (numArguments == 2) {
                    if (listTerms[1].equals("on")) {
                        System.out.println("Security check is now online");
                        enableSecurityChecks = true;
                    } else if (listTerms[1].equals("off")) {
						System.out.println("Security check is now offline");
						enableSecurityChecks = false;
                    }
                }
                break;
            case "screenshot":
                screenshot();
                break;
            case "reboot":

                try {
                    device.rebootDevice(deviceAddress);
                } catch (Exception e) {
                    System.out.println("Error in reboot command");
                }
                processReboot();

                break;
            case "scan":
                securityCheck("console");
                break;
            case "ban":
                if (numArguments == 2) {
                    banSystem.banUser(listTerms[1]);
                } else {
                    System.out.println(listTerms);
                }
                break;
            case "unban":
                if (numArguments == 2) {
                    banSystem.unbanUser(listTerms[1]);
                }
                break;
            case "mod":
                if (numArguments == 2) {
                    adminSystem.modUser(listTerms[1]);
                }
                break;
            case "unmod":
                if (numArguments == 2) {
                    adminSystem.unmodUser(listTerms[1]);
                }
                break;
            case "whitelist":
                if (numArguments == 2) {
                    banSystem.whitelistUser(listTerms[1]);
                }
                break;
			case "unwhitelist":
                if (numArguments == 2) {
                    banSystem.unwhiteListUser(listTerms[1]);
                }
                break;
            case "touch":
                if (numArguments == 3) {
                    device.sendTouchCommand(Integer.parseInt(listTerms[1]), Integer.parseInt(listTerms[2]));
                }
                break;
                /*
            case "grid":
                if (numArguments == 2) {
                    boolean state;
                    if (listTerms[1].equals("on")) {
                        state = true;
                    } else {
                        state = false;
                    }
                    initializer.getSocketSystem().toggleOverlay("pixelGrid", state);
                }
                break;
            */
            case "mark":
                if (numArguments == 3) {
                    initializer.getSocketSystem().markScreen(
                            Integer.parseInt(listTerms[1]),
                            Integer.parseInt(listTerms[2]),
                            15000);
                }
                break;
            default:
                executedCommand = false;
        }

        return executedCommand;

    }

} 
