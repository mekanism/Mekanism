package mekanism.common.capabilities.fluid.item;

import java.util.List;
import java.util.function.Consumer;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class for implementing fluid handlers for items
 */
@ParametersAreNotNullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackMekanismFluidHandler extends ItemCapability implements IMekanismFluidHandler, IFluidHandlerItem {

    protected List<IExtendedFluidTank> tanks;

    protected abstract List<IExtendedFluidTank> getInitialTanks();

    @Override
    protected void init() {
        super.init();
        this.tanks = getInitialTanks();
    }

    @Override
    protected void load() {
        super.load();
        ItemDataUtils.readContainers(getStack(), NBTConstants.FLUID_TANKS, getFluidTanks(null));
    }

    @NotNull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return tanks;
    }

    @Override
    public void onContentsChanged() {
        ItemDataUtils.writeContainers(getStack(), NBTConstants.FLUID_TANKS, getFluidTanks(null));
    }

    @NotNull
    @Override
    public ItemStack getContainer() {
        return getStack();
    }

    @Override
    protected void gatherCapabilityResolvers(Consumer<ICapabilityResolver> consumer) {
        consumer.accept(BasicCapabilityResolver.constant(ForgeCapabilities.FLUID_HANDLER_ITEM, this));
    }
}