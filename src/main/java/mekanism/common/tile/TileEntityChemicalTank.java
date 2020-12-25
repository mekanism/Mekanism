package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.IIncrementalEnum;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.merged.MergedChemicalTank.Current;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.chemical.ChemicalTankChemicalTank;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.slot.chemical.MergedChemicalInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.interfaces.IHasGasMode;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.upgrade.ChemicalTankUpgradeData;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;

public class TileEntityChemicalTank extends TileEntityMekanism implements ISideConfiguration, ISustainedData, IHasGasMode {

    public final TileComponentEjector ejectorComponent;
    public final TileComponentConfig configComponent;
    public GasMode dumping = GasMode.IDLE;

    private MergedChemicalTank chemicalTank;
    private ChemicalTankTier tier;

    private MergedChemicalInventorySlot<MergedChemicalTank> drainSlot;
    private MergedChemicalInventorySlot<MergedChemicalTank> fillSlot;

    public TileEntityChemicalTank(IBlockProvider blockProvider) {
        super(blockProvider);
        configComponent = new TileComponentConfig(this, TransmissionType.GAS, TransmissionType.INFUSION, TransmissionType.PIGMENT, TransmissionType.SLURRY,
              TransmissionType.ITEM);
        configComponent.setupIOConfig(TransmissionType.ITEM, drainSlot, fillSlot, RelativeSide.FRONT, true).setCanEject(false);
        configComponent.setupIOConfig(TransmissionType.GAS, getGasTank(), getGasTank(), RelativeSide.FRONT).setEjecting(true);
        configComponent.setupIOConfig(TransmissionType.INFUSION, getInfusionTank(), getInfusionTank(), RelativeSide.FRONT).setEjecting(true);
        configComponent.setupIOConfig(TransmissionType.PIGMENT, getPigmentTank(), getPigmentTank(), RelativeSide.FRONT).setEjecting(true);
        configComponent.setupIOConfig(TransmissionType.SLURRY, getSlurryTank(), getSlurryTank(), RelativeSide.FRONT).setEjecting(true);
        ejectorComponent = new TileComponentEjector(this);
    }

    @Override
    protected void presetVariables() {
        super.presetVariables();
        tier = Attribute.getTier(getBlockType(), ChemicalTankTier.class);
        chemicalTank = ChemicalTankChemicalTank.create(tier, this);
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
        builder.addTank(getGasTank());
        return builder.build();
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> getInitialInfusionTanks() {
        ChemicalTankHelper<InfuseType, InfusionStack, IInfusionTank> builder = ChemicalTankHelper.forSideInfusionWithConfig(this::getDirection, this::getConfig);
        builder.addTank(getInfusionTank());
        return builder.build();
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> getInitialPigmentTanks() {
        ChemicalTankHelper<Pigment, PigmentStack, IPigmentTank> builder = ChemicalTankHelper.forSidePigmentWithConfig(this::getDirection, this::getConfig);
        builder.addTank(getPigmentTank());
        return builder.build();
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Slurry, SlurryStack, ISlurryTank> getInitialSlurryTanks() {
        ChemicalTankHelper<Slurry, SlurryStack, ISlurryTank> builder = ChemicalTankHelper.forSideSlurryWithConfig(this::getDirection, this::getConfig);
        builder.addTank(getSlurryTank());
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addSlot(drainSlot = MergedChemicalInventorySlot.drain(chemicalTank, this, 16, 16));
        builder.addSlot(fillSlot = MergedChemicalInventorySlot.fill(chemicalTank, this, 16, 48));
        drainSlot.setSlotType(ContainerSlotType.OUTPUT);
        drainSlot.setSlotOverlay(SlotOverlay.PLUS);
        fillSlot.setSlotType(ContainerSlotType.INPUT);
        fillSlot.setSlotOverlay(SlotOverlay.MINUS);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        drainSlot.drainChemicalTanks();
        fillSlot.fillChemicalTanks();
        Current current = chemicalTank.getCurrent();
        if (current != Current.EMPTY) {
            IChemicalTank<?, ?> currentTank = null;
            if (MekanismUtils.canFunction(this) && (tier == ChemicalTankTier.CREATIVE || dumping != GasMode.DUMPING)) {
                currentTank = getCurrentTank(current);
                if (current == Current.GAS) {
                    doAutoEject(TransmissionType.GAS, currentTank);
                } else if (current == Current.INFUSION) {
                    doAutoEject(TransmissionType.INFUSION, currentTank);
                } else if (current == Current.PIGMENT) {
                    doAutoEject(TransmissionType.PIGMENT, currentTank);
                } else if (current == Current.SLURRY) {
                    doAutoEject(TransmissionType.SLURRY, currentTank);
                }
            }
            if (tier != ChemicalTankTier.CREATIVE) {
                if (dumping == GasMode.DUMPING) {
                    getCurrentTank(currentTank, current).shrinkStack(tier.getStorage() / 400, Action.EXECUTE);
                } else if (dumping == GasMode.DUMPING_EXCESS) {
                    currentTank = getCurrentTank(currentTank, current);
                    long needed = currentTank.getNeeded();
                    if (needed < tier.getOutput()) {
                        currentTank.shrinkStack(tier.getOutput() - needed, Action.EXECUTE);
                    }
                }
            }
        }
    }

    private IChemicalTank<?, ?> getCurrentTank(IChemicalTank<?, ?> currentTank, Current current) {
        if (currentTank == null) {
            return getCurrentTank(current);
        }
        return currentTank;
    }

    private IChemicalTank<?, ?> getCurrentTank(Current current) {
        if (current == Current.GAS) {
            return getGasTank();
        } else if (current == Current.INFUSION) {
            return getInfusionTank();
        } else if (current == Current.PIGMENT) {
            return getPigmentTank();
        } else if (current == Current.SLURRY) {
            return getSlurryTank();
        }
        throw new IllegalStateException("Unknown chemical type");
    }

    private void doAutoEject(TransmissionType type, IChemicalTank<?, ?> tank) {
        ConfigInfo config = configComponent.getConfig(type);
        if (config != null && config.isEjecting()) {
            ChemicalUtil.emit(config.getAllOutputtingSides(), tank, this, tier.getOutput());
        }
    }

    @Override
    public void nextMode(int tank) {
        if (tank == 0) {
            dumping = dumping.getNext();
            markDirty(false);
        }
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbtTags) {
        super.read(state, nbtTags);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.DUMP_MODE, GasMode::byIndexStatic, mode -> dumping = mode);
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt(NBTConstants.DUMP_MODE, dumping.ordinal());
        return nbtTags;
    }

    @Override
    protected void dumpRadiation() {
        if (tier != ChemicalTankTier.CREATIVE) {
            //Don't dump radioactive materials from creative chemical tanks
            super.dumpRadiation();
        }
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(getStoredAmount(), tier.getStorage());
    }

    private long getStoredAmount() {
        switch (chemicalTank.getCurrent()) {
            case GAS:
                return getGasTank().getStored();
            case INFUSION:
                return getInfusionTank().getStored();
            case PIGMENT:
                return getPigmentTank().getStored();
            case SLURRY:
                return getSlurryTank().getStored();
        }
        return 0;
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

    public ChemicalTankTier getTier() {
        return tier;
    }

    public MergedChemicalTank getChemicalTank() {
        return chemicalTank;
    }

    public IGasTank getGasTank() {
        return chemicalTank.getGasTank();
    }

    public IInfusionTank getInfusionTank() {
        return chemicalTank.getInfusionTank();
    }

    public IPigmentTank getPigmentTank() {
        return chemicalTank.getPigmentTank();
    }

    public ISlurryTank getSlurryTank() {
        return chemicalTank.getSlurryTank();
    }

    @Override
    public void parseUpgradeData(@Nonnull IUpgradeData upgradeData) {
        if (upgradeData instanceof ChemicalTankUpgradeData) {
            ChemicalTankUpgradeData data = (ChemicalTankUpgradeData) upgradeData;
            redstone = data.redstone;
            setControlType(data.controlType);
            drainSlot.setStack(data.drainSlot.getStack());
            fillSlot.setStack(data.fillSlot.getStack());
            dumping = data.dumping;
            getGasTank().setStack(data.storedGas);
            getInfusionTank().setStack(data.storedInfusion);
            getPigmentTank().setStack(data.storedPigment);
            getSlurryTank().setStack(data.storedSlurry);
            for (ITileComponent component : getComponents()) {
                component.read(data.components);
            }
        } else {
            super.parseUpgradeData(upgradeData);
        }
    }

    @Nonnull
    @Override
    public ChemicalTankUpgradeData getUpgradeData() {
        return new ChemicalTankUpgradeData(redstone, getControlType(), drainSlot, fillSlot, dumping, getGasTank().getStack(), getInfusionTank().getStack(),
              getPigmentTank().getStack(), getSlurryTank().getStack(), getComponents());
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