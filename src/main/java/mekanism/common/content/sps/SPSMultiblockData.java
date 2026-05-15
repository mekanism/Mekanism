package mekanism.common.content.sps;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.math.MathUtils;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.multiblock.TileEntitySPSCasing;
import mekanism.common.tile.multiblock.TileEntitySPSPort;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.ResourceUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueInput.ValueInputList;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.ValueOutputList;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

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
    private final List<CapabilityOutputTarget<ResourceHandler<ChemicalResource>>> chemicalOutputTargets = new ArrayList<>();

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
    private AABB deathZone, advancementArea;

    public SPSMultiblockData(TileEntitySPSCasing tile) {
        super(tile);
        chemicalTanks.add(inputTank = VariableCapacityChemicalTank.input(this, this::getMaxInputGas, chemical -> chemical.is(MekanismChemicals.POLONIUM),
              ChemicalAttributeValidator.ALWAYS_ALLOW, createSaveAndComparator()));
        chemicalTanks.add(outputTank = VariableCapacityChemicalTank.output(this, MekanismConfig.general.spsOutputTankCapacity,
              chemical -> chemical.is(MekanismChemicals.ANTIMATTER), ChemicalAttributeValidator.ALWAYS_ALLOW, this));
    }

    @Override
    public void onCreated(Level world) {
        super.onCreated(world);
        deathZone = AABB.encapsulatingFullBlocks(getMinPos().offset(1, 1, 1), getMaxPos().offset(-1, -1, -1));
        advancementArea = deathZone.inflate(15);
    }

    private long getMaxInputGas() {
        return MekanismConfig.general.spsInputPerAntimatter.get() * 2L;
    }

    @Override
    public boolean tick(ServerLevel world) {
        boolean needsPacket = super.tick(world);
        double processed = 0;
        couldOperate = canOperate();
        if (couldOperate && receivedEnergy > 0L) {
            double lastProgress = progress;
            final int inputPerAntimatter = MekanismConfig.general.spsInputPerAntimatter.get();
            int inputNeeded = (inputPerAntimatter - inputProcessed) + inputPerAntimatter * (outputTank.getNeededAsInt(outputTank.getResource()) - 1);
            double processable = (double) receivedEnergy / MekanismConfig.general.spsEnergyPerInput.get();
            if (processable + progress >= inputNeeded) {
                processed = process(inputNeeded);
                progress = 0;
            } else {
                processed = processable;
                progress += processable;
                int toProcess = MathUtils.clampToInt(progress);
                int actualProcessed = process(toProcess);
                if (actualProcessed < toProcess) {
                    //If we processed less than we intended to we need to adjust how much our values actually changed by
                    int processedDif = toProcess - actualProcessed;
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
        if (!chemicalOutputTargets.isEmpty() && !outputTank.isEmpty()) {
            ResourceUtils.emit(getActiveOutputs(chemicalOutputTargets), outputTank, null);
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
        chemicalOutputTargets.clear();
        for (ValveData valve : valves) {
            TileEntitySPSPort tile = WorldUtils.getTileEntity(TileEntitySPSPort.class, world, valve.location);
            if (tile != null) {
                tile.addChemicalTargetCapability(chemicalOutputTargets, valve.side);
            }
        }
    }

    @Override
    public void readUpdateTag(@NotNull ValueInput input) {
        super.readUpdateTag(input);
        coilData.read(input);
        lastReceivedEnergy = input.getLongOr(SerializationConstants.ENERGY_USAGE, lastReceivedEnergy);
        lastProcessed = input.getDoubleOr(SerializationConstants.LAST_PROCESSED, lastProcessed);
    }

    @Override
    public void writeUpdateTag(@NotNull ValueOutput output) {
        super.writeUpdateTag(output);
        coilData.write(output);
        output.putLong(SerializationConstants.ENERGY_USAGE, lastReceivedEnergy);
        output.putDouble(SerializationConstants.LAST_PROCESSED, lastProcessed);
    }

    @Override
    protected int getMultiblockRedstoneLevel() {
        return ResourceUtils.getRedstoneSignalFromContainer(inputTank);
    }

    private int process(int operations) {
        ChemicalResource inputResource = inputTank.getResource();
        if (operations == 0 || inputResource.isEmpty()) {
            return 0;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int processed = inputTank.extract(inputResource, operations, transaction, AutomationType.INTERNAL);
            //Limit how much input we actually increase the input processed by to how much we were actually able to remove from the input tank
            int totalProcessed = inputProcessed + processed;
            final int inputPerAntimatter = MekanismConfig.general.spsInputPerAntimatter.get();
            if (totalProcessed >= inputPerAntimatter) {
                int toAdd = totalProcessed / inputPerAntimatter;
                if (outputTank.insert(MekanismChemicals.ANTIMATTER.asResource(), toAdd, transaction, AutomationType.INTERNAL) < toAdd) {
                    return 0;
                }
                totalProcessed %= inputPerAntimatter;
            }
            if (totalProcessed != inputProcessed) {
                inputProcessed = totalProcessed;
                markDirty();
            }
            transaction.commit();
            return processed;
        }
    }

    private void kill(ServerLevel world) {
        if (lastReceivedEnergy > 0L && couldOperate && world.getRandom().nextInt() % SharedConstants.TICKS_PER_SECOND == 0) {
            List<Entity> entitiesToDie = world.getEntitiesOfClass(Entity.class, deathZone);
            if (!entitiesToDie.isEmpty()) {
                DamageSource damageSource = MekanismDamageTypes.SPS.source(world, deathZone.getCenter());
                LightningBolt lightningBolt = null;
                List<ServerPlayer> nearbyPlayers = null;
                for (Entity entity : entitiesToDie) {
                    if (entity.hurtServer(world, damageSource, lastReceivedEnergy / 1_000F) && entity.isAlive()) {
                        //If the entity is still alive, see if there is any special handling we want to do
                        if (entity.typeHolder().is(MekanismTags.Entities.VALID_SPS_EXPERIMENT)) {
                            if (lightningBolt == null) {
                                lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, world);
                                //Set the damage to zero so when we call thunderHit we don't actually hurt the entity
                                lightningBolt.setDamage(0);
                                lightningBolt.setVisualOnly(true);
                            }
                            if (!EventHooks.onEntityStruckByLightning(entity, lightningBolt)) {
                                //Keep track of the remaining fire ticks so that we can skip lighting it on fire as we are not actual lightning
                                int remainingFireTicks = entity.getRemainingFireTicks();
                                entity.thunderHit(world, lightningBolt);
                                entity.setRemainingFireTicks(remainingFireTicks);
                                //Trigger advancements for nearby players
                                if (nearbyPlayers == null) {
                                    nearbyPlayers = new ArrayList<>();
                                    for (ServerPlayer player : world.players()) {
                                        if (advancementArea.contains(player.position())) {
                                            nearbyPlayers.add(player);
                                        }
                                    }
                                }
                                for (ServerPlayer player : nearbyPlayers) {
                                    MekanismCriteriaTriggers.SPS_EXPERIMENT.value().trigger(player, entity.typeHolder());
                                }
                            }
                        }
                    }
                }
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
        return !inputTank.isEmpty() && outputTank.getNeededAsLong(outputTank.getResource()) > 0;
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

        public void write(@NotNull ValueOutput output) {
            if (!coilMap.isEmpty()) {
                ValueOutputList coilsOutput = output.childrenList(SerializationConstants.COILS);
                for (CoilData data : coilMap.values()) {
                    ValueOutput coilOutput = coilsOutput.addChild();
                    coilOutput.store(SerializationConstants.POSITION, BlockPos.CODEC, data.coilPos);
                    NBTUtils.writeEnum(coilOutput, SerializationConstants.SIDE, data.side);
                    coilOutput.putInt(SerializationConstants.LEVEL, data.prevLevel);
                }
            }
        }

        public void read(@NotNull ValueInput input) {
            coilMap.clear();
            ValueInputList coilsInput = input.childrenListOrEmpty(SerializationConstants.COILS);
            for (ValueInput coilInput : coilsInput) {
                Optional<BlockPos> pos = coilInput.read(SerializationConstants.POSITION, BlockPos.CODEC);
                if (pos.isPresent()) {
                    Direction side = Direction.from3DDataValue(coilInput.getIntOr(SerializationConstants.SIDE, 0));
                    CoilData data = new CoilData(pos.get(), side);
                    data.prevLevel = coilInput.getIntOr(SerializationConstants.LEVEL, data.prevLevel);
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
