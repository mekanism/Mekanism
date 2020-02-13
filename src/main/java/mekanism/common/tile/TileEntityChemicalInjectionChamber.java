package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismMachines;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.GasSlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import net.minecraft.util.Direction;

public class TileEntityChemicalInjectionChamber extends TileEntityAdvancedElectricMachine {

    public TileEntityChemicalInjectionChamber() {
        super(MekanismMachines.CHEMICAL_INJECTION_CHAMBER, BASE_TICKS_REQUIRED, BASE_GAS_PER_TICK);
        configComponent.addSupported(TransmissionType.GAS);
        ConfigInfo gasConfig = configComponent.getConfig(TransmissionType.GAS);
        if (gasConfig != null) {
            gasConfig.addSlotInfo(DataType.INPUT, new GasSlotInfo(true, false, gasTank));
            //Set default config directions
            gasConfig.fill(DataType.INPUT);
            gasConfig.setCanEject(false);
        }
    }

    @Nonnull
    @Override
    public MekanismRecipeType<ItemStackGasToItemStackRecipe> getRecipeType() {
        return MekanismRecipeType.INJECTING;
    }

    @Override
    public boolean canReceiveGas(Direction side, @Nonnull Gas type) {
        ISlotInfo slotInfo = configComponent.getSlotInfo(TransmissionType.GAS, side);
        if (slotInfo instanceof GasSlotInfo) {
            GasSlotInfo gasSlotInfo = (GasSlotInfo) slotInfo;
            return gasSlotInfo.canInput() && gasSlotInfo.hasTank(gasTank) && gasTank.canReceive(type) && isValidGas(type);
        }
        return false;
    }

    @Override
    public boolean useStatisticalMechanics() {
        return true;
    }
}