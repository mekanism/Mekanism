package mekanism.common.capabilities.fluid.item;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class for implementing fluid handlers for items
 */
@ParametersAreNotNullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackMekanismFluidHandler implements IMekanismFluidHandler, IFluidHandlerItem {

    protected final ItemStack stack;
    protected final List<IExtendedFluidTank> tanks;

    @SafeVarargs
    protected ItemStackMekanismFluidHandler(ItemStack stack, Function<IContentsListener, IExtendedFluidTank>... tankProviders) {
        this.stack = stack;
        this.tanks = Arrays.stream(tankProviders)
              .map(provider -> provider.apply(this))
              .toList();
        ItemDataUtils.readContainers(this.stack, NBTConstants.FLUID_TANKS, this.tanks);
    }

    @NotNull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return tanks;
    }

    @Override
    public void onContentsChanged() {
        ItemDataUtils.writeContainers(stack, NBTConstants.FLUID_TANKS, getFluidTanks(null));
    }

    @NotNull
    @Override
    public ItemStack getContainer() {
        return stack;
    }
}