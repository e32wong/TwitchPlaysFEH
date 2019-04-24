package com.mycompany.app;
  
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.TimeUnit;

public class Uptime extends Thread {

	long currentUptime = 0l;
    String filePath;
    SocketInitializer initializer;

    public Uptime(SocketInitializer initializer, String configFolder) {

        this.initializer = initializer;
        this.filePath = configFolder + "/uptime.txt";

        try {
            File uptimeFile = new File(filePath);
			if (!uptimeFile.exists()) {

				FileWriter fw = new FileWriter(uptimeFile);
				fw.write("0");
				fw.close();

				currentUptime = 0l;
			} else {
				BufferedReader br = new BufferedReader(new FileReader(uptimeFile));
				String line = br.readLine();

				currentUptime = Long.parseLong(line);
			}

        } catch (Exception e) {

            System.out.println("error on uptime file init");

        }

    }

    public void run() {

        while (true) {

            try {
                Thread.sleep(1000);

				int day = (int)TimeUnit.SECONDS.toDays(currentUptime);        
				long hours = TimeUnit.SECONDS.toHours(currentUptime) - (day *24);
				long minute = TimeUnit.SECONDS.toMinutes(currentUptime) - (TimeUnit.SECONDS.toHours(currentUptime)* 60);
				long second = TimeUnit.SECONDS.toSeconds(currentUptime) - (TimeUnit.SECONDS.toMinutes(currentUptime) *60);

                initializer.getSocketSystem().updateTime(Integer.toString(day) + ":" + 
                        Long.toString(hours) +  ":" + Long.toString(minute) + 
                        ":" + Long.toString(second));

                currentUptime = currentUptime + 1l;

				File uptimeFile = new File(filePath);
                FileWriter fw = new FileWriter(uptimeFile);
                fw.write(Long.toString(currentUptime));
                fw.close();

            } catch (Exception e) {

            }

        }
    }
}


