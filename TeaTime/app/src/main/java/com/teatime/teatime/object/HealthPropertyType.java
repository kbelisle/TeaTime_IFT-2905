package com.teatime.teatime.object;

import java.util.EnumSet;
import java.util.Set;

/**
 * Created by Kevin on 2018-04-05.
 */

public enum HealthPropertyType {
    NONE (0),
    CAFFEINE (1),
    ANTI_INFLAMMATORY (2),
    ALL(3);

    private final int value;

    HealthPropertyType (int value) {
        this.value = value;
    }

    public int getValue() { return value; }

    /*Code adpaté de
     https://stackoverflow.com/questions/5346477/implementing-a-bitfield-using-java-enums
     */
    /**
     * Translates a numeric status code into a Set of HealthPropertyType enums
     * @param numeric statusValue
     * @return EnumSet representing the HealthPropertyType Flag
     */
    public static EnumSet<HealthPropertyType> getStatusFlags(int statusValue) {
        EnumSet statusFlags = EnumSet.noneOf(HealthPropertyType.class);
        for (HealthPropertyType h : HealthPropertyType.values()) {
            int flagValue = h.value;
            if ((flagValue&statusValue) == flagValue) {
                statusFlags.add(h);
            }
        }
        return statusFlags;
    }


    /**
     * Translates a set of GoodWithType enums into a numeric status code
     * @param Set if statusFlags
     * @return numeric representation of the goodWith Flag
     */
    public static int getStatusValue(Set<HealthPropertyType> flags) {
        int value = 0;
        for (HealthPropertyType h : flags) {
            value |= h.getValue();
        }
        return value;
    }
}
