package com.harbinger.wintercore.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

public class WinterCoreConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static class Common {
        public final ForgeConfigSpec.ConfigValue<Integer> effectRadius;
        public final ForgeConfigSpec.ConfigValue<Double> damageMultiplier;
        public final ForgeConfigSpec.ConfigValue<Boolean> renderSnow;
        public final ForgeConfigSpec.ConfigValue<Boolean> preventSporeSpawns;
        public final ForgeConfigSpec.ConfigValue<Integer> energyPerTick;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> blockConversions;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("general");
            
            effectRadius = builder.comment("Radius of the Winter Core's effect (in blocks, default = 6 chunks = 96 blocks)")
                    .translation("wintercore.config.effectRadius")
                    .define("effectRadius", 96);
            
            damageMultiplier = builder.comment("Damage multiplier against hostile monsters within the radius")
                    .translation("wintercore.config.damageMultiplier")
                    .define("damageMultiplier", 5.0);
            
            renderSnow = builder.comment("Whether it should render a continuous snowstorm inside the radius")
                    .translation("wintercore.config.renderSnow")
                    .define("renderSnow", true);

            preventSporeSpawns = builder.comment("Whether the Winter Core should prevent Spore mod entities from spawning within its radius")
                    .translation("wintercore.config.preventSporeSpawns")
                    .define("preventSporeSpawns", true);
            
            energyPerTick = builder.comment("Energy consumed per tick when the core is active (default 5 FE/t = 100 FE/s)")
                    .translation("wintercore.config.energyPerTick")
                    .define("energyPerTick", 5);

            blockConversions = builder.comment("List of block conversions in the format 'modid:source_block|modid:target_block'")
                    .translation("wintercore.config.blockConversions")
                    .defineList("blockConversions", Arrays.asList(
                            "spore:infested_stone|minecraft:stone", 
                            "minecraft:mycelium|minecraft:dirt", 
                            "spore:infested_dirt|minecraft:dirt", 
                            "spore:infested_deepslate|minecraft:deepslate", 
                            "spore:infested_sand|minecraft:sand", 
                            "spore:infested_gravel|minecraft:gravel", 
                            "spore:infested_netherrack|minecraft:netherrack", 
                            "spore:infested_end_stone|minecraft:end_stone", 
                            "spore:infested_soul_sand|minecraft:soul_sand", 
                            "spore:infested_red_sand|minecraft:red_sand", 
                            "spore:infested_clay|minecraft:clay", 
                            "spore:infested_cobblestone|minecraft:cobblestone", 
                            "spore:infested_cobbled_deepslate|minecraft:cobbled_deepslate", 
                            "spore:infested_stone_bricks|minecraft:stone_bricks", 
                            "spore:infested_bricks|minecraft:bricks", 
                            "spore:infested_laboratory_block|spore:lab_block", 
                            "spore:infested_laboratory_block1|spore:lab_block1", 
                            "spore:infested_laboratory_block2|spore:lab_block2", 
                            "spore:infested_laboratory_block3|spore:lab_block3",
                            "spore:biomass_block|minecraft:blue_ice",
                            "the_flesh_that_hates:flesh_block|minecraft:blue_ice",
                            "the_flesh_that_hates:flesh_sand|minecraft:sand",
                            "the_flesh_that_hates:flesh_tree|minecraft:spruce_log",
                            "the_flesh_that_hates:flesh_plank|minecraft:spruce_planks",
                            "the_flesh_that_hates:flesh_pile|minecraft:gravel",
                            "the_flesh_that_hates:tumor|minecraft:ice",
                            "the_flesh_that_hates:purulent_tumor|minecraft:packed_ice",
                            "spore:fungal_shell|minecraft:mushroom_stem",
                            "spore:brain_remnants|minecraft:ice",
                            "spore:rotten_log|minecraft:oak_log",
                            "spore:crusted_bile|minecraft:gravel",
                            "spore:rotten_grass|minecraft:grass_block",
                            "spore:mycelium|minecraft:dirt",
                            "spore:mycelium_block|minecraft:mushroom_stem",
                            "spore:root_block|minecraft:mushroom_stem"
                    ), obj -> obj instanceof String && ((String) obj).contains("|"));
            
            builder.pop();
        }
    }
}
