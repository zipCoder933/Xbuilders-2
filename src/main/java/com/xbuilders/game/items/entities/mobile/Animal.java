/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.game.items.entities.mobile;

import com.xbuilders.engine.VoxelGame;
import com.xbuilders.engine.player.UserControlledPlayer;
import com.xbuilders.engine.items.Item;
import com.xbuilders.engine.items.ItemType;
import com.xbuilders.engine.items.entity.Entity;
import com.xbuilders.engine.items.tool.Tool;
import com.xbuilders.engine.rendering.entity.glEntityMesh;
import com.xbuilders.engine.utils.math.TrigUtils;
import com.xbuilders.engine.utils.worldInteraction.collision.PositionHandler;
import com.xbuilders.engine.world.chunk.SubChunk;
import com.xbuilders.engine.world.chunk.XBFilterOutputStream;
import com.xbuilders.engine.world.wcc.WCCf;
import com.xbuilders.game.Main;
import com.xbuilders.game.items.GameItems;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.IOException;

public abstract class Animal extends Entity {


    public void drawLeg(glEntityMesh shape, float x, float y, float z, float progression) {
        modelMatrix.translate(x, y, z);
        float rotation = (float) Math.sin(progression) * 0.5f;
        if (progression != 0) {
            modelMatrix.rotateX(rotation);
        }
        shape.updateModelMatrix(modelMatrix);
        shape.draw();
        if (progression != 0) {
            modelMatrix.rotateX(-rotation);
        }
        modelMatrix.translate(-x, -y, -z);
    }

    public boolean inWater() {
        if (this.getPointerHandler().getWorld().getBlock(
                (int) this.worldPosition.x,
                (int) this.worldPosition.y,
                (int) this.worldPosition.z
        ).isLiquid()
                || this.getPointerHandler().getWorld().getBlock(
                (int) this.worldPosition.x - 1,
                (int) this.worldPosition.y,
                (int) this.worldPosition.z
        ).isLiquid()
                || this.getPointerHandler().getWorld().getBlock(
                (int) this.worldPosition.x + 1,
                (int) this.worldPosition.y,
                (int) this.worldPosition.z
        ).isLiquid()
                || this.getPointerHandler().getWorld().getBlock(
                (int) this.worldPosition.x,
                (int) this.worldPosition.y,
                (int) this.worldPosition.z - 1
        ).isLiquid()) {
            return true;
        }
        return (this.getPointerHandler().getWorld().getBlock(
                (int) this.worldPosition.x,
                (int) this.worldPosition.y,
                (int) this.worldPosition.z + 1
        ).isLiquid());
    }

    /**
     * @return the rotationY
     */
    public float getRotationY() {
        return rotationY;
    }

    /**
     * @param rotationY the rotationY to setBlock
     */
    public void setRotationY(float rotationY) {
        this.rotationY = rotationY;
    }

    /**
     * @param add the amount to add to rotationY
     */
    public void addToRotationY(float add) {
        this.rotationY += add;
    }

    /**
     * Resets the range of the degree back to 0-360
     */
    public void normalizeRotationY() {
        // Use modulo operator to wrap the degree to be in the range of 0 to 360 degrees
        this.rotationY = this.rotationY % 360;
    }

    /**
     * @param needsConstantSaving the needsConstantSaving to setBlock
     */
    public void setNeedsConstantSaving(boolean needsConstantSaving) {
        this.needsConstantSaving = needsConstantSaving;
    }

    /**
     * @return the alwaysInFrustum
     */
    public boolean isAlwaysInFrustum() {
        return alwaysInFrustum;
    }

    /**
     * @param alwaysInFrustum the alwaysInFrustum to setBlock
     */
    public void setAlwaysInFrustum(boolean alwaysInFrustum) {
        this.alwaysInFrustum = alwaysInFrustum;
    }

    /**
     * @return the tamed
     */
    public boolean isTamed() {
        return tamed;
    }

    private float rotationY = 0;
    private PositionHandler posHandler;
    private boolean pendingDestruction = false;
    private boolean collisionEnabledWithPlayer = true;
    private AnimalRandom random = new AnimalRandom(this);
    public int SEED_MAXIMUM = 100000;
    Vector3f renderOffset;
    private boolean alwaysInFrustum = false;
    final Vector3f aabbSize;
    final float animalRadius;
    final boolean centeredOnGround;

    public Animal(Vector3f aabbSize, boolean centeredOnGround, float animalRadius) {
        super();
        this.aabbSize = aabbSize;
        this.animalRadius = animalRadius;
        this.centeredOnGround = centeredOnGround;
    }

    /**
     * @return the enableCollisionWithPlayer
     */
    public boolean isCollisionWithPlayerEnabled() {
        return collisionEnabledWithPlayer;
    }

    /**
     * @param collisionEnabled the enableCollisionWithPlayer to setBlock
     */
    public void enableCollisionWithPlayer(boolean collisionEnabled) {
        this.collisionEnabledWithPlayer = collisionEnabled;
    }

    public void makeAnimalLeave(boolean isPendingDestruction) {
        if (Main.getSettings().getSettingsFile().additionalFeatures) {
            destroy(true);
        }
        this.pendingDestruction = isPendingDestruction;
        onDestructionInitiated();
    }

    /**
     * @return the pendingDestruction
     */
    public boolean isPendingDestruction() {
        return pendingDestruction;
    }

    /**
     * @return the posHandler
     */
    public PositionHandler getPosHandler() {
        return posHandler;
    }

    /**
     * @return the random
     */
    public AnimalRandom getRandom() {
        return random;
    }

    public void goForward(float amount) {
        WCCf wcc = new WCCf().set(worldPosition);
        boolean hasCurrentChunk = wcc.chunkExists();
        if (hasCurrentChunk) {
            Vector2f vec = TrigUtils.getCircumferencePoint(-getRotationY(), amount);
            worldPosition.add(vec.x, 0, vec.y);
        }
    }

    @Override
    public void initializeImmediate(byte[] bytes, boolean setByUser) {
        setFrustumSphereRadius(animalRadius);

        posHandler = new PositionHandler(VoxelGame.getWorld(), Main.getMain(), aabb,
                VoxelGame.getPlayer(), VoxelGame.playerList);
        alwaysInFrustum = false;
        aabb.size.set(aabbSize);
        float renderOffsetX, renderOffsetY, renderOffsetZ;
        if (aabbSize.x <= 1) {
            renderOffsetX = 0.5f;
        } else {
            renderOffsetX = 0.5f - (1 - aabbSize.x);
        }
        if (centeredOnGround) {
            if (aabbSize.y <= 1) {
                renderOffsetY = 1.0f;
            } else {
                renderOffsetY = 1.0f - (1 - aabbSize.y);
            }
        } else {
            if (aabbSize.y <= 1) {
                renderOffsetY = 0.5f;
            } else {
                renderOffsetY = 0.5f - (1 - aabbSize.y);
            }
        }
        if (aabbSize.z <= 1) {
            renderOffsetZ = 0.5f;
        } else {
            renderOffsetZ = 0.5f - (1 - aabbSize.z);
        }
        this.renderOffset = new Vector3f(renderOffsetX, renderOffsetY, renderOffsetZ);
        pendingDestruction = false;

        getRandom().setSeed((int) (Math.random() * SEED_MAXIMUM));
        this.tamed = setByUser;

        if (bytes != null && bytes.length > 1) {
            tamed = bytes[0] == 1;
            byte[] output = new byte[bytes.length - START_BYTES];
            for (int i = 0; i < output.length; i++) {
                output[i] = bytes[i + START_BYTES];
            }
            initAnimal(output);
        } else {
            initAnimal(null);
        }
    }

    private boolean tamed = false;

    public void tameAnimal() {
        if (!isTamed()) {
            System.out.println("Animal " + this.toString() + " tamed!");
        }
        tamed = true;
    }

    @Override
    public final boolean onClickEvent() {
        if (isPendingDestruction()) {
            makeAnimalLeave(false);
            System.out.println("Destruction of " + this.toString() + " canceled.");
            onDestructionCancel();
        }
        tameAnimal();
        animalClicked();
        return false;
    }

    public abstract void animalClicked();

    @Override
    public final void onDestroyClickEvent() {
        onDestructionInitiated();
        makeAnimalLeave(true);
    }

    public float getAngleToPlayer() {
        UserControlledPlayer userControlledPlayer = VoxelGame.getGameScene().player;
        UserControlledPlayer userControlledPlayer1 = VoxelGame.getGameScene().player;
        return TrigUtils.getAngleOfPoints(worldPosition.x, worldPosition.z, userControlledPlayer1.worldPos.x,
                userControlledPlayer.worldPos.z);
    }

    public void facePlayer() {
        float distAngle = TrigUtils.getSignedAngleDistance(getAngleToPlayer(), getRotationY());
        if (Math.abs(distAngle) > 170) {
            setRotationY(distAngle);
        } else {
            setRotationY(getRotationY() + distAngle * 0.08f);
        }
    }

    public boolean playerHasAnimalFeed() {
        if (VoxelGame.getGameScene().player.blockPanel.curItemIsNull()) {
            return false;
        }
        Item curItem = VoxelGame.getGameScene().player.blockPanel.getCurItem().getItem();
        if (curItem.itemType == ItemType.TOOL) {
            Tool t = (Tool) curItem;
            return t == GameItems.ANIMAL_FEED || t == GameItems.ANIMAL_LEAVE;
        }
        return false;
    }

    public void eatAnimalFeed() {
        if (getPointerHandler().getApplet().frameCount % 10 == 0 && playerHasAnimalFeed() && distToPlayer < 3) {
            VoxelGame.getGameScene().player.blockPanel.getCurItem().addQuantity(-1);
        }
    }

    public abstract void renderAnimal();

    public abstract boolean move();

    /**
     * @return if the animal actually moved
     */
    public abstract boolean moveWhenOutOfFrustum();

    @Override
    public boolean update() {
        random.updateNoiseSeed();
        boolean allow = false;
        if (inFrustum) {
            allow = true;
            if (move()) {
                processMovements();
            }
        } else if (tamed
                && distToPlayer < SubChunk.WIDTH
                && getPointerHandler().getApplet().frameCount % 4 == 0
                && moveWhenOutOfFrustum()) {
            processMovements();
        }
        aabb.updateBox();

        if (!allow && isPendingDestruction()) {
            destroy(true);
            return false;
        }
        return allow;
    }

    @Override
    public final void draw() {
        modelMatrix.translate(renderOffset.x, renderOffset.y, renderOffset.z);
        modelMatrix.rotateY((float) (getRotationY() * (Math.PI / 180)));
        renderAnimal();
    }

    public abstract void postProcessMovement();

    private void processMovements() {
        postProcessMovement();
        getPosHandler().update();
        if (needsConstantSaving && getPointerHandler().getApplet().frameCount % 1000 == 0) {
            getChunk().getParentChunk().markAsNeedsSaving();
        }
    }

    private boolean needsConstantSaving = false;
    private static final int START_BYTES = 1;

    @Override
    public final void toBytes(XBFilterOutputStream fout) throws IOException {
        fout.write((byte) (tamed ? 1 : 0));
        byte[] bytes = animalToBytes();
        if (bytes != null) {
            for (int i = 0; i < bytes.length; i++) {
                fout.write(bytes[i]);
            }
        }
    }

    public abstract byte[] animalToBytes();

    public abstract void initAnimal(byte[] bytes);

    @Override
    public String toString() {
        return "animal \"" + this.link.name + "\" (hash=" + this.hashCode() + ")";
    }

    public abstract void onDestructionInitiated();

    public abstract void onDestructionCancel();

}
