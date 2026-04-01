package com.harbinger.wintercore.client.ponder;

import com.harbinger.wintercore.WinterCoreAddon;
import com.harbinger.wintercore.init.WinterCoreBlocks;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

/**
 * 注册 Fimbulwinter Core 的思索（Ponder）场景。
 * 兼容 Create 0.6.0 的 PonderPlugin 接口。
 */
public class WinterCorePonderPlugin implements PonderPlugin {

    @Override
    public String getModId() {
        return WinterCoreAddon.MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        // 为凛冬核心注册多方块组装教程场景
        helper.addStoryBoard(
                net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(WinterCoreBlocks.WINTER_CORE_ITEM.get()),
                "winter_core_assembly",
                WinterCorePonderScenes::assembly,
                net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(WinterCoreBlocks.WINTER_CORE_ITEM.get())
        );
    }
}

