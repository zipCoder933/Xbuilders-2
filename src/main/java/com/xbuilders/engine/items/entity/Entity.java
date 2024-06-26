/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.engine.items.entity;

import com.xbuilders.engine.VoxelGame;
import com.xbuilders.engine.items.entity.shapeSet.OrientedShape;
import com.xbuilders.engine.items.block.Block;
import com.xbuilders.engine.utils.ErrorHandler;
import com.xbuilders.engine.utils.worldInteraction.collision.EntityAABB;
import com.xbuilders.engine.world.chunk.XBFilterOutputStream;
import com.xbuilders.engine.world.wcc.WCCf;
import com.xbuilders.game.PointerHandler;
import com.xbuilders.engine.utils.math.MathUtils;
import com.xbuilders.engine.world.chunk.SubChunk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.joml.*;
import processing.core.PImage;

/**
 * @author zipCoder933
 */
public abstract class Entity {


   

    public final boolean playerIsRidingThis() {
        if (getPointerHandler().getPlayer().positionLock == null) return false;
        return getPointerHandler().getPlayer().positionLock.entity == this;
    }

    /**
     * @return the frustumSphereRadius
     */
    public float getFrustumSphereRadius() {
        return frustumSphereRadius;
    }

    /**
     * @param frustumSphereRadius the frustumSphereRadius to setBlock
     */
    public void setFrustumSphereRadius(float frustumSphereRadius) {
        this.frustumSphereRadius = frustumSphereRadius;
    }

    public PointerHandler getPointerHandler() {
        return VoxelGame.ph();
    }

    /**
     * @return the chunk
     */
    public final SubChunk getChunk() {
        return chunk;
    }


    /**
     * Get position of entity in chunk space.
     *
     * @return a created Vector3f of the chunk worldPosition. (Note that entity
     * worldPosition is stored in world space, so what you get here is just a
     * deep copy, converted into chunk space)
     */
    public Vector3f getChunkPosition() {
        return new Vector3f(
                MathUtils.positiveMod(worldPosition.x, SubChunk.WIDTH),
                MathUtils.positiveMod(worldPosition.y, SubChunk.WIDTH),
                MathUtils.positiveMod(worldPosition.z, SubChunk.WIDTH));
    }


    public final void markAsModifiedByUser() {
        chunk.getParentChunk().markAsModifiedByUser();
        chunk.getParentChunk().markAsNeedsSaving();
    }

    public final void markAsNeedsSaving() {
        chunk.getParentChunk().markAsNeedsSaving();
    }

    public boolean destroyMode = false;

    public final void destroy(boolean doneByUser) {
        destroyMode = true;
        if (doneByUser) {
            chunk.getParentChunk().markAsModifiedByUser();
        }
    }

    public boolean isSolid(Block block) {
        return block.isSolid();
    }

    public void updateEntityMesh() {
        needsUpdating = true;
    }

    public float distToPlayer;
    public boolean hasMoved;
    public final Vector3f worldPosition;
    protected final Vector3f lastWorldPosition;
    public final WCCf chunkPosition;
    protected boolean needsUpdating;
    public EntityLink link;
    public SubChunk chunk;
    public boolean inFrustum;
    private float frustumSphereRadius = 1;
    public final EntityAABB aabb;

    protected byte[] loadBytes;
    public final Matrix4f modelMatrix = new Matrix4f();

    public void sendModelMatrixToShader() {
        sendModelMatrixToShader(modelMatrix);
    }

    //Having an entity shader does nothing for performance

    public static void sendModelMatrixToShader(Matrix4f matrix) {
        VoxelGame.getShaderHandler().setBlockShaderModelMatrix(matrix);
    }

    public Entity() {
        aabb = new EntityAABB();
        chunkPosition = new WCCf();
        worldPosition = aabb.worldPosition;
        lastWorldPosition = new Vector3f();
        needsInit = true;
    }

    protected boolean needsInit;

    //We can also call initialize() in the entity Link at the start of the game

    public abstract void initializeImmediate(byte[] bytes, boolean setByUser); //Initialize immediately

    public void initializeOnDraw(byte[] bytes, boolean setByUser){} //Initialize on draw method

    public boolean onClickEvent(){
        return true;
    }

    public void onDestroyClickEvent(){

    }


    public ArrayList<OrientedShape> getStaticMeshes() {
        return null;
    }

    public PImage getStaticTexture() {
        return null;
    }

    public boolean hasStaticMeshes() {
        return getStaticMeshes() != null;
    }


    public abstract boolean update();

    /**
     * Renders the entity. NOTE: the entity should be rendered in world space.
     * The chunk will not translate the entity to chunk space for you.
     */
    public void draw() {
    }

    public abstract void toBytes(XBFilterOutputStream fout) throws IOException;


    @Override
    public String toString() {
        return "Entity{" + link.name + " id=" + link.id + "}";
    }

    /**
     * @return this.toBytes() as a byte array instead of an XBFilterOutputStream
     */
    public final byte[] toByteArray() {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            XBFilterOutputStream filterOutputStream = new XBFilterOutputStream(byteArrayOutputStream);
            this.toBytes(filterOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException ex) {
            ErrorHandler.saveErrorToLogFile(ex);
        }
        return null;
    }
}
