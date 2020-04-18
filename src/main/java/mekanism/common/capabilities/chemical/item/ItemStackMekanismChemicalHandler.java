package mekanism.common.capabilities.chemical.item;

import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;

/**
 * Helper class for implementing chemical handlers for items
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackMekanismChemicalHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      TANK extends IChemicalTank<CHEMICAL, STACK>> extends ItemCapability {

    protected List<TANK> tanks;

    @Override
    protected void init() {
        this.tanks = getInitialTanks();
    }

    protected abstract List<TANK> getInitialTanks();
}