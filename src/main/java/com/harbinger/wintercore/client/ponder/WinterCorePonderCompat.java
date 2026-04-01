package com.harbinger.wintercore.client.ponder;

public class WinterCorePonderCompat {
    public static void init() {
        net.createmod.ponder.foundation.PonderIndex.addPlugin(new WinterCorePonderPlugin());
    }
}
