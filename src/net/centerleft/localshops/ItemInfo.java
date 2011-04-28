package net.centerleft.localshops;

import java.util.Arrays;

import org.bukkit.inventory.ItemStack;

public class ItemInfo {
    public String name = null;
    public String[][] search = null;
    public int typeId = -1;
    public short subTypeId = 0;
    
    public ItemInfo(String name, String[][] search, int typeId, short subTypeId) {
        this.name = name;
        this.search = search;
        this.typeId = typeId;
        this.subTypeId = subTypeId;
    }
    
    public String toString() {
        return String.format("%s, %s [%d,%d]", name, Arrays.deepToString(search), typeId, subTypeId);
    }
    
    public ItemStack toStack() {
        return new ItemStack (typeId, 1, subTypeId);
    }
}