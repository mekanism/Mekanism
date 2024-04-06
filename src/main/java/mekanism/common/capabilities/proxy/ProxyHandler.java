package mekanism.common.capabilities.proxy;

import mekanism.api.annotations.FieldsAreNotNullByDefault;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@FieldsAreNotNullByDefault
public class ProxyHandler {

    @Nullable
    protected final Direction side;
    @Nullable
    private final IHolder holder;
    protected final boolean readOnly;

    protected ProxyHandler(@Nullable Direction side, @Nullable IHolder holder) {
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