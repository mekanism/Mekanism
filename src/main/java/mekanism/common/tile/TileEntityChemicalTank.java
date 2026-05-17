package mekanism.common.tile;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.IntFunction;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.IIncrementalEnum;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.api.text.IHasTextComponent.IHasEnumNameTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.chemical.ChemicalTankChemicalTank;
import mekanism.common.capabilities.holder.IContainerHolder;
import mekanism.common.capabilities.holder.MekContainerHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.interfaces.IHasGasMode;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.upgrade.ChemicalTankUpgradeData;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.ResourceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

public class TileEntityChemicalTank extends TileEntityConfigurableMachine implements IHasGasMode {

    @SyntheticComputerMethod(getter = "getDumpingMode", getterDescription = "Get the current Dumping configuration")
    public GasMode dumping = GasMode.IDLE;

    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getStored", "getCapacity", "getNeeded",
                                                                                        "getFilledPercentage"}, docPlaceholder = "tank")
    private IChemicalTank chemicalTank;
    private ChemicalTankTier tier;

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getDrainItem", docPlaceholder = "drain slot")
    ChemicalInventorySlot drainSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getFillItem", docPlaceholder = "fill slot")
    ChemicalInventorySlot fillSlot;

    public TileEntityChemicalTank(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
        configComponent.setupIOConfig(TransmissionType.ITEM, drainSlot, fillSlot, RelativeSide.FRONT, true).setCanEject(false);
        configComponent.setupIOConfig(TransmissionType.CHEMICAL, chemicalTank, RelativeSide.FRONT);
        ejectorComponent = new TileComponentEjector(this, () -> tier.getOutput());
        ejectorComponent.setOutputData(configComponent, TransmissionType.CHEMICAL)
              .setCanEject(type -> canFunction() && (tier == ChemicalTankTier.CREATIVE || dumping != GasMode.DUMPING));
    }

    @Override
    protected void presetVariables() {
        super.presetVariables();
        tier = Attribute.getTier(getBlockHolder(), ChemicalTankTier.class);
    }

    @NotNull
    @Override
    public IContainerHolder<IChemicalTank> getInitialChemicalTanks(IContentsListener listener) {
        MekContainerHelper<IChemicalTank> builder = MekContainerHelper.forSideWithChemicalConfig(this);
        builder.addContainer(chemicalTank = ChemicalTankChemicalTank.create(tier, listener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener) {
        MekContainerHelper<IInventorySlot> builder = MekContainerHelper.forSideWithItemConfig(this);
        builder.addContainer(drainSlot = ChemicalInventorySlot.drain(chemicalTank, listener, 16, 16));
        builder.addContainer(fillSlot = ChemicalInventorySlot.fill(chemicalTank, listener, 16, 48));
        drainSlot.setSlotType(ContainerSlotType.OUTPUT);
        drainSlot.setSlotOverlay(SlotOverlay.PLUS);
        fillSlot.setSlotType(ContainerSlotType.INPUT);
        fillSlot.setSlotOverlay(SlotOverlay.MINUS);
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        drainSlot.drainTank();
        fillSlot.fillTank();
        if (dumping != GasMode.IDLE && tier != ChemicalTankTier.CREATIVE) {
            ChemicalResource chemicalType = chemicalTank.getResource();
            if (!chemicalType.isEmpty()) {
                long toDump = 0;
                if (dumping == GasMode.DUMPING) {
                    toDump = tier.getStorage() / 400;
                } else {//dumping == GasMode.DUMPING_EXCESS
                    long target = MathUtils.clampToLong(chemicalTank.capacityAsLong(chemicalType) * MekanismConfig.general.dumpExcessKeepRatio.get());
                    long stored = chemicalTank.amountAsLong();
                    if (target < stored) {
                        //Dump excess that we need to get to the target (capping at our eject rate for how much we can dump at once)
                        toDump = Math.min(stored - target, tier.getOutput());
                    }
                }
                if (toDump > 0) {
                    try (Transaction transaction = Transaction.openRoot()) {
                        //TODO - 26.1: Re-evaluate this clamping and see how we can avoid it
                        // Also do we have any rate limits on our chemical tank that might mean we need to just directly modify the stack?
                        chemicalTank.extract(chemicalType, Ints.saturatedCast(toDump), transaction, AutomationType.INTERNAL);
                        transaction.commit();
                    }
                }
            }
        }
        return sendUpdatePacket;
    }

    @Override
    public void nextMode(int tank) {
        if (tank == 0) {
            dumping = dumping.getNext();
            markForSave();
        }
    }

    @Override
    public boolean shouldDumpRadiation() {
        return tier != ChemicalTankTier.CREATIVE;
    }

    @Override
    public int getRedstoneLevel() {
        return ResourceUtils.getRedstoneSignalFromContainer(chemicalTank);
    }

    @Override
    protected boolean makesComparatorDirty(ContainerType<?, ?, ?> type) {
        return type == ContainerType.CHEMICAL;
    }

    public ChemicalTankTier getTier() {
        return tier;
    }

    public IChemicalTank getChemicalTank() {
        return chemicalTank;
    }

    @Override
    public void parseUpgradeData(@NotNull IUpgradeData upgradeData, Provider provider) {
        if (upgradeData instanceof ChemicalTankUpgradeData data) {
            redstone = data.redstone;
            setControlType(data.controlType);
            drainSlot.copyContents(data.drainSlot);
            fillSlot.copyContents(data.fillSlot);
            dumping = data.dumping;
            chemicalTank.copyContents(data.chemicalTank);
            try (var reporter = new ProblemReporter.ScopedCollector(problemPath(), Mekanism.logger)) {
                ValueInput input = TagValueInput.create(reporter, provider, data.components);
                for (ITileComponent component : getComponents()) {
                    component.read(input);
                }
            }
        } else {
            super.parseUpgradeData(upgradeData, provider);
        }
    }

    @NotNull
    @Override
    public ChemicalTankUpgradeData getUpgradeData(HolderLookup.Provider provider) {
        return new ChemicalTankUpgradeData(provider, redstone, getControlType(), drainSlot, fillSlot, dumping, chemicalTank, getComponents(), problemPath());
    }

    @Override
    public void writeSustainedData(@NotNull ValueOutput output) {
        super.writeSustainedData(output);
        NBTUtils.writeEnum(output, SerializationConstants.DUMP_MODE, dumping);
    }

    @Override
    public void readSustainedData(@NotNull ValueInput input) {
        super.readSustainedData(input);
        NBTUtils.setEnumIfPresent(input, SerializationConstants.DUMP_MODE, GasMode.BY_ID, mode -> dumping = mode);
    }

    @Override
    protected void collectImplicitComponents(@NotNull DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(MekanismDataComponents.DUMP_MODE, dumping);
    }

    @Override
    protected void applyImplicitComponents(@NotNull DataComponentGetter input) {
        super.applyImplicitComponents(input);
        dumping = input.getOrDefault(MekanismDataComponents.DUMP_MODE, dumping);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableEnum.create(GasMode.BY_ID, GasMode.IDLE, () -> dumping, value -> dumping = value));
    }

    //Methods relating to IComputerTile
    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Set the Dumping mode of the tank")
    void setDumpingMode(GasMode mode) throws ComputerException {
        validateSecurityIsPublic();
        if (dumping != mode) {
            dumping = mode;
            markForSave();
        }
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Advance the Dumping mode to the next configuration in the list")
    void incrementDumpingMode() throws ComputerException {
        validateSecurityIsPublic();
        nextMode(0);
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Descend the Dumping mode to the previous configuration in the list")
    void decrementDumpingMode() throws ComputerException {
        validateSecurityIsPublic();
        dumping = dumping.getPrevious();
        markForSave();
    }
    //End methods IComputerTile

    @NothingNullByDefault
    public enum GasMode implements IIncrementalEnum<GasMode>, IHasEnumNameTextComponent, StringRepresentable {
        IDLE(MekanismLang.IDLE),
        DUMPING_EXCESS(MekanismLang.DUMPING_EXCESS),
        DUMPING(MekanismLang.DUMPING);

        public static final Codec<GasMode> CODEC = StringRepresentable.fromEnum(GasMode::values);
        public static final IntFunction<GasMode> BY_ID = ByIdMap.continuous(GasMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, GasMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, GasMode::ordinal);

        private final String serializedName;
        private final ILangEntry langEntry;

        GasMode(ILangEntry langEntry) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.langEntry = langEntry;
        }

        @Override
        public Component getTextComponent() {
            return langEntry.translate();
        }

        @Override
        public GasMode byIndex(int index) {
            return BY_ID.apply(index);
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}
