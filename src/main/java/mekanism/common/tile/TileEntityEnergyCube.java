package mekanism.common.tile;

import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.energy.EnergyCubeEnergyContainer;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.container.MekContainerHelper;
import mekanism.common.capabilities.holder.energy.EnergyConfigHolder;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.containers.type.IContainerType;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.upgrade.EnergyCubeUpgradeData;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.UnknownNullability;

public class TileEntityEnergyCube extends TileEntityConfigurableMachine {

    public static final ModelProperty<CubeSideState[]> SIDE_STATE_PROPERTY = new ModelProperty<>();

    /**
     * This Energy Cube's tier.
     */
    private final EnergyCubeTier tier;
    private float prevScale;

    @UnknownNullability//Initialized via getInitialEnergyContainer
    private EnergyCubeEnergyContainer energyContainer;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getChargeItem", docPlaceholder = "charge slot")
    EnergyInventorySlot chargeSlot;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getDischargeItem", docPlaceholder = "discharge slot")
    EnergyInventorySlot dischargeSlot;

    /**
     * A block used to store and transfer electricity.
     */
    public TileEntityEnergyCube(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        EnergyCubeTier tier = Attribute.getTierNN(blockProvider, EnergyCubeTier.class);
        this.tier = tier;
        super(blockProvider, pos, state, tile -> TileComponentEjector.energy(tile, tier::getTransferRate));
        configComponent.setupIOConfig(TransmissionType.ITEM, chargeSlot, dischargeSlot, true).setCanEject(false);
        configComponent.setupIOConfig(TransmissionType.ENERGY, energyContainer);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ENERGY).setCanEject(_ -> canFunction());
    }

    @Override
    protected IEnergyContainerHolder getInitialEnergyContainer(IContentsListener listener) {
        energyContainer = EnergyCubeEnergyContainer.create(this, listener);
        return new EnergyConfigHolder(energyContainer, this);
    }

    @Override
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener) {
        MekContainerHelper<IInventorySlot> builder = MekContainerHelper.forSideWithItemConfig(this);
        builder.addContainer(dischargeSlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 17, 35));
        builder.addContainer(chargeSlot = EnergyInventorySlot.drain(energyContainer, listener, 143, 35));
        dischargeSlot.setSlotOverlay(SlotOverlay.MINUS);
        chargeSlot.setSlotOverlay(SlotOverlay.PLUS);
        return builder.build();
    }

    public EnergyCubeTier getTier() {
        return tier;
    }

    @Override
    protected boolean onUpdateServer(ServerLevel level) {
        boolean sendUpdatePacket = super.onUpdateServer(level);
        chargeSlot.drainContainerIntoSlot(null);
        dischargeSlot.fillContainerOrConvert(null);
        float newScale = MekanismUtils.getScale(prevScale, energyContainer);
        if (MekanismUtils.scaleChanged(newScale, prevScale)) {
            prevScale = newScale;
            sendUpdatePacket = true;
        }
        return sendUpdatePacket;
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(energyContainer.getAmountAsLong(), energyContainer.getCapacityAsLong());
    }

    @Override
    protected boolean makesComparatorDirty(IContainerType<?, ?> type) {
        return type == ContainerType.ENERGY;
    }

    @Override
    public void parseUpgradeData(IUpgradeData upgradeData, Provider provider, TransactionContext transaction) {
        if (upgradeData instanceof EnergyCubeUpgradeData data) {
            redstone = data.redstone;
            setControlType(data.controlType);
            energyContainer.copyContents(data.energyContainer, transaction);
            chargeSlot.copyContents(data.chargeSlot, transaction);
            dischargeSlot.copyContents(data.dischargeSlot, transaction);
            try (var reporter = new ProblemReporter.ScopedCollector(problemPath(), Mekanism.logger)) {
                ValueInput input = TagValueInput.create(reporter, provider, data.components);
                for (ITileComponent component : getComponents()) {
                    component.read(input);
                }
            }
        } else {
            super.parseUpgradeData(upgradeData, provider, transaction);
        }
    }

    public EnergyCubeEnergyContainer energyContainer() {
        return energyContainer;
    }

    @Override
    public EnergyCubeUpgradeData getUpgradeData(HolderLookup.Provider provider) {
        return new EnergyCubeUpgradeData(provider, redstone, getControlType(), energyContainer, chargeSlot, dischargeSlot, getComponents(), problemPath());
    }

    public float getEnergyScale() {
        return prevScale;
    }

    @Override
    public void writeReducedUpdatedTag(ValueOutput output) {
        super.writeReducedUpdatedTag(output);
        output.putFloat(SerializationConstants.SCALE, prevScale);
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        ConfigInfo config = getConfig().getConfig(TransmissionType.ENERGY);
        DataType[] currentConfig = new DataType[EnumUtils.SIDES.length];
        if (config != null) {
            for (RelativeSide side : EnumUtils.SIDES) {
                currentConfig[side.ordinal()] = config.getDataType(side);
            }
        }
        super.handleUpdateTag(input);
        prevScale = input.getFloatOr(SerializationConstants.SCALE, prevScale);
        if (config != null) {
            for (RelativeSide side : EnumUtils.SIDES) {
                if (currentConfig[side.ordinal()] != config.getDataType(side)) {
                    //Only update the model data if at least one side had the config change
                    updateModelData();
                    break;
                }
            }
        }
    }

    @Override
    public ModelData getModelData() {
        ConfigInfo config = getConfig().getConfig(TransmissionType.ENERGY);
        if (config == null) {//Should not happen but validate it anyway
            return super.getModelData();
        }
        CubeSideState[] sideStates = new CubeSideState[EnumUtils.SIDES.length];
        for (RelativeSide side : EnumUtils.SIDES) {
            CubeSideState state = CubeSideState.INACTIVE;
            ISlotInfo slotInfo = config.getSlotInfo(side);
            if (slotInfo != null) {
                if (slotInfo.canOutput()) {
                    state = CubeSideState.ACTIVE_LIT;
                } else if (slotInfo.canInput()) {
                    state = CubeSideState.ACTIVE_UNLIT;
                }
            }
            sideStates[side.ordinal()] = state;
        }
        return ModelData.of(SIDE_STATE_PROPERTY, sideStates);
    }

    public enum CubeSideState {
        INACTIVE,//NB: EnergyCubeModel relies on this being ordinal 0
        ACTIVE_LIT,
        ACTIVE_UNLIT
    }
}
