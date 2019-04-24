package com.mycompany.app;

public class WallpaperThread implements Runnable {

	SocketServerSystem socketSystem;

	public WallpaperThread(SocketServerSystem socketSystem) {
		this.socketSystem = socketSystem;
	}

    @Override
	public void run() {
		while (true) {

			socketSystem.changeWallpaper();

			try {
				Thread.sleep(1000 * 60 * 5);
			} catch (Exception e) {
				System.out.println("error in sleep");
			}
		}
	}
}
