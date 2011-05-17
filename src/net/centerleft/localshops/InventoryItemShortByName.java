package net.centerleft.localshops;

import java.util.Comparator;

public class InventoryItemShortByName implements Comparator<InventoryItem> {

    @Override
    public int compare(InventoryItem o1, InventoryItem o2) {
        return o1.getInfo().name.compareTo(o2.getInfo().name);
    }
    
}
