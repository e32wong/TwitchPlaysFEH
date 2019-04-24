package com.mycompany.app;

import java.util.Scanner;

public class UserInput extends Thread {

    EventSystem eventSystem;

    public UserInput(EventSystem eventSystem) {

        this.eventSystem = eventSystem;
    }

    public void run() {

        Scanner keyboard= new Scanner(System.in);

        while (true) {
            String input = keyboard.nextLine();
            eventSystem.addMessage(input, "console");

        }
    }


}
