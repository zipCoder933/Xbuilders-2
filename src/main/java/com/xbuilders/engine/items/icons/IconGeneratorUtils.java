/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.engine.items.icons;

import com.xbuilders.engine.items.BlockList;
import com.xbuilders.engine.items.ItemList;
import com.xbuilders.engine.items.block.Block;
import com.xbuilders.engine.rendering.blocks.IconMesh;
import com.xbuilders.game.items.blockType.BlockRenderType;
import com.xbuilders.game.Main;
import com.xbuilders.game.PointerHandler;

import java.io.File;

import static processing.core.PConstants.PI;

import processing.core.PGraphics;
import processing.core.PImage;
import com.xbuilders.window.ui4j.UIExtensionFrame;

/**
 * @author zipCoder933
 */
public class IconGeneratorUtils {

    PointerHandler ph;
    UIExtensionFrame frame;

    public IconGeneratorUtils(PointerHandler ph, UIExtensionFrame frame) {
        this.ph = ph;
        this.frame = frame;
    }

    final int blockSize = 86;

    public void generateBlockIcon(Block b, PGraphics pg) {
        if (!b.hasTexture())
            return;
        if (b.getRenderType() != BlockRenderType.SPRITE
                && b.getRenderType() != BlockRenderType.WALL_ITEM
                && b.getRenderType() != BlockRenderType.TORCH
                && b.getRenderType() != BlockRenderType.TRACK
                && b.getRenderType() != BlockRenderType.FLOOR
                && b.getRenderType() != BlockRenderType.WIRE) {
            if (b.getRenderType() != BlockRenderType.SUNFLOWER_HEAD) {
                IconMesh shape = makeBlockShape(b);

                pg.ortho(-frame.width / 2, frame.width / 2, -frame.height / 2, frame.height / 2, -1000, 1000);
                pg.noStroke();
                pg.translate((frame.width / 2), (frame.height / 2));
                pg.rotateX(0 - (PI / 4) * 0.5f);
                pg.rotateY(PI / 4);

                pg.translate(0 - (blockSize / 2), 0 - (blockSize / 2), 0 - (blockSize / 2));
                pg.scale(blockSize);
                shape.draw(pg);

                PImage image = pg.get();
                image.save(new File(Main.BLOCK_ICON_DIR, b.id + ".png").getAbsolutePath());
            }
        }
    }

    private final Block opaqueBlock = new Block(1, "opaque", true, true);

    private IconMesh makeBlockShape(Block b) {
        IconMesh shape = new IconMesh(this.frame);
        shape.beginShape();
        if (b.getRenderType() == BlockRenderType.FENCE) {
            ItemList.blocks.getBlockType(b.type).constructBlock(shape, b, null,
                    opaqueBlock,
                    opaqueBlock,
                    BlockList.BLOCK_AIR,
                    BlockList.BLOCK_AIR,
                    BlockList.BLOCK_AIR,
                    BlockList.BLOCK_AIR, 0, 0, 0);
        } else {
            ItemList.blocks.getBlockType(b.type).constructBlock(shape, b, null,
                    BlockList.BLOCK_AIR, BlockList.BLOCK_AIR,
                    BlockList.BLOCK_AIR, BlockList.BLOCK_AIR,
                    BlockList.BLOCK_AIR, BlockList.BLOCK_AIR, 0, 0, 0);
        }

        shape.endShape();
        return shape;
    }

    public static void deleteAllExistingIcons() {
        for (File file : Main.BLOCK_ICON_DIR.listFiles()) {
            if (file.getName().endsWith(".png")) {
                file.delete();
            }
        }
    }
}
