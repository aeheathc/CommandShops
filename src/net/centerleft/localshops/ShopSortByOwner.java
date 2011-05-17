package net.centerleft.localshops;

import java.util.Comparator;

public class ShopSortByOwner implements Comparator<Shop> {

    @Override
    public int compare(Shop o1, Shop o2) {
        return o1.getOwner().compareTo(o2.getOwner());
    }

}