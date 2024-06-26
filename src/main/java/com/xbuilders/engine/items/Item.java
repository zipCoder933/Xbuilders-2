/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.engine.items;

import com.xbuilders.engine.VoxelGame;
import com.xbuilders.game.PointerHandler;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author zipCoder933
 */
public abstract class Item {

    public final ArrayList<String> tags = new ArrayList<>();
    public final HashMap<String, String> properties = new HashMap<>();

    public boolean containsTag(String tag) {
        for (String t : tags) {
            if (t.equalsIgnoreCase(tag)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsPieceOfTag(String tag) {
        for (String t : tags) {
            if (t.toLowerCase().contains(tag.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public void initialize() {
    }

    private int[] iconAtlasPosition = null;

    public final void setIconAtlasPosition(int x, int y) {
        this.iconAtlasPosition = new int[]{x, y};
    }

    /**
     * @return the iconAtlasPosition
     */
    public final int[] getIconAtlasPosition() {
        return iconAtlasPosition;
    }

    public void onWorldClose() {
    }

    public void onWorldOpen() {
    }

    public boolean saveInChunk() {
        return true;
    }

    public PointerHandler getPointerHandler() {
        return VoxelGame.ph();
    }

    public Item(int id, String name, ItemType type) {
        if (id > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Item ID Can not exceed " + Short.MAX_VALUE);
        }
        this.id = (short) id;
        this.name = name.trim();
        this.itemType = type;
    }

    public boolean isInfiniteResource() {
        return maxStackSize() == Item.INFINITE_RESOURCE_AMT;
    }

    public static byte INFINITE_RESOURCE_AMT = -1;

    public byte maxStackSize() {
        return INFINITE_RESOURCE_AMT;
    }

    /**
     * @return the time in MS for the item to break
     */
    public int breakTimeMS() {
        return 1000;
    }

    /**
     * @return If a single item should run out gradually instead of an item
     * getting used up entirely once per click
     */
    public boolean continuousUse() {
        return false;
    }

    public final short id;
    public final String name;
    public final ItemType itemType;

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Item other = (Item) obj;
        return id == other.id && itemType == other.itemType;
    }

    @Override
    public String toString() {
        return itemType.toString().toLowerCase().replace("_", " ") + " \"" + this.name + "\" (id: " + id + ")";
    }

    public void onDropEvent() {
    }

}
