package mekanism.common.tile;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.IIncrementalEnum;
import mekanism.api.MekanismAPI;
import mekanism.api.RelativeSide;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ITierUpgradeable;
import mekanism.common.block.BlockGasTank;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.inventory.slot.holder.IInventorySlotHolder;
import mekanism.common.inventory.slot.holder.InventorySlotHelper;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tier.BaseTier;
import mekanism.common.tier.GasTankTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.GasSlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
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

    private GasInventorySlot drainSlot;
    private GasInventorySlot fillSlot;

    public TileEntityGasTank(IBlockProvider blockProvider) {
        super(blockProvider);
        configComponent = new TileComponentConfig(this, TransmissionType.GAS, TransmissionType.ITEM);

        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            itemConfig.addSlotInfo(DataType.INPUT, new InventorySlotInfo(drainSlot));
            itemConfig.addSlotInfo(DataType.OUTPUT, new InventorySlotInfo(fillSlot));
            //Set default config directions
            itemConfig.setDataType(RelativeSide.TOP, DataType.INPUT);
            itemConfig.setDataType(RelativeSide.BOTTOM, DataType.OUTPUT);

            itemConfig.setCanEject(false);
        }

        ConfigInfo gasConfig = configComponent.getConfig(TransmissionType.GAS);
        if (gasConfig != null) {
            gasConfig.addSlotInfo(DataType.INPUT, new GasSlotInfo(gasTank));
            gasConfig.addSlotInfo(DataType.OUTPUT, new GasSlotInfo(gasTank));
            //Set default config directions
            gasConfig.fill(DataType.INPUT);
            gasConfig.setDataType(RelativeSide.FRONT, DataType.OUTPUT);
            gasConfig.setEjecting(true);
        }

        dumping = GasMode.IDLE;

        ejectorComponent = new TileComponentEjector(this);
    }

    @Override
    protected void presetVariables() {
        tier = ((BlockGasTank) getBlockType()).getTier();
        gasTank = new GasTank(tier.getStorage());
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addSlot(drainSlot = GasInventorySlot.drain(gasTank, this, 8, 8));
        builder.addSlot(fillSlot = GasInventorySlot.fill(gasTank, gas -> true, this, 8, 40));
        return builder.build();
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            TileUtils.drawGas(drainSlot.getStack(), gasTank, Action.get(tier != GasTankTier.CREATIVE));
            if (TileUtils.receiveGas(fillSlot.getStack(), gasTank) && tier == GasTankTier.CREATIVE && !gasTank.isEmpty()) {
                gasTank.setStack(new GasStack(gasTank.getStack(), Integer.MAX_VALUE));
            }
            if (!gasTank.isEmpty() && MekanismUtils.canFunction(this) && (tier == GasTankTier.CREATIVE || dumping != GasMode.DUMPING)) {
                ConfigInfo config = configComponent.getConfig(TransmissionType.GAS);
                if (config != null && config.isEjecting()) {
                    Set<Direction> sidesForData = config.getSidesForData(DataType.OUTPUT);
                    if (!sidesForData.isEmpty()) {
                        GasStack toSend = new GasStack(gasTank.getStack(), Math.min(gasTank.getStored(), tier.getOutput()));
                        gasTank.drain(GasUtils.emit(toSend, this, sidesForData), Action.get(tier != GasTankTier.CREATIVE));
                    }
                }
            }

            if (tier != GasTankTier.CREATIVE) {
                if (dumping == GasMode.DUMPING) {
                    gasTank.drain(tier.getStorage() / 400, Action.EXECUTE);
                }
                if (dumping == GasMode.DUMPING_EXCESS && gasTank.getNeeded() < tier.getOutput()) {
                    gasTank.drain(tier.getOutput() - gasTank.getNeeded(), Action.EXECUTE);
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
        //TODO: Upgrade
        /*if (upgradeTier.ordinal() != tier.ordinal() + 1) {
            return false;
        }
        tier = EnumUtils.GAS_TANK_TIERS[upgradeTier.ordinal()];
        gasTank.setCapacity(tier.getStorage());
        Mekanism.packetHandler.sendUpdatePacket(this);
        markDirty();*/
        return true;
    }

    @Override
    public int receiveGas(Direction side, @Nonnull GasStack stack, Action action) {
        if (tier == GasTankTier.CREATIVE) {
            return stack.getAmount();
        }
        return gasTank.fill(stack, action);
    }

    @Nonnull
    @Override
    public GasStack drawGas(Direction side, int amount, Action action) {
        if (canDrawGas(side, MekanismAPI.EMPTY_GAS)) {
            return gasTank.drain(amount, action.combine(tier != GasTankTier.CREATIVE));
        }
        return GasStack.EMPTY;
    }

    @Override
    public boolean canDrawGas(Direction side, @Nonnull Gas type) {
        ISlotInfo slotInfo = configComponent.getSlotInfo(TransmissionType.GAS, side);
        if (slotInfo instanceof GasSlotInfo) {
            GasSlotInfo gasSlotInfo = (GasSlotInfo) slotInfo;
            return gasSlotInfo.canOutput() && gasSlotInfo.hasTank(gasTank) && gasTank.canDraw(type);
        }
        return false;
    }

    @Override
    public boolean canReceiveGas(Direction side, @Nonnull Gas type) {
        ISlotInfo slotInfo = configComponent.getSlotInfo(TransmissionType.GAS, side);
        if (slotInfo instanceof GasSlotInfo) {
            GasSlotInfo gasSlotInfo = (GasSlotInfo) slotInfo;
            return gasSlotInfo.canInput() && gasSlotInfo.hasTank(gasTank) && gasTank.canReceive(type);
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
        return configComponent.isCapabilityDisabled(capability, side) || super.isCapabilityDisabled(capability, side);
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        if (!isRemote()) {
            int type = dataStream.readInt();
            if (type == 0) {
                dumping = dumping.getNext();
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
        dumping = GasMode.byIndexStatic(nbtTags.getInt("dumping"));
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

    public enum GasMode implements IIncrementalEnum<GasMode>, IHasTranslationKey {
        IDLE("gui.mekanism.idle"),
        DUMPING_EXCESS("gui.mekanism.dumping_excess"),
        DUMPING("gui.mekanism.dumping");

        private static final GasMode[] MODES = values();
        private final String langKey;

        GasMode(String langKey) {
            this.langKey = langKey;
        }

        @Override
        public String getTranslationKey() {
            return langKey;
        }

        @Nonnull
        @Override
        public GasMode byIndex(int index) {
            return byIndexStatic(index);
        }

        public static GasMode byIndexStatic(int index) {
            //TODO: Is it more efficient to check if index is negative and then just do the normal mod way?
            return MODES[Math.floorMod(index, MODES.length)];
        }
    }
}