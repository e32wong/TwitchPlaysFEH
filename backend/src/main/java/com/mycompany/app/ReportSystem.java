package com.mycompany.app;

import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.nio.file.Paths;
import java.nio.file.Files;

import java.io.IOException;

public class ReportSystem {

    private Logger logger;
    private FileHandler fh;
    private String reportFolder;

	public ReportSystem(String targetFolder) {

        this.reportFolder = targetFolder + "/reports/";

		createReportFolder();

        logger = Logger.getLogger("ReportLog");
        try {
            // This block configure the logger with handler and formatter  
            String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
            fh = new FileHandler(reportFolder + date + ".txt", true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	public void addReport(String report) {
		logger.info(report);
	}

    private void createReportFolder() {
        try {
            Files.createDirectories(Paths.get(reportFolder));
        } catch (Exception e) {
            System.out.println("error at creating report folder");
        }

    }

}


