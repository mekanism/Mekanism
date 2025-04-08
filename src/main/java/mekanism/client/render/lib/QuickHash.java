package mekanism.client.render.lib;

import java.util.Arrays;

public record QuickHash(Object... objs) {

    @Override
    public int hashCode() {
        //TODO: Cache the hashcode?
        return Arrays.deepHashCode(objs);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof QuickHash(Object[] otherObjs) && Arrays.deepEquals(objs, otherObjs);
    }
}