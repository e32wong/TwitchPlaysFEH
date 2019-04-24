package com.mycompany.app;

import java.lang.Process;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class Launcher 
{

    public static void launchApplicaton(String executable) {
        try {
            Process process = new ProcessBuilder(executable).start();
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
        } catch (Exception e) {
            System.out.println("Error while trying to launch the application");
        }

    }


}
