package mekanism.common.content.sps;

import java.util.Map;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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
import net.minecraft.world.World;

public class SPSMultiblockData extends MultiblockData {

    private static final long MAX_INPUT_GAS = 1_000_000;
    private static final long MAX_OUTPUT_GAS = 1_000_000;

    @ContainerSync
    public MultiblockGasTank<SPSMultiblockData> inputTank;
    @ContainerSync
    public MultiblockGasTank<SPSMultiblockData> outputTank;

    private SyncableCoilData coilData = new SyncableCoilData();

    private boolean active;

    private FloatingLong receivedEnergy = FloatingLong.ZERO;
    private FloatingLong lastReceivedEnergy = FloatingLong.ZERO;

    public SPSMultiblockData(TileEntitySPSCasing tile) {
        super(tile);

        inputTank = MultiblockGasTank.create(this, tile, () -> MAX_INPUT_GAS,
            (stack, automationType) -> automationType != AutomationType.EXTERNAL && isFormed(), (stack, automationType) -> isFormed(),
            gas -> gas == MekanismGases.POLONIUM.get(), ChemicalAttributeValidator.ALWAYS_ALLOW, null);
        outputTank = MultiblockGasTank.create(this, tile, () -> MAX_OUTPUT_GAS,
            (stack, automationType) -> isFormed(), (stack, automationType) -> automationType != AutomationType.EXTERNAL && isFormed(),
            gas -> gas == MekanismGases.ANTIMATTER.get(), ChemicalAttributeValidator.ALWAYS_ALLOW, null);
    }

    @Override
    public boolean tick(World world) {
        boolean needsPacket = super.tick(world);

        needsPacket |= coilData.tick();
        return needsPacket;
    }

    public boolean canSupplyCoilEnergy(TileEntitySPSPort tile) {
        return false;
    }

    public void addCoil(BlockPos pos) {

    }

    public void supplyCoilEnergy(FloatingLong energy) {

    }

    public static int getLaserLevel(FloatingLong energy) {
        return (int) Math.log10(energy.doubleValue());
    }

    private static class SyncableCoilData {

        private Map<BlockPos, CoilData> coilMap = new Object2ObjectOpenHashMap<>();
        private int prevHash;

        private boolean tick() {
            coilMap.values().forEach(data -> {
                data.prevLevel = data.laserLevel;
                data.laserLevel = 0;
            });

            int newHash = coilMap.hashCode();
            boolean ret = newHash != prevHash;
            prevHash = newHash;
            return ret;
        }
    }

    private static class CoilData {

        private final BlockPos coilPos;

        private int prevLevel;
        private int laserLevel;

        private CoilData(BlockPos pos) {
            this.coilPos = pos;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + coilPos.hashCode();
            result = prime * result + prevLevel;
            return result;
        }
    }
}
