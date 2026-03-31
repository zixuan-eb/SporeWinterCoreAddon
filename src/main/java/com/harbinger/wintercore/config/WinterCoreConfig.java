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
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> blockConversions;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("general");
            
            effectRadius = builder.comment("Radius of the Winter Core's effect").define("effectRadius", 32);
            
            damageMultiplier = builder.comment("Damage multiplier against hostile monsters within the radius").define("damageMultiplier", 5.0);
            
            renderSnow = builder.comment("Whether it should render a continuous snowstorm inside the radius").define("renderSnow", true);
            
            blockConversions = builder.comment("List of block conversions in the format 'modid:source_block|modid:target_block'")
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
                            "spore:biomass_block|minecraft:snow_block",
                            "the_flesh_that_hates:flesh_block|minecraft:snow_block",
                            "the_flesh_that_hates:flesh_sand|minecraft:snow_block",
                            "the_flesh_that_hates:flesh_tree|minecraft:snow_block",
                            "the_flesh_that_hates:flesh_plank|minecraft:snow_block",
                            "the_flesh_that_hates:flesh_pile|minecraft:snow_block",
                            "the_flesh_that_hates:tumor|minecraft:snow_block",
                            "the_flesh_that_hates:purulent_tumor|minecraft:snow_block"
                    ), obj -> obj instanceof String && ((String) obj).contains("|"));
            
            builder.pop();
        }
    }
}
