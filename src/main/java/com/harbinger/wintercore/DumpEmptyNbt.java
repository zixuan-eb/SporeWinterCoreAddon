package com.harbinger.wintercore;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.core.BlockPos;

import java.io.File;

public class DumpEmptyNbt {
    public static void main(String[] args) {
        try {
            CompoundTag compoundTag = new CompoundTag();
            
            net.minecraft.nbt.ListTag size = new net.minecraft.nbt.ListTag();
            size.add(net.minecraft.nbt.IntTag.valueOf(9));
            size.add(net.minecraft.nbt.IntTag.valueOf(5));
            size.add(net.minecraft.nbt.IntTag.valueOf(9));
            compoundTag.put("size", size);
            
            net.minecraft.nbt.ListTag palette = new net.minecraft.nbt.ListTag();
            addPalette(palette, "minecraft:air");               // 0
            addPalette(palette, "wintercore:winter_core_base"); // 1
            addPalette(palette, "wintercore:winter_core_pedestal");// 2
            addPalette(palette, "wintercore:winter_core_pillar");  // 3
            addPalette(palette, "wintercore:winter_core");         // 4
            compoundTag.put("palette", palette);
            
            net.minecraft.nbt.ListTag blocks = new net.minecraft.nbt.ListTag();
            for (int x = 0; x < 9; x++) {
                for (int y = 0; y < 5; y++) {
                    for (int z = 0; z < 9; z++) {
                        int state = 0; // 默认空气
                        
                        // Y=1 (索引为对齐原点，实际我们使用Y=0代表底盘坐标系)
                        // 结构图:
                        // Base在 (4,0,4)
                        if (y == 0) {
                            if ((x==4&&z==4) || (x==5&&z==4) || (x==3&&z==4) || (x==4&&z==5) || (x==4&&z==3) ||
                                (x==6&&z==4) || (x==2&&z==4) || (x==4&&z==6) || (x==4&&z==2)) {
                                state = 1;
                            }
                        }
                        // Pedestal在 (4,1,4)
                        if (y == 1 && x == 4 && z == 4) {
                            state = 2;
                        }
                        // Pillar在 (2,1,4), (6,1,4), (4,1,2), (4,1,6) 以及 Y=2
                        if (y == 1 || y == 2) {
                            if ((x==2&&z==4) || (x==6&&z==4) || (x==4&&z==2) || (x==4&&z==6)) {
                                state = 3;
                            }
                        }
                        // Core在 (4,2,4)
                        if (y == 2 && x == 4 && z == 4) {
                            state = 4;
                        }

                        if (state != 0) { // 优化：只存非空气块
                            CompoundTag block = new CompoundTag();
                            net.minecraft.nbt.ListTag pos = new net.minecraft.nbt.ListTag();
                            pos.add(net.minecraft.nbt.IntTag.valueOf(x));
                            pos.add(net.minecraft.nbt.IntTag.valueOf(y));
                            pos.add(net.minecraft.nbt.IntTag.valueOf(z));
                            block.put("pos", pos);
                            block.putInt("state", state);
                            blocks.add(block);
                        }
                    }
                }
            }
            compoundTag.put("blocks", blocks);
            compoundTag.put("entities", new net.minecraft.nbt.ListTag());
            compoundTag.putInt("DataVersion", 3463);
            
            File out = new File("src/main/resources/assets/wintercore/ponder/winter_core_assembly.nbt");
            out.getParentFile().mkdirs();
            NbtIo.writeCompressed(compoundTag, out);
            System.out.println("DUMP SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void addPalette(net.minecraft.nbt.ListTag palette, String name) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Name", name);
        net.minecraft.nbt.CompoundTag props = new net.minecraft.nbt.CompoundTag();
        if (name.equals("wintercore:winter_core")) {
            props.putString("formed", "false");
            tag.put("Properties", props);
        }
        palette.add(tag);
    }
}
