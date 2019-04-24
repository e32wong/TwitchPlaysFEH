package com.mycompany.app;

public class TextPosition {

    private int x;
    private int y;
    private String text;

    public TextPosition (int x, int y, String text) {
        this.x = x;
        this.y = y;
        this.text = text;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getText() {
        return text;
    }

}
