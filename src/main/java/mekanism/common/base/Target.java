package mekanism.common.base;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.util.EnumFacing;

public abstract class Target<HANDLER, TYPE> {
    protected final Map<EnumFacing, HANDLER> wrappers = new EnumMap<>(EnumFacing.class);
    protected final Map<EnumFacing, TYPE> needed = new EnumMap<>(EnumFacing.class);
    protected final Map<EnumFacing, TYPE> given = new EnumMap<>(EnumFacing.class);

    public boolean hasAcceptors() {
        return !wrappers.isEmpty();
    }

    public void addSide(EnumFacing side, HANDLER acceptor) {
        wrappers.put(side, acceptor);
    }

    public Map<EnumFacing, HANDLER> getWrappers() {
        return wrappers;
    }

    public void addAmount(EnumFacing side, TYPE amountNeeded, boolean canGive) {
        if (canGive) {
            given.put(side, amountNeeded);
        } else {
            needed.put(side, amountNeeded);
        }
    }

    public abstract TYPE sendGivenWithDefault(TYPE amountPer);

    public void addGiven(EnumFacing side, TYPE amountNeeded) {
        given.put(side, amountNeeded);
    }

    public Iterator<Entry<EnumFacing, TYPE>> getNeededIterator() {
        return needed.entrySet().iterator();
    }

    public boolean noneNeeded() {
        return needed.isEmpty();
    }
}