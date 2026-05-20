package mekanism.common.inventory.container.sync;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.network.to_client.container.property.LongPropertyData;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.resource.ResourceStackPropertyData;
import net.minecraft.core.RegistryAccess;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling large resource stacks
 */
@NothingNullByDefault
public final class SyncableLargeResourceStack<RESOURCE extends Resource> implements ISyncableData {

    public static <RESOURCE extends Resource> SyncableLargeResourceStack<RESOURCE> create(IResourceContainer<RESOURCE> handler) {
        //Note: We need to use unchecked setters on the client is that if a recipe got removed so there is a substance
        // in a tank that was valid but no longer is valid, we want to ensure that the client is able to properly render
        // it instead of printing an error due to the client thinking that it is invalid
        //Note: We initialize the empty stack, so that we don't have to worry about having types we don't know how to handle
        return new SyncableLargeResourceStack<>(handler::asStack, handler::setContentsUnchecked, handler.stackHelper().empty());
    }

    @Internal
    public static <RESOURCE extends Resource> SyncableLargeResourceStack<RESOURCE> forSyncableProperty(Supplier<Object> getter,
          Consumer<Object> setter, Object emptyStack) {
        return new SyncableLargeResourceStack<>(() -> (LargeResourceStack<RESOURCE>) getter.get(), setter::accept, (LargeResourceStack<RESOURCE>) emptyStack);
    }

    private final Supplier<LargeResourceStack<RESOURCE>> getter;
    private final Consumer<LargeResourceStack<RESOURCE>> setter;
    private final LargeResourceStack<RESOURCE> emptyStack;
    private LargeResourceStack<RESOURCE> lastKnownValue;

    private SyncableLargeResourceStack(Supplier<LargeResourceStack<RESOURCE>> getter, Consumer<LargeResourceStack<RESOURCE>> setter, LargeResourceStack<RESOURCE> emptyStack) {
        this.getter = getter;
        this.setter = setter;
        this.emptyStack = emptyStack;
        this.lastKnownValue = this.emptyStack;
    }

    private LargeResourceStack<RESOURCE> get() {
        return getter.get();
    }

    public void set(LargeResourceStack<RESOURCE> stack) {
        setter.accept(stack);
    }

    public void set(long amount) {
        LargeResourceStack<RESOURCE> stack = get();
        if (!stack.isEmpty()) {
            //Double check it is not empty
            if (amount == 0) {
                set(this.emptyStack);
            } else {
                set(new LargeResourceStack<>(stack.resource(), amount));
            }
        }
    }

    @Override
    public DirtyType isDirty() {
        LargeResourceStack<RESOURCE> value = get();
        boolean sameType = value.resource().equals(this.lastKnownValue.resource());
        if (!sameType || value.amount() != this.lastKnownValue.amount()) {
            this.lastKnownValue = value;
            return sameType ? DirtyType.SIZE : DirtyType.DIRTY;
        }
        return DirtyType.CLEAN;
    }

    @Override
    public PropertyData getPropertyData(RegistryAccess registryAccess, short property, DirtyType dirtyType) {
        LargeResourceStack<RESOURCE> stack = get();
        if (dirtyType == DirtyType.SIZE) {
            //If only the size changed, don't bother re-syncing the type
            return new LongPropertyData(property, stack.amount());
        }
        return new ResourceStackPropertyData(property, stack);
    }
}