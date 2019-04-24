package com.mycompany.app;

public class Scheduler implements Runnable {

    EventSystem eventSystem;
    long rebootInterval;
    long lastCommandTime = System.currentTimeMillis();

    public Scheduler(EventSystem eventSystem, long rebootInterval) {

        this.eventSystem = eventSystem;
        this.rebootInterval = rebootInterval;

    }

    public void commandTriggered() {
        lastCommandTime = System.currentTimeMillis();
    }

    public void run() {

        while (true) {

            if (System.currentTimeMillis() - lastCommandTime > rebootInterval) {
                lastCommandTime = System.currentTimeMillis();

                eventSystem.addMessage("reboot", "console");

            } else {

                // System.out.println("Reboot: " + (System.currentTimeMillis() - lastCommandTime) + "/" + rebootInterval);

				try {
					Thread.sleep(10000);
				} catch (Exception e) {
					System.out.println("Exception in sleep");
				}
            }

        }


    }


}
