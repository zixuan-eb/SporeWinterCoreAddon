package com.harbinger.wintercore.client.ponder;

import com.harbinger.wintercore.block.WinterCoreBlock;
import com.harbinger.wintercore.init.WinterCoreBlocks;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.PonderPalette;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 芬布尔凛冬核心多方块结构的 Ponder 思索场景。
 * 场景坐标系（9×7×9 空气结构体）：
 *   核心中心 = (4, 3, 4)
 *   凛冬石层 Y=1，基座+下部石柱层 Y=2，核心+上部石柱层 Y=3
 */
public class WinterCorePonderScenes {

    public static void assembly(SceneBuilder scene, SceneBuildingUtil util) {
        // ── 场景初始化 ──────────────────────────────────────────────────────
        scene.title("winter_core_assembly", "Assembling the Fimbulwinter Core");
        scene.configureBasePlate(0, 0, 9);
        scene.world().hideSection(util.select().everywhere(), Direction.DOWN);
        scene.idle(10);

        // ── 第一步：铺设九块凛冬石（Y=1 的十字延伸图案）────────────────────
        BlockState baseState = WinterCoreBlocks.WINTER_CORE_BASE.get().defaultBlockState();

        // 中心
        scene.world().setBlock(util.grid().at(4, 1, 4), baseState, false);
        // 距离 1 的四方
        scene.world().setBlock(util.grid().at(5, 1, 4), baseState, false);
        scene.world().setBlock(util.grid().at(3, 1, 4), baseState, false);
        scene.world().setBlock(util.grid().at(4, 1, 5), baseState, false);
        scene.world().setBlock(util.grid().at(4, 1, 3), baseState, false);
        // 距离 2 的四方（石柱将立于此）
        scene.world().setBlock(util.grid().at(6, 1, 4), baseState, false);
        scene.world().setBlock(util.grid().at(2, 1, 4), baseState, false);
        scene.world().setBlock(util.grid().at(4, 1, 6), baseState, false);
        scene.world().setBlock(util.grid().at(4, 1, 2), baseState, false);

        scene.world().showSection(util.select().fromTo(2, 1, 2, 6, 1, 6), Direction.UP);
        scene.overlay().showText(80)
                .text(Component.translatable("ponder.wintercore.winter_core_assembly.step1").getString())
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 4), Direction.UP));
        scene.idle(90);

        // ── 第二步：放置凛冬基座（Y=2 中心）──────────────────────────────────
        BlockState pedestalState = WinterCoreBlocks.WINTER_CORE_PEDESTAL.get().defaultBlockState();
        scene.world().setBlock(util.grid().at(4, 2, 4), pedestalState, false);
        scene.world().showSection(util.select().position(4, 2, 4), Direction.UP);
        scene.overlay().showText(70)
                .text(Component.translatable("ponder.wintercore.winter_core_assembly.step2").getString())
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(util.grid().at(4, 2, 4), Direction.UP));
        scene.idle(80);

        // ── 第三步：四方各立两块凛冬石柱（Y=2 和 Y=3）────────────────────────
        BlockState pillarState = WinterCoreBlocks.WINTER_CORE_PILLAR.get().defaultBlockState();
        // 下部石柱（Y=2 四方位置）
        scene.world().setBlock(util.grid().at(2, 2, 4), pillarState, false);
        scene.world().setBlock(util.grid().at(6, 2, 4), pillarState, false);
        scene.world().setBlock(util.grid().at(4, 2, 2), pillarState, false);
        scene.world().setBlock(util.grid().at(4, 2, 6), pillarState, false);
        // 上部石柱（Y=3 四方位置）
        scene.world().setBlock(util.grid().at(2, 3, 4), pillarState, false);
        scene.world().setBlock(util.grid().at(6, 3, 4), pillarState, false);
        scene.world().setBlock(util.grid().at(4, 3, 2), pillarState, false);
        scene.world().setBlock(util.grid().at(4, 3, 6), pillarState, false);

        scene.world().showSection(util.select().fromTo(2, 2, 2, 6, 3, 6), Direction.UP);
        scene.overlay().showText(80)
                .text(Component.translatable("ponder.wintercore.winter_core_assembly.step3").getString())
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(util.grid().at(2, 2, 4), Direction.UP));
        scene.idle(90);

        // ── 第四步：将凛冬核心放在基座正上方（Y=3 中心）────────────────────
        BlockState coreState = WinterCoreBlocks.WINTER_CORE.get().defaultBlockState()
                .setValue(WinterCoreBlock.FORMED, false);
        scene.world().setBlock(util.grid().at(4, 3, 4), coreState, false);
        scene.world().showSection(util.select().position(4, 3, 4), Direction.DOWN);
        scene.overlay().showText(80)
                .text(Component.translatable("ponder.wintercore.winter_core_assembly.step4").getString())
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(util.grid().at(4, 3, 4), Direction.UP));
        scene.idle(90);

        // ── 完成提示 ─────────────────────────────────────────────────────────
        scene.overlay().showText(80)
                .colored(PonderPalette.GREEN)
                .text(Component.translatable("ponder.wintercore.winter_core_assembly.activated").getString())
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(util.grid().at(4, 3, 4), Direction.UP));
        scene.idle(80);
    }
}
