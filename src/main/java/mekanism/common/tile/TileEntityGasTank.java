package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.MekanismAPI;
import mekanism.api.TileNetworkList;
import mekanism.api.chemical.ChemicalAction;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.SideData;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ITierUpgradeable;
import mekanism.common.block.BlockGasTank;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tier.BaseTier;
import mekanism.common.tier.GasTankTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.GasUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityGasTank extends TileEntityMekanism implements IGasHandler, ISideConfiguration, ITierUpgradeable, IComputerIntegration, IComparatorSupport {

    private static final String[] methods = new String[]{"getMaxGas", "getStoredGas", "getGas"};
    /**
     * The type of gas stored in this tank.
     */
    public GasTank gasTank;

    public GasTankTier tier;

    public GasMode dumping;

    public int currentGasAmount;

    public int currentRedstoneLevel;

    public TileComponentEjector ejectorComponent;
    public TileComponentConfig configComponent;

    public TileEntityGasTank(IBlockProvider blockProvider) {
        super(blockProvider);
        this.tier = ((BlockGasTank) blockProvider.getBlock()).getTier();
        configComponent = new TileComponentConfig(this, TransmissionType.GAS, TransmissionType.ITEM);

        configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GRAY, InventoryUtils.EMPTY));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Fill", EnumColor.DARK_BLUE, new int[]{0}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Empty", EnumColor.DARK_RED, new int[]{1}));

        configComponent.setConfig(TransmissionType.ITEM, new byte[]{2, 1, 0, 0, 0, 0});
        configComponent.setCanEject(TransmissionType.ITEM, false);
        configComponent.setIOConfig(TransmissionType.GAS);
        configComponent.setEjecting(TransmissionType.GAS, true);

        gasTank = new GasTank(tier.getStorage());
        dumping = GasMode.IDLE;

        ejectorComponent = new TileComponentEjector(this);
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            TileUtils.drawGas(getStackInSlot(0), gasTank, ChemicalAction.get(tier != GasTankTier.CREATIVE));
            if (TileUtils.receiveGas(getStackInSlot(1), gasTank) && tier == GasTankTier.CREATIVE && !gasTank.isEmpty()) {
                gasTank.setStack(new GasStack(gasTank.getStack(), Integer.MAX_VALUE));
            }
            if (!gasTank.isEmpty() && MekanismUtils.canFunction(this) && (tier == GasTankTier.CREATIVE || dumping != GasMode.DUMPING)) {
                if (configComponent.isEjecting(TransmissionType.GAS)) {
                    GasStack toSend = new GasStack(gasTank.getStack(), Math.min(gasTank.getStored(), tier.getOutput()));
                    gasTank.drain(GasUtils.emit(toSend, this, configComponent.getSidesForData(TransmissionType.GAS, getDirection(), 2)), ChemicalAction.get(tier != GasTankTier.CREATIVE));
                }
            }

            if (tier != GasTankTier.CREATIVE) {
                if (dumping == GasMode.DUMPING) {
                    gasTank.drain(tier.getStorage() / 400, ChemicalAction.EXECUTE);
                }
                if (dumping == GasMode.DUMPING_EXCESS && gasTank.getNeeded() < tier.getOutput()) {
                    gasTank.drain(tier.getOutput() - gasTank.getNeeded(), ChemicalAction.EXECUTE);
                }
            }

            int newGasAmount = gasTank.getStored();
            if (newGasAmount != currentGasAmount) {
                MekanismUtils.saveChunk(this);
            }
            currentGasAmount = newGasAmount;
            int newRedstoneLevel = getRedstoneLevel();
            if (newRedstoneLevel != currentRedstoneLevel) {
                markDirty();
                currentRedstoneLevel = newRedstoneLevel;
            }
        }
    }

    @Override
    public boolean upgrade(BaseTier upgradeTier) {
        if (upgradeTier.ordinal() != tier.ordinal() + 1) {
            return false;
        }
        tier = EnumUtils.GAS_TANK_TIERS[upgradeTier.ordinal()];
        gasTank.setCapacity(tier.getStorage());
        Mekanism.packetHandler.sendUpdatePacket(this);
        markDirty();
        return true;
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull Direction side) {
        if (slotID == 1) {
            return itemstack.getItem() instanceof IGasItem && ((IGasItem) itemstack.getItem()).getGas(itemstack).isEmpty();
        } else if (slotID == 0) {
            if (itemstack.getItem() instanceof IGasItem) {
                IGasItem itemGas = (IGasItem) itemstack.getItem();
                GasStack gasInItem = itemGas.getGas(itemstack);
                return !gasInItem.isEmpty() && gasInItem.getAmount() == itemGas.getMaxGas(itemstack);
            }
        }
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (slotID == 0) {
            return itemstack.getItem() instanceof IGasItem && (gasTank.isEmpty() || ((IGasItem) itemstack.getItem()).canReceiveGas(itemstack, gasTank.getType()));
        } else if (slotID == 1) {
            return itemstack.getItem() instanceof IGasItem && (gasTank.isEmpty() || ((IGasItem) itemstack.getItem()).canProvideGas(itemstack, gasTank.getType()));
        }
        return false;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull Direction side) {
        return configComponent.getOutput(TransmissionType.ITEM, side, getDirection()).availableSlots;
    }

    @Override
    public int receiveGas(Direction side, @Nonnull GasStack stack, ChemicalAction action) {
        if (tier == GasTankTier.CREATIVE) {
            return stack.getAmount();
        }
        return gasTank.fill(stack, action);
    }

    @Nonnull
    @Override
    public GasStack drawGas(Direction side, int amount, ChemicalAction action) {
        if (canDrawGas(side, MekanismAPI.EMPTY_GAS)) {
            return gasTank.drain(amount, action.combine(tier != GasTankTier.CREATIVE));
        }
        return GasStack.EMPTY;
    }

    @Override
    public boolean canDrawGas(Direction side, @Nonnull Gas type) {
        if (configComponent.hasSideForData(TransmissionType.GAS, getDirection(), 2, side)) {
            return gasTank.canDraw(type);
        }
        return false;
    }

    @Override
    public boolean canReceiveGas(Direction side, @Nonnull Gas type) {
        if (configComponent.hasSideForData(TransmissionType.GAS, getDirection(), 1, side)) {
            return gasTank.canReceive(type);
        }
        return false;
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return new GasTankInfo[]{gasTank};
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return LazyOptional.empty();
        }
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return Capabilities.GAS_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        return configComponent.isCapabilityDisabled(capability, side, getDirection()) || super.isCapabilityDisabled(capability, side);
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        if (!isRemote()) {
            int type = dataStream.readInt();
            if (type == 0) {
                int index = (dumping.ordinal() + 1) % EnumUtils.GAS_MODES.length;
                dumping = EnumUtils.GAS_MODES[index];
            }
            for (PlayerEntity player : playersUsing) {
                Mekanism.packetHandler.sendTo(new PacketTileEntity(this), (ServerPlayerEntity) player);
            }

            return;
        }
        super.handlePacketData(dataStream);
        if (isRemote()) {
            GasTankTier prevTier = tier;
            tier = dataStream.readEnumValue(GasTankTier.class);
            gasTank.setCapacity(tier.getStorage());
            TileUtils.readTankData(dataStream, gasTank);
            dumping = dataStream.readEnumValue(GasMode.class);
            if (prevTier != tier) {
                MekanismUtils.updateBlock(getWorld(), getPos());
            }
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        tier = EnumUtils.GAS_TANK_TIERS[nbtTags.getInt("tier")];
        gasTank.read(nbtTags.getCompound("gasTank"));
        dumping = EnumUtils.GAS_MODES[nbtTags.getInt("dumping")];
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt("tier", tier.ordinal());
        nbtTags.put("gasTank", gasTank.write(new CompoundNBT()));
        nbtTags.putInt("dumping", dumping.ordinal());
        return nbtTags;
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(tier);
        TileUtils.addTankData(data, gasTank);
        data.add(dumping);
        return data;
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(gasTank.getStored(), gasTank.getCapacity());
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
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws NoSuchMethodException {
        switch (method) {
            case 0:
                return new Object[]{gasTank.getCapacity()};
            case 1:
                return new Object[]{gasTank.getStored()};
            case 2:
                return new Object[]{gasTank.getStack()};
            default:
                throw new NoSuchMethodException();
        }
    }

    public enum GasMode implements IHasTranslationKey {
        IDLE("gui.mekanism.idle"),
        DUMPING_EXCESS("gui.mekanism.dumping_excess"),
        DUMPING("gui.mekanism.dumping");

        private final String langKey;

        GasMode(String langKey) {
            this.langKey = langKey;
        }

        @Override
        public String getTranslationKey() {
            return langKey;
        }
    }
}