// 
// Decompiled by Procyon v0.5.36
// 
package com.xbuilders.engine.world;

import com.xbuilders.engine.VoxelGame;
import com.xbuilders.engine.light.sunlight.InitialSunlightUtils;
import com.xbuilders.engine.player.UserControlledPlayer;
import com.xbuilders.engine.rendering.worldLightMap.SLMUpdatingUtils;
import com.xbuilders.engine.rendering.worldLightMap.ShaderLightMap;
import com.xbuilders.engine.utils.ErrorHandler;
import com.xbuilders.engine.utils.math.AABB;
import com.xbuilders.engine.utils.math.MathUtils;
import com.xbuilders.engine.world.chunk.Chunk;
import com.xbuilders.engine.world.chunk.ChunkCoords;
import com.xbuilders.engine.world.chunk.SubChunk;
import com.xbuilders.engine.world.chunk.wcc.WCCi;
import com.xbuilders.game.PointerHandler;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.io.IOException;

class UpdaterThread extends Thread {

    TerrainUpdater parent;
    boolean active = false;
    public PointerHandler ph;
    float lastPlayerX, lastPlayerZ;

    public UpdaterThread(PointerHandler ph, final TerrainUpdater parent) {
        this.ph = ph;
        this.parent = parent;
        aabb = new AABB();
    }

    @Override
    public void run() {
        try {
            UserControlledPlayer userControlledPlayer4 = ph.getPlayer();
            if (userControlledPlayer4.worldPos != null) {
                UserControlledPlayer userControlledPlayer1 = ph.getPlayer();
                lastPlayerX = userControlledPlayer1.worldPos.x;
                UserControlledPlayer userControlledPlayer = ph.getPlayer();
                lastPlayerZ = userControlledPlayer.worldPos.z;
            }
            while (!isInterrupted()) {
                UserControlledPlayer player = ph.getPlayer();
                if (MathUtils.dist(lastPlayerX, lastPlayerZ,
                        player.worldPos.x,
                        player.worldPos.z) > 2) { //If the player has moved
                    active = true;
                    this.updateTerrain();
                    active = false;
                    lastPlayerX = player.worldPos.x;
                    lastPlayerZ = player.worldPos.z;
                }

                updateSLM(chunkDeletionDist, player.worldPos);
                Thread.sleep(200);
            }
        } catch (InterruptedException ex) {
            System.out.println("Updater interrupted");
        } catch (Exception ex) {
            ErrorHandler.handleFatalError("Error", "Error updating terrain", ex);
        }
    }

    public String printStatus() {
        if (active) {
            return "running ";
        } else {
            return "idle";
        }
    }

    int chunkDist, chunkDeletionDist;
    AABB aabb;

    private boolean chunkInFrustum(ChunkCoords coords) {
        Chunk.setChunkAABB(aabb, coords);
        return ph.getPlayer().camera.frustum.isAABBInside(aabb);
    }


    /*
     * The memory issues here are due to updateTerrain(). Probbably because the
     * queue never gets emptied, and so it just keeps filling up the heap.
     * Goal: solve the problem where the queue never finishes (empties itself).
     */
    private void updateTerrain() throws IOException, InterruptedException {
        ChunkCoords coords = new ChunkCoords(0, 0);
        boolean foundSomething = true;

        while (!isInterrupted() && foundSomething) {
            foundSomething = false;

            // If the generation radius is larger than the SLM radius, than chunks that
            // couldnt get their SLM generated will keep the loop ever continuous, trying to
            // finish them
            // Also, the generation radius must be small enough that you dont have the
            // updater firing up for every small move the player makes
            chunkDist = ShaderLightMap.getChunkRadius(ph) - SubChunk.WIDTH;
            chunkDeletionDist = chunkDist + (SubChunk.WIDTH * 2);// There must be some padding between creation and deletion.
            if (chunkDist < TerrainUpdater.MIN_CHUNK_DIST) {
                chunkDist = TerrainUpdater.MIN_CHUNK_DIST;
                chunkDeletionDist = chunkDist + (SubChunk.WIDTH * 4);
            }

            if (!parent.regularViewDistance) {
                chunkDist /= 2;
                if (chunkDist < TerrainUpdater.MIN_CHUNK_DIST) {
                    chunkDist = TerrainUpdater.MIN_CHUNK_DIST;
                    chunkDeletionDist = chunkDist + (SubChunk.WIDTH * 4);
                }
                chunkDeletionDist *= 5;
            }

            UserControlledPlayer userControlledPlayer = ph.getPlayer();
            float centerX = userControlledPlayer.worldPos.x;
            float centerZ = userControlledPlayer.worldPos.z;

            int xStart = (int) (centerX - chunkDist) / Chunk.CHUNK_X_LENGTH;
            int xEnd = (int) (centerX + chunkDist) / Chunk.CHUNK_X_LENGTH;
            int zStart = (int) (centerZ - chunkDist) / Chunk.CHUNK_Z_LENGTH;
            int zEnd = (int) (centerZ + chunkDist) / Chunk.CHUNK_Z_LENGTH;

            boolean firstClear = true;

            for (int x = xStart; x < xEnd; x++) {
                for (int z = zStart; z < zEnd; z++) {
                    double distance = WCCi.chunkDistToPlayer(x, z, centerX, centerZ);
                    if (distance < chunkDist) {
                        // ----------------------------------------------------------
                        coords.set(x, z); // for some reason the creation of these was enough to increase memory
                        // significantly if it was added to a list
                        if (chunkIsUnfinished(coords) && chunkInFrustum(coords)) {
                            if (firstClear) { // We dont have to clear chunks if no new ones have been created
                                clearChunksOutsideOfBoundary(chunkDeletionDist); // We want to erase any chunks outside
                                // of bounds before we create more
                                firstClear = false;
                            }

                            processNode(coords);
                            foundSomething = true;
                        }
                        // ----------------------------------------------------------
                    }
                }
            }
            updateSLM(chunkDeletionDist, userControlledPlayer.worldPos);
        }
    }

    public void removeChunk(ChunkCoords coords) throws IOException {
        VoxelGame.getWorld().removeChunk(coords);
    }

    public boolean chunkIsUnfinished(ChunkCoords coords) {
        Chunk chunk = ph.getWorld().getChunk(coords);
        return chunk == null || !chunk.lightmapInit
                // Investigate why this solves the no-lightmap-on-initial-chunks mystery.
                // It could be that the chunks on the edge get their SLM pasted even though they
                // have no lightmap
                || !ShaderLightMap.inBounds(chunk.getPosition()) || !chunk.inSLM();
    }

    // HashSet<ChunkCoords> madeChunks = new HashSet<>();

    public void processNode(ChunkCoords coords) throws IOException, InterruptedException {
        /**
         * TODO: The memory still increases faster than when the updater is
         * idle. The culprit was found to be within this method.
         *
         * THEROIES: * Sometimes too many new chunks are created * The lightmap
         * propagation may increase memory usage
         *
         * TODO: Investigate the cause of the performance decrease when updating terrain
         * THEROIES: * The garbage collector
         * * It is definietly not the deletion and recreation in chunks, a chunks is never created twice iin the similar timeframe
         */

        Chunk chunk = VoxelGame.getWorld().makeChunk(coords);

        //Make neighbors
        parent.ph.getWorld().makeChunk(coords.set(coords.x + 1, coords.z));
        parent.ph.getWorld().makeChunk(coords.set(coords.x, coords.z + 1));
        parent.ph.getWorld().makeChunk(coords.set(coords.x, coords.z - 1));
        parent.ph.getWorld().makeChunk(coords.set(coords.x + 1, coords.z + 1));
        parent.ph.getWorld().makeChunk(coords.set(coords.x - 1, coords.z - 1));
        parent.ph.getWorld().makeChunk(coords.set(coords.x + 1, coords.z - 1));
        parent.ph.getWorld().makeChunk(coords.set(coords.x - 1, coords.z + 1));
        parent.ph.getWorld().makeChunk(coords.set(coords.x - 1, coords.z));

        if (!chunk.lightmapInit) {
            InitialSunlightUtils.generateInitialSunlight(chunk, false);
        }
        pasteSLM(chunk);
        if (!chunk.hasGeneratedMeshes()
            // && chunk.isSurroundedByChunks()
        ) {
            // (new Thread() {
            // public void run() {
            chunk.generateInitialMeshes();
            // }
            // }).start();
        }
    }

    public boolean pasteSLM(Chunk chunk) {
        UserControlledPlayer userControlledPlayer = ph.getPlayer();
        Vector3f playerPos = userControlledPlayer.worldPos;
        // We dont want to paste a chunk unless we first know it will even show up in
        // the lightmap once it has moved.
        if (SLMUpdatingUtils.wouldBeWithinWLM(playerPos.x, playerPos.z, chunk.getPosition())) {
            SLMUpdatingUtils.setStartPositionAroundPlayer(playerPos.x, playerPos.z);
            SLMneedsUpdating = true;
        }
        return fillWLMInChunk(chunk);
    }

    private boolean fillWLMInChunk(Chunk chunk) {
        if (!ShaderLightMap.inBounds(chunk.getPosition()) || !chunk.lightmapInit) {
            return false;
        }
        for (int i = 0; i < chunk.getSubChunks().length; i++) {
            SubChunk subChunk = chunk.getSubChunks()[i];
            if (subChunk.getLightMap().inSLM()) {
                subChunk.getLightMap().markAsPastedIntoSLM();
            } else if (subChunk.getLightMap().pasteIntoSLM()) {
                subChunk.getLightMap().markAsPastedIntoSLM();
                SLMneedsUpdating = true;
            }
        }
        return true;
    }

    public void clearChunksOutsideOfBoundary(int radius) throws IOException {
        // System.out.println("Clearing chunks...");
        for (ChunkCoords coords : ph.getWorld().chunks.keySet()) {
            if (ph.getWorld().hasChunk(coords)) {
                UserControlledPlayer userControlledPlayer = ph.getPlayer();
                final double distance = WCCi.chunkDistToPlayer(
                        coords.x, coords.z,
                        userControlledPlayer.worldPos.x,
                        userControlledPlayer.worldPos.z);

                if (distance > radius) {
                    removeChunk(coords);
                }

            }
        }
    }

    public boolean SLMneedsUpdating = false;
    long timeSinceUpdate;
    Vector3f slmUpdatePos = new Vector3f(0, 0, 0);

    public void updateSLM(int chunkDeletionDist, Vector3f playerPos) throws IOException {
        if (SLMneedsUpdating
                && System.currentTimeMillis() - timeSinceUpdate > 2000 &&//If it takes longer than 1 second to update
                slmUpdatePos.distance(playerPos) > 2) { //If the player has moved more than 2 blocks

            /**
             * If we send the SLM over on another thread it could disrupt the
             * rendering and cause concurrent errors when drawing shapes.
             */
//            System.out.println("Updating SLM");
            ShaderLightMap.markAsChanged();
            SLMneedsUpdating = false;
            slmUpdatePos.set(playerPos);
            timeSinceUpdate = System.currentTimeMillis();
        }
    }

}
