package mekanism.common.capabilities.fluid.item;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.common.capabilities.CapabilityCache;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

/**
 * Helper class for implementing fluid handlers for items
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackMekanismFluidHandler extends ItemCapability implements IMekanismFluidHandler, IFluidHandlerItem {

    protected List<IExtendedFluidTank> tanks;

    protected abstract List<IExtendedFluidTank> getInitialTanks();

    @Override
    protected void init() {
        this.tanks = getInitialTanks();
    }

    @Override
    protected void load() {
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            DataHandlerUtils.readContainers(getFluidTanks(null), ItemDataUtils.getList(stack, NBTConstants.FLUID_TANKS));
        }
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return tanks;
    }

    @Override
    public void onContentsChanged() {
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            ItemDataUtils.setList(stack, NBTConstants.FLUID_TANKS, DataHandlerUtils.writeContainers(getFluidTanks(null)));
        }
    }

    @Nonnull
    @Override
    public ItemStack getContainer() {
        return getStack();
    }

    @Override
    protected void addCapabilityResolvers(CapabilityCache capabilityCache) {
        capabilityCache.addCapabilityResolver(BasicCapabilityResolver.constant(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, this));
    }
}