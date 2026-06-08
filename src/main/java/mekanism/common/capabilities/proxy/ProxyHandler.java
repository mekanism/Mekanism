package mekanism.common.capabilities.proxy;

import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public class ProxyHandler<HOLDER extends @Nullable IHolder> {

    @Nullable
    protected final Direction side;
    protected final HOLDER holder;
    protected final boolean readOnly;

    protected ProxyHandler(@Nullable Direction side, HOLDER holder) {
        this.side = side;
        this.holder = holder;
        this.readOnly = this.side == null;
    }

    protected boolean readOnlyInsert() {
        return readOnly || holder != null && !holder.canInsert(side);
    }

    protected boolean readOnlyExtract() {
        return readOnly || holder != null && !holder.canExtract(side);
    }
}