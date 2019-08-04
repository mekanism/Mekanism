package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.api.IConfigCardAccess;
import mekanism.api.TileNetworkList;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.SideData;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ITierUpgradeable;
import mekanism.common.block.BlockEnergyCube;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tier.BaseTier;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tile.base.TileEntityElectric;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.util.CableUtils;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityEnergyCube extends TileEntityElectric implements IComputerIntegration, IRedstoneControl, ISideConfiguration, ISecurityTile, ITierUpgradeable,
      IConfigCardAccess, IComparatorSupport {

    private static final String[] methods = new String[]{"getEnergy", "getOutput", "getMaxEnergy", "getEnergyNeeded"};
    /**
     * This Energy Cube's tier.
     */
    public EnergyCubeTier tier;
    /**
     * The redstone level this Energy Cube is outputting at.
     */
    public int currentRedstoneLevel;
    /**
     * This machine's current RedstoneControl type.
     */
    public RedstoneControl controlType;
    public int prevScale;
    public TileComponentEjector ejectorComponent;
    public TileComponentConfig configComponent;
    public TileComponentSecurity securityComponent;

    public TileEntityEnergyCube() {
        this((BlockEnergyCube) MekanismBlock.BASIC_ENERGY_CUBE.getBlock());
    }

    /**
     * A block used to store and transfer electricity.
     */
    public TileEntityEnergyCube(BlockEnergyCube energyCube) {
        super(energyCube);
        this.tier = energyCube.getTier();

        configComponent = new TileComponentConfig(this, TransmissionType.ENERGY, TransmissionType.ITEM);

        configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Charge", EnumColor.DARK_BLUE, new int[]{0}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Discharge", EnumColor.DARK_RED, new int[]{1}));

        configComponent.setConfig(TransmissionType.ITEM, new byte[]{0, 0, 0, 0, 2, 1});
        configComponent.setCanEject(TransmissionType.ITEM, false);
        configComponent.setIOConfig(TransmissionType.ENERGY);
        configComponent.setEjecting(TransmissionType.ENERGY, true);

        inventory = NonNullList.withSize(2, ItemStack.EMPTY);
        controlType = RedstoneControl.DISABLED;

        ejectorComponent = new TileComponentEjector(this);

        securityComponent = new TileComponentSecurity(this);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
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
    public boolean canReceiveEnergy(EnumFacing side) {
        return configComponent.hasSideForData(TransmissionType.ENERGY, getDirection(), 1, side);
    }

    @Override
    public boolean canOutputEnergy(EnumFacing side) {
        return configComponent.hasSideForData(TransmissionType.ENERGY, getDirection(), 2, side);
    }

    @Override
    public boolean canSetFacing(@Nonnull EnumFacing facing) {
        return true;
    }

    @Override
    public double getMaxEnergy() {
        return tier.getMaxEnergy();
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        return configComponent.getOutput(TransmissionType.ITEM, side, getDirection()).availableSlots;
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
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
                return new Object[]{(getMaxEnergy() - getEnergy())};
            default:
                throw new NoSuchMethodException();
        }
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            EnergyCubeTier prevTier = tier;
            tier = EnergyCubeTier.values()[dataStream.readInt()];
            controlType = RedstoneControl.values()[dataStream.readInt()];
            if (prevTier != tier) {
                MekanismUtils.updateBlock(world, getPos());
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(tier.ordinal());
        data.add(controlType.ordinal());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);
        tier = EnergyCubeTier.values()[nbtTags.getInteger("tier")];
        controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);
        nbtTags.setInteger("tier", tier.ordinal());
        nbtTags.setInteger("controlType", controlType.ordinal());
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
    public RedstoneControl getControlType() {
        return controlType;
    }

    @Override
    public void setControlType(RedstoneControl type) {
        controlType = type;
    }

    @Override
    public boolean canPulse() {
        return false;
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
    public EnumFacing getOrientation() {
        return getDirection();
    }

    @Override
    public TileComponentSecurity getSecurity() {
        return securityComponent;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        return capability == Capabilities.CONFIG_CARD_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        //Special isCapabilityDisabled override not needed here as it already gets handled in TileEntityElectricBlock
        if (capability == Capabilities.CONFIG_CARD_CAPABILITY) {
            return Capabilities.CONFIG_CARD_CAPABILITY.cast(this);
        }
        return super.getCapability(capability, side);
    }
}