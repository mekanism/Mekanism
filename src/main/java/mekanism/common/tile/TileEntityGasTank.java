package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.IIncrementalEnum;
import mekanism.api.RelativeSide;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.sustained.ISustainedData;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.MekanismLang;
import mekanism.common.base.IChemicalTankHolder;
import mekanism.common.base.ILangEntry;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ITileComponent;
import mekanism.common.base.handler.ChemicalTankHelper;
import mekanism.common.base.handler.GasTankGasTank;
import mekanism.common.block.BlockGasTank;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.inventory.slot.holder.IInventorySlotHolder;
import mekanism.common.inventory.slot.holder.InventorySlotHelper;
import mekanism.common.tier.GasTankTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.GasSlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.upgrade.GasTankUpgradeData;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.GasUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;

public class TileEntityGasTank extends TileEntityMekanism implements IGasHandler, ISideConfiguration, IComputerIntegration, ISustainedData {

    private static final String[] methods = new String[]{"getMaxGas", "getStoredGas", "getGas"};
    /**
     * The type of gas stored in this tank.
     */
    public GasTankGasTank gasTank;

    public GasTankTier tier;

    public GasMode dumping;

    public int currentGasAmount;

    public TileComponentEjector ejectorComponent;
    public TileComponentConfig configComponent;

    private GasInventorySlot drainSlot;
    private GasInventorySlot fillSlot;

    public TileEntityGasTank(IBlockProvider blockProvider) {
        super(blockProvider);
        configComponent = new TileComponentConfig(this, TransmissionType.GAS, TransmissionType.ITEM);

        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            //Note: we allow inputting and outputting in both directions as the slot is a processing slot
            itemConfig.addSlotInfo(DataType.INPUT, new InventorySlotInfo(true, true, drainSlot));
            itemConfig.addSlotInfo(DataType.OUTPUT, new InventorySlotInfo(true, true, fillSlot));
            //Set default config directions
            itemConfig.setDataType(RelativeSide.TOP, DataType.INPUT);
            itemConfig.setDataType(RelativeSide.BOTTOM, DataType.OUTPUT);

            itemConfig.setCanEject(false);
        }

        ConfigInfo gasConfig = configComponent.getConfig(TransmissionType.GAS);
        if (gasConfig != null) {
            gasConfig.addSlotInfo(DataType.INPUT, new GasSlotInfo(true, false, gasTank));
            gasConfig.addSlotInfo(DataType.OUTPUT, new GasSlotInfo(false, true, gasTank));
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
    }

    @Nonnull
    @Override
    protected IChemicalTankHolder<Gas, GasStack> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
        builder.addTank(gasTank = GasTankGasTank.create(tier, this));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addSlot(drainSlot = GasInventorySlot.drain(gasTank, this, 16, 16));
        builder.addSlot(fillSlot = GasInventorySlot.fill(gasTank, gas -> true, this, 16, 48));
        drainSlot.setSlotType(ContainerSlotType.OUTPUT);
        drainSlot.setSlotOverlay(SlotOverlay.PLUS);
        fillSlot.setSlotType(ContainerSlotType.INPUT);
        fillSlot.setSlotOverlay(SlotOverlay.MINUS);
        return builder.build();
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            //TODO: FIXME use logic via GasInventorySlot
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
                        gasTank.extract(GasUtils.emit(toSend, this, sidesForData), Action.get(tier != GasTankTier.CREATIVE));
                    }
                }
            }

            if (tier != GasTankTier.CREATIVE) {
                if (dumping == GasMode.DUMPING) {
                    gasTank.extract(tier.getStorage() / 400, Action.EXECUTE);
                }
                if (dumping == GasMode.DUMPING_EXCESS && gasTank.getNeeded() < tier.getOutput()) {
                    gasTank.extract(tier.getOutput() - gasTank.getNeeded(), Action.EXECUTE);
                }
            }

            int newGasAmount = gasTank.getStored();
            if (newGasAmount != currentGasAmount) {
                markDirty();
            }
            currentGasAmount = newGasAmount;
        }
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
        } else {
            super.handlePacketData(dataStream);
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        dumping = GasMode.byIndexStatic(nbtTags.getInt("dumping"));
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt("dumping", dumping.ordinal());
        return nbtTags;
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

    @Override
    public void parseUpgradeData(@Nonnull IUpgradeData upgradeData) {
        if (upgradeData instanceof GasTankUpgradeData) {
            GasTankUpgradeData data = (GasTankUpgradeData) upgradeData;
            redstone = data.redstone;
            setControlType(data.controlType);
            drainSlot.setStack(data.drainSlot.getStack());
            fillSlot.setStack(data.fillSlot.getStack());
            dumping = data.dumping;
            gasTank.setStack(data.stored);
            for (ITileComponent component : getComponents()) {
                component.read(data.components);
            }
        } else {
            super.parseUpgradeData(upgradeData);
        }
    }

    @Nonnull
    @Override
    public GasTankUpgradeData getUpgradeData() {
        return new GasTankUpgradeData(redstone, getControlType(), drainSlot, fillSlot, dumping, gasTank.getStack(), getComponents());
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (!gasTank.isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "stored", gasTank.getStack().write(new CompoundNBT()));
        }
        ItemDataUtils.setInt(itemStack, "dumping", dumping.ordinal());
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        gasTank.setStack(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "stored")));
        dumping = GasMode.byIndexStatic(ItemDataUtils.getInt(itemStack, "dumping"));
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new Object2ObjectOpenHashMap<>();
        remap.put("gasTank.stored", "stored");
        remap.put("dumping", "dumping");
        return remap;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableEnum.create(GasMode::byIndexStatic, GasMode.IDLE, () -> dumping, value -> dumping = value));
    }

    public enum GasMode implements IIncrementalEnum<GasMode>, IHasTextComponent {
        IDLE(MekanismLang.IDLE),
        DUMPING_EXCESS(MekanismLang.DUMPING_EXCESS),
        DUMPING(MekanismLang.DUMPING);

        private static final GasMode[] MODES = values();
        private final ILangEntry langEntry;

        GasMode(ILangEntry langEntry) {
            this.langEntry = langEntry;
        }

        @Override
        public ITextComponent getTextComponent() {
            return langEntry.translate();
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