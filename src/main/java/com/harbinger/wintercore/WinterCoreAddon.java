package com.harbinger.wintercore;

import com.harbinger.wintercore.init.WinterCoreBlocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(WinterCoreAddon.MODID)
public class WinterCoreAddon {
    public static final String MODID = "wintercore";

    public WinterCoreAddon() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register Config
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, com.harbinger.wintercore.config.WinterCoreConfig.COMMON_SPEC);

        // Register Blocks and BlockEntities
        WinterCoreBlocks.BLOCKS.register(modEventBus);
        WinterCoreBlocks.ITEMS.register(modEventBus);
        WinterCoreBlocks.BLOCK_ENTITIES.register(modEventBus);
        WinterCoreBlocks.CREATIVE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }
}
