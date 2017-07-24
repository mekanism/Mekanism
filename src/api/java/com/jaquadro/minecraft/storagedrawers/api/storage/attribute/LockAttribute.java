package com.jaquadro.minecraft.storagedrawers.api.storage.attribute;

import java.util.EnumSet;

public enum LockAttribute
{
    LOCK_POPULATED,
    LOCK_EMPTY;

    public int getFlagValue () {
        return 1 << ordinal();
    }

    public static int getBitfield (EnumSet<LockAttribute> attributes) {
        int value = 0;
        if (attributes == null)
            return value;

        for (LockAttribute attr : attributes)
            value |= attr.getFlagValue();

        return value;
    }

    public static EnumSet<LockAttribute> getEnumSet (int bitfield) {
        if (bitfield == 0)
            return null;

        EnumSet<LockAttribute> set = EnumSet.noneOf(LockAttribute.class);
        for (LockAttribute attr : values()) {
            if ((bitfield & attr.getFlagValue()) != 0)
                set.add(attr);
        }

        return set;
    }
}
