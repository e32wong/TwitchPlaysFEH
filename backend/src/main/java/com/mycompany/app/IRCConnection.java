package com.mycompany.app;

import java.io.PrintStream;
import java.net.UnknownHostException;
import java.io.IOException;
import java.net.Socket;

public class IRCConnection extends Thread
{

	private String host;
	private int port;
	private PrintStream out;
    private EventSystem eventSystem;
    private String configFolder;
    private String twitchOAuth;
    private String twitchChannel;
    private SocketInitializer initializer;
    
    private Channel cha;

    private Thread t = null; // for debugging
    private Socket socket;

	/*
    public IRCConnection( String host ) throws UnknownHostException, IOException
	{
		this( host, 6667 );
	}*/

	public IRCConnection( String host, int port, EventSystem eventSystem, 
            SocketInitializer initializer, String configFolder,
            String twitchOAuth, String twitchChannel)
            throws UnknownHostException, IOException
	{
		this.host = host;
		this.port = port;
        this.eventSystem = eventSystem;
        this.initializer = initializer;
        this.configFolder = configFolder;
        this.twitchOAuth = twitchOAuth;
        this.twitchChannel = twitchChannel;
        
        startConnection();
	}

    private void startConnection() {

        try {
            socket = new Socket( host, port );
            out = new PrintStream( socket.getOutputStream() );

            register();
            connect();
        } catch (Exception e) {
            System.out.println("Error in start connection for IRC");
            e.printStackTrace();
        }
    }

    private void connect() throws UnknownHostException, IOException
    {
		createChannel();

        t = new InputDumper( socket.getInputStream(), eventSystem, configFolder );
        t.setDaemon( true );
        t.start();

    }

	private void createChannel() {
		cha = new Channel(twitchChannel, out);
        cha.println( "i'm in the channel!" );
	}
	
	private void reconnect() {
		try {
			socket.close();
			socket = new Socket( host, port );
		} catch (Exception e) {
			System.out.println("Error at reconnection");
		}
	}

	private void checkConnected() {
		try {
			if (socket.isConnected() == false) {
				reconnect();
			}
		} catch (Exception e) {
			System.out.println("Error at checking if socket is connected");
		}
	}

    public void sendText(String message) {
		checkConnected();
        try {
            cha.println(message);
        } catch (Exception e) {
            System.out.println("Error at sending message to channel: " + message);
			reconnect();
        }
    }

    public void sendTextRaw(String message) {
		checkConnected();
        try {
            out.print(message);
        } catch (Exception e) {
            System.out.println("Error at sending message to channel: " + message);
			reconnect();
        }
    }

	private void register()
	{

		String nickname = twitchChannel;
		String localhost = "localhost";
        out.println( "PASS" + " " + twitchOAuth);
		out.println( "USER" + " " + nickname + " " + localhost + " " + host + " " + nickname );
		out.println( "NICK" + " " + nickname);
	}

    /*
	public Channel getChannel()
	{
        return cha;
	}*/

    public void run()
    {

        while (true) {
            try {
                t.join();
                System.out.println("Detected IRC failure so restarting IRC line");

            } catch (Exception e) {
                System.out.println("Error while executing join on input dump thread");
                e.printStackTrace();
            }

            try {
                System.out.println("Sleeping 10 seconds before restarting IRC line");
                Thread.sleep(10000);
                startConnection();
            } catch (Exception e) {
                System.out.println("Error in sleep");
            }

        }

		// keep checking for health information
        /*
        while (true) {
            try {
                sendTextRaw( "PING :hello\r\n" );
                Thread.sleep(15000);
            } catch (Exception e) {
				System.out.println("Error in IRC ping protocol");
                reconnect();
            }
        }*/
    }

}
