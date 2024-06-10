/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.window;

import com.jogamp.opengl.GL4;
import org.joml.Matrix4f;

import java.nio.FloatBuffer;

/**
 * @author zipCoder933
 */
public class MVP {

    FloatBuffer buffer;
    public final Matrix4f mvp; //final just means the object cannot be reassigned
    private final GL4 gl;
    /*
    TODO: Think About loading projection and view into the constructor
        - we will no longer be putting projection and view matricies into draw mehtods
        - We can still load mvps as static final variables because view and projection are also static
    final Matrix4f projection = new Matrix4f();
    final Matrix4f view = new Matrix4f();
     */

    public MVP(GL4 gl) {
        this.gl = gl;
        buffer = BufferUtils.allocateDirectFloatBuffer(16);
        mvp = new Matrix4f();
    }

    public void update(final Matrix4f projection, final Matrix4f view, final Matrix4f model) {
        mvp.identity().mul(projection).mul(view).mul(model);
        mvp.get(buffer);
    }

    public void update(){
        mvp.get(buffer);
    }

    public void update(Matrix4f matrix){
        mvp.set(matrix);
        mvp.get(buffer);
    }

    public void update(final Matrix4f... matrices) {
        mvp.identity();
        for (int i = 0; i < matrices.length; i++) {
            mvp.mul(matrices[i]);
        }
        mvp.get(buffer);
    }

    public void sendToShader(int programID, int uniformID) {
        gl.glUseProgram(programID);
        gl.glUniformMatrix4fv(uniformID, 1, false, buffer);
    }
}