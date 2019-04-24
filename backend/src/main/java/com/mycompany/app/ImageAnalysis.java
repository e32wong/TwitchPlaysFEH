package com.mycompany.app;

import java.nio.*;
import net.sourceforge.tess4j.ITessAPI.*;
import net.sourceforge.tess4j.TessAPI1;
import net.sourceforge.tess4j.util.ImageIOHelper;
import com.sun.jna.Pointer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import javax.imageio.ImageIO;
import java.util.Arrays;
import java.util.ArrayList;
import net.sourceforge.tess4j.util.ImageHelper;

public class ImageAnalysis {

	public static String getTextPosition(String[] targetWords, int width, int height, int x, int y) {

        //System.out.println("List: " + Arrays.toString(targetWords));

        System.out.println("Analyzing image..");
        long tStart = System.currentTimeMillis();

        String dimension = Integer.toString(width) + "x" + Integer.toString(height) +
            "+" + Integer.toString(x) + "+" + Integer.toString(y);
        Device.cropImage(dimension, false);

        boolean found = false;
        ArrayList<String> listFoundTerms = new ArrayList<String>();
        
        int finalX = 0;
        int finalY = 0;
        String finalWord = "";
		TessBaseAPI api = null;
        String detectedText = null;
        try {
            File png = new File("./screenshots/screenshot-cropped.png");
            BufferedImage image = ImageIO.read(new FileInputStream(png)); // require jai-imageio lib to read TIFF
            //BufferedImage image = ImageHelper.getScaledInstance(imageFull, width / 2, height / 2);
            //System.out.println(height);
            //System.out.println(image.getHeight());
            //image = ImageHelper.convertImageToBinary(image);
            ByteBuffer buf = ImageIOHelper.convertImageData(image);
            int bpp = image.getColorModel().getPixelSize();
            int bytespp = bpp / 8;
            int bytespl = (int) Math.ceil(image.getWidth() * bpp / 8.0);

            api = TessAPI1.TessBaseAPICreate();
            TessAPI1.TessBaseAPIInit3(api, "/usr/share/tesseract-ocr/4.00/tessdata/", "eng");
            TessAPI1.TessBaseAPISetPageSegMode(api, TessAPI1.TessPageSegMode.PSM_AUTO );
            //TessAPI1.TessBaseAPISetPageSegMode(api, TessAPI1.TessPageSegMode.PSM_SINGLE_COLUMN );
            TessAPI1.TessBaseAPISetImage(api, buf, image.getWidth(), image.getHeight(), bytespp, bytespl);
            Pointer ptr = TessAPI1.TessBaseAPIGetUTF8Text(api);

            detectedText = ptr.getString(1);
            System.out.println("Detected text:\n" + detectedText);
            TessAPI1.TessDeleteText(ptr);

            /*
            //TessBaseAPI api = TessAPI1.TessBaseAPICreate();
            TessResultIterator ri = TessAPI1.TessBaseAPIGetIterator(api);
            TessPageIterator pi = TessAPI1.TessResultIteratorGetPageIterator(ri);
            do {

                Pointer ptr = TessAPI1.TessResultIteratorGetUTF8Text(ri, TessPageIteratorLevel.RIL_WORD);
                String word = ptr.getString(0);
                IntBuffer leftB = IntBuffer.allocate(1);
                IntBuffer topB = IntBuffer.allocate(1);
                IntBuffer rightB = IntBuffer.allocate(1);
                IntBuffer bottomB = IntBuffer.allocate(1);
                TessAPI1.TessPageIteratorBoundingBox(pi, TessPageIteratorLevel.RIL_WORD, leftB, topB, rightB, bottomB);
                int left = leftB.get();
                int top = topB.get();
                int right = rightB.get();
                int bottom = bottomB.get();

				System.out.println("Word: " + word);
				System.out.println(left + " " + top + " " + right + " " + bottom);

                for (String targetWord : targetWords) {
                    if (word.contains(targetWord)) {
                        finalX = (left + right) / 2 + x;
                        finalY = (top + bottom) / 2 + y;
                        found = true;
                        listFoundTerms.add(word);
                        finalWord = word;
                    }
                }
                
            } while (TessAPI1.TessPageIteratorNext(pi, TessAPI1.TessPageIteratorLevel.RIL_WORD) == TessAPI1.TRUE);
            */
        } catch (Exception e) {
            System.out.println("Error at getting text information");
            e.printStackTrace();
        } finally {
			if (api != null) {
				TessAPI1.TessBaseAPIClear(api);
				TessAPI1.TessBaseAPIEnd(api);
                TessAPI1.TessBaseAPIDelete(api);
			}
		}

        /*
		TextPosition pos = null;
		if (found == true) {
			pos = new TextPosition(finalX, finalY, finalWord);
        }

        if (found == false) {
            System.out.println("No text detected");
        } else {
            System.out.println("Text detected:");
            for (String term : listFoundTerms) {
                System.out.println("- " + term);
            }
        }
        */

        long tEnd = System.currentTimeMillis();
        long tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000.0;

        System.out.println("done analyzing image: " + Double.toString(elapsedSeconds));

        return detectedText;
	}


}

