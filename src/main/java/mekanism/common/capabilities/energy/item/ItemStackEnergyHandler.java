package mekanism.common.capabilities.energy.item;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.NBTConstants;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.resolver.EnergyCapabilityResolver;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;

/**
 * Helper class for implementing fluid handlers for items
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackEnergyHandler extends ItemCapability implements IMekanismStrictEnergyHandler {

    protected List<IEnergyContainer> energyContainers;

    protected abstract List<IEnergyContainer> getInitialContainers();

    @Override
    protected void init() {
        super.init();
        this.energyContainers = getInitialContainers();
    }

    @Override
    protected void load() {
        super.load();
        ItemDataUtils.readContainers(getStack(), NBTConstants.ENERGY_CONTAINERS, getEnergyContainers(null));
    }

    @Nonnull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return energyContainers;
    }

    @Override
    public void onContentsChanged() {
        ItemDataUtils.writeContainers(getStack(), NBTConstants.ENERGY_CONTAINERS, getEnergyContainers(null));
    }

    @Override
    protected void gatherCapabilityResolvers(Consumer<ICapabilityResolver> consumer) {
        consumer.accept(new EnergyCapabilityResolver(this));
    }
}