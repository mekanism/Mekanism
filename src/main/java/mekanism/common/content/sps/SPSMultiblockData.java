package mekanism.common.content.sps;

import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.chemical.MultiblockGasTank;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tile.multiblock.TileEntitySPSCasing;
import mekanism.common.tile.multiblock.TileEntitySPSPort;
import net.minecraft.util.math.BlockPos;

public class SPSMultiblockData extends MultiblockData {

    private static final long MAX_INPUT_GAS = 1_000_000;
    private static final long MAX_OUTPUT_GAS = 1_000_000;

    @ContainerSync
    public MultiblockGasTank<SPSMultiblockData> inputTank;
    @ContainerSync
    public MultiblockGasTank<SPSMultiblockData> outputTank;

    private SyncableCoilData coilData = new SyncableCoilData();

    private boolean active;
    private FloatingLong receivedEnergy;

    public SPSMultiblockData(TileEntitySPSCasing tile) {
        super(tile);

        inputTank = MultiblockGasTank.create(this, tile, () -> MAX_INPUT_GAS,
            (stack, automationType) -> automationType != AutomationType.EXTERNAL && isFormed(), (stack, automationType) -> isFormed(),
            gas -> gas == MekanismGases.POLONIUM.get(), ChemicalAttributeValidator.ALWAYS_ALLOW, null);
        outputTank = MultiblockGasTank.create(this, tile, () -> MAX_OUTPUT_GAS,
            (stack, automationType) -> isFormed(), (stack, automationType) -> automationType != AutomationType.EXTERNAL && isFormed(),
            gas -> gas == MekanismGases.ANTIMATTER.get(), ChemicalAttributeValidator.ALWAYS_ALLOW, null);
    }

    public boolean canSupplyCoilEnergy(TileEntitySPSPort tile) {
        return false;
    }

    public void supplyCoilEnergy(FloatingLong energy) {

    }

    private static class SyncableCoilData {

    }

    private static class CoilData {

        private BlockPos coil;
        private int laserLevel;
    }
}
