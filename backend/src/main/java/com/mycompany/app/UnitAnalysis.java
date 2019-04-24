package com.mycompany.app;

import java.io.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.util.ArrayList;

public class UnitAnalysis {

    public UnitAnalysis() {

    }

    private static void createFolder(String targetFolder) {
           File file = new File(targetFolder);
           file.mkdirs();
    }

    public static ArrayList<Integer> getUnitsFromMap() {

        ArrayList<Integer> listUnits = new ArrayList<Integer>();

        createFolder("./screenshots/tiles/");

        try {
            File file= new File("./screenshots/screenshot.png");
            BufferedImage image = ImageIO.read(file);

			int offset = 180;
			int startX = 0;
			int startY = 304;

            ColorUnit enemyUnitColor = new ColorUnit(254, 75, 105);
            ColorUnit friendUnitColor = new ColorUnit(75, 225, 254);
            ColorUnit enemyBlackColor = new ColorUnit(50, 9, 16);
            ColorUnit friendBlackColor = new ColorUnit(9, 36, 50);

            // iterate over all tiles
            int index = 1;
            String saveTileImageBase = "./screenshots/tiles/cropped-";
			for (int y = 0; y < 8; y++) {
                for (int x = 0; x < 6; x++) {

                    System.out.println(index + ":");

                    String tileSavePath = saveTileImageBase + Integer.toString(index) + ".png";
					String dimension = Integer.toString(offset) + "x" + Integer.toString(offset) + "+" + 
								Integer.toString(startX + x * offset) + "+" + Integer.toString(startY + y * offset);
                    String cmd = "convert ./screenshots/screenshot.png -crop " + dimension + " +repage " + tileSavePath;
                    //System.out.println(cmd);
                    Device.runBashCommand("convert ./screenshots/screenshot.png -crop " + dimension + " +repage " + tileSavePath);

					File fileTile = new File(tileSavePath);
					BufferedImage imageTile = ImageIO.read(fileTile);
                    ColorUnit inputColorUnit1 = getPixelColor(imageTile, 156, 161);
                    ColorUnit inputColorUnit2 = getPixelColor(imageTile, 78, 161);
                    ColorUnit inputColorUnit3 = getPixelColor(imageTile, 177, 160);

                    boolean isEnemy1 = checkColorInRange(inputColorUnit1, enemyUnitColor, 3);
                    boolean isEnemy2 = checkColorInRange(inputColorUnit2, enemyUnitColor, 3);
                    boolean isEnemy3 = checkColorInRange(inputColorUnit3, enemyBlackColor, 3);
                    boolean isFriend1 = checkColorInRange(inputColorUnit1, friendUnitColor, 3);
                    boolean isFriend2 = checkColorInRange(inputColorUnit2, friendUnitColor, 3);
                    boolean isFriend3 = checkColorInRange(inputColorUnit3, friendBlackColor, 3);

                    if (isEnemy1 && isEnemy2 && isEnemy3) {
                        System.out.println("enemy on index: " + index);
                        listUnits.add(index);
                    }

                    if (isFriend1 && isFriend2 && isFriend3) {
                        System.out.println("friend on index: " + index);
                        listUnits.add(index);
                    }

                    System.out.println("\n\n");

                    index = index + 1;
                }
			}

        } catch (Exception e) {
            System.out.println("Error at getting units from map");
            e.printStackTrace();
        }

        return listUnits;
    }

    private static boolean checkColorInRange(ColorUnit sourceInput, ColorUnit targetColor, int range) {
        
        int sourceRed = sourceInput.getRed();
        int sourceGreen = sourceInput.getGreen();
        int sourceBlue = sourceInput.getBlue();

        int targetRed = targetColor.getRed();
        int targetGreen = targetColor.getGreen();
        int targetBlue = targetColor.getBlue();

        if (checkNumberInRange(sourceRed, targetRed, 5) &&
                checkNumberInRange(sourceRed, targetRed, 5) &&
                checkNumberInRange(sourceRed, targetRed, 5)) {
            return true;
        } else {
            return false;
        }

    }

    private static boolean checkNumberInRange(int input, int target, int range) {

        int lowerBound = target - range;
        int upperBound = target + range;

        if (input > lowerBound && input < upperBound) {
            return true;
        } else {
            return false;
        }

    }

	public static ColorUnit getPixelColor(BufferedImage image, int x, int y) {


        try {
            // Getting pixel color by position x and y 
            int clr=  image.getRGB(x, y); 
            int  red   = (clr & 0x00ff0000) >> 16;
            int  green = (clr & 0x0000ff00) >> 8;
            int  blue  =  clr & 0x000000ff;

            System.out.println("RGB: "+ red + "," + green + "," + blue);

            ColorUnit colorUnit = new ColorUnit(red, green, blue);
            return colorUnit;

        } catch (Exception e) {
            System.out.println("error at getting pixel color");
            e.printStackTrace();
        }

        return null;

	}

    private static class ColorUnit {
        
        int red;
        int green;
        int blue;

        public ColorUnit(int red, int green, int blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public int getRed() {
            return red;
        }

        public int getGreen() {
            return green;
        }

        public int getBlue() {
            return blue;
        }
    }

}

