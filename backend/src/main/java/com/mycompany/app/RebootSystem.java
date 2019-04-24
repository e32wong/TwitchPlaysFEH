package com.mycompany.app;
  
import java.util.Scanner;

public class RebootSystem extends Thread {

    EventSystem eventSystem;

    public RebootSystem(EventSystem eventSystem) {

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
