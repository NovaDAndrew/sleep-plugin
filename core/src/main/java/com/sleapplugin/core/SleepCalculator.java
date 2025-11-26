package com.sleapplugin.core;

public class SleepCalculator {

    public int calculateRequiredSleeping(int onlinePlayers) {
        if (onlinePlayers <= 1) {
            return Integer.MAX_VALUE;
        }
        if (onlinePlayers % 2 == 1) {
            return (onlinePlayers - 1) / 2;
        } else {
            return onlinePlayers / 2;
        }
    }

    public boolean isNight(long time) {
        return time >= 12541 && time <= 23458;
    }
}

