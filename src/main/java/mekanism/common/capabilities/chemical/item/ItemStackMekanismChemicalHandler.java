package mekanism.common.capabilities.chemical.item;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.DataHandlerUtils;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

/**
 * Helper class for implementing chemical handlers for items
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackMekanismChemicalHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      TANK extends IChemicalTank<CHEMICAL, STACK>> extends ItemCapability implements IMekanismChemicalHandler<CHEMICAL, STACK, TANK> {

    protected List<TANK> tanks;

    @Override
    protected void init() {
        this.tanks = getInitialTanks();
    }

    @Override
    protected void load() {
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            DataHandlerUtils.readContainers(getChemicalTanks(null), ItemDataUtils.getList(stack, getNbtKey()));
        }
    }

    @Override
    public void onContentsChanged() {
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            ItemDataUtils.setList(stack, getNbtKey(), DataHandlerUtils.writeContainers(getChemicalTanks(null)));
        }
    }

    @Nonnull
    @Override
    public List<TANK> getChemicalTanks(@Nullable Direction side) {
        return tanks;
    }

    protected abstract List<TANK> getInitialTanks();

    protected abstract String getNbtKey();
}