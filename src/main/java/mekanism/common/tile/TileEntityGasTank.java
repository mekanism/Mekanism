package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.IIncrementalEnum;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.sustained.ISustainedData;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.chemical.GasTankGasTank;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.slot.chemical.GasInventorySlot;
import mekanism.common.tier.GasTankTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.interfaces.IHasGasMode;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.upgrade.GasTankUpgradeData;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.GasUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;

public class TileEntityGasTank extends TileEntityMekanism implements ISideConfiguration, ISustainedData, IHasGasMode {

    /**
     * The type of gas stored in this tank.
     */
    public GasTankGasTank gasTank;

    public GasTankTier tier;

    public GasMode dumping;

    public long currentGasAmount;

    public TileComponentEjector ejectorComponent;
    public TileComponentConfig configComponent;

    private GasInventorySlot drainSlot;
    private GasInventorySlot fillSlot;

    public TileEntityGasTank(IBlockProvider blockProvider) {
        super(blockProvider);
        configComponent = new TileComponentConfig(this, TransmissionType.GAS, TransmissionType.ITEM);
        configComponent.setupIOConfig(TransmissionType.ITEM, drainSlot, fillSlot, RelativeSide.FRONT, true)
              .setCanEject(false);
        configComponent.setupIOConfig(TransmissionType.GAS, gasTank, gasTank, RelativeSide.FRONT)
              .setEjecting(true);

        dumping = GasMode.IDLE;

        ejectorComponent = new TileComponentEjector(this);
    }

    @Override
    protected void presetVariables() {
        tier = Attribute.getTier(getBlockType(), GasTankTier.class);
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(@Nonnull IMekanismGasHandler handler) {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
        builder.addTank(gasTank = GasTankGasTank.create(tier, handler));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addSlot(drainSlot = GasInventorySlot.drain(gasTank, this, 16, 16));
        builder.addSlot(fillSlot = GasInventorySlot.fill(gasTank, this, 16, 48));
        drainSlot.setSlotType(ContainerSlotType.OUTPUT);
        drainSlot.setSlotOverlay(SlotOverlay.PLUS);
        fillSlot.setSlotType(ContainerSlotType.INPUT);
        fillSlot.setSlotOverlay(SlotOverlay.MINUS);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        drainSlot.drainTank();
        fillSlot.fillTank();
        if (!gasTank.isEmpty() && MekanismUtils.canFunction(this) && (tier == GasTankTier.CREATIVE || dumping != GasMode.DUMPING)) {
            ConfigInfo config = configComponent.getConfig(TransmissionType.GAS);
            if (config != null && config.isEjecting()) {
                GasUtils.emit(config.getAllOutputtingSides(), gasTank, this, tier.getOutput());
            }
        }

        if (tier != GasTankTier.CREATIVE) {
            if (dumping == GasMode.DUMPING) {
                gasTank.shrinkStack(tier.getStorage() / 400, Action.EXECUTE);
            } else if (dumping == GasMode.DUMPING_EXCESS) {
                long needed = gasTank.getNeeded();
                if (needed < tier.getOutput()) {
                    gasTank.shrinkStack(tier.getOutput() - needed, Action.EXECUTE);
                }
            }
        }

        long newGasAmount = gasTank.getStored();
        if (newGasAmount != currentGasAmount) {
            markDirty(false);
        }
        currentGasAmount = newGasAmount;
    }

    @Override
    public void nextMode(int tank) {
        if (tank == 0) {
            dumping = dumping.getNext();
            markDirty(false);
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.DUMP_MODE, GasMode::byIndexStatic, mode -> dumping = mode);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt(NBTConstants.DUMP_MODE, dumping.ordinal());
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
        ItemDataUtils.setInt(itemStack, NBTConstants.DUMP_MODE, dumping.ordinal());
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        dumping = GasMode.byIndexStatic(ItemDataUtils.getInt(itemStack, NBTConstants.DUMP_MODE));
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new Object2ObjectOpenHashMap<>();
        remap.put(NBTConstants.DUMP_MODE, NBTConstants.DUMP_MODE);
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
            return MathUtils.getByIndexMod(MODES, index);
        }
    }
}