package com.mycompany.app;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringJoiner;
import net.sourceforge.tess4j.*;


public class Device {

    private String address;

    public Device(String address) {
        this.address = address;
    }

    public static boolean connectADB(String deviceAddress) throws InterruptedException {

        //String result1 = runBashCommand("adb disconnect " + address);
        //Thread.sleep(2000);

        boolean result1 = runBashCommand("adb connect " + deviceAddress);
        if (result1 == false) {
            return false;
        } else {
            return true;
        }

    }

    public void focusApp(String gameID) throws InterruptedException {
        runBashCommand("adb -s " + address + " shell monkey -p " + gameID + " -c android.intent.category.LAUNCHER 1");
    }

    public boolean rebootDevice(String deviceAddress) throws InterruptedException {

        boolean result1 = runBashCommand("adb -s " + address + " reboot");

        Thread.sleep(60 * 1000);

        if (result1 == false) {
            return false;
        } else {
            return true;
        }
    }

    public boolean rebootApp(String gameID) throws InterruptedException {

        boolean result1 = runBashCommand("adb -s " + address + " shell am force-stop " + gameID);
        Thread.sleep(10000);
        boolean result2 = runBashCommand("adb -s " + address + " shell monkey -p " + gameID + " -c android.intent.category.LAUNCHER 1");

        if (result1 == false || result2 == false) {
            return false;
        } else {
            return true;
        }
    }

    // # http://www.guidingtech.com/15008/adb-control-keypress-broken-android-keys/
    public boolean sendKeyEvent(int keyNumber) {

		System.out.println("Sending key event: " + Integer.toString(keyNumber));
		String bashCommand = "adb -s " + address + " shell input keyevent " + Integer.toString(keyNumber);
        boolean result = runBashCommand(bashCommand);

        if (result == false) {
            return false;
        } else {
            return true;
        }

    }

    private boolean deviceSwipe(int mode) {

        String bashCommand = null;

		// mode 1 = left to right (right), 2 = bottom to top (down), 
		// mode 3 = top to bottom (up), 4 = right to left (left)
		if (mode == 1) {
			bashCommand = "adb -s " + address + " shell input swipe 132 1014 900 1014";
		} else if (mode == 2) {
			bashCommand = "adb -s " + address + " shell input swipe 574 1125 576 900";
		} else if (mode == 3) {
			bashCommand = "adb -s " + address + " shell input swipe 576 900 574 1125";
		} else if (mode == 4) {
			bashCommand = "adb -s " + address + " shell input swipe 900 1014 132 1014";
		}

        if (bashCommand != null) {
            boolean result = runBashCommand(bashCommand);

            if (result == false) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
	}

    public boolean sendSwipeCommand(int x1, int y1, int x2, int y2, int numberMS) {

        System.out.println("Swiping screen");
        String bashCommand = "adb -s " + address + " shell input swipe " + x1 + " " + y1 + " " + 
            x2 + " " + y2 + " " + Integer.toString(numberMS);
        boolean result = runBashCommand(bashCommand);

        if (result == false) {
            try {
                System.out.println("Retrying the reconnection");
                connectADB(address);

                Thread.sleep(1000);

                boolean result2 = runBashCommand(bashCommand);
                return result2;


            } catch (Exception e) {
                System.out.println("Failed at reconnect");
            }
        }

        return result;

    }

    // mode true = up, mode false = down
    public void multiSwipe(boolean mode) {

        System.out.println("Swiping..");

        if (mode == true) {
			deviceSwipe(3);
        } else {
			deviceSwipe(2);
		}

    }

    public void copyCroppedImage(String targetLocation) {

        String croppedName = "./screenshots/screenshot-cropped.png";
        String commandCopy = "cp " + croppedName + " " + targetLocation;
        runBashCommand(commandCopy);
    }

    public static void cropImage(String dimension, boolean enableNegate) {
        String screenshotName = "./screenshots/screenshot.png";
        String croppedName = "./screenshots/screenshot-cropped.png";
        // -negate
        String commandCrop = "";
        if (enableNegate) {
            commandCrop = "convert " + screenshotName + " -negate -crop " + dimension + " +repage " + croppedName;
        } else {
            commandCrop = "convert " + screenshotName + " -crop " + dimension + " +repage " + croppedName;
        }
        runBashCommand(commandCrop);
    }

    public String detectText(int width, int height, int x, int y) {

        System.out.println("Detecting text from screenshot..");

        long tStart = System.currentTimeMillis();

        String screenshotName = "./screenshots/screenshot.png";
        String croppedName = "./screenshots/screenshot-cropped.png";

        String dimension = Integer.toString(width) + "x" + Integer.toString(height) + 
            "+" + Integer.toString(x) + "+" + Integer.toString(y);
        cropImage(dimension, true);

		String textData = getText(croppedName);
        System.out.println(textData);

        long tEnd = System.currentTimeMillis();
        long tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000.0;
        System.out.println("done text analysis: " + Double.toString(elapsedSeconds));

        return textData;
    }

    public boolean takeScreenshotRaw() {

        // https://stackoverflow.com/questions/32883784/which-commands-line-are-used-to-take-a-screenshot-on-android-device-except-scre
        System.out.println("Taking screenshot..");
        long tStart = System.currentTimeMillis();

        String fileName = "screenshot.raw";
        String bashCommand1 = "adb -s " + address + " shell screencap /sdcard/" + fileName;
        String bashCommand2 = "adb -s " + address + " pull /sdcard/" + fileName + " ./screenshots/" + fileName;
		String bashCommand3 = "./removeHeader.sh";
        String bashCommand4 = "convert -size 1080x1920 -depth 8 -define png:compression-level=0 " + 
            "./screenshots/screenshot.rgba ./screenshots/screenshot.png";

        boolean result1 = runBashCommand(bashCommand1);
		boolean result2 = runBashCommand(bashCommand2);
		boolean result3 = runBashCommand(bashCommand3);
        boolean result4 = runBashCommand(bashCommand4);

        long tEnd = System.currentTimeMillis();
        long tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000.0;

        System.out.println("done screenshot: " + Double.toString(elapsedSeconds));

        if (result1 == false || result2 == false) {
            return false;
        } else {
            return true;
        }

    }

    public boolean takeScreenshot() {

        System.out.println("Taking screenshot..");
        long tStart = System.currentTimeMillis();

        String fileName = "screenshot.png";
        String bashCommand1 = "adb -s " + address + " shell screencap -p /sdcard/" + fileName;
        String bashCommand2 = "adb -s " + address + " pull /sdcard/" + fileName + " ./screenshots/" + fileName;
        boolean result1 = runBashCommand(bashCommand1);
        boolean result2 = runBashCommand(bashCommand2);


		long tEnd = System.currentTimeMillis();
		long tDelta = tEnd - tStart;
		double elapsedSeconds = tDelta / 1000.0;

		System.out.println("done screenshot: " + Double.toString(elapsedSeconds));

        if (result1 == false || result2 == false) {
            return false;
        } else {
            return true;
        }

    }

    public boolean sendTouchCommand(int xPoint, int yPoint) {

		System.out.println("Touching screen: " + Integer.toString(xPoint) + " " + Integer.toString(yPoint));
		String bashCommand = "adb -s " + address + " shell input tap " + Integer.toString(xPoint) + " " + Integer.toString(yPoint);
		boolean result = runBashCommand(bashCommand);
		
        if (result == false) {
            try {
                System.out.println("Retrying the reconnection");
                connectADB(address);

                Thread.sleep(1000);

                boolean result2 = runBashCommand(bashCommand);
                return result2;


            } catch (Exception e) {
                System.out.println("Failed at reconnect");
            }
        }

        return result;
    }

    public static boolean runBashCommand(String inputCommand) {

		String[] elements = inputCommand.split(" ");
        String stdout = null;
        try {
            Process process =
                new ProcessBuilder(elements)
                    .redirectErrorStream(true)
                    .start();
            final BufferedReader reader = new BufferedReader(new 
                    InputStreamReader(process.getInputStream()));
			StringJoiner sj = new StringJoiner(System.getProperty("line.separator"));
			reader.lines().iterator().forEachRemaining(sj::add);
			stdout = sj.toString();
            //System.out.println(stdout);

            //There should really be a timeout here.
            if (0 != process.waitFor()) {
                process.destroy();
                System.out.println("Failed bash execution: " + inputCommand);
                return false;
            } else {
                process.destroy();
            }

        } catch (Exception e) {
            //Warning: doing this is no good in high quality applications.
            //Instead, present appropriate error messages to the user.
            //But it's perfectly fine for prototyping.
            System.out.println("Exception");
            return false;
        }

        return true;

    }

    private String getText(String imageFileName) {

        File imageFile = new File(imageFileName);
        ITesseract instance = new Tesseract();  // JNA Interface Mapping
        // ITesseract instance = new Tesseract1(); // JNA Direct Mapping
        instance.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata/"); 
        instance.setLanguage("eng");

        String textData = null;

        try {
            textData = instance.doOCR(imageFile);
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
        }

        return textData;

    }

    private class ExecuteStatus {

        private boolean status;
        private String stdout;

        public ExecuteStatus(boolean status, String stdout) {
            this.status = status;
            this.stdout = stdout;
        }

    }

}
