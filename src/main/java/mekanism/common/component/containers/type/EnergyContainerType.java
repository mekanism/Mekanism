package mekanism.common.component.containers.type;

import mekanism.api.SerializationConstants;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.component.containers.energy.ComponentBackedEnergyHandler;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnergyUtils;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public final class EnergyContainerType extends CapableContainerType<IEnergyContainer, Long, EnergyHandler> implements ISingleContainerType<IEnergyContainer, Long> {

    EnergyContainerType() {
        super(MekanismDataComponents.ATTACHED_ENERGY, SerializationConstants.ENERGY_CONTAINER, Capabilities.ENERGY);
    }

    @Nullable
    @Override
    protected EnergyHandler createHandler(ItemAccess itemAccess) {
        ItemResource resource = itemAccess.getResource();
        if (supports(resource)) {
            //Note: All our energy handlers that we expose on items, currently validate the backing item type just like Neo's ItemAccessEnergyHandler does.
            // If it is desired to skip that check similar to ItemAccessResourceHandler, such as because we have a handler that changes between item instances
            // similar to a bucket, then we just need to adjust this to pass false in those cases to the handler.
            return new ComponentBackedEnergyHandler(this, itemAccess, true);
        }
        return null;
    }

    @Override
    public @Nullable IEnergyContainer getContainer(TileEntityMekanism tile) {
        return tile.getEnergyContainer();
    }

    @Override
    public void copyToContainer(IEnergyContainer container, Long stored) {
        //Clamp contents to the max amount of energy that can be stored
        container.setEnergy(Math.min(stored, container.getCapacityAsLong()), null);
        //TODO - 26.1: Should we clamp on pick up as well?
    }

    @Override
    public Long attachedCopyOf(IEnergyContainer container) {
        return container.getAmountAsLong();
    }

    @Override
    public boolean canHandle(TileEntityMekanism tile) {
        return tile.canHandleEnergy();
    }

    @Override
    public void copy(IEnergyContainer from, IEnergyContainer to, @Nullable TransactionContext transaction) {
        to.copyContents(from, transaction);
    }

    /// @param toFill      Item type to try and fill.
    /// @param transaction The transaction that this operation is part of. May be `null`.
    ///
    /// @return Stack representation of the item access once it has been filled with energy.
    public ItemStack getFilledVariant(Holder<Item> toFill, @Nullable TransactionContext transaction) {
        return getFilledVariant(ItemResource.of(toFill), transaction);
    }

    /// @param toFill      Item type to try and fill.
    /// @param transaction The transaction that this operation is part of. May be `null`.
    ///
    /// @return Stack representation of the item access once it has been filled with energy.
    public ItemStack getFilledVariant(ItemResource toFill, @Nullable TransactionContext transaction) {
        return getFilledVariant(ItemAccessUtils.sideEffectFreeAccess(toFill), transaction);
    }

    /// @param itemAccess  Item access to try and fill the represented item.
    /// @param transaction The transaction that this operation is part of. May be `null`.
    ///
    /// @return Stack representation of the item access once it has been filled with energy.
    public ItemStack getFilledVariant(ItemAccess itemAccess, @Nullable TransactionContext transaction) {
        EnergyHandler energyHandler = Capabilities.ENERGY.getCapability(itemAccess);
        IEnergyContainer energyContainer = EnergyUtils.getEnergyContainer(energyHandler);
        if (energyContainer != null) {
            //Note: Just directly interact with the containers as we want to change the entire access and don't care about splitting between multiple items
            energyContainer.setEnergy(energyContainer.getCapacityAsLong(), transaction);
        }
        //The item is now filled return it for convenience
        return ItemAccessUtils.asStack(itemAccess);
    }

    /// Clamps the contents of the container to its capacity.
    ///
    /// @param container   Container to clamp.
    /// @param transaction The transaction that this operation is part of. May be `null`.
    ///
    /// @implNote If the capacity is zero, and the container is of variable size, this will skip clamping the container.
    public void clampContents(IEnergyContainer container, @Nullable TransactionContext transaction) {
        //TODO - 26.1: Evaluate what other spots should be clamped
        if (!container.isEmpty()) {
            long capacity = container.getCapacityAsLong();
            if (capacity == 0 && container instanceof VariableCapacityEnergyContainer) {
                //Our capacity should never actually be zero, and given we fake it being zero until we finish building the network,
                // we need to override this method to bypass the upper limit check when our upper limit is zero
                return;
            }
            if (container.getAmountAsLong() > capacity) {
                container.setEnergy(capacity, transaction);
            }
        }
    }

    /// Divides amount stored in the container by the capacity of the container and returns the result as a double.
    ///
    /// @param container The container to calculate the level of.
    ///
    /// @return A double representing the value of dividing the amount stored by the capacity, or `1` if the capacity is `0`, or the stored amount is larger than the
    /// capacity.
    ///
    /// @implNote This caps the returned value at `1`
    public double divideToLevel(EnergyHandler container) {
        return MathUtils.divideToLevel(container.getAmountAsLong(), container.getCapacityAsLong());
    }

    public long getOrZero(ItemAccess itemAccess) {
        return getOrZero(itemAccess.getResource());
    }

    public long getOrZero(DataComponentGetter componentGetter) {
        return componentGetter.getOrDefault(getComponentType(), 0L);
    }
}