package com.harbinger.wintercore.client.ponder;

import com.harbinger.wintercore.WinterCoreAddon;
import com.harbinger.wintercore.init.WinterCoreBlocks;
import com.simibubi.create.foundation.ponder.PonderRegistrationHelper;

/**
 * 注册 Fimbulwinter Core 的思索（Ponder）场景。
 * 此类仅在 Create 已加载时才会被实例化，通过 ModList 守卫进行条件调用。
 */
public class WinterCorePonderPlugin {

    private static final PonderRegistrationHelper HELPER =
            new PonderRegistrationHelper(WinterCoreAddon.MODID);

    public static void register() {
        // 为凛冬核心注册多方块组装教程场景
        HELPER.addStoryBoard(
                WinterCoreBlocks.WINTER_CORE_ITEM.get(),
                "winter_core_assembly",
                WinterCorePonderScenes::assembly
        );
    }
}
