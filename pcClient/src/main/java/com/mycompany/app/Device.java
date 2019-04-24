package com.mycompany.app;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class Device
{

	private static void sendKey(int event) {
        try {
            Robot robot = new Robot();
            robot.keyPress(event);
            robot.keyRelease(event);
        } catch (Exception e) {
            System.out.println("Issue at key press");
        }
	}

    public static void keyboard(String key) throws AWTException {

		if (key.equals("enter")) {
			sendKey(KeyEvent.VK_ENTER);
		} else if (key.equals("w")) {
            sendKey(KeyEvent.VK_W);
        } else if (key.equals("a")) {
            sendKey(KeyEvent.VK_A);
        } else if (key.equals("s")) {
            sendKey(KeyEvent.VK_S);
        } else if (key.equals("d")) {
            sendKey(KeyEvent.VK_D);
        }

    }

	public static void click(int x, int y) throws AWTException {

		Robot bot = new Robot();
		bot.mouseMove(x, y);    
		bot.mousePress(InputEvent.BUTTON1_MASK);
		bot.mouseRelease(InputEvent.BUTTON1_MASK);
	}

}
