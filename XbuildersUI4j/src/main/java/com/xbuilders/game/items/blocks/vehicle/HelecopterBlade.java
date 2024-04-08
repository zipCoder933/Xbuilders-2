package com.xbuilders.game.items.blocks.vehicle;

import com.xbuilders.engine.items.entity.Entity;
import com.xbuilders.engine.rendering.ShaderHandler;
import com.xbuilders.engine.utils.math.MathUtils;
import com.xbuilders.game.Main;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class HelecopterBlade {
    public final Vector3f position;
    public final Matrix4f matrix;
    boolean clockwise;

    public HelecopterBlade(Vector3f position, boolean clockwise) {
        this.position = position;
        this.clockwise = clockwise;
        this.matrix = new Matrix4f();
    }

    float targeRotation, actualRotation;

    public void render(boolean spin, float rotation) {
        Main.getPG().pushMatrix();
        float x = matrix.m30();
        float y = matrix.m31();
        float z = matrix.m32();
//        matrix.translate(position.x, position.y, position.z);

//        VoxelGame.getShaderHandler().setModelMatrix(matrix);
//        Main.getPG().shader(VoxelGame.getShaderHandler().blockShader);

        if (spin) {
            if (clockwise) targeRotation += 1;
            else targeRotation -= 1;
        }
        actualRotation = (float) MathUtils.curve(actualRotation, targeRotation, 0.02f);


        Main.getPG().translate(x, y, z);
        Main.getPG().rotateY(actualRotation + rotation);
        Main.getPG().box(0.4f, 0.2f, 17);
        Main.getPG().popMatrix();
    }

    public static void startDrawingBlades() {
        Main.getPG().resetShader();
        Main.getPG().noStroke();
        Main.getPG().fill(20, 20, 20);
        Main.getPG().noStroke();
    }

    public static void stopDrawingBlades() {
        Entity.defaultShader();
    }
}
