package com.integreight.onesheeld.utils;


public class TimeOut extends Thread {

    boolean isTimeout;

    public interface TimeoutHandler {
        void onTimeout();

        void onTick(int secondsLeft);
    }

    int secondsLeft;
    int totalSeconds;
    TimeoutHandler handler;

    public TimeOut(int seconds, TimeoutHandler handler) {
        isTimeout = false;
        this.totalSeconds = seconds;
        this.handler = handler;
        stopTimer();
        start();
    }

    public TimeOut(int seconds) {
        isTimeout = false;
        this.totalSeconds = seconds;
        stopTimer();
        start();
    }

    public void resetTimer() {
        secondsLeft = totalSeconds;
    }

    public boolean isTimeout() {
        return isTimeout;
    }

    public void stopTimer() {
        if (isAlive()) this.interrupt();
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
                if (handler != null && secondsLeft != 0) handler.onTick(secondsLeft);
                secondsLeft--;
            } while (secondsLeft >= 0);
            isTimeout = true;
            if (handler != null) handler.onTimeout();
        } catch (InterruptedException e) {
            return;
        }
    }
}
