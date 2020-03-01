package mekanism.common.capabilities.chemical;

import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.DataHandlerUtils;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.infuse.IMekanismInfusionHandler;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

/**
 * Helper class for implementing infusion handlers for items
 */
//TODO: Evaluate if this should be moved into the API package
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackMekanismInfusionHandler extends ItemStackMekanismChemicalHandler<InfuseType, InfusionStack> implements IMekanismInfusionHandler {

    @Override
    protected void load() {
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            DataHandlerUtils.readTanks(getInfusionTanks(null), ItemDataUtils.getList(stack, "InfusionTanks"));
        }
    }

    @Override
    public List<? extends IChemicalTank<InfuseType, InfusionStack>> getInfusionTanks(@Nullable Direction side) {
        return tanks;
    }

    @Override
    public void onContentsChanged() {
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            ItemDataUtils.setList(stack, "InfusionTanks", DataHandlerUtils.writeTanks(getInfusionTanks(null)));
        }
    }

    @Override
    public boolean canProcess(Capability<?> capability) {
        return capability == Capabilities.INFUSION_HANDLER_CAPABILITY;
    }
}