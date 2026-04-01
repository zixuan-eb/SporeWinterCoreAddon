package com.harbinger.wintercore.data;

import com.harbinger.wintercore.WinterCoreAddon;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 注册数据生成 DataProvider（运行 ./gradlew runData 时触发）。
 */
@Mod.EventBusSubscriber(modid = WinterCoreAddon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WinterCoreDataGen {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        // 仅在客户端资源生成时（--client 参数）生成 Ponder 结构文件
        event.getGenerator().addProvider(
                event.includeClient(),
                new WinterCorePonderStructureProvider(event.getGenerator().getPackOutput())
        );
    }
}
