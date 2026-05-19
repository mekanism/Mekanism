package mekanism.common.capabilities;

import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.resource.IResourceContainer;
import mekanism.common.attachments.containers.ComponentBackedResourceContainer;
import mekanism.common.attachments.containers.ResourceContainersBuilder;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.TriPredicate;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class GenericTankSpec<RESOURCE extends Resource> {

    protected final Predicate<RESOURCE> isValid;
    protected final BiPredicate<RESOURCE, AutomationType> canExtract;
    protected final TriPredicate<RESOURCE, AutomationType, ItemResource> canInsert;
    private final Predicate<ItemResource> supportsStack;
    private final IntSupplier rate;
    private final LongSupplier capacity;
    @Nullable
    private final ToLongFunction<ItemResource> itemBasedCapacity;

    protected GenericTankSpec(IntSupplier rate, LongSupplier capacity, BiPredicate<RESOURCE, AutomationType> canExtract,
          TriPredicate<RESOURCE, AutomationType, ItemResource> canInsert, Predicate<RESOURCE> isValid, Predicate<ItemResource> supportsStack) {
        this(rate, capacity, null, canExtract, canInsert, isValid, supportsStack);
    }

    protected GenericTankSpec(IntSupplier rate, @Nullable ToLongFunction<ItemResource> itemBasedCapacity, BiPredicate<RESOURCE, AutomationType> canExtract,
          TriPredicate<RESOURCE, AutomationType, ItemResource> canInsert, Predicate<RESOURCE> isValid, Predicate<ItemResource> supportsStack) {
        this(rate, ConstantPredicates.ZERO_LONG, itemBasedCapacity, canExtract, canInsert, isValid, supportsStack);
    }

    private GenericTankSpec(IntSupplier rate, LongSupplier capacity, @Nullable ToLongFunction<ItemResource> itemBasedCapacity, BiPredicate<RESOURCE, AutomationType> canExtract,
          TriPredicate<RESOURCE, AutomationType, ItemResource> canInsert, Predicate<RESOURCE> isValid, Predicate<ItemResource> supportsStack) {
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

    public <TANK extends IResourceContainer<RESOURCE>> TANK createTank(TankFromSpecCreator<RESOURCE, TANK> tankCreator, ItemResource itemType) {
        LongSupplier capacity = itemBasedCapacity == null ? this.capacity : () -> itemBasedCapacity.applyAsLong(itemType);
        return tankCreator.create(rate, capacity, canExtract, (chemicalType, automationType) ->
              canInsert.test(chemicalType, automationType, itemType), isValid, null);
    }

    //TODO - 1.20.5: Re-evaluate this
    public <CONTAINER extends ComponentBackedResourceContainer<RESOURCE>> void addTank(ResourceContainersBuilder<RESOURCE, CONTAINER, ?> builder,
          ComponentTankFromSpecCreator<RESOURCE, CONTAINER> tankCreator) {
        if (itemBasedCapacity == null) {
            builder.addContainer((_, attachedTo, containerIndex) -> tankCreator.create(attachedTo, containerIndex, canExtract,
                  (resource, automationType) -> canInsert.test(resource, automationType, ItemResource.of(attachedTo)), isValid, rate, capacity));
        } else {
            builder.addContainer((_, attachedTo, containerIndex) -> {
                ItemResource itemType = ItemResource.of(attachedTo);
                return tankCreator.create(attachedTo, containerIndex, canExtract,
                      (chemicalType, automationType) -> canInsert.test(chemicalType, automationType, itemType), isValid, rate,
                      () -> itemBasedCapacity.applyAsLong(itemType));
            });
        }
    }

    public static <RESOURCE extends Resource> GenericTankSpec<RESOURCE> create(IntSupplier rate, LongSupplier capacity) {
        return new GenericTankSpec<>(rate, capacity, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueTri(), ConstantPredicates.alwaysTrue(),
              ConstantPredicates.alwaysTrue());
    }

    public static <RESOURCE extends Resource> GenericTankSpec<RESOURCE> createFillOnly(IntSupplier rate, LongSupplier capacity, Predicate<RESOURCE> isValid) {
        return createFillOnly(rate, capacity, isValid, ConstantPredicates.alwaysTrue());
    }

    public static <RESOURCE extends Resource> GenericTankSpec<RESOURCE> createFillOnly(IntSupplier rate, LongSupplier capacity, Predicate<RESOURCE> isValid,
          Predicate<ItemResource> supportsStack) {
        return new GenericTankSpec<>(rate, capacity, ConstantPredicates.notExternal(), (_, _, itemType) -> supportsStack.test(itemType),
              isValid, supportsStack);
    }

    public static <RESOURCE extends Resource> GenericTankSpec<RESOURCE> createFillOnly(IntSupplier rate, ToLongFunction<ItemResource> itemBasedCapacity,
          Predicate<RESOURCE> isValid, Predicate<ItemResource> supportsStack) {
        return new GenericTankSpec<>(rate, itemBasedCapacity, ConstantPredicates.notExternal(),
              (_, _, itemType) -> supportsStack.test(itemType), isValid, supportsStack);
    }

    @FunctionalInterface
    public interface ComponentTankFromSpecCreator<RESOURCE extends Resource, CONTAINER extends ComponentBackedResourceContainer<RESOURCE>> {

        CONTAINER create(ItemStack attachedTo, int tankIndex, BiPredicate<RESOURCE, AutomationType> canExtract, BiPredicate<RESOURCE, AutomationType> canInsert,
              Predicate<RESOURCE> isValid, IntSupplier rate, LongSupplier capacity);
    }

    @FunctionalInterface
    public interface TankFromSpecCreator<RESOURCE extends Resource, TANK extends IResourceContainer<RESOURCE>> {

        TANK create(IntSupplier rate, LongSupplier capacity, BiPredicate<RESOURCE, AutomationType> canExtract, BiPredicate<RESOURCE, AutomationType> canInsert,
              Predicate<RESOURCE> isValid, @Nullable IContentsListener listener);

        default TANK create(IntSupplier rate, LongSupplier capacity, BiPredicate<RESOURCE, AutomationType> canExtract,
              BiPredicate<RESOURCE, AutomationType> canInsert, Predicate<RESOURCE> isValid) {
            return create(rate, capacity, canExtract, canInsert, isValid, null);
        }
    }
}