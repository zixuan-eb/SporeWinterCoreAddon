package com.harbinger.wintercore.block;

import com.harbinger.wintercore.data.WinterCoreData;
import com.harbinger.wintercore.init.WinterCoreBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

import com.harbinger.wintercore.config.WinterCoreConfig;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WinterCoreBlockEntity extends BlockEntity {

    // Global registry of all active Winter Cores to quickly lookup blockspawn rules anywhere in the world.
    public static final Map<ResourceKey<Level>, Set<BlockPos>> ACTIVE_CORES = new ConcurrentHashMap<>();
    
    // Config dynamic properties
    private static Map<ResourceLocation, ResourceLocation> dynamicReplaceMap = null;
    private static long lastConfigRead = 0;
    private static final int[][] cornerCoords = {{-2, 0}, {0, -2}, {2, 0}, {0, 2}};

    public boolean isFormed = false;
    private int tickCounter = 0;
    private int scanY = Integer.MIN_VALUE; // 哨兵值，首次使用时重置到世界最低点
    private int scanX = 0;
    private int scanZ = 0;

    // 净化波半径：从 0 缓慢增长到 effectRadius，形成从核心向外蔓延的净化效果
    private float waveRadius = 0f;
    private static final float WAVE_GROWTH_PER_SECOND = 0.5f; // 每秒扩展 0.5 格（radius=96 时约 3.2 分钟铺满）

    // 光波环动画：伤害触发后环形粒子从内向外扩散淡出
    private int waveAnimTicks = 0;
    private static final int WAVE_ANIM_DURATION = 25; // 动画总帧数（25 tick ≈ 1.25 秒）
    
    public WinterCoreBlockEntity(BlockPos pos, BlockState state) {
        super(WinterCoreBlocks.WINTER_CORE_BE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    protected void saveAdditional(net.minecraft.nbt.CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("IsFormed", this.isFormed);
        tag.putFloat("WaveRadius", this.waveRadius);
        // 持久化扫描坐标，避免重载后从头扫
        tag.putInt("ScanX", this.scanX);
        tag.putInt("ScanZ", this.scanZ);
        tag.putInt("ScanY", this.scanY == Integer.MIN_VALUE ? 0 : this.scanY);
    }

    @Override
    public void load(net.minecraft.nbt.CompoundTag tag) {
        super.load(tag);
        this.isFormed = tag.getBoolean("IsFormed");
        this.waveRadius = tag.getFloat("WaveRadius");
        this.scanX = tag.getInt("ScanX");
        this.scanZ = tag.getInt("ScanZ");
        // ScanY 为 0 时安全钳会在 processBlockConversion 中自动修正到 worldMinY
        this.scanY = tag.contains("ScanY") ? tag.getInt("ScanY") : Integer.MIN_VALUE;
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag() {
        return this.saveWithFullMetadata();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide()) {
            unregisterCore();
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(100.0, 300.0, 100.0);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, WinterCoreBlockEntity blockEntity) {
        if (!blockEntity.isFormed) return;
        
        // Spawn massive snowstorms if config allows
        if (WinterCoreConfig.COMMON.renderSnow.get()) {
            net.minecraft.client.player.LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
            int radius = WinterCoreConfig.COMMON.effectRadius.get();
            if (player != null && player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) <= radius * radius) {
                for (int i = 0; i < 60; i++) {
                    double px = player.getX() + level.random.nextGaussian() * 20;
                    double pz = player.getZ() + level.random.nextGaussian() * 20;
                    double py = player.getY() + 15 + level.random.nextFloat() * 15;
                    level.addParticle(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE, px, py, pz, 0, -0.05, 0);
                }
            }
        }
    }

    private void registerCore() {
        if (!ACTIVE_CORES.containsKey(level.dimension()) || !ACTIVE_CORES.get(level.dimension()).contains(getBlockPos())) {
            ACTIVE_CORES.computeIfAbsent(level.dimension(), k -> new HashSet<>()).add(getBlockPos());
        }
    }

    private void unregisterCore() {
        if (ACTIVE_CORES.containsKey(level.dimension())) {
            ACTIVE_CORES.get(level.dimension()).remove(getBlockPos());
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WinterCoreBlockEntity blockEntity) {
        blockEntity.tickCounter++;

        // 每 tick：更新光波环动画（不受 20tick 节流影响，确保动画流畅）
        if (blockEntity.isFormed && blockEntity.waveAnimTicks > 0
                && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            blockEntity.updateWaveAnimation(serverLevel);
        }

        // 每 20 tick（约 1 秒）：结构检测 + 方块净化 + 实体伤害
        if (blockEntity.tickCounter % 20 != 0) return;

        if (blockEntity.checkMultiblock()) {
            if (!blockEntity.isFormed) return;
            blockEntity.processBlockConversion();
            // 造成伤害后触发一次新的光波环动画
            blockEntity.processEntityDamage();
            if (blockEntity.waveAnimTicks <= 0) {
                blockEntity.waveAnimTicks = WAVE_ANIM_DURATION;
            }
        }
    }

    /**
     * 光波环动画：END_ROD 粒子从核心中心向外扩散，粒子数随半径增大而减少，
     * 形成「从内向外慢慢淡出」的光环效果。每 tick 调用一次。
     */
    private void updateWaveAnimation(net.minecraft.server.level.ServerLevel serverLevel) {
        // progress = 0（动画刚开始，环在核心处）→ 1（动画结束，环在外缘）
        float progress = 1f - (waveAnimTicks / (float) WAVE_ANIM_DURATION);
        float ringR = progress * 5.5f;                              // 环半径：0 → 5.5 格
        int count = Math.max(6, (int) (40 * (1f - progress * 0.87f))); // 粒子数：40 → 6（淡出）

        double cx = worldPosition.getX() + 0.5;
        double cy = worldPosition.getY() + 0.5 + 0.15 + progress * 0.4; // 随扩散轻微上浮
        double cz = worldPosition.getZ() + 0.5;

        for (int i = 0; i < count; i++) {
            double angle = (2.0 * Math.PI * i) / count;
            double x = cx + Math.cos(angle) * ringR;
            double z = cz + Math.sin(angle) * ringR;
            // END_ROD：白色发光点，贴合凛冬/魔法主题
            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.END_ROD,
                    x, cy, z,
                    1,        // 粒子数
                    0.0, 0.03, 0.0, // 极小速度，只让粒子轻微上飘
                    0.0       // speed（保持 0 让位置精准）
            );
        }
        waveAnimTicks--;
    }

    private boolean checkMultiblock() {
        if (level == null) return false;
        
        int[][] baseCoords = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {2, 0}, {-2, 0}, {0, 2}, {0, -2}
        };
        boolean formed = level.getBlockState(worldPosition.below()).getBlock() == WinterCoreBlocks.WINTER_CORE_PEDESTAL.get() &&
                         level.getBlockState(worldPosition.below(2)).getBlock() == WinterCoreBlocks.WINTER_CORE_BASE.get();

        for (int[] c : baseCoords) {
            if (level.getBlockState(worldPosition.offset(c[0], -2, c[1])).getBlock() != WinterCoreBlocks.WINTER_CORE_BASE.get()) {
                formed = false;
                break;
            }
        }

        if (formed) {
            for (int i = 0; i < 4; i++) {
                int[] p = cornerCoords[i];
                BlockPos p1 = worldPosition.offset(p[0], -1, p[1]);
                BlockPos p2 = worldPosition.offset(p[0], 0, p[1]);

                BlockState s1 = level.getBlockState(p1);
                BlockState s2 = level.getBlockState(p2);

                if (s1.getBlock() != WinterCoreBlocks.WINTER_CORE_PILLAR.get() || 
                    s2.getBlock() != WinterCoreBlocks.WINTER_CORE_PILLAR.get()) {
                    formed = false;
                    break;
                }
            }
        }
        
        if (this.isFormed != formed) {
            this.isFormed = formed;
            if (formed) {
               registerCore();
               level.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 0.8f);
               
               for (int i = 0; i < 4; i++) {
                   int cornerIndex = i;
                   int[] p = cornerCoords[i];
                   BlockPos p1 = worldPosition.offset(p[0], -1, p[1]);
                   BlockPos p2 = worldPosition.offset(p[0], 0, p[1]);
                   
                   level.setBlock(p1, level.getBlockState(p1).setValue(WinterCorePillarBlock.FORMED, true).setValue(WinterCorePillarBlock.CORNER, cornerIndex).setValue(WinterCorePillarBlock.IS_TOP, false), 3);
                   level.setBlock(p2, level.getBlockState(p2).setValue(WinterCorePillarBlock.FORMED, true).setValue(WinterCorePillarBlock.CORNER, cornerIndex).setValue(WinterCorePillarBlock.IS_TOP, true), 3);
               }
               level.setBlock(worldPosition, getBlockState().setValue(WinterCoreBlock.FORMED, true), 3);

               // Grant Advancement "Fimbulwinter" to the nearest player
               net.minecraft.world.entity.player.Player nearest = level.getNearestPlayer(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 20.0, false);
               if (nearest instanceof net.minecraft.server.level.ServerPlayer serverPlayer && level.getServer() != null) {
                   net.minecraft.advancements.Advancement advancement = level.getServer().getAdvancements().getAdvancement(new ResourceLocation("wintercore:fimbulwinter"));
                   if (advancement != null) {
                       net.minecraft.advancements.AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(advancement);
                       for (String criterion : progress.getRemainingCriteria()) {
                           serverPlayer.getAdvancements().award(advancement, criterion);
                       }
                   }
               }

            } else {
               unregisterCore();
               level.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.BEACON_DEACTIVATE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 0.8f);
               // 结构解散时重置净化波，下次重新激活时从核心重新扩散
               this.waveRadius = 0f;
               this.scanX = 0; this.scanZ = 0; this.scanY = Integer.MIN_VALUE;
               this.revertMultiblock();
               level.setBlock(worldPosition, getBlockState().setValue(WinterCoreBlock.FORMED, false), 3);
            }
            this.setChanged();
        }
        return formed;
    }

    public void revertMultiblock() {
        if (level == null) return;
        for (int[] p : cornerCoords) {
            BlockPos p1 = worldPosition.offset(p[0], -1, p[1]);
            BlockPos p2 = worldPosition.offset(p[0], 0, p[1]);
            
            if (level.getBlockState(p1).getBlock() == WinterCoreBlocks.WINTER_CORE_PILLAR.get()) {
                level.setBlock(p1, level.getBlockState(p1).setValue(WinterCorePillarBlock.FORMED, false), 3);
            }
            if (level.getBlockState(p2).getBlock() == WinterCoreBlocks.WINTER_CORE_PILLAR.get()) {
                level.setBlock(p2, level.getBlockState(p2).setValue(WinterCorePillarBlock.FORMED, false), 3);
            }
        }
    }

    private void reloadReplacementMap() {
        dynamicReplaceMap = new HashMap<>();
        List<? extends String> cfg = WinterCoreConfig.COMMON.blockConversions.get();
        for (String pair : cfg) {
            String[] split = pair.split("\\|");
            if (split.length == 2) {
                dynamicReplaceMap.put(new ResourceLocation(split[0]), new ResourceLocation(split[1]));
            }
        }
        lastConfigRead = System.currentTimeMillis();
    }

    private void processBlockConversion() {
        if (dynamicReplaceMap == null || System.currentTimeMillis() - lastConfigRead > 5000) {
            reloadReplacementMap();
        }

        int radius = WinterCoreConfig.COMMON.effectRadius.get();
        int rSq = radius * radius;
        int worldMinY = level.getMinBuildHeight();
        int worldMaxY = level.getMaxBuildHeight() - 1;

        // 每秒推进净化波前沿（从核心向外缓慢扩散）
        if (waveRadius < radius) {
            boolean wasNotFull = waveRadius < radius;
            waveRadius = Math.min(radius, waveRadius + WAVE_GROWTH_PER_SECOND);
            this.setChanged();
            // 首次铺满：触发净化完成特效
            if (wasNotFull && waveRadius >= radius && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                onPurificationComplete(serverLevel);
            }
        }
        int activeRadius = (int) Math.ceil(waveRadius);
        int activeRSq = activeRadius * activeRadius;

        // 安全钳：坐标必须在当前 activeRadius 与世界高度范围内
        if (scanZ < -activeRadius || scanZ > activeRadius) { scanZ = -activeRadius; }
        if (scanY < worldMinY || scanY > worldMaxY) { scanY = worldMinY; }

        // 根据当前 scanZ 预算出有效 X 半跨度，并约束 scanX
        int xHalf = (int) Math.sqrt(activeRSq - (long) scanZ * scanZ);
        if (scanX < -xHalf || scanX > xHalf) { scanX = -xHalf; }

        int checks = 0;
        int maxChecksPerTick = 20000; // 每 tick 最多处理有效方块数（无空转浪费）

        while (checks < maxChecksPerTick) {
            // scanX 已保证在 [-xHalf, xHalf] 内，即必然在圆柱体内，无需再判断 XZ 距离
            BlockPos targetPos = worldPosition.offset(scanX, scanY, scanZ);
            if (level.isLoaded(targetPos)) {
                BlockState state = level.getBlockState(targetPos);

                // 1. Spore's removable foliage (e.g., infected grass, eyes)
                if (state.is(net.minecraft.tags.TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation("spore", "removable_foliage")))) {
                    level.removeBlock(targetPos, false);
                } else {
                    boolean converted = false;

                    // 2. CDU Native Conversion Data (The JSON data inside Spore itself)
                    Block targetBlock = com.Harbinger.Spore.ExtremelySusThings.CustomJsonReader.SporeCduConversionData.getResult(state.getBlock());
                    if (targetBlock != null) {
                        BlockState newState = targetBlock.defaultBlockState();
                        for (Map.Entry<net.minecraft.world.level.block.state.properties.Property<?>, Comparable<?>> entry : state.getValues().entrySet()) {
                            net.minecraft.world.level.block.state.properties.Property<?> property = newState.getBlock().getStateDefinition().getProperty(entry.getKey().getName());
                            if (property != null) {
                                try {
                                    newState = newState.setValue((net.minecraft.world.level.block.state.properties.Property) property, (Comparable) entry.getValue());
                                } catch (Exception ignored) {}
                            }
                        }
                        level.setBlockAndUpdate(targetPos, newState);
                        converted = true;
                    }

                    // 3. Spore Hardcoded Custom Blocks (Bile -> Crusted Bile, Membrane -> Burned Biomass, etc)
                    if (!converted) {
                        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
                        if (id != null && id.getNamespace().equals("spore")) {
                            String path = id.getPath();
                            if (path.equals("remains")) {
                                level.setBlockAndUpdate(targetPos, ForgeRegistries.BLOCKS.getValue(new ResourceLocation("spore", "frozen_remains")).defaultBlockState());
                                converted = true;
                            } else if (path.equals("bile")) {
                                level.setBlockAndUpdate(targetPos, ForgeRegistries.BLOCKS.getValue(new ResourceLocation("spore", "crusted_bile")).defaultBlockState());
                                converted = true;
                            } else if (path.equals("membrane_block") || path.contains("biomass")) {
                                level.setBlockAndUpdate(targetPos, ForgeRegistries.BLOCKS.getValue(new ResourceLocation("spore", "frost_burned_biomass")).defaultBlockState());
                                converted = true;
                            }
                        }
                    }

                    // 4. Our own dynamic Replace Map from Config (as a final catch-all)
                    if (!converted) {
                        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
                        if (blockId != null && dynamicReplaceMap.containsKey(blockId)) {
                            Block finalTarget = ForgeRegistries.BLOCKS.getValue(dynamicReplaceMap.get(blockId));
                            if (finalTarget != net.minecraft.world.level.block.Blocks.AIR) {
                                level.setBlockAndUpdate(targetPos, finalTarget.defaultBlockState());
                                converted = true;
                            }
                        }
                    }

                    // 5. Native Snow Layering (Ambient Blizzards and Immediate Purge Frosting)
                    if (WinterCoreConfig.COMMON.renderSnow.get()) {
                        BlockState above = level.getBlockState(targetPos.above());
                        boolean canSeeSky = level.canSeeSkyFromBelowWater(targetPos.above());

                        // Immediately frost over purged blocks, or slowly coat naked vanilla plain blocks outdoors
                        if (above.isAir() && state.isSolidRender(level, targetPos)) {
                            if (converted) {
                                level.setBlockAndUpdate(targetPos.above(), net.minecraft.world.level.block.Blocks.SNOW.defaultBlockState().setValue(net.minecraft.world.level.block.SnowLayerBlock.LAYERS, level.random.nextInt(1, 3)));
                            } else if (canSeeSky && level.random.nextInt(8) == 0) {
                                level.setBlockAndUpdate(targetPos.above(), net.minecraft.world.level.block.Blocks.SNOW.defaultBlockState().setValue(net.minecraft.world.level.block.SnowLayerBlock.LAYERS, 1));
                            }
                        }
                        // Dynamically accumulate deeper snowpiles over time (ONLY OUTDOORS)
                        else if (above.getBlock() == net.minecraft.world.level.block.Blocks.SNOW && canSeeSky) {
                            if (level.random.nextInt(40) == 0) {
                                int layers = above.getValue(net.minecraft.world.level.block.SnowLayerBlock.LAYERS);
                                if (layers < 4) {
                                    level.setBlockAndUpdate(targetPos.above(), above.setValue(net.minecraft.world.level.block.SnowLayerBlock.LAYERS, layers + 1));
                                }
                            }
                        }
                        // Deep freeze water into ice (ONLY OUTDOORS)
                        else if (above.getBlock() == net.minecraft.world.level.block.Blocks.WATER && canSeeSky && level.random.nextInt(10) == 0) {
                            level.setBlockAndUpdate(targetPos.above(), net.minecraft.world.level.block.Blocks.ICE.defaultBlockState());
                        }
                    }
                }
            }

            checks++;

            // 推进 scanX；当前 Z 列扫完后移至下一个有效 Z，并重新计算 xHalf
            scanX++;
            if (scanX > xHalf) {
                scanZ++;
                if (scanZ > activeRadius) {
                    scanZ = -activeRadius;
                    scanY++;
                    if (scanY > worldMaxY) {
                        scanY = worldMinY;
                    }
                }
                xHalf = (int) Math.sqrt(Math.max(0, activeRSq - (long) scanZ * scanZ));
                scanX = -xHalf;
            }
        }
    }

    private void processEntityDamage() {
        if (level == null) return;
        
        int radius = WinterCoreConfig.COMMON.effectRadius.get();
        double multiplier = WinterCoreConfig.COMMON.damageMultiplier.get();
        AABB aabb = new AABB(worldPosition).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, aabb);
        
        for (LivingEntity entity : entities) {
            double dx = entity.getX() - worldPosition.getX();
            double dz = entity.getZ() - worldPosition.getZ();
            if ((dx * dx + dz * dz) <= (radius * radius)) {
                
                // Add Weakness and Slowness to Hostile Monsters
                if (entity instanceof net.minecraft.world.entity.monster.Monster) {
                    entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 100, 1, false, true));
                    entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS, 100, 1, false, true));
                }
                
                String entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString();
                if (entityId.contains("spore:") || entityId.contains("flesh_that_hates")) {
                    float baseDamage = 3.0F * (float)multiplier;
                    entity.hurt(level.damageSources().magic(), baseDamage);
                    
                    if (entity.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) {
                        entity.setTicksFrozen(entity.getTicksFrozen() + 600);
                        entity.hurt(level.damageSources().freeze(), 10.0f * (float)multiplier); 
                    }
                    
                    // Visual frost on entity
                    if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE,
                                entity.getX(), entity.getY() + 1.0, entity.getZ(),
                                5, 0.2, 0.5, 0.2, 0.0);
                    }
                    
                    // Specific hardcoded entity deletion if they are small/particles like Scent or Tendril
                    String name = entity.getClass().getSimpleName();
                    if (name.equals("ScentEntity") || name.equals("InfectionTendril")) {
                        entity.discard();
                    }
                }
            }
        }
    }

    /**
     * 净化波首次铺满最大半径时触发：音效 + 双层粒子爆发环，给予玩家明确的"净化完成"反馈。
     */
    private void onPurificationComplete(net.minecraft.server.level.ServerLevel serverLevel) {
        // 共鸣音效：低沉的信标充能音，象征冰霜领域完全扩张
        serverLevel.playSound(null, worldPosition,
                net.minecraft.sounds.SoundEvents.BEACON_POWER_SELECT,
                net.minecraft.sounds.SoundSource.BLOCKS, 2.5f, 0.55f);
        // 略微延后的第二声（在上一声的回声感）
        serverLevel.playSound(null, worldPosition,
                net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_RESONATE,
                net.minecraft.sounds.SoundSource.BLOCKS, 1.5f, 0.4f);

        double cx = worldPosition.getX() + 0.5;
        double cy = worldPosition.getY() + 0.5;
        double cz = worldPosition.getZ() + 0.5;
        int maxR = WinterCoreConfig.COMMON.effectRadius.get();

        // 内圈爆发：核心周围螺旋上升的粒子柱
        for (int i = 0; i < 60; i++) {
            double angle = (2 * Math.PI * i) / 60;
            double r = 1.0 + serverLevel.random.nextDouble() * 2.5;
            double yOffset = serverLevel.random.nextDouble() * 3.0;
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                    cx + Math.cos(angle) * r, cy + yOffset, cz + Math.sin(angle) * r,
                    1, 0.0, 0.06, 0.0, 0.0);
        }

        // 外圈边界环：在最大半径处勾勒出完整的领域边界（仅地面高度）
        int boundaryCount = Math.min(360, maxR * 4); // 半径越大，粒子越多
        for (int i = 0; i < boundaryCount; i++) {
            double angle = (2 * Math.PI * i) / boundaryCount;
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                    cx + Math.cos(angle) * maxR, cy, cz + Math.sin(angle) * maxR,
                    1, 0.0, 0.05, 0.0, 0.0);
        }
    }
}

