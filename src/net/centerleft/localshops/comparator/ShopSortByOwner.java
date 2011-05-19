package net.centerleft.localshops.comparator;

import java.util.Comparator;

import net.centerleft.localshops.Shop;

public class ShopSortByOwner implements Comparator<Shop> {

    @Override
    public int compare(Shop o1, Shop o2) {
        return o1.getOwner().compareTo(o2.getOwner());
    }

}