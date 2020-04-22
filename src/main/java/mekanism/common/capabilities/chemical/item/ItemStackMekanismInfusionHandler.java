package mekanism.common.capabilities.chemical.item;

import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.IMekanismInfusionHandler;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.CapabilityCache;
import mekanism.common.capabilities.resolver.basic.BasicCapabilityResolver;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

/**
 * Helper class for implementing infusion handlers for items
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackMekanismInfusionHandler extends ItemStackMekanismChemicalHandler<InfuseType, InfusionStack, IInfusionTank> implements IMekanismInfusionHandler {

    @Override
    protected void load() {
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            DataHandlerUtils.readContainers(getInfusionTanks(null), ItemDataUtils.getList(stack, NBTConstants.INFUSION_TANKS));
        }
    }

    @Override
    public List<IInfusionTank> getInfusionTanks(@Nullable Direction side) {
        return tanks;
    }

    @Override
    public void onContentsChanged() {
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            ItemDataUtils.setList(stack, NBTConstants.INFUSION_TANKS, DataHandlerUtils.writeContainers(getInfusionTanks(null)));
        }
    }

    @Override
    protected void addCapabilityResolvers(CapabilityCache capabilityCache) {
        capabilityCache.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.INFUSION_HANDLER_CAPABILITY, this));
    }
}