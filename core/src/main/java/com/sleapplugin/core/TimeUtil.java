package com.sleapplugin.core;

import java.util.ArrayList;
import java.util.List;

public class TimeUtil {

    public List<Long> smoothTimes(long currentTime, long targetTime, int steps) {
        long diff = (targetTime - currentTime + 24000L) % 24000L;
        List<Long> times = new ArrayList<>(steps);
        for (int i = 1; i <= steps; i++) {
            double progress = (double) i / steps;
            double smoothProgress = (1 - Math.cos(Math.PI * progress)) / 2;
            long newTime = (currentTime + (long) (diff * smoothProgress)) % 24000L;
            times.add(newTime);
        }
        times.add(targetTime);
        return times;
    }
}

