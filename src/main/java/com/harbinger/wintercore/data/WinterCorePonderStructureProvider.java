package com.harbinger.wintercore.data;

import com.harbinger.wintercore.WinterCoreAddon;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * 为 Ponder 思索场景生成初始空白结构 NBT 文件（9×7×9 全空气）。
 * 运行 ./gradlew runData 后会生成到 src/generated/resources/ 下。
 * 生成后需提交到 git，之后不必再次运行（除非场景尺寸变动）。
 */
public class WinterCorePonderStructureProvider implements DataProvider {

    private final PackOutput output;

    public WinterCorePonderStructureProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path path = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                .resolve(WinterCoreAddon.MODID + "/ponder/winter_core_assembly.nbt");

        return CompletableFuture.runAsync(() -> {
            try {
                CompoundTag nbt = buildSceneNbt();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                NbtIo.writeCompressed(nbt, baos);
                byte[] bytes = baos.toByteArray();

                @SuppressWarnings("deprecation")
                com.google.common.hash.HashCode hashCode = com.google.common.hash.Hashing.sha1().hashBytes(bytes);
                cache.writeIfNeeded(path, bytes, hashCode);
            } catch (IOException e) {
                throw new RuntimeException("Failed to generate ponder structure NBT", e);
            }
        });
    }

    /**
     * 构造 9×7×9 全空气结构体的 CompoundTag。
     * 仅在两个对角放置空气块以声明尺寸（bounding box），其余隐式为空气。
     */
    private CompoundTag buildSceneNbt() {
        CompoundTag root = new CompoundTag();

        // 结构尺寸：宽 9、高 7、深 9（覆盖整个多方块的搭建范围）
        root.putIntArray("size", new int[]{9, 7, 9});

        // 调色板：仅含空气
        ListTag palette = new ListTag();
        CompoundTag airEntry = new CompoundTag();
        airEntry.putString("Name", "minecraft:air");
        palette.add(airEntry);
        root.put("palette", palette);

        // 在对角两端各放一个空气块以声明尺寸边界
        ListTag blocks = new ListTag();
        blocks.add(makeBlock(0, 0, 0, 0));
        blocks.add(makeBlock(8, 6, 8, 0));
        root.put("blocks", blocks);

        root.put("entities", new ListTag());
        root.putInt("DataVersion", 3465); // Minecraft 1.20.1
        return root;
    }

    private CompoundTag makeBlock(int x, int y, int z, int stateIndex) {
        CompoundTag block = new CompoundTag();
        block.put("pos", new IntArrayTag(new int[]{x, y, z}));
        block.putInt("state", stateIndex);
        return block;
    }

    @Override
    public String getName() {
        return "WinterCore Ponder Structures";
    }
}
