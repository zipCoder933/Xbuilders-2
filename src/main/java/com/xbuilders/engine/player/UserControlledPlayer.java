package com.xbuilders.engine.player;

import com.xbuilders.engine.VoxelGame;
import com.xbuilders.engine.game.GameMode;
import com.xbuilders.engine.game.ScreenshotUtils;
import com.xbuilders.engine.items.BlockList;
import com.xbuilders.engine.player.blockPipeline.BlockPipeline;
import com.xbuilders.engine.player.camera.Camera;
import com.xbuilders.engine.items.Item;
import com.xbuilders.engine.items.ItemQuantity;
import com.xbuilders.engine.items.ItemType;
import com.xbuilders.engine.items.block.Block;
import com.xbuilders.engine.items.block.construction.BlockGeometry;
import com.xbuilders.engine.items.entity.ChunkEntitySet;
import com.xbuilders.engine.world.chunk.SubChunk;
import com.xbuilders.game.items.blockType.BlockRenderType;
import com.xbuilders.game.items.entities.mobile.Animal;
import com.xbuilders.engine.items.tool.Tool;
import com.xbuilders.engine.utils.InventoryUtils;
import com.xbuilders.engine.utils.math.MathUtils;
import com.xbuilders.engine.utils.worldInteraction.collision.PositionHandler;
import com.xbuilders.engine.world.chunk.Chunk;
import com.xbuilders.engine.world.blockData.BlockData;
import com.xbuilders.engine.world.blockData.BlockOrientation;
import com.xbuilders.engine.world.wcc.WCCi;
import com.xbuilders.game.Main;
import com.xbuilders.game.PointerHandler;
import com.xbuilders.game.blockMode.BlockTools;
import com.xbuilders.engine.gui.game.inventory.Block1DPanel;
import com.xbuilders.game.skins.FoxSkin;
import org.joml.Vector3f;
import org.joml.Vector3i;
import processing.core.KeyCode;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import com.xbuilders.window.ui4j.UIExtension;

import java.util.ArrayList;

public class UserControlledPlayer extends Player {

    public BlockTools blockTools;
    public Block1DPanel blockPanel;
    KeyCode KEY_SET_BLOCK = KeyCode.EQUALS;
    KeyCode KEY_PICK_BLOCK = KeyCode.ZERO;
    KeyCode KEY_REMOVE_BLOCK = KeyCode.DASH;

    //<editor-fold defaultstate="collapsed" desc="bedtime mode">
    public long bedTimeModeStart;
    public boolean bedtimeMode = false;
    public boolean bedtime_timeChanged;

    public void setBedtimeMode(PositionLock pos) {
        setPositionLock(pos);
        VoxelGame.getGameScene().alert("Goodnight!");
        bedTimeModeStart = System.currentTimeMillis();
        bedtime_timeChanged = false;
        bedtimeMode = true;
    }

    public void cancelBedtimeMode() {
        clearPositionLock();
        worldPos.y -= 2;
        bedtimeMode = false;
    }
    //</editor-fold>

    public void newGame() {
        autoForwardEnabled = false;
        passThroughMode = false;
        ChunkEntitySet.drawEntities = true;
        flying = false;
        System.out.println("Player Spawn Point: " + worldPos);
        camera.centerMouse();
        getBlockActionQueue().clear();
        if (getPointerHandler().isDevMode()) {
            positionHandler.setGravityEnabled(false);
            flying = true;
        }
        blockPanel = new Block1DPanel(VoxelGame.getWorld().infoFile.getInfoFile().backpack, this);
        blockPanel.initialize();
        blockTools.newGame();
        positionLock = null;
        VoxelGame.getWorld().terrain.worldBackground();
    }

    public void endGame() {
        blockTools.stop();
    }

    public UserControlledPlayer(UIExtension ext) {
        super(ext);
        ScreenshotUtils.initialize();

    }

    /**
     * @return the pointerHandler
     */
    public PointerHandler getPointerHandler() {
        return VoxelGame.ph();
    }

    /**
     * @param positionLock the positionLock to setBlock
     */
    public void setPositionLock(PositionLock positionLock) {
        this.positionLock = positionLock;
    }

    public void clearPositionLock() {
        if (positionLock != null) {
            worldPos.y = positionLock.entity.aabb.box.minPoint.y - aabb.box.getYLength();
            positionHandler.velocity.y = 0;
            this.setPositionLock(null);
        }
    }

    /**
     * @return the blockActionQueue
     */
    public ArrayList<Runnable> getBlockActionQueue() {
        return blockActionQueue;
    }

    /**
     * @return if the sunlight at player origin is under a specific threshold
     */
    public boolean isInDarkness() {
        return isInDarkness;
    }

    public PositionLock positionLock;
    public Camera camera;

    private PositionHandler positionHandler;
    private ArrayList<Runnable> blockActionQueue;

    public Block lastHeadBlock = BlockList.BLOCK_AIR;
    public Block headBlock = BlockList.BLOCK_AIR;
    private boolean isInDarkness = false;
    boolean isClimbing = false;
    public boolean passThroughMode = false;
    int undergroundAmt = 0;
    boolean flying = false;
    long lastAutoBlockSet;
    boolean autoForwardEnabled;
    public long lastMousePress;

    //<editor-fold defaultstate="collapsed" desc="Item drops">
    public void dropItem(ItemQuantity item) {
//        VoxelGame.getWorld().hologramList.add(
//                new ItemDrop(new Vector3i((int) worldPos.x, (int) (worldPos.y + 0.5f), (int) worldPos.z), item, true));
    }

    public void createDrop(int x, int y, int z, ItemQuantity iq) {
//        VoxelGame.getGame().itemDropper.createDrop(this, new Vector3i(x, y, z), iq, false);
    }

    public void createDrop(Vector3i vec, ItemQuantity iq) {
//        VoxelGame.getGame().itemDropper.createDrop(this, vec, iq, false);
    }

    //</editor-fold>
    public void initialize() {
        this.camera = new Camera(getParentFrame(), VoxelGame.getGameScene(), this);
        this.worldPos.set(0, 0, 0);

        positionHandler = new PositionHandler(VoxelGame.getWorld(), Main.getMain(), aabb, this, VoxelGame.playerList);
        blockTools = new BlockTools(this);
        blockActionQueue = new ArrayList<>();

        skin = new FoxSkin(this);
        addToFrame();
    }

    /**
     * @return the block orientation based on the cameras origin
     */
    public BlockOrientation cameraBlockOrientation() {
        return new BlockOrientation((byte) camera.simplifiedPanTilt.x, (byte) camera.simplifiedPanTilt.y);
    }

    Vector3f lastWorldPos = new Vector3f();
    float rotation;

    public Block getBlockPlayerIsIn() {
        return lastHeadBlock;
    }

    private void calculateBackgroundColor() {
        if (worldPos.y <= 0) {
            isInDarkness = false;
        } else {
            WCCi wcc = new WCCi().set((int) worldPos.x, (int) worldPos.y, (int) worldPos.z);
            byte sunAtHeadPos = 0;
            if (wcc.subChunkExists()) {
                SubChunk subChunk = wcc.getSubChunk();
                sunAtHeadPos = subChunk.lightMap.getSunlight(wcc.subChunkVoxel);
            }
            VoxelGame.getShaderHandler().setLightValueAroundPlayer(sunAtHeadPos);
            isInDarkness = (sunAtHeadPos < 4);
        }
    }

    private void calculateUnderground() {
        int height = VoxelGame.getWorld().terrain.getHeightmapOfVoxel((int) worldPos.x, (int) worldPos.z);
        if (height < 0) {
            undergroundAmt = 0;
        } else {
            undergroundAmt = (int) worldPos.y - height;
        }
    }

    public boolean forwardKeyPressed() {
        if (VoxelGame.getGameScene().menu.isShown()) {
            return false;
        }
        return keyIsPressed(KeyCode.UP) || keyIsPressed(KeyCode.W) || autoForwardEnabled;
    }

    public boolean leftKeyPressed() {
        if (VoxelGame.getGameScene().menu.isShown()) {
            return false;
        }
        return keyIsPressed(KeyCode.LEFT) || keyIsPressed(KeyCode.A);
    }

    public boolean rightKeyPressed() {
        if (VoxelGame.getGameScene().menu.isShown()) {
            return false;
        }
        return keyIsPressed(KeyCode.RIGHT) || keyIsPressed(KeyCode.D);
    }

    public boolean backKeyPressed() {
        if (VoxelGame.getGameScene().menu.isShown()) {
            return false;
        }
        return keyIsPressed(KeyCode.DOWN) || (keyIsPressed(KeyCode.S) && !keyIsPressed(KeyCode.CTRL));
    }

    public boolean upKeyPressed() {
        if (VoxelGame.getGameScene().menu.isShown()) {
            return false;
        }
        return keyIsPressed(KeyCode.F) && !keyIsPressed(KeyCode.SHIFT);
    }

    public boolean downKeyPressed() {
        if (VoxelGame.getGameScene().menu.isShown()) {
            return false;
        }
        return keyIsPressed(KeyCode.SHIFT) && keyIsPressed(KeyCode.F);
    }

    public void update(PGraphics pg) {
        calculateBackgroundColor();
        calculateUnderground();

        headBlock = getPointerHandler().getWorld().getBlock((int) Math.floor(camera.position.x), (int) Math.floor(camera.position.y), (int) Math.floor(camera.position.z));

        if (!VoxelGame.getGameScene().menu.isShown()) {
            if (positionLock != null) {
                worldPos.set(positionLock.getPosition());
                aabb.updateBox();
            } else {
                if (!positionHandler.isFrozen()) {
                    float speed = 0;
                    if (VoxelGame.getGameScene().mode == GameMode.FREEPLAY && keyIsPressed(KeyCode.SHIFT)) {
//                        System.out.println("RUNNING SPEED: " + getPointerHandler().getSettingsFile().runSpeed);
                        speed = getPointerHandler().getSettingsFile().runSpeed * (1.0f / getParentFrame().frameRate);
                    } else {
                        speed = getPointerHandler().getSettingsFile().walkSpeed * (1.0f / (getParentFrame().frameRate));
                    }
                    Vector3f vec = new Vector3f();

                    if (leftKeyPressed()) {
                        vec.set(camera.right);
                        vec.mul(speed);
                        positionHandler.velocity.add(vec);
                    }
                    if (rightKeyPressed()) {
                        vec.set(camera.right);
                        vec.mul(speed);
                        positionHandler.velocity.sub(vec);
                    }
                    if (forwardKeyPressed()) {
                        vec.set(camera.cameraForward);
                        vec.mul(speed);
                        positionHandler.velocity.add(vec);
                    }
                    if (backKeyPressed()) {
                        vec.set(camera.cameraForward);
                        vec.mul(speed);
                        positionHandler.velocity.sub(vec);
                    }

                    if (keyIsPressed(KeyCode.SPACE)) {
                        positionHandler.setGravityEnabled(true);
                        if (passThroughMode) {
                            passThroughMode = false;
                        } else {
                            flying = false;
                        }
                    }
                } //Flying and climbing
                Vector3f flying = new Vector3f(camera.up).mul(getPointerHandler().getSettingsFile().flySpeed * (2.0f / getParentFrame().frameRate));
                Vector3f climbing = new Vector3f(camera.up).mul(1f * (2.0f / getParentFrame().frameRate));

                Block blockYPlusTwo = VoxelGame.getWorld().getBlock((int) Math.floor(worldPos.x), (int) Math.floor(worldPos.y + aabb.box.getYLength()), (int) Math.floor(worldPos.z));

                if (headBlock.type == BlockRenderType.WALL_ITEM || blockYPlusTwo.type == BlockRenderType.WALL_ITEM) {
                    positionHandler.setGravityEnabled(false);
                    isClimbing = true;

                    if (downKeyPressed()) {
                        worldPos.add(climbing);
                    } else if (upKeyPressed() || keyIsPressed(KeyCode.SPACE)) {
                        worldPos.sub(climbing);
                    }
                } else {
                    if (isClimbing) {
                        if (!this.flying) {
                            positionHandler.setGravityEnabled(true);
                        }
                        if (!keyIsPressed(KeyCode.F)) {
                            isClimbing = false;
                        }
                    } else if (VoxelGame.getGameScene().mode == GameMode.FREEPLAY) {
                        if (this.flying && downKeyPressed()) {
                            positionHandler.velocity.sub(flying);
                        } else if (upKeyPressed()) {
                            positionHandler.velocity.add(flying);
                            positionHandler.setGravityEnabled(false);
                            this.flying = true;
                        }
                    } else {
                        this.flying = false;
                    }
                }

                positionHandler.collisionsEnabled = !(passThroughMode && this.flying);
                positionHandler.update();
            }

            if (getParentFrame().mousePressed) {
                if (System.currentTimeMillis() - lastMousePress > getPointerHandler().getSettingsFile().blockAutoSetTimeThreshold) {
                    if (System.currentTimeMillis() - lastAutoBlockSet > getPointerHandler().getSettingsFile().blockAutoSetInterval) {
                        blockEvent(mouseButton == MouseButton.CREATE);
                        lastAutoBlockSet = System.currentTimeMillis();
                    }
                }
            }
        }

        //Draw block overlay on the screen
        if (lastHeadBlock != headBlock) {
            lastHeadBlock = headBlock;
            if (headBlock.isAir()) {
                VoxelGame.getWorld().terrain.worldBackground();
                VoxelGame.getShaderHandler().setFogDistance(1);
            } else {
                headBlock.playerHeadEnterBlockEvent(this);
            }
        }

        //Render
        float rotation2;
        if (worldPos.distance(lastWorldPos) > 0.001f) {
            rotation2 = (float) Math.atan2(worldPos.x - lastWorldPos.x, worldPos.z - lastWorldPos.z);
            lastWorldPos.set(worldPos);
        } else {
            rotation2 = (float) Math.atan2(camera.cameraForward.x, camera.cameraForward.z);
        }
        rotation = (float) MathUtils.curve(rotation, rotation2, 0.4f);

        if (camera.getThirdPersonDist() != 0) {
            drawSkin(pg, rotation, positionLock == null ? 0f : -0.9f);
        }

        camera.cursorRay.drawCursor(blockTools, pg);
        this.blockTools.drawCursor(camera.cursorRay, pg);
        this.camera.draw(pg);
    }


    public Item breakItem = null;

    private boolean shouldDestroy(Item item) {
        if (VoxelGame.getGameScene().mode == GameMode.FREEPLAY) {
            return true;
        }
        if (!item.equals(breakItem)) {
            breakItem = item;
            lastMousePress = System.currentTimeMillis();
        } else if (breakItem != null && System.currentTimeMillis() - lastMousePress > breakItem.breakTimeMS()) {
            lastMousePress = System.currentTimeMillis();
            return true;
        }
        return false;
    }


    public synchronized void pickBlock() {
        Vector3i hitPos = camera.cursorRay.getHitPositionAsInt();
        Vector3i hitPosNormal = camera.cursorRay.getHitPosPlusNormal();
        Block hitBlock = getPointerHandler().getWorld().getBlock(hitPos);
        if (camera.cursorRay.getEntity() != null) {
            blockPanel.setCurItem(camera.cursorRay.getEntity().link);
        } else {
            blockPanel.setCurItem(hitBlock);
        }
        blockTools.resetBlockMode();
    }

    private synchronized void blockEvent(boolean create) {
        BlockData data = !blockPanel.curItemIsNull()
                && blockPanel.getCurItem().getItem().itemType == ItemType.BLOCK
                ? BlockGeometry.getInitialBlockData(this, (Block) blockPanel.getCurItem().getItem(), camera.cursorRay.cursorRay) : null;

        Vector3i hitPos = camera.cursorRay.getHitPositionAsInt();
        Vector3i hitPosNormal = camera.cursorRay.getHitPosPlusNormal();
        Block hitBlock = getPointerHandler().getWorld().getBlock(hitPos);
        if (!(camera.isMouseFocused && camera.cursorRay.hitTarget())) {
            return;
        }

        boolean createAllowed = true;
        if (create) {
            if (!camera.cursorRay.createClickEvent()) {
            } else {
                if (camera.cursorRay.getEntity() != null) {
                    createAllowed = camera.cursorRay.getEntity().onClickEvent();
                    camera.cursorRay.getEntity().markAsModifiedByUser();
                } else {
                    createAllowed = hitBlock.onClickEvent(hitPos.x, hitPos.y, hitPos.z);
                }
                if (createAllowed) {
                    blockTools.setBlock(blockPanel.getCurItem(), camera.cursorRay, data, true); //Block mode
                }
            }
        } else {
            if (!camera.cursorRay.destroyClickEvent()) {
            } else {
                if (camera.cursorRay.getEntity() != null && shouldDestroy(camera.cursorRay.getEntity().link)) {
                    camera.cursorRay.getEntity().onDestroyClickEvent();
                    camera.cursorRay.getEntity().markAsModifiedByUser();
                    boolean isAnimal = camera.cursorRay.getEntity() instanceof Animal;
                    if (!isAnimal && (!camera.cursorRay.getEntity().link.isInfiniteResource() || VoxelGame.getGameScene().mode == GameMode.WALKTHOUGH)) {
                        createDrop((int) camera.cursorRay.getEntity().worldPosition.x, (int) camera.cursorRay.getEntity().worldPosition.y, (int) camera.cursorRay.getEntity().worldPosition.z, new ItemQuantity(camera.cursorRay.getEntity().link, (byte) 1));
                    }
                } else if (shouldDestroy(hitBlock)) {
                    blockTools.setBlock(blockPanel.getCurItem(), camera.cursorRay, data, false);
                }
            }
        }
    }

    /**
     * @param items
     * @return if the inventory could fit the new item
     */
    public boolean acquireItems(ItemQuantity items) {
        return InventoryUtils.appendBlocksToInventory(blockPanel.itemList, items);
    }

    /**
     * @param item
     * @param quantity
     * @return if the inventory could fit the new item
     */
    public boolean acquireItems(Item item, int quantity) {
        if (item.isInfiniteResource()) {
            return true;
        }
        return InventoryUtils.appendBlocksToInventory(blockPanel.itemList, new ItemQuantity(item, (byte) quantity));
    }

    public boolean setBlock(Block block, BlockData data, int x, int y, int z) {
        if (y >= Chunk.HEIGHT - 1 || y == 0) {
            return false;
        }
        WCCi wcc = new WCCi().set(x, y, z);
        boolean wasSet = false;
        if (wcc.chunkExists()) {
            Chunk chunk = wcc.getChunk();
            BlockPipeline.startLocalChange(new Vector3i(x, y, z), block);
            block.setBlock(x, y, z, data);
            block.set(x, y, z);
            wasSet = true;
            chunk.markAsModifiedByUser();
            chunk.markAsNeedsSaving();
        }
        return wasSet;
    }


//</editor-fold>

    public enum MouseButton {
        CREATE, DESTROY, MIDDLE
    }

    public MouseButton mouseButton;
    boolean scrollView;
    public final KeyCode KEY_CHANGE_VIEW = KeyCode.O;


    @Override
    public void mouseEvent(MouseEvent event) {
        if (event.getAction() == MouseEvent.PRESS) {
            mouseButton = switch (event.getButton()) {
                case 37 ->
                        getPointerHandler().getSettingsFile().switchMouseButtons ? MouseButton.DESTROY : MouseButton.CREATE;
                case 39 ->
                        getPointerHandler().getSettingsFile().switchMouseButtons ? MouseButton.CREATE : MouseButton.DESTROY;
                default -> MouseButton.MIDDLE;
            };
            if (!(VoxelGame.getGameScene().mode == GameMode.WALKTHOUGH && mouseButton == MouseButton.DESTROY)) {
                clickEvent(mouseButton);
            }
        } else if (event.getAction() == MouseEvent.WHEEL) {
            if (camera.cursorRay.mouseEvent(event, getParentFrame())) {

            } else if (VoxelGame.getGameScene().mode == GameMode.FREEPLAY && keyIsPressed(KeyCode.SHIFT)) {
                blockTools.mouseWheel(event.getCount());
            } else if (keyIsPressed(KEY_CHANGE_VIEW) || positionLock != null) {
                camera.setThirdPersonDist(camera.getThirdPersonDist() - (event.getCount() * 2));
                scrollView = true;
            } else {
                blockPanel.changeSelection(event.getCount());
            }
        } else if (event.getAction() == MouseEvent.RELEASE) {
            breakItem = null;
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (!VoxelGame.getGameScene().menu.isShown()) {
            blockTools.keyPressed(positionHandler.window, event);
            if (keyIsPressed(KeyCode.SPACE)) {
                positionHandler.jump();
                if (bedtimeMode) {
                    cancelBedtimeMode();
                }
                clearPositionLock();
            } else {
                camera.cursorRay.keyPressed(event, getParentFrame());
            }
        }
    }

//    public boolean blockIsIntersectingEntity(Vector3i worldCoords) {
//        WCCi wcc = new WCCi().set(worldCoords);
//        for (int ccx = wcc.subChunk.x - 1; ccx < wcc.subChunk.x + 2; ccx++) {
//            for (int ccy = wcc.subChunk.y - 1; ccy < wcc.subChunk.y + 2; ccy++) {
//                for (int ccz = wcc.subChunk.z - 1; ccz < wcc.subChunk.z + 2; ccz++) {
//
//                    SubChunk sc = getPointerHandler().getWorld().getSubChunk(new Vector3i(ccx, ccy, ccz));
//                    if (sc != null) {
//                        for (Entity e : sc.getEntities().list) {
//                            ArrayList<Vector3i> otherEntityBoxes = e.getStaticBoxes((int) e.worldPosition.x, (int) e.worldPosition.y, (int) e.worldPosition.z);
//                            if (otherEntityBoxes != null && otherEntityBoxes.contains(worldCoords)) {
//                                return true;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return false;
//    }

    @Override
    public void keyReleased(KeyEvent ke) {
        if (!VoxelGame.getGameScene().menu.isShown()) {
            blockTools.keyReleased(positionHandler.window, ke);

            if (keyIsPressed(KeyCode.J)) {
                autoForwardEnabled = !autoForwardEnabled;
                VoxelGame.getGameScene().alert("Auto forward " + (autoForwardEnabled ? "enabled" : "disabled"));
            } else if (VoxelGame.getGameScene().mode == GameMode.FREEPLAY && keyIsPressed(KeyCode.P)) {
                passThroughMode = !passThroughMode;
                if (passThroughMode) {
                    flying = true;
                }
            } else if (keyIsPressed(KEY_SET_BLOCK)) {
                clickEvent(MouseButton.CREATE);
            } else if (keyIsPressed(KEY_REMOVE_BLOCK)) {
                clickEvent(MouseButton.DESTROY);
            } else if (keyIsPressed(KEY_PICK_BLOCK)) {
                clickEvent(MouseButton.MIDDLE);
            } else if (keyIsPressed(KEY_CHANGE_VIEW)) {
                if (!scrollView) {
                    camera.cycleToNextView(5);
                }
                scrollView = false;
            } else {
                camera.cursorRay.keyReleased(ke, getParentFrame());
            }
        }
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
    }

    private void clickEvent(MouseButton button) {
        if (button == MouseButton.CREATE && blockPanel.getCurItem() != null) {
            Item item = blockPanel.getCurItem().getItem();
            if (item.itemType == ItemType.TOOL) {
                Tool tool = (Tool) blockPanel.getCurItem().getItem();
                tool.onClick();
            }
        }

        if (mouseButton == MouseButton.MIDDLE) pickBlock();
        else blockEvent(button == MouseButton.CREATE);
        lastMousePress = System.currentTimeMillis();

        if (camera.isMouseFocused == false) {
            camera.centerMouse();
            camera.isMouseFocused = true;
        }
    }
}
