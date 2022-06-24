package mekanism.generators.common.content.turbine;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.common.capabilities.chemical.multiblock.MultiblockChemicalTankBuilder.MultiblockGasTank;
import mekanism.common.registries.MekanismGases;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class TurbineGasTank extends MultiblockGasTank<TurbineMultiblockData> {

    public TurbineGasTank(TurbineMultiblockData multiblock, TileEntityTurbineCasing tile) {
        super(multiblock, tile, multiblock::getSteamCapacity, (stack, automationType) -> automationType != AutomationType.EXTERNAL || multiblock.isFormed(),
              (stack, automationType) -> automationType != AutomationType.EXTERNAL || multiblock.isFormed(), gas -> gas == MekanismGases.STEAM.getChemical(),
              null, null);
    }

    @Override
    public GasStack insert(@NotNull GasStack stack, Action action, AutomationType automationType) {
        GasStack returned = super.insert(stack, action, automationType);
        if (action == Action.EXECUTE && multiblock.isFormed()) {
            multiblock.newSteamInput += stack.getAmount() - returned.getAmount();
        }
        return returned;
    }
}