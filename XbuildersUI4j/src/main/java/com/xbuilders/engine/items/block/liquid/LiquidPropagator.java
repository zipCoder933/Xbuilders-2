/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.engine.items.block.liquid;

import com.xbuilders.engine.VoxelGame;
import com.xbuilders.engine.items.Item;
import com.xbuilders.engine.items.block.Block;
import com.xbuilders.game.items.blockType.BlockRenderType;
import com.xbuilders.engine.utils.BFS.HashQueue;
import com.xbuilders.engine.utils.BFS.TravelNode;
import com.xbuilders.game.items.GameItems;
import com.xbuilders.game.items.blocks.liquid.BlockWater;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author zipCoder933
 */
class LiquidPropagator {

    int maxSpread;
    int speed;
    public HashQueue<TravelNode> queue;
    PropagatorThread thread;
    public final Object addLock = new Object();
    Liquid parent;

    /**
     *
     * @param block
     * @param VoxelGame.getPointerHandler()
     * @param queue
     * @param speed the speed of each interval
     * @param maxSpread the maximum horizontal spread of the water.
     */
    public LiquidPropagator(Liquid parent, final int speed, final int maxSpread) {
        this.parent = parent;
        this.speed = speed;
        this.maxSpread = maxSpread;

        queue = new HashQueue<>();
        thread = new PropagatorThread(this);

//        if (travelQueueSaver == null) {
//            travelQueueSaver = new QueueSaver<TravelNode>() {
//                @Override
//                public TravelNode nodeFromString(String line, char delimiter) {
//                    TravelNode travelNode = new TravelNode();
//                    travelNode.fromFileString(line, delimiter);
//                    return travelNode;
//                }
//
//                @Override
//                public String nodeToFileString(TravelNode node, char delimiter) {
//                    return node.toFileString(delimiter);
//                }
//            };
//        }
        thread.start();
    }

    public final boolean isPenatrable(Block block) {
        if (block.isSolid()
                || block.getRenderType() == BlockRenderType.WALL_ITEM) return false;
        return block.getRenderType() != BlockRenderType.FLOOR
                && parent.isPenetrableCustom(block);
    }

 

    public void newWorld() {
        //        queueFile = new File(VoxelGame.getPointerHandler().getWorld().getInfo().getDirectory(), "liquid_queue_" + block.getName() + ".txt");
//        System.out.println(block.getName().toUpperCase() + " THREAD STARTED");
//        queue.clear();
//        if (queueFile.exists()) {
//            travelQueueSaver.loadWorld(queue, queueFile);
//        }
//        if (!queue.isEmpty()) {
//            thread.wake();
//        }
    }

    public void closeWorld() {
//        System.out.println(block.getName().toUpperCase() + " THREAD STOPPED");
//        if (queueFile != null) {
//            travelQueueSaver.save(queue, queueFile);
//        }
        thread.interrupt();
    }

    public boolean playerHasLiquidRemovalTool() {
        return VoxelGame.ph().getPlayer().blockPanel.curItemEquals(GameItems.LIQUID_REMOVAL_TOOL);
    }

    public void onRemoveEvent(final int x, final int y, final int z) {
        if (playerHasLiquidRemovalTool()) {
            return;
        }

        (new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    if (smallHead(x, y, z)) {
                        queue.add(new TravelNode(x, y, z, -1));
                    } else {
                        if (VoxelGame.ph().getWorld().getBlock(x, y, z).isAir()) {
                            VoxelGame.ph().getWorld().setBlockAndUpdate(parent, x, y, z);
                        }
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(BlockWater.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }).start();
    }

    protected boolean smallHead(int x, int y, int z) {
        int connections = 0;
        for (int x2 = x - 1; x2 < x + 2; x2++) {
            for (int z2 = z - 1; z2 < z + 2; z2++) {
                for (int y2 = y - 1; y2 < y + 1; y2++) {
                    boolean isCenterBlock = x == x2 && y == y2 && z == z2;

                    if (!isCenterBlock) {
                        Item item = VoxelGame.ph().getWorld().getBlock(x2, y2, z2);
                        if (item.id
                        == parent.id) {
                            connections++;
                        }
                    }
                }
            }
        }
        return connections <= 4;
    }

}
