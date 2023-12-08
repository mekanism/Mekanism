package mekanism.common.capabilities.energy.item;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class for implementing fluid handlers for items
 */
@ParametersAreNotNullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackEnergyHandler implements IMekanismStrictEnergyHandler {

    protected final ItemStack stack;
    protected final List<IEnergyContainer> energyContainers;

    @SafeVarargs
    protected ItemStackEnergyHandler(ItemStack stack, Function<IMekanismStrictEnergyHandler, IEnergyContainer>... energyContainerProvider) {
        this.stack = stack;
        this.energyContainers = Arrays.stream(energyContainerProvider)
              .map(provider -> provider.apply(this))
              .toList();
        ItemDataUtils.readContainers(this.stack, NBTConstants.ENERGY_CONTAINERS, this.energyContainers);
    }

    @NotNull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return energyContainers;
    }

    @Override
    public void onContentsChanged() {
        ItemDataUtils.writeContainers(stack, NBTConstants.ENERGY_CONTAINERS, getEnergyContainers(null));
    }
}