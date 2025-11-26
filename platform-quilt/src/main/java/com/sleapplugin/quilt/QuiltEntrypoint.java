package com.sleapplugin.quilt;

import com.sleapplugin.core.SleepCalculator;

public class QuiltEntrypoint {
    private final SleepCalculator calculator = new SleepCalculator();

    public void onInitialize() {
        System.out.println("[SleepPlugin] Quilt entrypoint initialized");
    }
}
