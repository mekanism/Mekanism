package mekanism.common.capabilities;

import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.resource.IResourceContainer;
import mekanism.common.attachments.containers.ResourceContainersBuilder;
import net.neoforged.neoforge.common.util.TriPredicate;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class GenericTankSpec<RESOURCE extends Resource> {

    protected final Predicate<RESOURCE> isValid;
    protected final BiPredicate<RESOURCE, AutomationType> canExtract;
    protected final TriPredicate<RESOURCE, AutomationType, ItemAccess> canInsert;
    private final Predicate<ItemResource> supportsStack;
    private final IntSupplier rate;
    private final LongSupplier capacity;
    @Nullable
    private final ToLongFunction<ItemAccess> itemBasedCapacity;

    protected GenericTankSpec(IntSupplier rate, LongSupplier capacity, BiPredicate<RESOURCE, AutomationType> canExtract,
          TriPredicate<RESOURCE, AutomationType, ItemAccess> canInsert, Predicate<RESOURCE> isValid, Predicate<ItemResource> supportsStack) {
        this(rate, capacity, null, canExtract, canInsert, isValid, supportsStack);
    }

    protected GenericTankSpec(IntSupplier rate, @Nullable ToLongFunction<ItemAccess> itemBasedCapacity, BiPredicate<RESOURCE, AutomationType> canExtract,
          TriPredicate<RESOURCE, AutomationType, ItemAccess> canInsert, Predicate<RESOURCE> isValid, Predicate<ItemResource> supportsStack) {
        this(rate, ConstantPredicates.ZERO_LONG, itemBasedCapacity, canExtract, canInsert, isValid, supportsStack);
    }

    private GenericTankSpec(IntSupplier rate, LongSupplier capacity, @Nullable ToLongFunction<ItemAccess> itemBasedCapacity, BiPredicate<RESOURCE, AutomationType> canExtract,
          TriPredicate<RESOURCE, AutomationType, ItemAccess> canInsert, Predicate<RESOURCE> isValid, Predicate<ItemResource> supportsStack) {
        this.rate = rate;
        this.capacity = capacity;
        this.itemBasedCapacity = itemBasedCapacity;
        this.isValid = isValid;
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.supportsStack = supportsStack;
    }

    public boolean supportsStack(ItemResource itemType) {
        return supportsStack.test(itemType);
    }

    //TODO - 1.20.5: Re-evaluate this
    public <CONTAINER extends IResourceContainer<RESOURCE>> void addTank(ResourceContainersBuilder<RESOURCE, CONTAINER, ?> builder,
          ComponentTankFromSpecCreator<RESOURCE, CONTAINER> tankCreator) {
        if (itemBasedCapacity == null) {
            builder.addContainer((attachedAccess, containerIndex) -> tankCreator.create(attachedAccess, containerIndex, canExtract,
                  (resource, automationType) -> canInsert.test(resource, automationType, attachedAccess), isValid, rate, capacity));
        } else {
            builder.addContainer((attachedAccess, containerIndex) -> tankCreator.create(attachedAccess, containerIndex, canExtract,
                  (chemicalType, automationType) -> canInsert.test(chemicalType, automationType, attachedAccess), isValid, rate, () -> itemBasedCapacity.applyAsLong(attachedAccess)
            ));
        }
    }

    public static <RESOURCE extends Resource> GenericTankSpec<RESOURCE> createFillOnly(IntSupplier rate, LongSupplier capacity, Predicate<RESOURCE> isValid) {
        return createFillOnly(rate, capacity, isValid, ConstantPredicates.alwaysTrue());
    }

    public static <RESOURCE extends Resource> GenericTankSpec<RESOURCE> createFillOnly(IntSupplier rate, LongSupplier capacity, Predicate<RESOURCE> isValid,
          Predicate<ItemResource> supportsStack) {
        return new GenericTankSpec<>(rate, capacity, ConstantPredicates.notExternal(), (_, _, itemAccess) -> {
            ItemResource itemType = itemAccess.getResource();
            return !itemType.isEmpty() && supportsStack.test(itemType);
        }, isValid, supportsStack);
    }

    public static <RESOURCE extends Resource> GenericTankSpec<RESOURCE> createFillOnly(IntSupplier rate, ToLongFunction<ItemAccess> itemBasedCapacity,
          Predicate<RESOURCE> isValid, Predicate<ItemResource> supportsStack) {
        return new GenericTankSpec<>(rate, itemBasedCapacity, ConstantPredicates.notExternal(), (_, _, itemAccess) -> {
            ItemResource itemType = itemAccess.getResource();
            return !itemType.isEmpty() && supportsStack.test(itemType);
        }, isValid, supportsStack);
    }

    @FunctionalInterface
    public interface ComponentTankFromSpecCreator<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> {

        CONTAINER create(ItemAccess attachedAccess, int tankIndex, BiPredicate<RESOURCE, AutomationType> canExtract, BiPredicate<RESOURCE, AutomationType> canInsert,
              Predicate<RESOURCE> isValid, IntSupplier rate, LongSupplier capacity);
    }
}