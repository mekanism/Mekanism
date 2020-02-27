package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IConfigCardAccess;
import mekanism.api.RelativeSide;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ITileComponent;
import mekanism.common.block.BlockEnergyCube;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.EnergySlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.upgrade.EnergyCubeUpgradeData;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.CableUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityEnergyCube extends TileEntityMekanism implements IComputerIntegration, ISideConfiguration, IConfigCardAccess {

    private static final String[] methods = new String[]{"getEnergy", "getOutput", "getMaxEnergy", "getEnergyNeeded"};
    /**
     * This Energy Cube's tier.
     */
    public EnergyCubeTier tier;//TODO: Make this private
    public int prevScale;
    public TileComponentEjector ejectorComponent;
    public TileComponentConfig configComponent;

    private EnergyInventorySlot chargeSlot;
    private EnergyInventorySlot dischargeSlot;

    /**
     * A block used to store and transfer electricity.
     */
    public TileEntityEnergyCube(IBlockProvider blockProvider) {
        super(blockProvider);

        configComponent = new TileComponentConfig(this, TransmissionType.ENERGY, TransmissionType.ITEM);

        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            //Note: we allow inputting and outputting in both directions as the slot is a processing slot
            itemConfig.addSlotInfo(DataType.INPUT, new InventorySlotInfo(true, true, chargeSlot));
            itemConfig.addSlotInfo(DataType.OUTPUT, new InventorySlotInfo(true, true, dischargeSlot));
            //Set default config directions
            itemConfig.setDataType(RelativeSide.LEFT, DataType.INPUT);
            itemConfig.setDataType(RelativeSide.RIGHT, DataType.OUTPUT);

            itemConfig.setCanEject(false);
        }

        ConfigInfo energyConfig = configComponent.getConfig(TransmissionType.ENERGY);
        if (energyConfig != null) {
            energyConfig.addSlotInfo(DataType.INPUT, new EnergySlotInfo(true, false));
            energyConfig.addSlotInfo(DataType.OUTPUT, new EnergySlotInfo(false, true));
            //Set default config directions
            energyConfig.fill(DataType.INPUT);
            energyConfig.setDataType(RelativeSide.FRONT, DataType.OUTPUT);
            energyConfig.setEjecting(true);
        }

        ejectorComponent = new TileComponentEjector(this);
    }

    @Override
    protected void presetVariables() {
        tier = ((BlockEnergyCube) getBlockType()).getTier();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        //TODO: When we are breaking inventories, we may want to switch the order of these so that discharge is defined first
        // as it is on the left
        builder.addSlot(chargeSlot = EnergyInventorySlot.charge(this, 143, 35));
        builder.addSlot(dischargeSlot = EnergyInventorySlot.discharge(this, 17, 35));
        dischargeSlot.setSlotOverlay(SlotOverlay.MINUS);
        chargeSlot.setSlotOverlay(SlotOverlay.PLUS);
        return builder.build();
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            chargeSlot.charge(this);
            dischargeSlot.discharge(this);
            if (MekanismUtils.canFunction(this) && configComponent.isEjecting(TransmissionType.ENERGY)) {
                CableUtils.emit(this);
            }
            int newScale = (int) (getEnergy() * 20 / getMaxEnergy());
            if (newScale != prevScale) {
                Mekanism.packetHandler.sendUpdatePacket(this);
            }
            prevScale = newScale;
        }
    }

    @Override
    public double getMaxOutput() {
        return tier.getOutput();
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        ISlotInfo slotInfo = configComponent.getSlotInfo(TransmissionType.ENERGY, side);
        return slotInfo instanceof EnergySlotInfo && slotInfo.canInput();
    }

    @Override
    public boolean canOutputEnergy(Direction side) {
        ISlotInfo slotInfo = configComponent.getSlotInfo(TransmissionType.ENERGY, side);
        return slotInfo instanceof EnergySlotInfo && slotInfo.canOutput();
    }

    @Override
    public double getMaxEnergy() {
        return tier.getMaxEnergy();
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws NoSuchMethodException {
        switch (method) {
            case 0:
                return new Object[]{getEnergy()};
            case 1:
                return new Object[]{tier.getOutput()};
            case 2:
                return new Object[]{getMaxEnergy()};
            case 3:
                return new Object[]{(getNeededEnergy())};
            default:
                throw new NoSuchMethodException();
        }
    }

    @Override
    public void setEnergy(double energy) {
        if (tier != EnergyCubeTier.CREATIVE || energy == Double.MAX_VALUE) {
            super.setEnergy(energy);
        }
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(getEnergy(), getMaxEnergy());
    }

    @Override
    public TileComponentEjector getEjector() {
        return ejectorComponent;
    }

    @Override
    public TileComponentConfig getConfig() {
        return configComponent;
    }

    @Override
    public Direction getOrientation() {
        return getDirection();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        //Special isCapabilityDisabled override not needed here as it already gets handled in TileEntityElectricBlock
        if (capability == Capabilities.CONFIG_CARD_CAPABILITY) {
            return Capabilities.CONFIG_CARD_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void parseUpgradeData(@Nonnull IUpgradeData upgradeData) {
        if (upgradeData instanceof EnergyCubeUpgradeData) {
            EnergyCubeUpgradeData data = (EnergyCubeUpgradeData) upgradeData;
            redstone = data.redstone;
            setControlType(data.controlType);
            setEnergy(data.electricityStored);
            chargeSlot.setStack(data.chargeSlot.getStack());
            dischargeSlot.setStack(data.dischargeSlot.getStack());
            for (ITileComponent component : getComponents()) {
                component.read(data.components);
            }
        } else {
            super.parseUpgradeData(upgradeData);
        }
    }

    @Nonnull
    @Override
    public EnergyCubeUpgradeData getUpgradeData() {
        return new EnergyCubeUpgradeData(redstone, getControlType(), getEnergy(), chargeSlot, dischargeSlot, getComponents());
    }
}