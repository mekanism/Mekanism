package mekanism.common.tile;

import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.energy.EnergyCubeEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.upgrade.EnergyCubeUpgradeData;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityEnergyCube extends TileEntityConfigurableMachine {

    public static final ModelProperty<CubeSideState[]> SIDE_STATE_PROPERTY = new ModelProperty<>();

    /**
     * This Energy Cube's tier.
     */
    private EnergyCubeTier tier;
    private float prevScale;

    private EnergyCubeEnergyContainer energyContainer;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getChargeItem")
    private EnergyInventorySlot chargeSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getDischargeItem")
    private EnergyInventorySlot dischargeSlot;

    /**
     * A block used to store and transfer electricity.
     */
    public TileEntityEnergyCube(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
        configComponent = new TileComponentConfig(this, TransmissionType.ENERGY, TransmissionType.ITEM);
        configComponent.setupIOConfig(TransmissionType.ITEM, chargeSlot, dischargeSlot, RelativeSide.FRONT, true).setCanEject(false);
        configComponent.setupIOConfig(TransmissionType.ENERGY, energyContainer, RelativeSide.FRONT).setEjecting(true);
        ejectorComponent = new TileComponentEjector(this, () -> tier.getOutput());
        ejectorComponent.setOutputData(configComponent, TransmissionType.ENERGY).setCanEject(type -> MekanismUtils.canFunction(this));
    }

    @Override
    protected void presetVariables() {
        super.presetVariables();
        tier = Attribute.getTier(getBlockType(), EnergyCubeTier.class);
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addContainer(energyContainer = EnergyCubeEnergyContainer.create(tier, listener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addSlot(dischargeSlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 17, 35));
        builder.addSlot(chargeSlot = EnergyInventorySlot.drain(energyContainer, listener, 143, 35));
        dischargeSlot.setSlotOverlay(SlotOverlay.MINUS);
        chargeSlot.setSlotOverlay(SlotOverlay.PLUS);
        return builder.build();
    }

    public EnergyCubeTier getTier() {
        return tier;
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        chargeSlot.drainContainer();
        dischargeSlot.fillContainerOrConvert();
        float newScale = MekanismUtils.getScale(prevScale, energyContainer);
        if (newScale != prevScale) {
            prevScale = newScale;
            sendUpdatePacket();
        }
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(energyContainer.getEnergy(), energyContainer.getMaxEnergy());
    }

    @Override
    protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
        return type == SubstanceType.ENERGY;
    }

    @Override
    public void parseUpgradeData(@NotNull IUpgradeData upgradeData) {
        if (upgradeData instanceof EnergyCubeUpgradeData data) {
            redstone = data.redstone;
            setControlType(data.controlType);
            getEnergyContainer().setEnergy(data.energyContainer.getEnergy());
            chargeSlot.setStack(data.chargeSlot.getStack());
            //Copy the contents using NBT so that if it is not actually valid due to a reload we don't crash
            dischargeSlot.deserializeNBT(data.dischargeSlot.serializeNBT());
            for (ITileComponent component : getComponents()) {
                component.read(data.components);
            }
        } else {
            super.parseUpgradeData(upgradeData);
        }
    }

    public EnergyCubeEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    @NotNull
    @Override
    public EnergyCubeUpgradeData getUpgradeData() {
        return new EnergyCubeUpgradeData(redstone, getControlType(), getEnergyContainer(), chargeSlot, dischargeSlot, getComponents());
    }

    public float getEnergyScale() {
        return prevScale;
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag() {
        CompoundTag updateTag = super.getReducedUpdateTag();
        updateTag.putFloat(NBTConstants.SCALE, prevScale);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag) {
        ConfigInfo config = getConfig().getConfig(TransmissionType.ENERGY);
        DataType[] currentConfig = new DataType[EnumUtils.SIDES.length];
        if (config != null) {
            for (RelativeSide side : EnumUtils.SIDES) {
                currentConfig[side.ordinal()] = config.getDataType(side);
            }
        }
        super.handleUpdateTag(tag);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> prevScale = scale);
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

    @NotNull
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
        return ModelData.builder().with(SIDE_STATE_PROPERTY, sideStates).build();
    }

    public enum CubeSideState {
        ACTIVE_LIT,
        ACTIVE_UNLIT,
        INACTIVE
    }
}