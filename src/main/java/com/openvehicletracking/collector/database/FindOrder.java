package com.openvehicletracking.collector.database;

/**
 * Created oksuz yo on 23/07/2017.
 *
 */
public enum FindOrder {

    ASC(1), DESC(-1);

    private final int order;

    FindOrder(int order) {
        this.order = order;
    }

    public int getValue() {
        return order;
    }
}
