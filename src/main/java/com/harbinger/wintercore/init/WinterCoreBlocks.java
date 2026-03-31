package com.harbinger.wintercore.init;

import com.harbinger.wintercore.WinterCoreAddon;
import com.harbinger.wintercore.block.WinterCoreBlock;
import com.harbinger.wintercore.block.WinterCoreBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class WinterCoreBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, WinterCoreAddon.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WinterCoreAddon.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, WinterCoreAddon.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WinterCoreAddon.MODID);

    public static final RegistryObject<Block> WINTER_CORE = BLOCKS.register("winter_core", WinterCoreBlock::new);

    public static final RegistryObject<Item> WINTER_CORE_ITEM = ITEMS.register("winter_core",
            () -> new BlockItem(WINTER_CORE.get(), new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static final RegistryObject<Block> WINTER_CORE_BASE = BLOCKS.register("winter_core_base", 
            () -> new Block(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().strength(50.0F, 1200.0F).requiresCorrectToolForDrops()));
    
    public static final RegistryObject<Item> WINTER_CORE_BASE_ITEM = ITEMS.register("winter_core_base",
            () -> new BlockItem(WINTER_CORE_BASE.get(), new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static final RegistryObject<Block> WINTER_CORE_PILLAR = BLOCKS.register("winter_core_pillar", 
            () -> new WinterCorePillarBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().strength(50.0F, 1200.0F).requiresCorrectToolForDrops()));
    
    public static final RegistryObject<Item> WINTER_CORE_PILLAR_ITEM = ITEMS.register("winter_core_pillar",
            () -> new BlockItem(WINTER_CORE_PILLAR.get(), new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static final RegistryObject<BlockEntityType<WinterCoreBlockEntity>> WINTER_CORE_BE = BLOCK_ENTITIES.register("winter_core_be",
            () -> BlockEntityType.Builder.of(WinterCoreBlockEntity::new, WINTER_CORE.get()).build(null));

    public static final RegistryObject<CreativeModeTab> WINTER_CORE_TAB = CREATIVE_TABS.register("winter_core_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.winter_core"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> WINTER_CORE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(WINTER_CORE_ITEM.get());
                output.accept(WINTER_CORE_BASE_ITEM.get());
                output.accept(WINTER_CORE_PILLAR_ITEM.get());
            }).build());
}
