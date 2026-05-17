package mekanism.common.inventory.container.sync;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalResource;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.resource.ChemicalResourcePropertyData;
import mekanism.common.network.to_client.container.property.resource.FluidResourcePropertyData;
import mekanism.common.network.to_client.container.property.resource.ItemResourcePropertyData;
import net.minecraft.core.RegistryAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jetbrains.annotations.NotNull;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling resources
 */
@NothingNullByDefault
public class SyncableResource<RESOURCE extends Resource> implements ISyncableData {

    public static SyncableResource<ItemResource> createItem(Supplier<@NotNull ItemResource> getter, Consumer<@NotNull ItemResource> setter) {
        return new SyncableResource<>(getter, setter, ItemResource.EMPTY, ItemResourcePropertyData::new);
    }

    public static SyncableResource<FluidResource> createFluid(Supplier<@NotNull FluidResource> getter, Consumer<@NotNull FluidResource> setter) {
        return new SyncableResource<>(getter, setter, FluidResource.EMPTY, FluidResourcePropertyData::new);
    }

    public static SyncableResource<ChemicalResource> createChemical(Supplier<@NotNull ChemicalResource> getter, Consumer<@NotNull ChemicalResource> setter) {
        return new SyncableResource<>(getter, setter, ChemicalResource.EMPTY, ChemicalResourcePropertyData::new);
    }

    private final PropertyCreator<RESOURCE> propertyCreator;
    private final Supplier<@NotNull RESOURCE> getter;
    private final Consumer<@NotNull RESOURCE> setter;
    private RESOURCE lastKnownValue;

    private SyncableResource(Supplier<@NotNull RESOURCE> getter, Consumer<@NotNull RESOURCE> setter, RESOURCE emptyResource, PropertyCreator<RESOURCE> propertyCreator) {
        this.getter = getter;
        this.setter = setter;
        this.propertyCreator = propertyCreator;
        this.lastKnownValue = emptyResource;
    }

    public RESOURCE get() {
        return getter.get();
    }

    public void set(RESOURCE value) {
        setter.accept(value);
    }

    @Override
    public DirtyType isDirty() {
        RESOURCE value = get();
        if (!value.equals(this.lastKnownValue)) {
            this.lastKnownValue = value;
            return DirtyType.DIRTY;
        }
        return DirtyType.CLEAN;
    }

    @Override
    public PropertyData getPropertyData(RegistryAccess registryAccess, short property, DirtyType dirtyType) {
        return propertyCreator.create(property, get());
    }

    @FunctionalInterface
    private interface PropertyCreator<RESOURCE extends Resource> {

        PropertyData create(short property, RESOURCE resource);
    }
}