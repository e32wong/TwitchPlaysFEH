package com.mycompany.app;

import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.AWTException;
import java.io.File;
import java.io.IOException;     
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );

        Launcher.launchApplicaton("C:\\Windows\\System32\\notepad");
        SocketClient socketClient = new SocketClient();
        socketClient.connectWebsocket();

        try {
            Robot robot = new Robot();
            String format = "jpg";
            String fileName = "./screenshot." + format;

            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenFullImage = robot.createScreenCapture(screenRect);
            ImageIO.write(screenFullImage, format, new File(fileName));

            System.out.println("A full screenshot saved!");

        } catch (AWTException | IOException ex) {
            System.err.println(ex);
        }

    }
}
