package com.teatime.teatime.object;

import java.util.EnumSet;
import java.util.Set;

/**
 * Created by Kevin on 2018-04-05.
 */

public enum TeaType {
    //Fields
    NONE(0),
    WHITE (1),
    GREEN (2),
    OOLONG (4),
    BLACK (8),
    FERMENTED (16),
    ALL(31);

    private final int value;

    TeaType (int value) {
        this.value = value;
    }

    public int getValue() { return value; }

    /*Code adpaté de
     https://stackoverflow.com/questions/5346477/implementing-a-bitfield-using-java-enums
     */
    /**
     * Translates a numeric status code into a Set of TeaType enums
     * @return EnumSet representing the TeaType Flag
     */
    public static EnumSet<TeaType> getStatusFlags(int statusValue) {
        EnumSet statusFlags = EnumSet.noneOf(TeaType.class);
        for (TeaType t : TeaType.values()) {
            int flagValue = t.value;
            if ((flagValue&statusValue) == flagValue) {
                statusFlags.add(t);
            }
        }
        return statusFlags;
    }


    /**
     * Translates a set of TeaType enums into a numeric status code
     * @param Set if statusFlags
     * @return numeric representation of the TeaType Flag
     */
    public static int getStatusValue(Set<TeaType> flags) {
        int value = 0;
        for (TeaType t : flags) {
            value |= t.getValue();
        }
        return value;
    }
}

