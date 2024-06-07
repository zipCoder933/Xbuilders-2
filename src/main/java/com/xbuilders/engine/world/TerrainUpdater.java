/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.engine.world;

import com.xbuilders.engine.VoxelGame;
import com.xbuilders.engine.utils.MiscUtils;
import com.xbuilders.game.PointerHandler;
import com.xbuilders.engine.world.chunk.Chunk;

/**
 *
 * @author zipCoder933
 */
public class TerrainUpdater {

    /**
     * @return the terrainChunkDist
     */
    public int getTerrainChunkDist() {
        return terrainChunkDist;
    }
    
    UpdaterThread thread;
    private int terrainChunkDist;
    public final static int MIN_CHUNK_DIST = 60;

    public void update() {
        int extraDistMultiplier = VoxelGame.getSettings().getSettingsFile().extraChunkDistMultiplier;
        int chunkDist2 = ph.getSettingsFile().chunkRadius;
        chunkDist2 += (extraDistMultiplier * Chunk.WIDTH);
        if (ph.getPlayer().isInDarkness()) {
            chunkDist2 *= 0.4f;
        }
        if (chunkDist2 < 50) {
            chunkDist2 = 50;
        }
        terrainChunkDist = chunkDist2;
    }

    PointerHandler ph;

    public TerrainUpdater(PointerHandler ph) {
        this.ph = ph;
    }

    public synchronized void begin(PointerHandler pointerHandler) {
        update();
        thread = new UpdaterThread(pointerHandler, this);
        thread.start();
        thread.setPriority(1); //The setPriority() instance method takes an integer between 1 and 10 for changing the thread’s priority
    }

    public synchronized void end() throws InterruptedException {
        MiscUtils.stopThreadAndWaitForCompletion(thread);
        System.out.println("Updater has stoppped.");
    }

    public boolean isRunning() {
        return thread != null && thread.isAlive();
    }

    public boolean isActive() {
        return thread != null && thread.active;
    }

    public String getStatusString() {
        if (isRunning()) {
            return "Chunk Gen: " + thread.printStatus();
        } else {
            return "Updater-thread not running...";
        }
    }
}
