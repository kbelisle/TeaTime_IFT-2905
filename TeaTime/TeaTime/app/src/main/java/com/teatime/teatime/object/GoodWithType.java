package com.teatime.teatime.object;

import java.util.EnumSet;
import java.util.Set;

/**
 * Created by Kevin on 2018-04-05.
 */

public enum GoodWithType {
    NONE (0),
    SUGAR (1),
    MILK (2),
    OTHER (4),
    ALL (7);

    private final int value;

    GoodWithType (int value) {
        this.value = value;
    }

    public int getValue() { return value; }

    /*Code adpaté de
     https://stackoverflow.com/questions/5346477/implementing-a-bitfield-using-java-enums
     */
    /**
     * Translates a numeric status code into a Set of GoodWithType enums
     * @param numeric statusValue
     * @return EnumSet representing the goodWith Flag
     */
    public static EnumSet<GoodWithType> getStatusFlags(int statusValue) {
        EnumSet statusFlags = EnumSet.noneOf(GoodWithType.class);
        for (GoodWithType g : GoodWithType.values()) {
            int flagValue = g.value;
            if ((flagValue&statusValue) == flagValue) {
                statusFlags.add(g);
            }
        }
        return statusFlags;
    }


    /**
     * Translates a set of GoodWithType enums into a numeric status code
     * @param Set if statusFlags
     * @return numeric representation of the goodWith Flag
     */
    public static int getStatusValue(Set<GoodWithType> flags) {
        int value = 0;
        for (GoodWithType g : flags) {
            value |= g.getValue();
        }
        return value;
    }
}
