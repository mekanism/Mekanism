package mekanism.common.capabilities.chemical.item;

import java.util.List;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class for implementing chemical handlers for items
 */
@ParametersAreNotNullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackMekanismChemicalHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      TANK extends IChemicalTank<CHEMICAL, STACK>> extends ItemCapability implements IMekanismChemicalHandler<CHEMICAL, STACK, TANK> {

    protected List<TANK> tanks;

    @Override
    protected void init() {
        super.init();
        this.tanks = getInitialTanks();
    }

    @Override
    protected void load() {
        super.load();
        ItemDataUtils.readContainers(getStack(), getNbtKey(), getChemicalTanks(null));
    }

    @Override
    public void onContentsChanged() {
        ItemDataUtils.writeContainers(getStack(), getNbtKey(), getChemicalTanks(null));
    }

    @Override
    public List<TANK> getChemicalTanks(@Nullable Direction side) {
        return tanks;
    }

    protected abstract List<TANK> getInitialTanks();

    protected abstract String getNbtKey();
}