package mekanism.common.content.sps;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.chemical.multiblock.MultiblockChemicalTankBuilder;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tile.multiblock.TileEntitySPSCasing;
import mekanism.common.tile.multiblock.TileEntitySPSPort;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class SPSMultiblockData extends MultiblockData implements IValveHandler {

    private static final long MAX_OUTPUT_GAS = 1_000;

    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getInput", "getInputCapacity", "getInputNeeded", "getInputFilledPercentage"})
    public IGasTank inputTank;
    @ContainerSync
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getOutput", "getOutputCapacity", "getOutputNeeded", "getOutputFilledPercentage"})
    public IGasTank outputTank;

    public final SyncableCoilData coilData = new SyncableCoilData();

    @ContainerSync
    public double progress;
    @ContainerSync
    public int inputProcessed = 0;

    public FloatingLong receivedEnergy = FloatingLong.ZERO;
    @ContainerSync
    public FloatingLong lastReceivedEnergy = FloatingLong.ZERO;
    @ContainerSync
    public double lastProcessed;

    public boolean couldOperate;
    private AABB deathZone;

    public SPSMultiblockData(TileEntitySPSCasing tile) {
        super(tile);
        gasTanks.add(inputTank = MultiblockChemicalTankBuilder.GAS.create(this, tile, this::getMaxInputGas,
              (stack, automationType) -> automationType != AutomationType.EXTERNAL && isFormed(), (stack, automationType) -> isFormed(),
              gas -> gas == MekanismGases.POLONIUM.get(), ChemicalAttributeValidator.ALWAYS_ALLOW, null));
        gasTanks.add(outputTank = MultiblockChemicalTankBuilder.GAS.create(this, tile, () -> MAX_OUTPUT_GAS,
              (stack, automationType) -> isFormed(), (stack, automationType) -> automationType != AutomationType.EXTERNAL && isFormed(),
              gas -> gas == MekanismGases.ANTIMATTER.get(), ChemicalAttributeValidator.ALWAYS_ALLOW, null));
    }

    @Override
    public void onCreated(Level world) {
        super.onCreated(world);
        deathZone = new AABB(getMinPos().offset(1, 1, 1), getMaxPos());
    }

    private long getMaxInputGas() {
        return MekanismConfig.general.spsInputPerAntimatter.get() * 2L;
    }

    @Override
    public boolean tick(Level world) {
        boolean needsPacket = super.tick(world);
        double processed = 0;
        couldOperate = canOperate();
        if (couldOperate && !receivedEnergy.isZero()) {
            double lastProgress = progress;
            final int inputPerAntimatter = MekanismConfig.general.spsInputPerAntimatter.get();
            long inputNeeded = (inputPerAntimatter - inputProcessed) + inputPerAntimatter * (outputTank.getNeeded() - 1);
            double processable = receivedEnergy.doubleValue() / MekanismConfig.general.spsEnergyPerInput.get().doubleValue();
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

        if (!receivedEnergy.equals(lastReceivedEnergy) || processed != lastProcessed) {
            needsPacket = true;
        }
        lastReceivedEnergy = receivedEnergy;
        receivedEnergy = FloatingLong.ZERO;
        lastProcessed = processed;

        kill(world);

        needsPacket |= coilData.tick();
        return needsPacket;
    }

    @Override
    public void readUpdateTag(CompoundTag tag) {
        super.readUpdateTag(tag);
        coilData.read(tag);
        lastReceivedEnergy = FloatingLong.parseFloatingLong(tag.getString(NBTConstants.ENERGY_USAGE));
        lastProcessed = tag.getDouble(NBTConstants.LAST_PROCESSED);
    }

    @Override
    public void writeUpdateTag(CompoundTag tag) {
        super.writeUpdateTag(tag);
        coilData.write(tag);
        tag.putString(NBTConstants.ENERGY_USAGE, lastReceivedEnergy.toString());
        tag.putDouble(NBTConstants.LAST_PROCESSED, lastProcessed);
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
            GasStack toAdd = MekanismGases.ANTIMATTER.getStack(inputProcessed / inputPerAntimatter);
            outputTank.insert(toAdd, Action.EXECUTE, AutomationType.INTERNAL);
            inputProcessed %= inputPerAntimatter;
        }
        if (lastInputProcessed != inputProcessed) {
            markDirty();
        }
        return processed;
    }

    private void kill(Level world) {
        if (!lastReceivedEnergy.isZero() && couldOperate && world.getRandom().nextInt() % 20 == 0) {
            List<Entity> entitiesToDie = getWorld().getEntitiesOfClass(Entity.class, deathZone);
            for (Entity entity : entitiesToDie) {
                entity.hurt(DamageSource.MAGIC, lastReceivedEnergy.floatValue() / 1_000F);
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

    public void supplyCoilEnergy(TileEntitySPSPort tile, FloatingLong energy) {
        receivedEnergy = receivedEnergy.plusEqual(energy);
        coilData.coilMap.get(tile.getBlockPos()).receiveEnergy(energy);
    }

    private boolean canOperate() {
        return !inputTank.isEmpty() && outputTank.getNeeded() > 0;
    }

    private static int getCoilLevel(FloatingLong energy) {
        if (energy.isZero()) {
            return 0;
        }
        return 1 + Math.max(0, (int) ((Math.log10(energy.doubleValue()) - 3) * 1.8));
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
    private int getCoils() {
        return coilData.coilMap.size();
    }
    //End computer related methods

    public static class SyncableCoilData {

        public final Map<BlockPos, CoilData> coilMap = new Object2ObjectOpenHashMap<>();
        public int prevHash;

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

        public void write(CompoundTag tags) {
            ListTag list = new ListTag();
            for (CoilData data : coilMap.values()) {
                CompoundTag tag = new CompoundTag();
                tag.put(NBTConstants.POSITION, NbtUtils.writeBlockPos(data.coilPos));
                NBTUtils.writeEnum(tag, NBTConstants.SIDE, data.side);
                tag.putInt(NBTConstants.LEVEL, data.prevLevel);
                list.add(tag);
            }
            tags.put(NBTConstants.COILS, list);
        }

        public void read(CompoundTag tags) {
            coilMap.clear();
            ListTag list = tags.getList(NBTConstants.COILS, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                BlockPos pos = NbtUtils.readBlockPos(tag.getCompound(NBTConstants.POSITION));
                Direction side = Direction.from3DDataValue(tag.getInt(NBTConstants.SIDE));
                CoilData data = new CoilData(pos, side);
                data.prevLevel = tag.getInt(NBTConstants.LEVEL);
                coilMap.put(data.coilPos, data);
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

        private void receiveEnergy(FloatingLong energy) {
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
