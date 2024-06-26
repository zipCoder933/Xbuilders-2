/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.game.items.blockType;

import com.xbuilders.engine.items.block.Block;
import com.xbuilders.engine.items.block.construction.blockTypes.BlockType;
import com.xbuilders.engine.player.UserControlledPlayer;
import com.xbuilders.engine.player.raycasting.Ray;
import com.xbuilders.engine.rendering.blocks.BlockMesh_Base;
import com.xbuilders.engine.utils.math.AABB;
import com.xbuilders.engine.world.blockData.BlockData;
import com.xbuilders.engine.world.blockData.BlockOrientation;
import processing.core.PVector;

import java.util.function.Consumer;

/**
 *
 * @author zipCoder933
 */
public class FloorItemRenderer extends BlockType {

    @Override
    public BlockData getInitialBlockData(UserControlledPlayer player, Block block, Ray ray) {
        return player.cameraBlockOrientation();
    }

    //<editor-fold defaultstate="collapsed" desc="Draw floor1">
//NOTES:
//The UV map for this block only exists on the top face.
//<editor-fold defaultstate="collapsed" desc="Verticies">
    static PVector[] verts_floor1 = {
        new PVector(-0.0f, 0.93750006f, 0.9999989f), //0
        new PVector(0.0f, 0.93750006f, -0.0f), //1
        new PVector(1.0f, 0.93750006f, 1.0f), //2
        new PVector(1.0f, 0.93750006f, 0.0f), //3
    };
    static PVector[] uv_floor1 = {
        new PVector(0.0f, 1.0f), //0
        new PVector(1.0f, 0.0f), //1
        new PVector(1.0f, 1.0f), //2
        new PVector(0.0f, 0.0f), //3
    };

//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="Face methods">
    private static void make_floor1_center_faces(PVector[] verts2, PVector[] uv2, Block block, BlockMesh_Base shape, int x, int y, int z) {
        int[] pos = block.texture.TOP;

        shape.vertex(verts2[3].x + x, verts2[3].y + y, verts2[3].z + z, getUVTextureCoord_X(pos, uv2[2].x), getUVTextureCoord_Y(pos, uv2[2].y));
        shape.vertex(verts2[1].x + x, verts2[1].y + y, verts2[1].z + z, getUVTextureCoord_X(pos, uv2[1].x), getUVTextureCoord_Y(pos, uv2[1].y));
        shape.vertex(verts2[2].x + x, verts2[2].y + y, verts2[2].z + z, getUVTextureCoord_X(pos, uv2[0].x), getUVTextureCoord_Y(pos, uv2[0].y));//FACE

        shape.vertex(verts2[1].x + x, verts2[1].y + y, verts2[1].z + z, getUVTextureCoord_X(pos, uv2[1].x), getUVTextureCoord_Y(pos, uv2[1].y));
        shape.vertex(verts2[0].x + x, verts2[0].y + y, verts2[0].z + z, getUVTextureCoord_X(pos, uv2[3].x), getUVTextureCoord_Y(pos, uv2[3].y));
        shape.vertex(verts2[2].x + x, verts2[2].y + y, verts2[2].z + z, getUVTextureCoord_X(pos, uv2[0].x), getUVTextureCoord_Y(pos, uv2[0].y));//FACE
    }

//</editor-fold>
//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Draw floor2">
    //NOTES:
    //The UV map for this block only exists on the top face.
    //<editor-fold defaultstate="collapsed" desc="Verticies">
    static PVector[] verts_floor2 = {
        new PVector(1.0E-6f, 0.93750006f, -1.0E-6f), //0
        new PVector(1.0f, 0.93750006f, 0.0f), //1
        new PVector(-0.0f, 0.93750006f, 1.0f), //2
        new PVector(1.0f, 0.93750006f, 1.0f), //3
    };
    static PVector[] uv_floor2 = {
        new PVector(0.0f, 1.0f), //0
        new PVector(1.0f, 0.0f), //1
        new PVector(1.0f, 1.0f), //2
        new PVector(0.0f, 0.0f), //3
    };

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Face methods">
    private static void make_floor2_center_faces(PVector[] verts2, PVector[] uv2, Block block, BlockMesh_Base shape, int x, int y, int z) {
        int[] pos = block.texture.TOP;

        shape.vertex(verts2[3].x + x, verts2[3].y + y, verts2[3].z + z, getUVTextureCoord_X(pos, uv2[2].x), getUVTextureCoord_Y(pos, uv2[2].y));
        shape.vertex(verts2[1].x + x, verts2[1].y + y, verts2[1].z + z, getUVTextureCoord_X(pos, uv2[1].x), getUVTextureCoord_Y(pos, uv2[1].y));
        shape.vertex(verts2[2].x + x, verts2[2].y + y, verts2[2].z + z, getUVTextureCoord_X(pos, uv2[0].x), getUVTextureCoord_Y(pos, uv2[0].y));//FACE

        shape.vertex(verts2[1].x + x, verts2[1].y + y, verts2[1].z + z, getUVTextureCoord_X(pos, uv2[1].x), getUVTextureCoord_Y(pos, uv2[1].y));
        shape.vertex(verts2[0].x + x, verts2[0].y + y, verts2[0].z + z, getUVTextureCoord_X(pos, uv2[3].x), getUVTextureCoord_Y(pos, uv2[3].y));
        shape.vertex(verts2[2].x + x, verts2[2].y + y, verts2[2].z + z, getUVTextureCoord_X(pos, uv2[0].x), getUVTextureCoord_Y(pos, uv2[0].y));//FACE
    }

    //</editor-fold>
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Draw floor3">
    //NOTES:
    //The UV map for this block only exists on the top face.
    //<editor-fold defaultstate="collapsed" desc="Verticies">
    static PVector[] verts_floor3 = {
        new PVector(1.0f, 0.93750006f, 1.0E-6f), //0
        new PVector(0.9999989f, 0.93750006f, 1.0f), //1
        new PVector(0.0f, 0.93750006f, -0.0f), //2
        new PVector(-1.0E-6f, 0.93750006f, 0.9999989f), //3
    };
    static PVector[] uv_floor3 = {
        new PVector(0.0f, 1.0f), //0
        new PVector(1.0f, 0.0f), //1
        new PVector(1.0f, 1.0f), //2
        new PVector(0.0f, 0.0f), //3
    };

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Face methods">
    private static void make_floor3_center_faces(PVector[] verts2, PVector[] uv2, Block block, BlockMesh_Base shape, int x, int y, int z) {
        int[] pos = block.texture.TOP;

        shape.vertex(verts2[3].x + x, verts2[3].y + y, verts2[3].z + z, getUVTextureCoord_X(pos, uv2[2].x), getUVTextureCoord_Y(pos, uv2[2].y));
        shape.vertex(verts2[1].x + x, verts2[1].y + y, verts2[1].z + z, getUVTextureCoord_X(pos, uv2[1].x), getUVTextureCoord_Y(pos, uv2[1].y));
        shape.vertex(verts2[2].x + x, verts2[2].y + y, verts2[2].z + z, getUVTextureCoord_X(pos, uv2[0].x), getUVTextureCoord_Y(pos, uv2[0].y));//FACE

        shape.vertex(verts2[1].x + x, verts2[1].y + y, verts2[1].z + z, getUVTextureCoord_X(pos, uv2[1].x), getUVTextureCoord_Y(pos, uv2[1].y));
        shape.vertex(verts2[0].x + x, verts2[0].y + y, verts2[0].z + z, getUVTextureCoord_X(pos, uv2[3].x), getUVTextureCoord_Y(pos, uv2[3].y));
        shape.vertex(verts2[2].x + x, verts2[2].y + y, verts2[2].z + z, getUVTextureCoord_X(pos, uv2[0].x), getUVTextureCoord_Y(pos, uv2[0].y));//FACE
    }

    //</editor-fold>
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Draw floor4">
    //NOTES:
    //The UV map for this block only exists on the top face.
    //<editor-fold defaultstate="collapsed" desc="Verticies">
    static PVector[] verts_floor4 = {
        new PVector(0.9999989f, 0.93750006f, 1.000001f), //0
        new PVector(-1.0E-6f, 0.93750006f, 0.9999989f), //1
        new PVector(1.000001f, 0.93750006f, 1.0E-6f), //2
        new PVector(1.0E-6f, 0.93750006f, -1.0E-6f), //3
    };
    static PVector[] uv_floor4 = {
        new PVector(0.0f, 1.0f), //0
        new PVector(1.0f, 0.0f), //1
        new PVector(1.0f, 1.0f), //2
        new PVector(0.0f, 0.0f), //3
    };

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Face methods">
    private static void make_floor4_center_faces(PVector[] verts2, PVector[] uv2, Block block, BlockMesh_Base shape, int x, int y, int z) {
        int[] pos = block.texture.TOP;

        shape.vertex(verts2[3].x + x, verts2[3].y + y, verts2[3].z + z, getUVTextureCoord_X(pos, uv2[2].x), getUVTextureCoord_Y(pos, uv2[2].y));
        shape.vertex(verts2[1].x + x, verts2[1].y + y, verts2[1].z + z, getUVTextureCoord_X(pos, uv2[1].x), getUVTextureCoord_Y(pos, uv2[1].y));
        shape.vertex(verts2[2].x + x, verts2[2].y + y, verts2[2].z + z, getUVTextureCoord_X(pos, uv2[0].x), getUVTextureCoord_Y(pos, uv2[0].y));//FACE

        shape.vertex(verts2[1].x + x, verts2[1].y + y, verts2[1].z + z, getUVTextureCoord_X(pos, uv2[1].x), getUVTextureCoord_Y(pos, uv2[1].y));
        shape.vertex(verts2[0].x + x, verts2[0].y + y, verts2[0].z + z, getUVTextureCoord_X(pos, uv2[3].x), getUVTextureCoord_Y(pos, uv2[3].y));
        shape.vertex(verts2[2].x + x, verts2[2].y + y, verts2[2].z + z, getUVTextureCoord_X(pos, uv2[0].x), getUVTextureCoord_Y(pos, uv2[0].y));//FACE
    }

    //</editor-fold>
    //</editor-fold>
    @Override
    public void constructBlock(BlockMesh_Base buffers, Block block, BlockData data, Block negativeX, Block positiveX, Block negativeY, Block positiveY, Block negativeZ, Block positiveZ, int x, int y, int z) {
        BlockOrientation orientation = BlockOrientation.getBlockOrientation(data);

        if (orientation == null || orientation.getXZ() == 3) {
            make_floor1_center_faces(verts_floor1, uv_floor1, block, buffers, x, y, z);
        } else if (orientation.getXZ() == 0) {
            make_floor2_center_faces(verts_floor2, uv_floor2, block, buffers, x, y, z);
        } else if (orientation.getXZ() == 1) {
            make_floor3_center_faces(verts_floor3, uv_floor3, block, buffers, x, y, z);
        } else {
            make_floor4_center_faces(verts_floor4, uv_floor4, block, buffers, x, y, z);
        }
    }

    private final float sixteenthConstant = 0.0625f;

    @Override
    public void getCollisionBoxes(Consumer<AABB> consumer, AABB box, Block block, BlockData data, int x, int y, int z) {
        box.setPosAndSize(x, y + (sixteenthConstant * 15), z, 1, sixteenthConstant * 1, 1);
        consumer.accept(box);
    }

    @Override
    public void getCursorBoxes(Consumer<AABB> consumer, AABB box, Block block, BlockData data, int x, int y, int z) {
        box.setPosAndSize(x, y + (sixteenthConstant * 12), z, 1, sixteenthConstant * 3, 1);
        consumer.accept(box);
    }

}
