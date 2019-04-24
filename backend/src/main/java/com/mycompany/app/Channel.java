package com.mycompany.app;

import java.io.PrintStream;

public class Channel
{
	private String name;
	private PrintStream out;
	protected Channel( String name, PrintStream out )
	{
		this.name = name;
		this.out = out;
		out.println( "JOIN" + " " + "#" + name );
	}

    public void printlnRaw( String msg )
    {
        out.print ( "PRIVMSG" + " " + "#" + name + " " + ":" + msg );
    }

	public void println( String msg )
	{
		out.println ( "PRIVMSG" + " " + "#" + name + " " + ":" + msg );
	}

}
