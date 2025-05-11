package mekanism.common.content.entangloporter;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.security.SecurityMode;
import mekanism.common.capabilities.chemical.IChemicalTracker;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.network.EnergyNetwork;
import mekanism.common.content.network.distribution.ChemicalHandlerTarget;
import mekanism.common.content.network.distribution.EnergyAcceptorTarget;
import mekanism.common.content.network.distribution.FluidHandlerTarget;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.EntangloporterInventorySlot;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.FluidUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InventoryFrequency extends Frequency implements IMekanismInventory, IMekanismFluidHandler, IMekanismStrictEnergyHandler, ITileHeatHandler, IChemicalTracker {

    @SuppressWarnings("removal")
    public static final Codec<InventoryFrequency> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          ExtraCodecs.NON_EMPTY_STRING.fieldOf(SerializationConstants.NAME).forGetter(Frequency::getName),
          UUIDUtil.CODEC.optionalFieldOf(SerializationConstants.OWNER_UUID).forGetter(freq -> Optional.ofNullable(freq.getOwner())),
          SecurityMode.CODEC.fieldOf(SerializationConstants.SECURITY_MODE).forGetter(Frequency::getSecurity),
          SerializerHelper.POSITIVE_LONG_CODEC_LEGACY.fieldOf(SerializationConstants.ENERGY).forGetter(freq -> freq.storedEnergy.getEnergy()),
          SerializerHelper.LENIENT_OPTIONAL_FLUID_CODEC.fieldOf(SerializationConstants.FLUID).forGetter(freq -> freq.storedFluid.getFluid()),
          ChemicalStack.LENIENT_OPTIONAL_CODEC.optionalFieldOf(SerializationConstants.CHEMICAL).forGetter(freq -> Optional.of(freq.storedChemical.getStack())),
          SerializerHelper.LENIENT_OPTIONAL_STACK_CODEC.fieldOf(SerializationConstants.ITEM).forGetter(freq -> freq.storedItem.getStack()),
          Codec.DOUBLE.fieldOf(SerializationConstants.HEAT_STORED).forGetter(freq -> freq.storedHeat.getHeat()),
          Codec.DOUBLE.fieldOf(SerializationConstants.HEAT_CAPACITY).forGetter(freq -> freq.storedHeat.getHeatCapacity()),

          //TODO - 1.22: remove backcompat and change Chemical field to non-optional
          ChemicalStack.LENIENT_OPTIONAL_CODEC.optionalFieldOf("gas").forGetter(freq -> Optional.empty()),
          ChemicalStack.LENIENT_OPTIONAL_CODEC.optionalFieldOf("infuse_type").forGetter(freq -> Optional.empty()),
          ChemicalStack.LENIENT_OPTIONAL_CODEC.optionalFieldOf("pigment").forGetter(freq -> Optional.empty()),
          ChemicalStack.LENIENT_OPTIONAL_CODEC.optionalFieldOf("slurry").forGetter(freq -> Optional.empty())
    ).apply(instance, (name, owner, securityMode, energy, fluid, chemical, item, heat, heatCapacity, legacyGas, legacyInfuse, legacyPigment, legacySlurry) -> {
        InventoryFrequency frequency = new InventoryFrequency(name, owner.orElse(null), securityMode);
        frequency.storedEnergy.setEnergy(energy);
        frequency.storedFluid.setStackUnchecked(fluid);
        //TODO - 1.22: remove backcompat and change Chemical field to non-optional (but keep the stack itself as an optional codec)
        if (chemical.isPresent()) {
            frequency.storedChemical.setStackUnchecked(chemical.get());
        } else if (legacyGas.isPresent() && !legacyGas.get().isEmpty()) {
            frequency.storedChemical.setStackUnchecked(legacyGas.get());
        } else if (legacyInfuse.isPresent() && !legacyInfuse.get().isEmpty()) {
            frequency.storedChemical.setStackUnchecked(legacyInfuse.get());
        } else if (legacyPigment.isPresent() && !legacyPigment.get().isEmpty()) {
            frequency.storedChemical.setStackUnchecked(legacyPigment.get());
        } else if (legacySlurry.isPresent() && !legacySlurry.get().isEmpty()) {
            frequency.storedChemical.setStackUnchecked(legacySlurry.get());
        }
        frequency.storedItem.setStackUnchecked(item);
        frequency.storedHeat.setHeat(heat);
        frequency.storedHeat.setHeatCapacity(heatCapacity, false);
        return frequency;
    }));
    public static final StreamCodec<RegistryFriendlyByteBuf, InventoryFrequency> STREAM_CODEC = StreamCodec.composite(
          baseStreamCodec(InventoryFrequency::new), Function.identity(),
          ByteBufCodecs.VAR_LONG, freq -> freq.storedEnergy.getEnergy(),
          FluidStack.OPTIONAL_STREAM_CODEC, freq -> freq.storedFluid.getFluid(),
          ChemicalStack.OPTIONAL_STREAM_CODEC, freq -> freq.storedChemical.getStack(),
          ItemStack.OPTIONAL_STREAM_CODEC, freq -> freq.storedItem.getStack(),
          ByteBufCodecs.DOUBLE, freq -> freq.storedHeat.getHeat(),
          (frequency, energy, fluid, chemical, item, heat) -> {
              frequency.storedEnergy.setEnergy(energy);
              frequency.storedFluid.setStack(fluid);
              frequency.storedChemical.setStack(chemical);
              frequency.storedItem.setStack(item);
              frequency.storedHeat.setHeat(heat);
              return frequency;
          }
    );

    //nb: we don't need to store these BLockPos as longs because they already exist on the Tiles as a field
    private final Table<ResourceKey<Level>, BlockPos, TileEntityQuantumEntangloporter> activeQEs = Tables.newCustomTable(new IdentityHashMap<>(), TreeMap::new);
    private long lastEject = -1;

    private BasicFluidTank storedFluid;
    private IChemicalTank storedChemical;
    private BasicInventorySlot storedItem;
    public IEnergyContainer storedEnergy;
    private BasicHeatCapacitor storedHeat;

    private List<IInventorySlot> inventorySlots;
    private List<IChemicalTank> chemicalTanks;
    private List<IExtendedFluidTank> fluidTanks;
    private List<IEnergyContainer> energyContainers;
    private List<IHeatCapacitor> heatCapacitors;

    /**
     * @param uuid Should only be null if we have incomplete data that we are loading
     */
    public InventoryFrequency(String n, @Nullable UUID uuid, SecurityMode securityMode) {
        super(FrequencyType.INVENTORY, n, uuid, securityMode);
        presetVariables();
    }

    private InventoryFrequency(String name, @Nullable UUID owner, String ownerName, SecurityMode securityMode) {
        super(FrequencyType.INVENTORY, name, owner, ownerName, securityMode);
        presetVariables();
    }

    private void presetVariables() {
        fluidTanks = Collections.singletonList(storedFluid = BasicFluidTank.create(MekanismConfig.general.entangloporterFluidBuffer.get(), this));
        chemicalTanks = Collections.singletonList(storedChemical = BasicChemicalTank.create(MekanismConfig.general.entangloporterChemicalBuffer.get(), this));
        inventorySlots = Collections.singletonList(storedItem = EntangloporterInventorySlot.create(this));
        energyContainers = Collections.singletonList(storedEnergy = BasicEnergyContainer.create(MekanismConfig.general.entangloporterEnergyBuffer.getAsLong(), this));
        heatCapacitors = Collections.singletonList(storedHeat = BasicHeatCapacitor.create(HeatAPI.DEFAULT_HEAT_CAPACITY, HeatAPI.DEFAULT_INVERSE_CONDUCTION,
              1_000, null, this));
    }

    @NotNull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return inventorySlots;
    }

    @NotNull
    @Override
    public List<IChemicalTank> getChemicalTanks(@Nullable Direction side) {
        return chemicalTanks;
    }

    @NotNull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return fluidTanks;
    }

    @NotNull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return energyContainers;
    }

    @NotNull
    @Override
    public List<IHeatCapacitor> getHeatCapacitors(@Nullable Direction side) {
        return heatCapacitors;
    }

    @Override
    public void onContentsChanged() {
        dirty = true;
    }

    @Override
    public boolean update(BlockEntity tile) {
        boolean changedData = super.update(tile);
        if (tile instanceof TileEntityQuantumEntangloporter entangloporter) {
            //This should always be the case, but validate it and remove if it isn't
            activeQEs.put(tile.getLevel().dimension(), entangloporter.getBlockPos(), entangloporter);
        } else {
            activeQEs.remove(tile.getLevel().dimension(), tile.getBlockPos());
        }
        return changedData;
    }

    @Override
    public boolean onDeactivate(BlockEntity tile) {
        boolean changedData = super.onDeactivate(tile);
        activeQEs.remove(tile.getLevel().dimension(), tile.getBlockPos());
        return changedData;
    }

    public void handleEject(long gameTime) {
        if (isValid() && !activeQEs.isEmpty() && lastEject != gameTime) {
            lastEject = gameTime;
            Map<TransmissionType, Consumer<?>> typesToEject = new EnumMap<>(TransmissionType.class);
            //All but heat and item
            List<Runnable> transferHandlers = new ArrayList<>(EnumUtils.TRANSMISSION_TYPES.length - 2);
            int expected = 6 * activeQEs.size();
            addEnergyTransferHandler(typesToEject, transferHandlers, expected);
            addFluidTransferHandler(typesToEject, transferHandlers, expected);
            addChemicalTransferHandler(typesToEject, transferHandlers, expected);
            if (!typesToEject.isEmpty()) {
                //If we have at least one type to eject (we are not entirely empty)
                // then go through all the QEs and build up the target locations
                for (TileEntityQuantumEntangloporter qe : activeQEs.values()) {
                    if (!qe.canFunction()) {
                        //Skip trying to eject for this QE if it can't function
                        continue;
                    }
                    ServerLevel level = (ServerLevel) qe.getLevel();
                    if (level == null || !level.shouldTickBlocksAt(ChunkPos.asLong(qe.getBlockPos()))) {
                        //Skip QEs that aren't supposed to be ticking
                        continue;
                    }
                    Direction facing = qe.getDirection();
                    for (Map.Entry<TransmissionType, Consumer<?>> entry : typesToEject.entrySet()) {
                        TransmissionType transmissionType = entry.getKey();
                        ConfigInfo config = qe.getConfig().getConfig(transmissionType);
                        //Validate the ejector for the config allows ejecting this transmission type. In theory, we already check all
                        // of this except config#isEjecting before we get here, but we do so anyway for consistency
                        if (config != null && qe.getEjector().isEjecting(config, transmissionType)) {
                            for (Map.Entry<RelativeSide, DataType> sideEntry : config.getSideConfig()) {
                                if (sideEntry.getValue().canOutput()) {
                                    Direction side = sideEntry.getKey().getDirection(facing);
                                    accept(entry.getValue(), qe, side, transmissionType);
                                }
                            }
                        }
                    }
                }
                //Run all our transfer handlers that we have
                for (Runnable transferHandler : transferHandlers) {
                    transferHandler.run();
                }
            }
        }
    }

    private static <TYPE> void accept(Consumer<TYPE> consumer, TileEntityQuantumEntangloporter qe, Direction side, TransmissionType transmissionType) {
        TYPE cachedCapability = qe.getCachedCapability(side, transmissionType);
        if (cachedCapability != null) {
            consumer.accept(cachedCapability);
        }
    }

    private void addEnergyTransferHandler(Map<TransmissionType, Consumer<?>> typesToEject, List<Runnable> transferHandlers, int expected) {
        long toSend = storedEnergy.extract(storedEnergy.getMaxEnergy(), Action.SIMULATE, AutomationType.INTERNAL);
        if (toSend > 0L) {
            SendingEnergyAcceptorTarget target = new SendingEnergyAcceptorTarget(expected, storedEnergy, toSend);
            typesToEject.put(TransmissionType.ENERGY, target);
            transferHandlers.add(target);
        }
    }

    private void addFluidTransferHandler(Map<TransmissionType, Consumer<?>> typesToEject, List<Runnable> transferHandlers, int expected) {
        FluidStack fluidToSend = storedFluid.extract(storedFluid.getCapacity(), Action.SIMULATE, AutomationType.INTERNAL);
        if (!fluidToSend.isEmpty()) {
            SendingFluidHandlerTarget target = new SendingFluidHandlerTarget(fluidToSend, expected, storedFluid);
            typesToEject.put(TransmissionType.FLUID, target);
            transferHandlers.add(target);
        }
    }

    private void addChemicalTransferHandler(Map<TransmissionType, Consumer<?>> typesToEject, List<Runnable> transferHandlers, int expected) {
        ChemicalStack toSend = storedChemical.extract(storedChemical.getCapacity(), Action.SIMULATE, AutomationType.INTERNAL);
        if (!toSend.isEmpty()) {
            SendingChemicalHandlerTarget target = new SendingChemicalHandlerTarget(toSend, expected, storedChemical);
            typesToEject.put(TransmissionType.CHEMICAL, target);
            transferHandlers.add(target);
        }
    }

    private static class SendingEnergyAcceptorTarget extends EnergyAcceptorTarget implements Runnable, Consumer<IStrictEnergyHandler> {

        private final IEnergyContainer storedEnergy;
        private final long toSend;

        public SendingEnergyAcceptorTarget(int expectedSize, IEnergyContainer storedEnergy, long toSend) {
            super(expectedSize);
            this.storedEnergy = storedEnergy;
            this.toSend = toSend;
        }

        @Override
        public void run() {
            if (getHandlerCount() > 0) {
                storedEnergy.extract(EmitUtils.sendToAcceptors(this, toSend, EnergyNetwork.ENERGY), Action.EXECUTE, AutomationType.INTERNAL);
            }
        }

        @Override
        public void accept(IStrictEnergyHandler handler) {
            addHandler(handler);
        }
    }

    private static class SendingFluidHandlerTarget extends FluidHandlerTarget implements Runnable, Consumer<IFluidHandler> {

        private final FluidStack toSend;
        private final IExtendedFluidTank storedFluid;

        public SendingFluidHandlerTarget(@NotNull FluidStack toSend, int expectedSize, IExtendedFluidTank storedFluid) {
            super(expectedSize);
            this.toSend = toSend;
            this.storedFluid = storedFluid;
        }

        @Override
        public void run() {
            if (getHandlerCount() > 0) {
                storedFluid.extract(EmitUtils.sendToAcceptors(this, toSend.getAmount(), toSend), Action.EXECUTE, AutomationType.INTERNAL);
            }
        }

        @Override
        public void accept(IFluidHandler handler) {
            if (FluidUtils.canFill(handler, toSend)) {
                addHandler(handler);
            }
        }
    }

    private static class SendingChemicalHandlerTarget extends ChemicalHandlerTarget implements Runnable, Consumer<IChemicalHandler> {

        private final ChemicalStack toSend;
        private final IChemicalTank storedChemical;

        public SendingChemicalHandlerTarget(ChemicalStack toSend, int expectedSize, IChemicalTank storedChemical) {
            super(expectedSize);
            this.toSend = toSend;
            this.storedChemical = storedChemical;
        }

        @Override
        public void run() {
            if (getHandlerCount() > 0) {
                storedChemical.extract(EmitUtils.sendToAcceptors(this, toSend.getAmount(), toSend), Action.EXECUTE, AutomationType.INTERNAL);
            }
        }

        @Override
        public void accept(IChemicalHandler handler) {
            if (ChemicalUtil.canInsert(handler, toSend)) {
                addHandler(handler);
            }
        }
    }
}