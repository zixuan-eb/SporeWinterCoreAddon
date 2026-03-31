package com.harbinger.wintercore.data;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class WinterCoreData {
    public static final Map<ResourceLocation, ResourceLocation> CONVERSION_MAP = new HashMap<>();
    
    // Load conversion mapping from the prompt
    static {
        CONVERSION_MAP.put(new ResourceLocation("spore", "crusted_bile"), new ResourceLocation("minecraft", "dirt"));
        CONVERSION_MAP.put(new ResourceLocation("spore", "organite"), new ResourceLocation("minecraft", "gravel"));
        CONVERSION_MAP.put(new ResourceLocation("spore", "mycelium_block"), new ResourceLocation("minecraft", "sand"));
        CONVERSION_MAP.put(new ResourceLocation("spore", "fungal_shell"), new ResourceLocation("minecraft", "sand"));
        CONVERSION_MAP.put(new ResourceLocation("spore", "growths_big"), new ResourceLocation("minecraft", "short_grass"));
        CONVERSION_MAP.put(new ResourceLocation("spore", "bloomfung2"), new ResourceLocation("minecraft", "brown_mushroom"));
        CONVERSION_MAP.put(new ResourceLocation("spore", "blomfung"), new ResourceLocation("minecraft", "brown_mushroom"));
        CONVERSION_MAP.put(new ResourceLocation("spore", "growths_small"), new ResourceLocation("minecraft", "brown_mushroom"));
        CONVERSION_MAP.put(new ResourceLocation("spore", "growth_mycelium"), new ResourceLocation("minecraft", "brown_mushroom"));
        CONVERSION_MAP.put(new ResourceLocation("spore", "fungal_roots"), new ResourceLocation("minecraft", "brown_mushroom"));
        CONVERSION_MAP.put(new ResourceLocation("spore", "fungal_stem"), new ResourceLocation("minecraft", "snow_block"));
        CONVERSION_MAP.put(new ResourceLocation("spore", "fungal_stem_top"), new ResourceLocation("minecraft", "snow_block"));
        CONVERSION_MAP.put(new ResourceLocation("spore", "hanging_fungal_stem"), new ResourceLocation("minecraft", "snow_block"));
        CONVERSION_MAP.put(new ResourceLocation("spore", "wall_growths_big"), new ResourceLocation("minecraft", "snow_block"));
        CONVERSION_MAP.put(new ResourceLocation("spore", "wall_growths"), new ResourceLocation("minecraft", "snow_block"));
    }
}
