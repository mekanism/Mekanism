package mekanism.common.content.sps;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.tile.multiblock.TileEntitySPSCasing;
import mekanism.common.tile.multiblock.TileEntitySPSPort;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class SPSMultiblockData extends MultiblockData implements IValveHandler {

    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getInput", "getInputCapacity", "getInputNeeded",
                                                                                        "getInputFilledPercentage"}, docPlaceholder = "input tank")
    public IChemicalTank inputTank;
    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getOutput", "getOutputCapacity", "getOutputNeeded",
                                                                                        "getOutputFilledPercentage"}, docPlaceholder = "output tank")
    public IChemicalTank outputTank;

    public final SyncableCoilData coilData = new SyncableCoilData();
    private final List<CapabilityOutputTarget<IChemicalHandler>> gasOutputTargets = new ArrayList<>();

    @ContainerSync
    public double progress;
    @ContainerSync
    public int inputProcessed = 0;

    public long receivedEnergy = 0;
    @ContainerSync
    public long lastReceivedEnergy = 0;
    @ContainerSync
    public double lastProcessed;

    public boolean couldOperate;
    private AABB deathZone;

    public SPSMultiblockData(TileEntitySPSCasing tile) {
        super(tile);
        chemicalTanks.add(inputTank = VariableCapacityChemicalTank.input(this, this::getMaxInputGas, gas -> gas == MekanismChemicals.POLONIUM.get(),
              ChemicalAttributeValidator.ALWAYS_ALLOW, createSaveAndComparator()));
        chemicalTanks.add(outputTank = VariableCapacityChemicalTank.output(this, MekanismConfig.general.spsOutputTankCapacity,
              gas -> gas == MekanismChemicals.ANTIMATTER.get(), ChemicalAttributeValidator.ALWAYS_ALLOW, this));
    }

    @Override
    public void onCreated(Level world) {
        super.onCreated(world);
        deathZone = AABB.encapsulatingFullBlocks(getMinPos().offset(1, 1, 1), getMaxPos().offset(-1, -1, -1));
    }

    private long getMaxInputGas() {
        return MekanismConfig.general.spsInputPerAntimatter.get() * 2L;
    }

    @Override
    public boolean tick(Level world) {
        boolean needsPacket = super.tick(world);
        double processed = 0;
        couldOperate = canOperate();
        if (couldOperate && receivedEnergy > 0L) {
            double lastProgress = progress;
            final int inputPerAntimatter = MekanismConfig.general.spsInputPerAntimatter.get();
            long inputNeeded = (inputPerAntimatter - inputProcessed) + inputPerAntimatter * (outputTank.getNeeded() - 1);
            double processable = (double) receivedEnergy / MekanismConfig.general.spsEnergyPerInput.get();
            if (processable + progress >= inputNeeded) {
                processed = process(inputNeeded);
                progress = 0;
            } else {
                processed = processable;
                progress += processable;
                long toProcess = MathUtils.clampToLong(progress);
                long actualProcessed = process(toProcess);
                if (actualProcessed < toProcess) {
                    //If we processed less than we intended to we need to adjust how much our values actually changed by
                    long processedDif = toProcess - actualProcessed;
                    progress -= processedDif;
                    processed -= processedDif;
                }
                progress %= 1;
            }
            if (lastProgress != progress) {
                markDirty();
            }
        }

        if (receivedEnergy != lastReceivedEnergy || processed != lastProcessed) {
            needsPacket = true;
        }
        if (!gasOutputTargets.isEmpty() && !outputTank.isEmpty()) {
            ChemicalUtil.emit(getActiveOutputs(gasOutputTargets), outputTank);
        }
        lastReceivedEnergy = receivedEnergy;
        receivedEnergy = 0L;
        lastProcessed = processed;

        kill(world);

        needsPacket |= coilData.tick();
        return needsPacket;
    }

    @Override
    protected void updateEjectors(Level world) {
        gasOutputTargets.clear();
        for (ValveData valve : valves) {
            TileEntitySPSPort tile = WorldUtils.getTileEntity(TileEntitySPSPort.class, world, valve.location);
            if (tile != null) {
                tile.addChemicalTargetCapability(gasOutputTargets, valve.side);
            }
        }
    }

    @Override
    public void readUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.readUpdateTag(tag, provider);
        coilData.read(tag);
        lastReceivedEnergy = tag.getLong(SerializationConstants.ENERGY_USAGE);
        lastProcessed = tag.getDouble(SerializationConstants.LAST_PROCESSED);
    }

    @Override
    public void writeUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.writeUpdateTag(tag, provider);
        coilData.write(tag);
        tag.putLong(SerializationConstants.ENERGY_USAGE, lastReceivedEnergy);
        tag.putDouble(SerializationConstants.LAST_PROCESSED, lastProcessed);
    }

    @Override
    protected int getMultiblockRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(inputTank.getStored(), inputTank.getCapacity());
    }

    private long process(long operations) {
        if (operations == 0) {
            return 0;
        }
        long processed = inputTank.shrinkStack(operations, Action.EXECUTE);
        int lastInputProcessed = inputProcessed;
        //Limit how much input we actually increase the input processed by to how much we were actually able to remove from the input tank
        inputProcessed += MathUtils.clampToInt(processed);
        final int inputPerAntimatter = MekanismConfig.general.spsInputPerAntimatter.get();
        if (inputProcessed >= inputPerAntimatter) {
            ChemicalStack toAdd = MekanismChemicals.ANTIMATTER.getStack(inputProcessed / inputPerAntimatter);
            outputTank.insert(toAdd, Action.EXECUTE, AutomationType.INTERNAL);
            inputProcessed %= inputPerAntimatter;
        }
        if (lastInputProcessed != inputProcessed) {
            markDirty();
        }
        return processed;
    }

    private void kill(Level world) {
        if (lastReceivedEnergy > 0L && couldOperate && world.getRandom().nextInt() % SharedConstants.TICKS_PER_SECOND == 0) {
            List<Entity> entitiesToDie = getLevel().getEntitiesOfClass(Entity.class, deathZone);
            for (Entity entity : entitiesToDie) {
                entity.hurt(entity.damageSources().magic(), lastReceivedEnergy / 1_000F);
            }
        }
    }

    public boolean canSupplyCoilEnergy(TileEntitySPSPort tile) {
        //We allow supplying coil energy for one tick more than the structure "canOperate" so that tick order does not
        // make it so that some coils are unable to supply energy
        return (couldOperate || canOperate()) && coilData.coilMap.containsKey(tile.getBlockPos());
    }

    public void addCoil(BlockPos portPos, Direction side) {
        coilData.coilMap.put(portPos, new CoilData(portPos, side));
    }

    public void supplyCoilEnergy(TileEntitySPSPort tile, long energy) {
        receivedEnergy = MathUtils.addClamped(receivedEnergy, energy);
        coilData.coilMap.get(tile.getBlockPos()).receiveEnergy(energy);
    }

    private boolean canOperate() {
        return !inputTank.isEmpty() && outputTank.getNeeded() > 0;
    }

    private static int getCoilLevel(long energy) {
        if (energy == 0L) {
            return 0;
        }
        return 1 + Math.max(0, (int) ((Math.log10(energy) - 3) * 1.8));
    }

    @ComputerMethod
    public double getProcessRate() {
        return Math.round((lastProcessed / MekanismConfig.general.spsInputPerAntimatter.get()) * 1_000) / 1_000D;
    }

    public double getScaledProgress() {
        return (inputProcessed + progress) / MekanismConfig.general.spsInputPerAntimatter.get();
    }

    public boolean handlesSound(TileEntitySPSCasing tile) {
        return tile.getBlockPos().equals(getMinPos().offset(3, 0, 0)) ||
               tile.getBlockPos().equals(getMaxPos().offset(-3, 0, 0));
    }

    //Computer related methods
    @ComputerMethod
    int getCoils() {
        return coilData.coilMap.size();
    }
    //End computer related methods

    public static class SyncableCoilData {

        public final Map<BlockPos, CoilData> coilMap = new Object2ObjectOpenHashMap<>();
        public int prevHash;

        private boolean tick() {
            for (CoilData data : coilMap.values()) {
                data.prevLevel = data.laserLevel;
                data.laserLevel = 0;
            }

            int newHash = coilMap.hashCode();
            boolean ret = newHash != prevHash;
            prevHash = newHash;
            return ret;
        }

        public void write(CompoundTag tags) {
            ListTag list = new ListTag();
            for (CoilData data : coilMap.values()) {
                CompoundTag tag = new CompoundTag();
                tag.put(SerializationConstants.POSITION, NbtUtils.writeBlockPos(data.coilPos));
                NBTUtils.writeEnum(tag, SerializationConstants.SIDE, data.side);
                tag.putInt(SerializationConstants.LEVEL, data.prevLevel);
                list.add(tag);
            }
            tags.put(SerializationConstants.COILS, list);
        }

        public void read(CompoundTag tags) {
            coilMap.clear();
            ListTag list = tags.getList(SerializationConstants.COILS, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                Optional<BlockPos> pos = NbtUtils.readBlockPos(tag, SerializationConstants.POSITION);
                if (pos.isPresent()) {
                    Direction side = Direction.from3DDataValue(tag.getInt(SerializationConstants.SIDE));
                    CoilData data = new CoilData(pos.get(), side);
                    data.prevLevel = tag.getInt(SerializationConstants.LEVEL);
                    coilMap.put(data.coilPos, data);
                }
            }
        }
    }

    public static class CoilData {

        public final BlockPos coilPos;
        public final Direction side;
        // prev level is synced, as laserLevel is reset to 0 each tick
        public int prevLevel;
        private int laserLevel;

        private CoilData(BlockPos pos, Direction side) {
            this.coilPos = pos;
            this.side = side;
        }

        private void receiveEnergy(long energy) {
            laserLevel += getCoilLevel(energy);
        }

        @Override
        public int hashCode() {
            int result = 1;
            result = 31 * result + coilPos.hashCode();
            result = 31 * result + prevLevel;
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            return o instanceof CoilData other && coilPos.equals(other.coilPos) && prevLevel == other.prevLevel;
        }
    }
}
