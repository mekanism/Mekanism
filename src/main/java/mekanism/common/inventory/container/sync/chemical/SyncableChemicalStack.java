package mekanism.common.inventory.container.sync.chemical;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.network.to_client.container.property.LongPropertyData;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.chemical.ChemicalStackPropertyData;
import net.minecraft.core.RegistryAccess;
import org.jetbrains.annotations.NotNull;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling chemical stacks
 */
public final class SyncableChemicalStack implements ISyncableData {

    public static SyncableChemicalStack create(IChemicalTank handler) {
        return create(handler, false);
    }

    public static SyncableChemicalStack create(IChemicalTank handler, boolean isClient) {
        //Note: While strictly speaking the server should never end up having the setter called, because we have side
        // information readily available here we use the checked setter on the server side just to be safe. The reason
        // that we need to use unchecked setters on the client is that if a recipe got removed so there is a substance
        // in a tank that was valid but no longer is valid, we want to ensure that the client is able to properly render
        // it instead of printing an error due to the client thinking that it is invalid
        return create(handler::getStack, isClient ? handler::setStackUnchecked : handler::setStack);
    }

    public static SyncableChemicalStack create(Supplier<@NotNull ChemicalStack> getter, Consumer<@NotNull ChemicalStack> setter) {
        return new SyncableChemicalStack(getter, setter);
    }

    @NotNull
    private ChemicalStack lastKnownValue;
    private final Supplier<ChemicalStack> getter;
    private final Consumer<ChemicalStack> setter;

    public SyncableChemicalStack(Supplier<ChemicalStack> getter, Consumer<ChemicalStack> setter) {
        this.getter = getter;
        this.setter = setter;
        lastKnownValue = ChemicalStack.EMPTY;
    }

    public ChemicalStack get() {
        return getter.get();
    }

    public void set(ChemicalStack value) {
        setter.accept(value);
    }

    public void set(long amount) {
        ChemicalStack stack = get();
        if (!stack.isEmpty()) {
            //Double check it is not empty
            set(stack.copyWithAmount(amount));
        }
    }

    @Override
    public DirtyType isDirty() {
        ChemicalStack value = get();
        boolean sameType = ChemicalStack.isSameChemical(value, this.lastKnownValue);
        if (!sameType || value.getAmount() != this.lastKnownValue.getAmount()) {
            //Make sure to copy it in case our infusion stack object is the same object so would be getting modified
            // only do so though if it is dirty, as we don't need to spam object creation
            this.lastKnownValue = value.copy();
            return sameType ? DirtyType.SIZE : DirtyType.DIRTY;
        }
        return DirtyType.CLEAN;
    }

    @Override
    public PropertyData getPropertyData(RegistryAccess registryAccess, short property, DirtyType dirtyType) {
        if (dirtyType == DirtyType.SIZE) {
            //If only the size changed, don't bother re-syncing the type
            return new LongPropertyData(property, get().getAmount());
        }
        //Note: While this copy operation isn't strictly necessary, it allows for simplifying the logic and ensuring we don't have the actual stack object
        // leak from one side to another when in single player. Given copying is rather cheap, and we only need to do this on change/when the data is dirty
        // we can easily get away with it
        return new ChemicalStackPropertyData(property, get().copy());
    }
}