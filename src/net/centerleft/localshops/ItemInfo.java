package net.centerleft.localshops;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.inventory.ItemStack;

public class ItemInfo {
    public String name;
    public ArrayList<String[]> search;
    public int typeId;
    public short subTypeId;
    
    public ItemInfo(String name, ArrayList<String[]> search, int typeId, short subTypeId) {
        this.name = name;
        this.search = search;
        this.typeId = typeId;
        this.subTypeId = subTypeId;
    }
    
    public String toString() {
        return String.format("%s, %s [%d,%d]", name, Arrays.deepToString(search.toArray()), typeId, subTypeId);
    }
    
    public ItemStack toStack() {
        return new ItemStack (typeId, 1, subTypeId);
    }
}