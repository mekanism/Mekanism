package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IConfigCardAccess;
import mekanism.api.RelativeSide;
import mekanism.api.TileNetworkList;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ITierUpgradeable;
import mekanism.common.block.BlockEnergyCube;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.inventory.IInventorySlotHolder;
import mekanism.common.inventory.InventorySlotHelper;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.tier.BaseTier;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.EnergySlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.util.CableUtils;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityEnergyCube extends TileEntityMekanism implements IComputerIntegration, ISideConfiguration, ITierUpgradeable, IConfigCardAccess,
      IComparatorSupport {

    private static final String[] methods = new String[]{"getEnergy", "getOutput", "getMaxEnergy", "getEnergyNeeded"};
    /**
     * This Energy Cube's tier.
     */
    public EnergyCubeTier tier;
    /**
     * The redstone level this Energy Cube is outputting at.
     */
    public int currentRedstoneLevel;
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
        this.tier = ((BlockEnergyCube) blockProvider.getBlock()).getTier();

        configComponent = new TileComponentConfig(this, TransmissionType.ENERGY, TransmissionType.ITEM);

        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            itemConfig.addSlotInfo(DataType.INPUT, new InventorySlotInfo(chargeSlot));
            itemConfig.addSlotInfo(DataType.OUTPUT, new InventorySlotInfo(dischargeSlot));
            //Set default config directions
            itemConfig.setDataType(RelativeSide.LEFT, DataType.INPUT);
            itemConfig.setDataType(RelativeSide.RIGHT, DataType.OUTPUT);

            itemConfig.setCanEject(false);
        }

        ConfigInfo energyConfig = configComponent.getConfig(TransmissionType.ENERGY);
        if (energyConfig != null) {
            energyConfig.addSlotInfo(DataType.INPUT, new EnergySlotInfo());
            energyConfig.addSlotInfo(DataType.OUTPUT, new EnergySlotInfo());
            //Set default config directions
            energyConfig.fill(DataType.INPUT);
            energyConfig.setDataType(RelativeSide.FRONT, DataType.OUTPUT);
            energyConfig.setEjecting(true);
        }

        ejectorComponent = new TileComponentEjector(this);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        //return configComponent.getOutput(TransmissionType.ITEM, side, getDirection()).availableSlots;
        //TODO: Some way to tie slots to a config component? So that we can filter by the config component?
        // This can probably be done by letting the configurations know the relative side information?
        InventorySlotHelper.Builder builder = InventorySlotHelper.Builder.forSide(this::getDirection);
        builder.addSlot(chargeSlot = EnergyInventorySlot.charge(this, 143, 35));
        builder.addSlot(dischargeSlot = EnergyInventorySlot.discharge(this, 17, 35));
        return builder.build();
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            ChargeUtils.charge(chargeSlot.getStack(), this);
            ChargeUtils.discharge(dischargeSlot.getStack(), this);
            if (MekanismUtils.canFunction(this) && configComponent.isEjecting(TransmissionType.ENERGY)) {
                CableUtils.emit(this);
            }
            int newScale = getScaledEnergyLevel(20);
            if (newScale != prevScale) {
                Mekanism.packetHandler.sendUpdatePacket(this);
            }
            prevScale = newScale;
        }
    }

    @Override
    public boolean upgrade(BaseTier upgradeTier) {
        if (upgradeTier.ordinal() != tier.ordinal() + 1) {
            return false;
        }
        tier = EnumUtils.ENERGY_CUBE_TIERS[upgradeTier.ordinal()];
        Mekanism.packetHandler.sendUpdatePacket(this);
        markDirty();
        return true;
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
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (isRemote()) {
            EnergyCubeTier prevTier = tier;
            tier = dataStream.readEnumValue(EnergyCubeTier.class);
            if (prevTier != tier) {
                MekanismUtils.updateBlock(getWorld(), getPos());
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(tier);
        return data;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        tier = EnumUtils.ENERGY_CUBE_TIERS[nbtTags.getInt("tier")];
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        //TODO: We shouldn't be having to save the "tier" to NBT given we have a different TileEntityType per tier?? Same with most of the other things that store tier
        nbtTags.putInt("tier", tier.ordinal());
        return nbtTags;
    }

    @Override
    public void setEnergy(double energy) {
        if (tier == EnergyCubeTier.CREATIVE && energy != Double.MAX_VALUE) {
            return;
        }
        super.setEnergy(energy);
        int newRedstoneLevel = getRedstoneLevel();
        if (newRedstoneLevel != currentRedstoneLevel) {
            markDirty();
            currentRedstoneLevel = newRedstoneLevel;
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
}