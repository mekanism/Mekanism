package mekanism.common.inventory.container.sync;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.network.to_client.container.property.FluidStackPropertyData;
import mekanism.common.network.to_client.container.property.IntPropertyData;
import mekanism.common.network.to_client.container.property.PropertyData;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling fluid stacks
 */
public class SyncableFluidStack implements ISyncableData {

    public static SyncableFluidStack create(@NotNull IExtendedFluidTank handler) {
        return create(handler, false);
    }

    public static SyncableFluidStack create(IExtendedFluidTank handler, boolean isClient) {
        //Note: While strictly speaking the server should never end up having the setter called, because we have side
        // information readily available here we use the checked setter on the server side just to be safe. The reason
        // that we need to use unchecked setters on the client is that if a recipe got removed so there is a substance
        // in a tank that was valid but no longer is valid, we want to ensure that the client is able to properly render
        // it instead of printing an error due to the client thinking that it is invalid
        return create(handler::getFluid, isClient ? handler::setStackUnchecked : handler::setStack);
    }

    public static SyncableFluidStack create(Supplier<@NotNull FluidStack> getter, Consumer<@NotNull FluidStack> setter) {
        return new SyncableFluidStack(getter, setter);
    }

    @NotNull
    private FluidStack lastKnownValue = FluidStack.EMPTY;
    private final Supplier<@NotNull FluidStack> getter;
    private final Consumer<@NotNull FluidStack> setter;

    private SyncableFluidStack(Supplier<@NotNull FluidStack> getter, Consumer<@NotNull FluidStack> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    @NotNull
    public FluidStack get() {
        return getter.get();
    }

    public void set(@NotNull FluidStack value) {
        setter.accept(value);
    }

    public void set(int amount) {
        FluidStack fluid = get();
        if (!fluid.isEmpty()) {
            //Double check it is not empty
            set(new FluidStack(fluid.getFluid(), amount));
        }
    }

    @Override
    public DirtyType isDirty() {
        FluidStack value = get();
        boolean sameFluid = value.isFluidEqual(this.lastKnownValue);
        if (!sameFluid || value.getAmount() != this.lastKnownValue.getAmount()) {
            //Make sure to copy it in case our fluid stack object is the same object so would be getting modified
            // only do so though if it is dirty, as we don't need to spam object creation
            this.lastKnownValue = value.copy();
            return sameFluid ? DirtyType.SIZE : DirtyType.DIRTY;
        }
        return DirtyType.CLEAN;
    }

    @Override
    public PropertyData getPropertyData(short property, DirtyType dirtyType) {
        if (dirtyType == DirtyType.SIZE) {
            //If only the size changed, don't bother re-syncing the type
            return new IntPropertyData(property, get().getAmount());
        }
        return new FluidStackPropertyData(property, get());
    }
}