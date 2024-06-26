/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.game.items.entities.animals;

import com.xbuilders.engine.items.entity.EntityLink;
import com.xbuilders.engine.rendering.ShaderHandler;
import com.xbuilders.engine.rendering.entity.glEntityMesh;
import com.xbuilders.engine.utils.ResourceUtils;
import com.xbuilders.game.Main;
import com.xbuilders.game.items.entities.mobile.LandAnimal;

import org.joml.Vector3f;
import processing.opengl.PJOGL;

import java.io.IOException;

/**
 * @author zipCoder933
 */
public class FoxEntityLink extends EntityLink {

    public FoxEntityLink(int id, String name, String texture) {
        super(id, name);
        setIconAtlasPosition(2, 3);
        setSupplier(() -> new Fox());
        this.texturePath = texture;
        tags.add("animal");
        tags.add("fox");
    }

    glEntityMesh body;
    @Override
    public void initialize() {

        if (body == null) {
            PJOGL pgl = Main.beginPJOGL();

            body = new glEntityMesh(Main.getFrame(), pgl, ShaderHandler.entityShader);
            try {
                body.loadFromOBJ(ResourceUtils.resource("items\\entities\\animals\\fox\\fox.obj"));
                body.setTexture(
                        ResourceUtils.resource("items\\entities\\animals\\fox\\" + texturePath));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Main.endPJOGL();
        }
    }



    public String texturePath;

    class Fox extends LandAnimal {

        public Fox() {
            super(new Vector3f(0.8f, 0.9f, 0.8f), 1);
            setMaxSpeed(0.17f);
            setActivity(0.8f);
            // setFreezeMode(true);
            // showAABBCollisionBoxes(true);
        }

        @Override
        public void renderAnimal() {
            body.updateModelMatrix(modelMatrix);
            body.draw();
        }

        @Override
        public void initAnimal(byte[] bytes) {
        }

        @Override
        public byte[] animalToBytes() {
            return null;
        }
    }

}
