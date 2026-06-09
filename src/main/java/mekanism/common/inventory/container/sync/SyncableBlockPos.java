package mekanism.common.inventory.container.sync;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.network.to_client.container.property.BlockPosPropertyData;
import mekanism.common.network.to_client.container.property.PropertyData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import org.jspecify.annotations.Nullable;

public class SyncableBlockPos implements ISyncableData {

    public static SyncableBlockPos create(Supplier<@Nullable BlockPos> getter, Consumer<@Nullable BlockPos> setter) {
        return new SyncableBlockPos(getter, setter);
    }

    private final Supplier<@Nullable BlockPos> getter;
    private final Consumer<@Nullable BlockPos> setter;
    private int lastKnownHashCode;

    private SyncableBlockPos(Supplier<@Nullable BlockPos> getter, Consumer<@Nullable BlockPos> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    @Nullable
    public BlockPos get() {
        return getter.get();
    }

    public void set(@Nullable BlockPos value) {
        setter.accept(value);
    }

    @Override
    public DirtyType isDirty() {
        BlockPos value = get();
        int valueHashCode = value == null ? 0 : value.hashCode();
        if (lastKnownHashCode == valueHashCode) {
            return DirtyType.CLEAN;
        }
        lastKnownHashCode = valueHashCode;
        return DirtyType.DIRTY;
    }

    @Override
    public PropertyData getPropertyData(RegistryAccess registryAccess, short property, DirtyType dirtyType) {
        return new BlockPosPropertyData(property, get());
    }
}