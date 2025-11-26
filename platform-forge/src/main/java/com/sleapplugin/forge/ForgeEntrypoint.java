package com.sleapplugin.forge;

import com.sleapplugin.core.SleepCalculator;

public class ForgeEntrypoint {
    private final SleepCalculator calculator = new SleepCalculator();

    public ForgeEntrypoint() {
        System.out.println("[SleepPlugin] Forge entrypoint constructed");
    }
}
