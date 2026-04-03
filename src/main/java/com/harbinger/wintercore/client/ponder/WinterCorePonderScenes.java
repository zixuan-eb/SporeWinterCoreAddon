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
        scene.title("winter_core_assembly", "组装芬布尔凛冬核心");
        scene.configureBasePlate(0, 0, 9);
        scene.idle(10);

        // ── 第一步：铺设九块凛冬石（底层，中心原点为 Y=0）────────────────────
        scene.world().showSection(util.select().fromTo(2, 0, 2, 6, 0, 6), Direction.UP);
        scene.overlay().showText(80)
                .text("第一步：铺设9块凛冬石作为底座")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(util.grid().at(4, 0, 4), Direction.UP));
        scene.idle(90);

        // ── 第二步：放置凛冬基座（Y=1 中心）──────────────────────────────────
        scene.world().showSection(util.select().position(4, 1, 4), Direction.DOWN);
        scene.overlay().showText(70)
                .text("第二步：在中心放置凛冬基座")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 4), Direction.UP));
        scene.idle(80);

        // ── 第三步：四方各立两块凛冬石柱（Y=1 和 Y=2）────────────────────────
        // 石柱分布在 X=2, Z=4 | X=6, Z=4 | X=4, Z=2 | X=4, Z=6
        scene.world().showSection(util.select().fromTo(2, 1, 2, 6, 2, 6).substract(util.select().position(4, 1, 4)).substract(util.select().position(4, 2, 4)), Direction.DOWN);
        scene.overlay().showText(80)
                .text("第三步：在四端竖立凛冬石柱")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 4), Direction.UP));
        scene.idle(90);

        // ── 第四步：将凛冬核心放在基座正上方（Y=2 中心）────────────────────
        scene.world().showSection(util.select().position(4, 2, 4), Direction.DOWN);
        scene.overlay().showText(80)
                .text("最后：安放凛冬核心")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(util.grid().at(4, 2, 4), Direction.UP));
        scene.idle(90);

        // ── 完成提示 ─────────────────────────────────────────────────────────
        scene.overlay().showText(80)
                .colored(PonderPalette.GREEN)
                .text("组装完成！核心将自动构建连接并开始工作")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(util.grid().at(4, 2, 4), Direction.UP));
        scene.idle(80);
    }
}
