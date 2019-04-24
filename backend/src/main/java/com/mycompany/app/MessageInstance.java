
package com.mycompany.app;

public class MessageInstance {

    private String message;
    private String userName;

    public MessageInstance(String userName, String message) {

        this.message = message;
        this.userName = userName;
    }

    public String getUsername() {
        return userName;
    }
    public String getMessage() {
        return message;
    }
}
