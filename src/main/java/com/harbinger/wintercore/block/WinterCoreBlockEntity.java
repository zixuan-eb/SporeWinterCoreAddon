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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WinterCoreBlockEntity extends BlockEntity {

    // Global registry of all active Winter Cores to quickly lookup blockspawn rules anywhere in the world.
    public static final Map<ResourceKey<Level>, Set<BlockPos>> ACTIVE_CORES = new HashMap<>();
    
    public static final int EFFECT_RADIUS = 96; // 6 chunks (6 * 16 blocks radius)

    private int currentRadius = 0;
    private int currentPerimeterIndex = 0;
    private int currentY = 0; 
    
    public boolean isFormed = false;
    
    public WinterCoreBlockEntity(BlockPos pos, BlockState state) {
        super(WinterCoreBlocks.WINTER_CORE_BE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        // Delay adding to active cores until multiblock is verified on tick
    }

    @Override
    protected void saveAdditional(net.minecraft.nbt.CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("IsFormed", this.isFormed);
    }

    @Override
    public void load(net.minecraft.nbt.CompoundTag tag) {
        super.load(tag);
        this.isFormed = tag.getBoolean("IsFormed");
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

    public static void serverTick(Level level, BlockPos pos, BlockState state, WinterCoreBlockEntity e) {
        // Only run structure check every 20 ticks to save performance
        if (level.getGameTime() % 20 == 0) {
             e.checkMultiblock();
        }

        if (!e.isFormed) return;

        // Run conversion sweep efficiently across chunks over multiple ticks
        e.processBlockConversionBatch();
        
        // Every 4 seconds, pulse massive frost damage to infections
        if (level.getGameTime() % 80 == 0) {
            e.processEntityDamage();
        }
    }

    private void checkMultiblock() {
        if (level == null) return;
        boolean formed = true;

        // Check 3x3 base directly underneath (-1 Y)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (level.getBlockState(worldPosition.offset(dx, -1, dz)).getBlock() != WinterCoreBlocks.WINTER_CORE_BASE.get()) {
                    formed = false;
                    break;
                }
            }
        }

        // Check 4 pillars (corners at Y and Y+1)
        if (formed) {
            int[][] corners = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
            for (int[] corner : corners) {
                if (level.getBlockState(worldPosition.offset(corner[0], 0, corner[1])).getBlock() != WinterCoreBlocks.WINTER_CORE_PILLAR.get()) formed = false;
                if (level.getBlockState(worldPosition.offset(corner[0], 1, corner[1])).getBlock() != WinterCoreBlocks.WINTER_CORE_PILLAR.get()) formed = false;
            }
        }
        
        if (this.isFormed != formed) {
            this.isFormed = formed;
            if (formed) {
               registerCore();
               level.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 0.8f);
               
               // Transform pillars into bent spikes
               int cornerIndex = 0;
               for (int[] corner : corners) {
                   BlockPos p1 = worldPosition.offset(corner[0], 0, corner[1]);
                   BlockPos p2 = worldPosition.offset(corner[0], 1, corner[1]);
                   level.setBlock(p1, level.getBlockState(p1).setValue(WinterCorePillarBlock.FORMED, true).setValue(WinterCorePillarBlock.CORNER, cornerIndex).setValue(WinterCorePillarBlock.IS_TOP, false), 3);
                   level.setBlock(p2, level.getBlockState(p2).setValue(WinterCorePillarBlock.FORMED, true).setValue(WinterCorePillarBlock.CORNER, cornerIndex).setValue(WinterCorePillarBlock.IS_TOP, true), 3);
                   cornerIndex++;
               }
               // Transform core
               level.setBlock(worldPosition, getBlockState().setValue(WinterCoreBlock.FORMED, true), 3);

            } else {
               unregisterCore();
               level.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.BEACON_DEACTIVATE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 0.8f);
               
               // Revert pillars
               for (int[] corner : corners) {
                   BlockPos p1 = worldPosition.offset(corner[0], 0, corner[1]);
                   if (level.getBlockState(p1).getBlock() == WinterCoreBlocks.WINTER_CORE_PILLAR.get()) {
                       level.setBlock(p1, level.getBlockState(p1).setValue(WinterCorePillarBlock.FORMED, false), 3);
                   }
                   BlockPos p2 = worldPosition.offset(corner[0], 1, corner[1]);
                   if (level.getBlockState(p2).getBlock() == WinterCoreBlocks.WINTER_CORE_PILLAR.get()) {
                       level.setBlock(p2, level.getBlockState(p2).setValue(WinterCorePillarBlock.FORMED, false), 3);
                   }
               }
               level.setBlock(worldPosition, getBlockState().setValue(WinterCoreBlock.FORMED, false), 3);
            }
            this.setChanged();
        }
    }

    private int[] getPoint(int r, int index) {
        if (r == 0) return new int[]{0, 0};
        int sideLength = 2 * r;
        if (index < sideLength) return new int[]{-r + index, -r}; 
        index -= sideLength;
        if (index < sideLength) return new int[]{r, -r + index}; 
        index -= sideLength;
        if (index < sideLength) return new int[]{r - index, r}; 
        index -= sideLength;
        return new int[]{-r, r - index}; 
    }

    /**
     * Outward-expanding shockwave algorithm.
     * Starts converting directly from the center radially outwards.
     */
    private void processBlockConversionBatch() {
        if (level == null || level.isClientSide()) return;
        
        int minHeight = level.getMinBuildHeight();
        int maxHeight = level.getMaxBuildHeight();
        int heightRange = maxHeight - minHeight;

        int stateChecks = 0;
        int blocksChanged = 0;

        while (stateChecks < 6000 && blocksChanged < 20) {
            if (currentRadius > EFFECT_RADIUS) {
                // Done expanding out to the edge. Restart from the center.
                currentRadius = 0;
                currentPerimeterIndex = 0;
                currentY = 0;
                break;
            }

            int[] pt = getPoint(currentRadius, currentPerimeterIndex);
            int dx = pt[0];
            int dz = pt[1];

            // Ensure we strictly operate within a circle
            if ((dx * dx + dz * dz) <= (EFFECT_RADIUS * EFFECT_RADIUS)) {
                int x = worldPosition.getX() + dx;
                int z = worldPosition.getZ() + dz;
                int y = minHeight + currentY;
                
                BlockPos targetPos = new BlockPos(x, y, z);
                
                if (level.isLoaded(targetPos)) {
                    BlockState targetState = level.getBlockState(targetPos);
                    
                    ResourceLocation registryName = ForgeRegistries.BLOCKS.getKey(targetState.getBlock());
                    if (registryName != null && WinterCoreData.CONVERSION_MAP.containsKey(registryName)) {
                        ResourceLocation toBlockLoc = WinterCoreData.CONVERSION_MAP.get(registryName);
                        Block applyBlock = ForgeRegistries.BLOCKS.getValue(toBlockLoc);
                        if (applyBlock != null) {
                            level.setBlock(targetPos, applyBlock.defaultBlockState(), 3);
                            blocksChanged++;
                        }
                    }
                }
            }

            stateChecks++;
            
            // Advance iterator: Trace Y height entirely before moving to the next spiral coordinate
            currentY++;
            if (currentY >= heightRange) {
                currentY = 0;
                currentPerimeterIndex++;
                int perimeterSize = (currentRadius == 0) ? 1 : (8 * currentRadius);
                if (currentPerimeterIndex >= perimeterSize) {
                    currentPerimeterIndex = 0;
                    currentRadius++;
                }
            }
        }
    }

    private void processEntityDamage() {
        if (level == null) return;
        
        // Massive AABB encompassing 96 block radius to kill entities
        AABB aabb = new AABB(worldPosition).inflate(EFFECT_RADIUS, level.getMaxBuildHeight() - level.getMinBuildHeight(), EFFECT_RADIUS);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, aabb);
        
        for (LivingEntity entity : entities) {
            ResourceLocation entityLoc = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
            if (entityLoc != null && entityLoc.getNamespace().equals("spore")) {
                
                // Directly check distancing
                double dx = entity.getX() - worldPosition.getX();
                double dz = entity.getZ() - worldPosition.getZ();
                if ((dx * dx + dz * dz) <= (EFFECT_RADIUS * EFFECT_RADIUS)) {
                    
                    // Apply heavy debuffs: Slowness IV (amp 3) and Weakness III (amp 2) for 5 seconds (100 ticks)
                    // (The pulse hits every 4 seconds, so the debuff will be permanent as long as they stay inside)
                    entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 100, 3, false, true));
                    entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS, 100, 2, false, true));

                    if (entity.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) {
                        // Max out the freeze bar visually and physically
                        entity.setTicksFrozen(entity.getTicksFrozen() + 600);
                        // Massive damage to freeze-vulnerable targets (40 damage = 20 hearts per 4 seconds)
                        entity.hurt(level.damageSources().freeze(), 40.0f); 
                    } else {
                        // Very high generic magic damage (20 damage = 10 hearts per 4 seconds)
                        entity.hurt(level.damageSources().magic(), 20.0f);
                    }
                    
                    // Specific hardcoded entity deletion if they are small/particles like Scent or Tendril
                    // Using simple class names as a duck-typing fallback to avoid deep dependency wiring here
                    String name = entity.getClass().getSimpleName();
                    if (name.equals("ScentEntity") || name.equals("InfectionTendril")) {
                        entity.discard();
                    }
                }
            }
        }
    }
}
