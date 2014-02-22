package com.integreight.onesheeld.utils;


public class TimeOut extends Thread {

	public interface TimeoutHandler {
		void onTimeout();

		void onTick(int secondsLeft);
	}

	int secondsLeft;
	int totalSeconds;
	TimeoutHandler handler;

	public TimeOut(int seconds, TimeoutHandler handler) {
		this.totalSeconds = seconds;
		this.handler = handler;
		stopTimer();
		start();
	}
	
	public void resetTimer(){
		secondsLeft=totalSeconds;
	}

	public void stopTimer(){
		if(isAlive())this.interrupt();
	}
	@Override
	public synchronized void start() {
		// TODO Auto-generated method stub
		resetTimer();
		super.start();
	}
	@Override
	public void run() {
		try {
			do {

				Thread.sleep(1000);
				handler.onTick(secondsLeft);
				secondsLeft--;
			} while (secondsLeft >= 0);
			handler.onTimeout();
		} catch (InterruptedException e) {
			return;
		}
	}
}
