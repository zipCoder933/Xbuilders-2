// 
// Decompiled by Procyon v0.5.36
// 
package com.xbuilders.engine.world;

import com.xbuilders.engine.VoxelGame;
import com.xbuilders.engine.items.entity.ChunkEntitySet;
import com.xbuilders.engine.player.blockPipeline.BlockPipeline;
import com.xbuilders.engine.items.BlockList;
import com.xbuilders.engine.items.ItemList;
import com.xbuilders.engine.items.block.Block;
import com.xbuilders.engine.items.block.BlockAction;
import com.xbuilders.engine.light.sunlight.InitialSunlightUtils;
import com.xbuilders.engine.player.UserControlledPlayer;
import com.xbuilders.engine.rendering.worldLightMap.ShaderLightMap;
import com.xbuilders.engine.utils.ErrorHandler;
import com.xbuilders.engine.utils.progress.ProgressData;
import com.xbuilders.engine.world.chunk.Chunk;
import com.xbuilders.engine.world.chunk.ChunkCoords;
import com.xbuilders.engine.world.chunk.FutureChunk;
import com.xbuilders.engine.world.chunk.SubChunk;
import com.xbuilders.engine.world.blockData.BlockData;
import com.xbuilders.engine.world.wcc.WCCi;
import com.xbuilders.engine.world.holograms.HologramSet;
import com.xbuilders.engine.world.info.WorldInfo;
import com.xbuilders.game.PointerHandler;
import com.xbuilders.game.terrain.TerrainsList;
import org.joml.Vector3f;
import org.joml.Vector3i;
import processing.core.PGraphics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//MASSIVE PERFORMANCE ISSUE:
//Is the garbage collector working overtime?

public class World {

    public boolean hasChunk(final Vector3i coords) {
        return this.subChunks.containsKey(coords);
    }

    public boolean hasChunk(final ChunkCoords coords) {
        return this.chunks.containsKey(coords);
    }

    public Chunk getChunk(final ChunkCoords coords) {
        return this.chunks.get(coords);
    }

    public SubChunk getSubChunk(final Vector3i coords) {
        return this.subChunks.get(coords);
    }

    public static File chunkFile(final WorldInfo infoFile, final ChunkCoords coords) throws IOException {
        return chunkFile(infoFile.getDirectory(), coords);
    }

    public static File chunkFile(File directory, ChunkCoords coords) throws IOException {
        return new File(directory, "\\" + coords.x + "_" + coords.z + ".chunk");
    }

    public HologramSet hologramList;
    public TerrainUpdater updater;
    private boolean open;
    public Terrain terrain;


    public World() {
        unusedChunks = new ArrayList<>();
        this.chunks = new ConcurrentHashMap<>();
        this.subChunks = new ConcurrentHashMap<>();
        this.hologramList = new HologramSet();
    }

    public void closeWorld() throws IOException, InterruptedException {
        this.open = false;
//        saveChangedChunks();
        this.ph.getMainThread().end();
        if (infoFile != null) {
            ItemList.worldClose();
            System.out.println("Closing world. Waiting for updater to complete...");
            this.updater.end();
            this.chunks.clear();
            this.subChunks.clear();
            this.futureSubChunks.clear();
        }
    }


    public final ConcurrentHashMap<ChunkCoords, Chunk> chunks;
    public final ConcurrentHashMap<Vector3i, SubChunk> subChunks;
    public final HashMap<Vector3i, FutureChunk> futureSubChunks = new HashMap<>();

    private final ArrayList<Chunk> unusedChunks;
    private PointerHandler ph;
    public WorldInfo infoFile;

    public void setPinpoint() {
        File pinpointFile = new File(this.infoFile.getDirectory(), "pinpoint.txt");
        VoxelGame.getMessageBox().show("Are you sure?", "Are you sure you want to set this as your Pinpoint?", () -> {
            try {
                Files.writeString(pinpointFile.toPath(),
                        ph.getPlayer().worldPos.x + ","
                                + (ph.getPlayer().worldPos.y - 0.2f) + ","
                                + ph.getPlayer().worldPos.z);
                VoxelGame.getMessageBox().show("Success", "Pinpoint set");
            } catch (IOException e) {
                VoxelGame.getMessageBox().show("Error", "Error saving Pinpoint");
            }
        });
    }

    public void loadPinpoint() {
        File pinpointFile = new File(this.infoFile.getDirectory(), "pinpoint.txt");
        if (pinpointFile.exists()) {
            try {
                String[] pinpoint = Files.readString(pinpointFile.toPath()).split(",");
                ph.getPlayer().worldPos.x = Float.parseFloat(pinpoint[0]);
                ph.getPlayer().worldPos.y = Float.parseFloat(pinpoint[1]);
                ph.getPlayer().worldPos.z = Float.parseFloat(pinpoint[2]);
            } catch (IOException e) {
                VoxelGame.getMessageBox().show("Error", "Error loading Pinpoint");
            }
        }
    }

    public boolean isOpen() {
        return this.open;
    }


    public void setPointerHandler(final PointerHandler ph) {
        this.ph = ph;
        this.updater = new TerrainUpdater(ph);
        this.infoFile = null;
    }


    public synchronized final boolean saveChangedChunks() {
        try {
            // System.out.print("Saving chunks: ");
            for (final Chunk chunk : this.chunks.values()) {// It could be the concurrent hashmap
                if (chunk != null && chunk.isModifiedByUser() && chunk.needsSaving()) {
                    // System.out.print(chunk.getPosition() + " ("+chunk.isModifiedByUser()+") ");
                    chunk.save(chunkFile(this.infoFile.getDirectory(), chunk.getPosition()));
                }
            }
            // System.out.println("\n");
            return true;
        } catch (IOException ex) {
            ErrorHandler.report("Error", "IOException saving changed chunks", ex);
            return false;
        } catch (Exception ex2) {
            ErrorHandler.report("Error", "Exception saving changed chunks", ex2);
            return false;
        }
    }

    public final void saveWorldInfo() {
        UserControlledPlayer userControlledPlayer = this.ph.getPlayer();
        infoFile.save(userControlledPlayer.worldPos);
    }

    public Chunk makeChunk(final ChunkCoords coords) throws IOException {
        Chunk existingChunk = this.getChunk(coords);
        if (existingChunk == null) {
            Chunk chunk;
            if (this.unusedChunks.isEmpty()) {
                chunk = new Chunk(this.ph);
            } else {
                chunk = this.unusedChunks.get(0);
                this.unusedChunks.remove(0);
            }
            this.chunks.put(chunk.init(coords), chunk);
            for (final SubChunk sc : chunk.getSubChunks()) {
                this.subChunks.put(sc.position, sc);
            }

            chunk.load(infoFile, terrain,futureSubChunks);

            return chunk;
        }
        return existingChunk;
    }

    public void removeChunk(final ChunkCoords coords) {
        try {
            if (this.chunks.containsKey(coords)) {
                final Chunk chunk = chunks.get(coords);
                this.unusedChunks.add(chunk);
                BlockAction.interruptActionOnChunk(coords);
                if (this.infoFile != null && chunk.isModifiedByUser()) {
                    chunk.save(chunkFile(this.infoFile, coords));
                }
                for (int i = 0; i < chunk.getSubChunks().length; ++i) {
                    final SubChunk sc = chunk.getSubChunks()[i];
                    this.subChunks.remove(sc.position);
                }
                this.chunks.remove(coords);
            }
        } catch (IOException ex) {
            ErrorHandler.report("Error", "IOException saving chunk before removal", ex);
        } catch (Exception ex2) {
            ErrorHandler.report("Error", "Exception saving chunk before removal", ex2);
        }
    }

    private void initializeGeneration(final ProgressData prog) throws IOException {
        prog.setTask("Initializing...");
        chunks.clear();
        this.terrain = TerrainsList.getTerrain(infoFile.getInfoFile().worldType);
    }

    public void setPlayerPosition() {
        int centerX = 0;
        int centerZ = 0;

        if (infoFile.getSpawnPoint() != null && this.inBounds(infoFile.getSpawnPoint())) {
            UserControlledPlayer userControlledPlayer = ph.getPlayer();
            userControlledPlayer.worldPos.set(infoFile.getSpawnPoint());
            return;
        } else {
            int ySpawn = 0;
            for (int xSpawn = centerX - 3; xSpawn < centerX + 4; xSpawn++) {
                for (int zSpawn = centerZ - 3; zSpawn < centerZ + 4; zSpawn++) {
                    for (ySpawn = 0; ySpawn < Chunk.HEIGHT; ySpawn++) { // Start from the top and go down
                        Block baseBlock = getBlock(xSpawn, ySpawn, zSpawn);
                        Block l1Block = getBlock(xSpawn, ySpawn - 1, zSpawn);
                        if (baseBlock.isSolid()) {
                            if (!baseBlock.name.toLowerCase().contains("leaves") && !l1Block.isSolid()) {
                                UserControlledPlayer userControlledPlayer = ph.getPlayer();
                                userControlledPlayer.worldPos.set(xSpawn, ySpawn - 2, zSpawn);
                                return;
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Nothing found!");
        UserControlledPlayer userControlledPlayer = ph.getPlayer();
        userControlledPlayer.worldPos.set(centerX, Chunk.HEIGHT / 2, centerZ);
    }

    private void finishWorldGeneration(final ProgressData prog, int centerX, int centerZ)
            throws InterruptedException, IOException {
        this.hologramList.newGame();
        int max = 0;
        int un_initializedMax = 0;
        for (final Map.Entry<ChunkCoords, Chunk> set : chunks.entrySet()) {
            ++max;
            if (!set.getValue().gen_lightmapGenerated) {
                ++un_initializedMax;
            }
        }
        InitialSunlightUtils.generateSunlightForWorldInitially(prog, this, un_initializedMax);
        prog.setTask("Generating Chunk Meshes");
        prog.getBar().set(0, max);

        chunks.values().forEach((chunk) -> {
            chunk.neighbors.cacheAllNeighbors();
            if(chunk.neighbors.allFacingNeighborsLoaded()) {
                chunk.generateInitialMeshes(); //Only generate if we are surrounded by loaded chunks
            }
            prog.getBar().changeValue(1);
        });


        this.setPlayerPosition();
        prog.setTask("Initializing Shader Lightmap");
        prog.setSpinMode(true);
        ShaderLightMap.initializeLightmap(this.ph, this.getClass());
        this.updater.begin(this.ph);
        this.open = true;
        ItemList.worldOpen();
        prog.finish();
        this.ph.getMainThread().start();
        System.gc();
    }

    private final void fatalError(final Throwable t, final ProgressData prog) {
        ErrorHandler.report("Fatal Error",
                "XBuilders was Unable to create the new world. The Process was aborted.", t);
        prog.abort();
    }

    public final void loadWorld(final WorldInfo info, final ProgressData prog, boolean isNewWorld) {

        this.chunks.clear();
        this.subChunks.clear();
        this.futureSubChunks.clear();

        this.infoFile = info;
        System.gc();
        new Thread() {
            @Override
            public void run() {
                try {
                    World.this.initializeGeneration(prog);
                    if (isNewWorld && infoFile.getSeed() <= 0) {
                        infoFile.setSeed((int) (Math.random() * 30000.0));
                    }
                    World.this.terrain.setTerrainProperties(
                            World.this.infoFile.getSeed(),
                            World.this.infoFile.getInfoFile().resolution);
                    prog.setTask("Loading Terrain");
                    prog.getBar().setProgress(0.0);

                    int centerX = 0;
                    int centerZ = 0;

                    if (infoFile.getSpawnPoint() != null) {
                        UserControlledPlayer userControlledPlayer = ph.getPlayer();
                        final Vector3f playerPos = userControlledPlayer.worldPos.set(infoFile.getSpawnPoint());
                        centerX = (int) playerPos.x;
                        centerZ = (int) playerPos.z;
                    }

                    final int xStart = WCCi.chunkDiv(centerX - initialWorldChunkRadius());
                    final int xEnd = WCCi.chunkDiv(centerX + initialWorldChunkRadius());
                    final int zStart = WCCi.chunkDiv(centerZ - initialWorldChunkRadius());
                    final int zEnd = WCCi.chunkDiv(centerZ + initialWorldChunkRadius());
                    final int max = (xEnd - xStart) * (zEnd - zStart) / 2;
                    prog.getBar().setProgress(0.0);
                    prog.getBar().setMax(max);
                    int i = 0;
                    for (int x = xStart; x < xEnd; ++x) {
                        for (int z = zStart; z < zEnd; ++z) {
                            final ChunkCoords coords = new ChunkCoords(x, z);
                            makeChunk(coords);
                            prog.getBar().setValue(i);
                            ++i;
                        }
                    }
                    World.this.finishWorldGeneration(prog, centerX, centerZ);
                } catch (Error | Exception error) {
                    World.this.fatalError(error, prog);
                }
            }

        }.start();
    }


    private int initialWorldChunkRadius() {
        return VoxelGame.getSettings().getSettingsFile().chunkRadius + (Chunk.WIDTH * 6);
    }

    public void draw(final PGraphics graphics, boolean drawEntities) throws Exception {
        this.infoFile.checkAndRemoveNoQuantityItems();
        VoxelGame.getShaderHandler().blockShader.bind(graphics);
        BlockPipeline.resolvePipeline();
        if (ShaderLightMap.hasChanged) {
            ShaderLightMap.sendLightmapToShader();
        }
        // frameTester.endProcess("Update A");

        this.chunks.values().forEach(chunk -> {
            chunk.drawUpdate();
        });

        // frameTester.startProcess();
        this.subChunks.values().forEach(chunk -> {// Draw opaque meshes of all chunks
            if (chunk.shouldDrawThis()) {
                chunk.drawOpaque();
            }
        });
        this.subChunks.values().forEach(chunk -> {// Draw transparent meshes of all chunks
            if (chunk.shouldDrawThis()) {
                chunk.drawTransparent();
            }
        });
        if(drawEntities){
            ChunkEntitySet.startDrawEntities();
            this.subChunks.values().forEach(chunk -> {// Draw entities
                if (chunk.shouldDrawThis()) {
                    chunk.entities.updateAndDrawEntities(chunk.inFrustum);
                }
            });
            ChunkEntitySet.endDrawEntities();
        }
        // frameTester.endProcess("Draw Chunks");

        // frameTester.startProcess();
        this.hologramList.renderHolograms(graphics);
        // frameTester.endProcess("Draw Holograms");
    }

    public boolean inBounds(final Vector3i vec) {
        return this.inBounds(vec.y);
    }

    public boolean inPlacableBounds(int y) {
        return y < Chunk.HEIGHT - 1 && y > 0;
    }

    public boolean inBounds(final int y) {
        return y >= 0 && y <= Chunk.HEIGHT - 1;
    }

    // <editor-fold defaultstate="collapsed" desc="getChunk block/block data">
    public Block getBlock(final Vector3i worldCoords) {
        return this.getBlock(worldCoords.x, worldCoords.y, worldCoords.z);
    }

    public Block getBlock(final int x, final int y, final int z) {
        return this.getBlock(x, y, z, false);
    }

    public Block getBlock(final int x, final int y, final int z, final boolean returnNullIfEmpty) {
        WCCi wcc = new WCCi().set(x, y, z);
        SubChunk chunk = getSubChunk(wcc.subChunk);
        if (chunk != null && chunk.getParentChunk().gen_terrainGenerated) {
            return ItemList.getBlock(
                    chunk.data.getBlock(wcc.subChunkVoxel.x, wcc.subChunkVoxel.y, wcc.subChunkVoxel.z));
        }
        return returnNullIfEmpty ? null : BlockList.BLOCK_AIR;
    }

    public BlockData getBlockData(final int x, final int y, final int z) {
        WCCi wcc = new WCCi().set(x, y, z);
        SubChunk chunk = getSubChunk(wcc.subChunk);
        if (chunk != null) {
            return chunk.data.getBlockData(wcc.subChunkVoxel.x, wcc.subChunkVoxel.y, wcc.subChunkVoxel.z);
        }
        return null;
    }

    public void setBlockData(BlockData data, final int x, final int y, final int z) {
        WCCi wcc = new WCCi().set(x, y, z);
        SubChunk chunk = getSubChunk(wcc.subChunk);
        if (chunk != null) {
            chunk.data.setBlockData(wcc.subChunkVoxel.x, wcc.subChunkVoxel.y, wcc.subChunkVoxel.z, data);
        }
    }

    public void setBlock(final Block block, final BlockData data, final int x, final int y, final int z) {
        WCCi wcc = new WCCi().set(x, y, z);
        SubChunk chunk = getSubChunk(wcc.subChunk);
        if (chunk != null) {
            chunk.data.setBlock(wcc.subChunkVoxel.x, wcc.subChunkVoxel.y, wcc.subChunkVoxel.z, block.id);
            chunk.data.setBlockData(wcc.subChunkVoxel.x, wcc.subChunkVoxel.y, wcc.subChunkVoxel.z, data);
        }
    }

    public void setBlock(final Block block, final int x, final int y, final int z) {
        WCCi wcc = new WCCi().set(x, y, z);
        SubChunk chunk = getSubChunk(wcc.subChunk);
        if (chunk != null) {
            chunk.data.setBlock(wcc.subChunkVoxel.x, wcc.subChunkVoxel.y, wcc.subChunkVoxel.z, block.id);
        }
    }

    /**
     * Set the block, mark as modified, and regenerate the mesh.
     */
    public void setBlockAndUpdate(final Block block, final BlockData data, final int x, final int y, final int z) {
        WCCi wcc = new WCCi().set(x, y, z);
        SubChunk chunk = getSubChunk(wcc.subChunk);
        if (chunk != null) {
            chunk.data.setBlock(wcc.subChunkVoxel.x, wcc.subChunkVoxel.y, wcc.subChunkVoxel.z, block.id);
            chunk.data.setBlockData(wcc.subChunkVoxel.x, wcc.subChunkVoxel.y, wcc.subChunkVoxel.z, data);
            chunk.getParentChunk().update(wcc.subChunkVoxel.x, wcc.subChunk.y, wcc.subChunkVoxel.y,
                    wcc.subChunkVoxel.z);
        }
    }

    /**
     * Set the block, mark as modified, and regenerate the mesh.
     */
    public void setBlockAndUpdate(final Block block, final int x, final int y, final int z) {
        WCCi wcc = new WCCi().set(x, y, z);
        SubChunk chunk = getSubChunk(wcc.subChunk);
        if (chunk != null) {
            chunk.data.setBlock(wcc.subChunkVoxel.x, wcc.subChunkVoxel.y, wcc.subChunkVoxel.z, block.id);
            chunk.getParentChunk().update(wcc.subChunkVoxel.x, wcc.subChunk.y, wcc.subChunkVoxel.y,
                    wcc.subChunkVoxel.z);
        }
    }

    public void setBlockDataAndUpdate(BlockData data, final int x, final int y, final int z) {
        WCCi wcc = new WCCi().set(x, y, z);
        SubChunk chunk = getSubChunk(wcc.subChunk);
        if (chunk != null) {
            chunk.data.setBlockData(wcc.subChunkVoxel.x, wcc.subChunkVoxel.y, wcc.subChunkVoxel.z, data);
            chunk.getParentChunk().update(wcc.subChunkVoxel.x, wcc.subChunk.y, wcc.subChunkVoxel.y,
                    wcc.subChunkVoxel.z);
        }
    }
    // </editor-fold>
}
