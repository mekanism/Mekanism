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
import mekanism.api.AutomationType;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.security.SecurityMode;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.network.EnergyNetwork;
import mekanism.common.content.network.distribution.EnergyAcceptorTarget;
import mekanism.common.content.network.distribution.ResourceHandlerTarget;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.EntangloporterInventorySlot;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyTypes;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InventoryFrequency extends Frequency implements ITileHeatHandler {

    public static final Codec<InventoryFrequency> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          ExtraCodecs.NON_EMPTY_STRING.fieldOf(SerializationConstants.NAME).forGetter(Frequency::getName),
          UUIDUtil.CODEC.optionalFieldOf(SerializationConstants.OWNER_UUID).forGetter(freq -> Optional.ofNullable(freq.getOwner())),
          SecurityMode.CODEC.fieldOf(SerializationConstants.SECURITY_MODE).forGetter(Frequency::getSecurity),
          SerializerHelper.POSITIVE_LONG_CODEC.fieldOf(SerializationConstants.ENERGY).forGetter(freq -> freq.storedEnergy.getEnergy()),
          SerializerHelper.LENIENT_OPTIONAL_FLUID_RESOURCE_STACK_CODEC.fieldOf(SerializationConstants.FLUID).forGetter(freq -> freq.storedFluid.asStack()),
          SerializerHelper.LENIENT_OPTIONAL_CHEMICAL_RESOURCE_STACK_CODEC.fieldOf(SerializationConstants.CHEMICAL).forGetter(freq -> freq.storedChemical.asStack()),
          SerializerHelper.LENIENT_OPTIONAL_ITEM_RESOURCE_STACK_CODEC.fieldOf(SerializationConstants.ITEM).forGetter(freq -> freq.storedItem.asStack()),
          Codec.DOUBLE.fieldOf(SerializationConstants.HEAT_STORED).forGetter(freq -> freq.storedHeat.getHeat()),
          Codec.DOUBLE.fieldOf(SerializationConstants.HEAT_CAPACITY).forGetter(freq -> freq.storedHeat.getHeatCapacity())
    ).apply(instance, (name, owner, securityMode, energy, fluid, chemical, item, heat, heatCapacity) -> {
        InventoryFrequency frequency = new InventoryFrequency(name, owner.orElse(null), securityMode);
        frequency.storedEnergy.setEnergy(energy);
        frequency.storedFluid.setContentsUnchecked(fluid.resource(), fluid.amount());
        frequency.storedChemical.setContentsUnchecked(chemical.resource(), chemical.amount());
        frequency.storedItem.setContentsUnchecked(item.resource(), item.amount());
        frequency.storedHeat.setHeat(heat);
        frequency.storedHeat.setHeatCapacity(heatCapacity, false);
        return frequency;
    }));
    public static final StreamCodec<RegistryFriendlyByteBuf, InventoryFrequency> STREAM_CODEC = StreamCodec.composite(
          baseStreamCodec(InventoryFrequency::new), Function.identity(),
          ByteBufCodecs.VAR_LONG, freq -> freq.storedEnergy.getEnergy(),
          SerializerHelper.FLUID_RESOURCE_STACK_STREAM_CODEC, freq -> freq.storedFluid.asStack(),
          SerializerHelper.CHEMICAL_RESOURCE_STACK_STREAM_CODEC, freq -> freq.storedChemical.asStack(),
          SerializerHelper.ITEM_RESOURCE_STACK_STREAM_CODEC, freq -> freq.storedItem.asStack(),
          ByteBufCodecs.DOUBLE, freq -> freq.storedHeat.getHeat(),
          (frequency, energy, fluid, chemical, item, heat) -> {
              frequency.storedEnergy.setEnergy(energy);
              //TODO - 26.1: Should these be set unchecked?
              frequency.storedFluid.setContents(fluid.resource(), fluid.amount());
              frequency.storedChemical.setContents(chemical.resource(), chemical.amount());
              frequency.storedItem.setContents(item.resource(), item.amount());
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
    private List<IFluidTank> fluidTanks;
    private List<IEnergyContainer> energyContainers;
    private List<IHeatCapacitor> heatCapacitors;

    /**
     * @param uuid Should only be null if we have incomplete data that we are loading
     */
    public InventoryFrequency(String n, @Nullable UUID uuid, SecurityMode securityMode) {
        super(FrequencyTypes.INVENTORY, n, uuid, securityMode);
        presetVariables();
    }

    private InventoryFrequency(String name, @Nullable UUID owner, String ownerName, SecurityMode securityMode) {
        super(FrequencyTypes.INVENTORY, name, owner, ownerName, securityMode);
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
    public List<IInventorySlot> getInventorySlots() {
        return inventorySlots;
    }

    @NotNull
    public List<IChemicalTank> getChemicalTanks() {
        return chemicalTanks;
    }

    @NotNull
    public List<IFluidTank> getFluidTanks() {
        return fluidTanks;
    }

    @NotNull
    public List<IEnergyContainer> getEnergyContainers() {
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
            List<TargetExecution> transferHandlers = new ArrayList<>(EnumUtils.TRANSMISSION_TYPES.length - 2);
            int expected = 6 * activeQEs.size();
            try (Transaction simulation = Transaction.openRoot()) {
                addEnergyTransferHandler(typesToEject, transferHandlers, expected, simulation);
                addResourceTransferHandler(typesToEject, transferHandlers, expected, TransmissionType.FLUID, storedFluid, simulation);
                addResourceTransferHandler(typesToEject, transferHandlers, expected, TransmissionType.CHEMICAL, storedChemical, simulation);
            }
            if (!typesToEject.isEmpty()) {
                //If we have at least one type to eject (we are not entirely empty)
                // then go through all the QEs and build up the target locations
                for (TileEntityQuantumEntangloporter qe : activeQEs.values()) {
                    if (!qe.canFunction()) {
                        //Skip trying to eject for this QE if it can't function
                        continue;
                    }
                    ServerLevel level = (ServerLevel) qe.getLevel();
                    if (level == null || !level.shouldTickBlocksAt(ChunkPos.pack(qe.getBlockPos()))) {
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
                try (Transaction transaction = Transaction.openRoot()) {
                    for (TargetExecution transferHandler : transferHandlers) {
                        if (transferHandler.getHandlerCount() > 0) {
                            transferHandler.extract(transaction);
                        }
                    }
                    transaction.commit();
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

    private void addEnergyTransferHandler(Map<TransmissionType, Consumer<?>> typesToEject, List<TargetExecution> transferHandlers, int expected, TransactionContext simulation) {
        long toSend = storedEnergy.extract(storedEnergy.getCapacity(), simulation, AutomationType.INTERNAL);
        if (toSend > 0L) {
            SendingEnergyAcceptorTarget target = new SendingEnergyAcceptorTarget(expected, storedEnergy, toSend);
            typesToEject.put(TransmissionType.ENERGY, target);
            transferHandlers.add(target);
        }
    }

    private <RESOURCE extends Resource> void addResourceTransferHandler(Map<TransmissionType, Consumer<?>> typesToEject, List<TargetExecution> transferHandlers,
          int expected, TransmissionType transmissionType, IResourceContainer<RESOURCE> container, TransactionContext simulation) {
        RESOURCE type = container.getResource();
        if (!type.isEmpty()) {
            int fluidToSend = container.extract(type, storedFluid.amountAsInt(), simulation, AutomationType.INTERNAL);
            if (fluidToSend > 0) {
                SendingResourceHandlerTarget<RESOURCE> target = new SendingResourceHandlerTarget<>(type, fluidToSend, expected, container);
                typesToEject.put(transmissionType, target);
                transferHandlers.add(target);
            }
        }
    }

    private interface TargetExecution {

        int getHandlerCount();

        void extract(TransactionContext transaction);
    }

    private static class SendingEnergyAcceptorTarget extends EnergyAcceptorTarget implements TargetExecution, Consumer<IStrictEnergyHandler> {

        private final IEnergyContainer storedEnergy;
        private final long toSend;

        public SendingEnergyAcceptorTarget(int expectedSize, IEnergyContainer storedEnergy, long toSend) {
            super(expectedSize);
            this.storedEnergy = storedEnergy;
            this.toSend = toSend;
        }

        @Override
        public void extract(TransactionContext transaction) {
            try (Transaction subTransaction = Transaction.open(transaction)) {
                long sent = EmitUtils.sendToAcceptors(this, toSend, EnergyNetwork.ENERGY, subTransaction);
                if (storedEnergy.extract(sent, transaction, AutomationType.INTERNAL) == sent) {
                    //If we were able to extract everything we thought we would be able to and had tried to send
                    // then commit all the changes
                    subTransaction.commit();
                }
            }
        }

        @Override
        public void accept(IStrictEnergyHandler handler) {
            addHandler(handler);
        }
    }

    private static class SendingResourceHandlerTarget<RESOURCE extends Resource> extends ResourceHandlerTarget<RESOURCE> implements TargetExecution, Consumer<ResourceHandler<RESOURCE>> {

        private final RESOURCE type;
        private final int toSend;
        private final IResourceContainer<RESOURCE> container;

        public SendingResourceHandlerTarget(RESOURCE type, int toSend, int expectedSize, IResourceContainer<RESOURCE> container) {
            super(expectedSize);
            this.type = type;
            this.toSend = toSend;
            this.container = container;
        }

        @Override
        public void extract(TransactionContext transaction) {
            try (Transaction subTransaction = Transaction.open(transaction)) {
                int sent = EmitUtils.sendToAcceptors(this, toSend, type, subTransaction);
                if (container.extract(type, sent, transaction, AutomationType.INTERNAL) == sent) {
                    //If we were able to extract everything we thought we would be able to and had tried to send
                    // then commit all the changes
                    subTransaction.commit();
                }
            }
        }

        @Override
        public void accept(ResourceHandler<RESOURCE> handler) {
            //TODO - 26.1: Is this called from a transactional context?
            try (Transaction simulation = Transaction.openRoot()) {
                if (handler.insert(type, toSend, simulation) > 0) {
                    addHandler(handler);
                }
            }
        }
    }
}