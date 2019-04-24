package com.mycompany.app;

import java.util.ArrayList;

public class HelpTools {

	public static ArrayList<String> containsText(String fullText, String[] listTerms) {

        ArrayList<String> foundList = new ArrayList<String>();

        for (String word : listTerms) {
            if (fullText.contains(word)) {
                foundList.add(word);
                System.out.println("found term: " + word);
            }
        }

		return foundList;
	}
}
