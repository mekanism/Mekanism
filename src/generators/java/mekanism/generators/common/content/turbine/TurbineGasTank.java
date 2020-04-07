package mekanism.generators.common.content.turbine;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.chemical.MultiblockGasTank;
import mekanism.common.registries.MekanismGases;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;

public class TurbineGasTank extends MultiblockGasTank<TileEntityTurbineCasing> {

    public TurbineGasTank(TileEntityTurbineCasing tile) {
        super(tile, () -> tile.structure == null ? 0 : tile.structure.getSteamCapacity(), gas -> gas == MekanismGases.STEAM.getGas());
    }

    @Override
    public GasStack insert(@Nonnull GasStack stack, Action action, AutomationType automationType) {
        GasStack returned = super.insert(stack, action, automationType);
        if (action == Action.EXECUTE && multiblock.structure != null) {
            multiblock.structure.newSteamInput += stack.getAmount() - returned.getAmount();
        }
        return returned;
    }
}