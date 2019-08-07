package mekanism.common.tile.energy_cube;

import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.api.IConfigCardAccess;
import mekanism.api.TileNetworkList;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.SideData;
import mekanism.common.base.IBlockProvider;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ITierUpgradeable;
import mekanism.common.block.BlockEnergyCube;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.tier.BaseTier;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.util.CableUtils;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

public abstract class TileEntityEnergyCube extends TileEntityMekanism implements IComputerIntegration, ISideConfiguration, ITierUpgradeable, IConfigCardAccess,
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

    /**
     * A block used to store and transfer electricity.
     */
    public TileEntityEnergyCube(IBlockProvider blockProvider) {
        super(blockProvider);
        this.tier = ((BlockEnergyCube) blockProvider.getBlock()).getTier();

        configComponent = new TileComponentConfig(this, TransmissionType.ENERGY, TransmissionType.ITEM);

        configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Charge", EnumColor.DARK_BLUE, new int[]{0}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Discharge", EnumColor.DARK_RED, new int[]{1}));

        configComponent.setConfig(TransmissionType.ITEM, new byte[]{0, 0, 0, 0, 2, 1});
        configComponent.setCanEject(TransmissionType.ITEM, false);
        configComponent.setIOConfig(TransmissionType.ENERGY);
        configComponent.setEjecting(TransmissionType.ENERGY, true);

        ejectorComponent = new TileComponentEjector(this);
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            ChargeUtils.charge(0, this);
            ChargeUtils.discharge(1, this);
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
        tier = EnergyCubeTier.values()[upgradeTier.ordinal()];
        Mekanism.packetHandler.sendUpdatePacket(this);
        markDirty();
        return true;
    }

    @Nonnull
    @Override
    public String getName() {
        return LangUtils.localize("tile.EnergyCube" + tier.getBaseTier().getSimpleName() + ".name");
    }

    @Override
    public double getMaxOutput() {
        return tier.getOutput();
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (slotID == 0) {
            return ChargeUtils.canBeCharged(itemstack);
        } else if (slotID == 1) {
            return ChargeUtils.canBeDischarged(itemstack);
        }
        return true;
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        return configComponent.hasSideForData(TransmissionType.ENERGY, getDirection(), 1, side);
    }

    @Override
    public boolean canOutputEnergy(Direction side) {
        return configComponent.hasSideForData(TransmissionType.ENERGY, getDirection(), 2, side);
    }

    @Override
    public boolean canSetFacing(@Nonnull Direction facing) {
        return true;
    }

    @Override
    public double getMaxEnergy() {
        return tier.getMaxEnergy();
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull Direction side) {
        return configComponent.getOutput(TransmissionType.ITEM, side, getDirection()).availableSlots;
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull Direction side) {
        if (slotID == 1) {
            return ChargeUtils.canBeOutputted(itemstack, false);
        } else if (slotID == 0) {
            return ChargeUtils.canBeOutputted(itemstack, true);
        }
        return false;
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
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            EnergyCubeTier prevTier = tier;
            tier = EnergyCubeTier.values()[dataStream.readInt()];
            if (prevTier != tier) {
                MekanismUtils.updateBlock(world, getPos());
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(tier.ordinal());
        return data;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        tier = EnergyCubeTier.values()[nbtTags.getInt("tier")];
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
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

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, Direction side) {
        return capability == Capabilities.CONFIG_CARD_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, Direction side) {
        //Special isCapabilityDisabled override not needed here as it already gets handled in TileEntityElectricBlock
        if (capability == Capabilities.CONFIG_CARD_CAPABILITY) {
            return Capabilities.CONFIG_CARD_CAPABILITY.cast(this);
        }
        return super.getCapability(capability, side);
    }
}