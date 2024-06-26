/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.engine.items.entity;

import com.jogamp.opengl.GL4;
import com.xbuilders.engine.VoxelGame;
import com.xbuilders.engine.player.UserControlledPlayer;
import com.xbuilders.engine.rendering.ShaderHandler;
import com.xbuilders.engine.utils.ErrorHandler;
import com.xbuilders.engine.utils.math.MathUtils;
import com.xbuilders.engine.world.chunk.Chunk;
import com.xbuilders.engine.world.chunk.SubChunk;
import com.xbuilders.game.Main;

import java.util.ArrayList;

import processing.core.PGraphics;
import processing.core.UIFrame;
import processing.opengl.PJOGL;

/**
 * @author zipCoder933
 */
// TODO: Replace all static entitys with blocks
public class ChunkEntitySet {

    SubChunk chunk;
    public ArrayList<Entity> list;


    public ChunkEntitySet(SubChunk chunk) {
        super();
        list = new ArrayList<>();
        this.chunk = chunk;


    }

    public Entity get(int i) {
        return list.get(i);
    }

    static UserControlledPlayer userControlledPlayer;
    static UIFrame uiFrame;
    static PGraphics pGraphics;
    static PJOGL pgl;
    static GL4 gl;

    static {
        uiFrame = Main.getFrame();
        pGraphics = Main.getPG();
        userControlledPlayer = VoxelGame.getPlayer();
    }


    public static void bindEntityShader() {
        ShaderHandler.entityShader.bind();
    }


    public static void startDrawEntities() {
        pgl = (PJOGL) uiFrame.beginPGL();
        gl = pgl.gl.getGL4();

        bindEntityShader();
        ShaderHandler.entityShader.updateProjViewMatrix(VoxelGame.getGameScene().projection, VoxelGame.getGameScene().view);

        //Enable backface culling
        gl.glEnable(GL4.GL_CULL_FACE);
        gl.glCullFace(GL4.GL_BACK);
    }

    public static void endDrawEntities() {
        VoxelGame.getShaderHandler().resetModelMatrix();
//        ShaderHandler.entityShader.unbind();
        uiFrame.endPGL();
    }

    public void updateAndDrawEntities(boolean chunkInFrustum) {
        for (int i = list.size() - 1; i >= 0; i--) { // Loop through the list of entities in reverse order
            Entity e = get(i); // Get the entity at index i

            if (e.needsInit) { //Initialize the entity if it has bytes
                e.initializeOnDraw(e.loadBytes, true);
                e.loadBytes = null;
                e.needsInit = false;
            }


            e.distToPlayer = e.worldPosition.distance(userControlledPlayer.worldPos); // Calculate the distance to the


            if (e.destroyMode) { // Check if the entity is in destroy mode
                list.remove(i); // Remove the entity from the list
                if (e.hasStaticMeshes()) { // Check if the entity has static meshes
                    chunk.generateStaticEntityMesh(); // Generate static entity mesh for the chunk
                }
                chunk.getParentChunk().markAsNeedsSaving(); // Mark the parent chunk as needing saving
                continue;
            } else if (!e.playerIsRidingThis()
                    && e.distToPlayer > VoxelGame.getSettings().getSettingsFile().entityFullMaxDistance
                    && (e.link.entityMaxDistToPlayer > 0 && e.distToPlayer > e.link.entityMaxDistToPlayer)) {
                continue; // Skip the rest of the loop for this entity
            } else if (e.needsUpdating) { // Check if the entity needs updating
                if (e.hasStaticMeshes()) { // Check if the entity has static meshes
                    chunk.generateStaticEntityMesh(); // Generate static entity mesh for the chunk
                }
                e.needsUpdating = false; // Set the needsUpdating flag to false
            }

            if (!e.hasStaticMeshes()) { // Check if the entity does not have static meshes
                // check if the entity is in frustum
                if (e.playerIsRidingThis() || e.getFrustumSphereRadius() <= 0) {
                    e.inFrustum = true;
                } else {
                    e.inFrustum = chunkInFrustum &&
                            VoxelGame.getPlayer().camera.frustum.isSphereInside(e.worldPosition,
                                    e.getFrustumSphereRadius());
                }


                if (e.update() && e.inFrustum && drawEntities) { //Update the entity
                    try {// draw the entity
                        e.modelMatrix.identity().translate(
                                e.worldPosition.x,
                                e.worldPosition.y,
                                e.worldPosition.z);
                        e.draw();
                    } catch (Exception ex) {
                        ErrorHandler.saveErrorToLogFile(ex);
                    }
                }
            }

            e.worldPosition.y = MathUtils.clamp(e.worldPosition.y, 1, Chunk.HEIGHT - 1);
            e.hasMoved = !e.lastWorldPosition.equals(e.worldPosition);

            if (e.hasMoved) {// If the entity has moved
                e.lastWorldPosition.set(e.worldPosition);
                e.chunkPosition.set(e.worldPosition); // update entity's chunk position
                if (!e.chunkPosition.subChunk.equals(e.chunk.position)) { // Check if the entity's chunk position is
                    // different from the entity's chunk
                    SubChunk toChunk = e.chunkPosition.getSubChunk();
                    if (toChunk != null) {
                        e.chunk = toChunk; // Set the entity's chunk to the new chunk
                        toChunk.entities.list.add(e); // Add the entity to the new chunk's entities list
                        list.remove(i); // Remove the entity from the list

                        // Save both chunks
                        e.chunk.getParentChunk().markAsNeedsSaving();
                        toChunk.getParentChunk().markAsNeedsSaving();
                    }
                }
            }
        }

    }


    public static boolean drawEntities = true;

    public void clear() {
        list.clear();
    }

}
