// 
// Decompiled by Procyon v0.5.36
// 
package com.xbuilders.engine.world;

import com.xbuilders.engine.VoxelGame;
import com.xbuilders.engine.items.BlockList;
import com.xbuilders.engine.items.ItemList;
import com.xbuilders.engine.items.block.Block;
import com.xbuilders.engine.items.entity.EntityLink;
import com.xbuilders.engine.utils.math.HashingUtils;
import com.xbuilders.engine.utils.math.MathUtils;
import com.xbuilders.engine.utils.random.FastNoise;
import com.xbuilders.engine.world.chunk.Chunk;
import com.xbuilders.engine.world.chunk.FutureChunk;
import com.xbuilders.engine.world.chunk.SubChunk;
import com.xbuilders.engine.world.blockData.BlockData;
import com.xbuilders.engine.world.wcc.WCCi;
import com.xbuilders.game.PointerHandler;
import com.xbuilders.game.terrain.Biome;

import java.util.Random;

public abstract class Terrain {

    /**
     * @return the enableGradientSky
     */
    public boolean isEnableGradientSky() {
        return enableGradientSky;
    }

    /**
     * @param enableGradientSky the enableGradientSky to set
     */
    public void setEnableGradientSky(boolean enableGradientSky) {
        this.enableGradientSky = enableGradientSky;
    }

    /**
     * @return the enableClouds
     */
    public boolean isEnableClouds() {
        return enableClouds;
    }

    /**
     * @param enableClouds the enableClouds to set
     */
    public void setEnableClouds(boolean enableClouds) {
        this.enableClouds = enableClouds;
    }

    private boolean enableGradientSky, enableClouds;
    private final Random random;
    public final FastNoise noise = new FastNoise();//TODO: Perlin noise is significantly slower than fast noise
    private PointerHandler pointerHandler;
    public float frequency;

    public PointerHandler getPointerHandler() {
        return this.pointerHandler;
    }


    public float getFrequency() {
        return this.frequency;
    }

    public Random getRandom() {
        return this.random;
    }

    public float getValueFractal(float x, float y, float z) {
        return (float) this.noise.GetValueFractal(x * this.getFrequency(), y, z * this.getFrequency());
    }

    public float getValueFractal(final float x, final float z) {
        return (float) this.noise.GetValueFractal(x * this.getFrequency(), z * this.getFrequency());
    }

    public Terrain() {
        enableGradientSky = true;
        enableClouds = true;
        this.frequency = 3.0f;
        this.pointerHandler = VoxelGame.ph();
        this.random = new Random();
        noise.SetFractalOctaves(1);
    }

    public abstract Biome getBiomeOfVoxel(final int p0, final int p1, final int p2);

    public void setTerrainProperties(final int seed, final float frequency) {
        this.frequency = frequency;
        this.noise.SetSeed(seed);
    }

    public final void createTerrainOnChunk(final Chunk chunk) {
        final int randHash = HashingUtils.PerfectlyHashThem((short) chunk.getPosition().x, (short) chunk.getPosition().z);
        this.getRandom().setSeed(randHash);
        this.generateChunkInner(chunk);
    }

    protected abstract void generateChunkInner(final Chunk p0);

    final WCCi targetChunk = new WCCi();

    public final void setBlockWorld(final Block block, final int x, final int y, final int z) {
        targetChunk.set(x, y, z);
        if (targetChunk.getSubChunk() != null) {
            targetChunk.getSubChunk().data.setBlock(targetChunk.subChunkVoxel.x, targetChunk.subChunkVoxel.y, targetChunk.subChunkVoxel.z, block.id);
        } else {//Add it to the future chunk
//            System.out.println("Adding block to future chunk: " + MiscUtils.printVector(targetChunk.subChunk));
            if (!VoxelGame.getWorld().futureSubChunks.containsKey(targetChunk.subChunk)) {
                VoxelGame.getWorld().futureSubChunks.put(targetChunk.subChunk, new FutureChunk(targetChunk.subChunk));
            }
            VoxelGame.getWorld().futureSubChunks.get(targetChunk.subChunk).addBlock(block.id,
                    targetChunk.subChunkVoxel.x, targetChunk.subChunkVoxel.y, targetChunk.subChunkVoxel.z);
        }
    }

    public final Block getBlock(final Chunk chunk, final int x, final int y, final int z) {
        try {
            final int chunkLocation = WCCi.chunkDiv(y);
            final int blockLocation = MathUtils.positiveMod(y, SubChunk.WIDTH);
            SubChunk subChunk = chunk.getSubChunks()[chunkLocation];
            final Block block = ItemList.getBlock(subChunk.data.getBlock(x, blockLocation, z));
            return block == null ? BlockList.BLOCK_AIR : block;
        } catch (IndexOutOfBoundsException | NullPointerException ex) {
            return BlockList.BLOCK_AIR;
        }
    }

    public final BlockData getBlockData(final Chunk chunk, final int x, final int y, final int z) {
        final int chunkLocation = WCCi.chunkDiv(y);
        final int blockLocation = MathUtils.positiveMod(y, SubChunk.WIDTH);
        SubChunk subChunk = chunk.getSubChunks()[chunkLocation];
        return subChunk.data.getBlockData(x, blockLocation, z);
    }

    public final void setBlock(final Chunk chunk, final Block block, final BlockData data, final int x, final int y, final int z) {
        final int chunkLocation = WCCi.chunkDiv(y);
        final int blockLocation = MathUtils.positiveMod(y, SubChunk.WIDTH);
        SubChunk subChunk1 = chunk.getSubChunks()[chunkLocation];
        subChunk1.data.setBlock(x, blockLocation, z, block.id);
        SubChunk subChunk = chunk.getSubChunks()[chunkLocation];
        subChunk.data.setBlockData(x, blockLocation, z, data);
    }

    public final void setBlock(final Chunk chunk, final Block block, final int x, final int y, final int z) {
        final int chunkLocation = WCCi.chunkDiv(y);
        final int blockLocation = MathUtils.positiveMod(y, SubChunk.WIDTH);
        SubChunk subChunk = chunk.getSubChunks()[chunkLocation];
        subChunk.data.setBlock(x, blockLocation, z, block.id);
    }

    public final void setEntity(final Chunk chunk, final EntityLink el, final int chunkX, final int worldY, final int chunkZ) {
        int worldX = chunkX + (chunk.getPosition().x * SubChunk.WIDTH);
        int worldZ = chunkZ + (chunk.getPosition().z * SubChunk.WIDTH);
        el.placeNew(worldX, worldY, worldZ, false);
    }

    public void worldBackground() {
        VoxelGame.getShaderHandler().setNaturalBackgroundEnabled(true);
        VoxelGame.getShaderHandler().setFogDistance(1);
    }

    public abstract int getHeightmapOfVoxel(final int p0, final int p1);
}
