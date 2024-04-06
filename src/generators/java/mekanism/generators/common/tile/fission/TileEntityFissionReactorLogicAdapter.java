package mekanism.generators.common.tile.fission;

import java.util.EnumSet;
import java.util.Map;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.util.NBTUtils;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.base.IReactorLogic;
import mekanism.generators.common.base.IReactorLogicMode;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.registries.GeneratorsAttachmentTypes;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter.FissionReactorLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;

public class TileEntityFissionReactorLogicAdapter extends TileEntityFissionReactorCasing implements IReactorLogic<FissionReactorLogic> {

    public FissionReactorLogic logicType = FissionReactorLogic.DISABLED;
    private RedstoneStatus prevStatus = RedstoneStatus.IDLE;

    public TileEntityFissionReactorLogicAdapter(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.FISSION_REACTOR_LOGIC_ADAPTER, pos, state);
    }

    @Override
    protected boolean onUpdateServer(FissionReactorMultiblockData multiblock) {
        boolean needsPacket = super.onUpdateServer(multiblock);
        RedstoneStatus status = getStatus();
        if (status != prevStatus) {
            Level world = getLevel();
            if (world != null) {
                Direction side = multiblock.getOutsideSide(worldPosition);
                if (side == null) {
                    //Not formed, just update all sides
                    world.updateNeighborsAt(getBlockPos(), getBlockType());
                } else if (!EventHooks.onNeighborNotify(world, worldPosition, getBlockState(), EnumSet.of(side), false).isCanceled()) {
                    world.neighborChanged(worldPosition.relative(side), getBlockType(), worldPosition);
                }
            }
            prevStatus = status;
        }
        return needsPacket;
    }

    @Override
    @ComputerMethod(nameOverride = "getLogicMode")
    public FissionReactorLogic getMode() {
        return logicType;
    }

    @Override
    public FissionReactorLogic[] getModes() {
        return FissionReactorLogic.values();
    }

    public int getRedstoneLevel(Direction side) {
        return !isRemote() && getMultiblock().isPositionOutsideBounds(worldPosition.relative(side)) && getStatus() == RedstoneStatus.OUTPUTTING ? 15 : 0;
    }

    @ComputerMethod(nameOverride = "getRedstoneLogicStatus")
    public RedstoneStatus getStatus() {
        if (isRemote()) {
            return prevStatus;
        }
        FissionReactorMultiblockData multiblock = getMultiblock();
        if (multiblock.isFormed()) {
            switch (logicType) {
                case ACTIVATION -> {
                    if (canFunction()) {
                        return RedstoneStatus.POWERED;
                    }
                }
                case TEMPERATURE -> {
                    if (multiblock.heatCapacitor.getTemperature() >= FissionReactorMultiblockData.MIN_DAMAGE_TEMPERATURE) {
                        return RedstoneStatus.OUTPUTTING;
                    }
                }
                case EXCESS_WASTE -> {
                    if (multiblock.wasteTank.getNeeded() == 0) {
                        return RedstoneStatus.OUTPUTTING;
                    }
                }
                case DAMAGED -> {
                    if (multiblock.reactorDamage >= FissionReactorMultiblockData.MAX_DAMAGE) {
                        return RedstoneStatus.OUTPUTTING;
                    }
                }
                case DEPLETED -> {
                    if (multiblock.fuelTank.isEmpty()) {
                        return RedstoneStatus.OUTPUTTING;
                    }
                }
            }
        }
        return RedstoneStatus.IDLE;
    }

    @ComputerMethod(nameOverride = "setLogicMode")
    public void setLogicTypeFromPacket(FissionReactorLogic logicType) {
        if (this.logicType != logicType) {
            this.logicType = logicType;
            markForSave();
        }
    }

    @Override
    public boolean supportsMode(RedstoneControl mode) {
        //Don't allow the mode to be disabled
        return super.supportsMode(mode) && mode != RedstoneControl.DISABLED;
    }


    @Override
    public void onPowerChange() {
        super.onPowerChange();
        if (!isRemote()) {
            FissionReactorMultiblockData multiblock = getMultiblock();
            if (multiblock.isFormed()) {
                if (logicType == FissionReactorLogic.ACTIVATION) {
                    multiblock.setActive(canFunction());
                }
            }
        }
    }

    @Override
    public void readSustainedData(@NotNull CompoundTag nbt) {
        super.readSustainedData(nbt);
        NBTUtils.setEnumIfPresent(nbt, NBTConstants.LOGIC_TYPE, FissionReactorLogic::byIndexStatic, logicType -> this.logicType = logicType);
    }

    @Override
    public void writeSustainedData(@NotNull CompoundTag nbtTags) {
        super.writeSustainedData(nbtTags);
        NBTUtils.writeEnum(nbtTags, NBTConstants.LOGIC_TYPE, logicType);
    }

    @Override
    public Map<String, Holder<AttachmentType<?>>> getTileDataAttachmentRemap() {
        Map<String, Holder<AttachmentType<?>>> remap = super.getTileDataAttachmentRemap();
        remap.put(NBTConstants.LOGIC_TYPE, GeneratorsAttachmentTypes.FISSION_LOGIC_TYPE);
        return remap;
    }

    @Override
    public void writeToStack(ItemStack stack) {
        super.writeToStack(stack);
        stack.setData(GeneratorsAttachmentTypes.FISSION_LOGIC_TYPE, logicType);
    }

    @Override
    public void readFromStack(ItemStack stack) {
        super.readFromStack(stack);
        logicType = stack.getData(GeneratorsAttachmentTypes.FISSION_LOGIC_TYPE);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableEnum.create(FissionReactorLogic::byIndexStatic, FissionReactorLogic.DISABLED, this::getMode, value -> logicType = value));
        container.track(SyncableEnum.create(RedstoneStatus::byIndexStatic, RedstoneStatus.IDLE, () -> prevStatus, value -> prevStatus = value));
    }

    @Override
    public boolean canBeMaster() {
        return false;
    }

    @NothingNullByDefault
    public enum FissionReactorLogic implements IReactorLogicMode<FissionReactorLogic>, IHasTranslationKey {
        DISABLED(GeneratorsLang.REACTOR_LOGIC_DISABLED, GeneratorsLang.DESCRIPTION_REACTOR_DISABLED, new ItemStack(Items.GUNPOWDER), EnumColor.DARK_GRAY),
        ACTIVATION(GeneratorsLang.REACTOR_LOGIC_ACTIVATION, GeneratorsLang.DESCRIPTION_REACTOR_ACTIVATION, new ItemStack(Items.FLINT_AND_STEEL), EnumColor.AQUA),
        TEMPERATURE(GeneratorsLang.REACTOR_LOGIC_TEMPERATURE, GeneratorsLang.DESCRIPTION_REACTOR_TEMPERATURE, new ItemStack(Items.REDSTONE), EnumColor.RED),
        EXCESS_WASTE(GeneratorsLang.REACTOR_LOGIC_EXCESS_WASTE, GeneratorsLang.DESCRIPTION_REACTOR_EXCESS_WASTE, new ItemStack(Items.REDSTONE), EnumColor.RED),
        DAMAGED(GeneratorsLang.REACTOR_LOGIC_DAMAGED, GeneratorsLang.DESCRIPTION_REACTOR_DAMAGED, new ItemStack(Items.REDSTONE), EnumColor.RED),
        DEPLETED(GeneratorsLang.REACTOR_LOGIC_DEPLETED, GeneratorsLang.DESCRIPTION_REACTOR_DEPLETED, new ItemStack(Items.REDSTONE), EnumColor.RED);

        private static final FissionReactorLogic[] MODES = values();

        private final ILangEntry name;
        private final ILangEntry description;
        private final ItemStack renderStack;
        private final EnumColor color;

        FissionReactorLogic(ILangEntry name, ILangEntry description, ItemStack stack, EnumColor color) {
            this.name = name;
            this.description = description;
            renderStack = stack;
            this.color = color;
        }

        @Override
        public ItemStack getRenderStack() {
            return renderStack;
        }

        @Override
        public String getTranslationKey() {
            return name.getTranslationKey();
        }

        @Override
        public Component getDescription() {
            return description.translate();
        }

        @Override
        public EnumColor getColor() {
            return color;
        }

        public static FissionReactorLogic byIndexStatic(int index) {
            return MathUtils.getByIndexMod(MODES, index);
        }
    }

    @NothingNullByDefault
    public enum RedstoneStatus implements IHasTranslationKey {
        IDLE(MekanismLang.IDLE),
        OUTPUTTING(GeneratorsLang.REACTOR_LOGIC_OUTPUTTING),
        POWERED(GeneratorsLang.REACTOR_LOGIC_POWERED);

        private static final RedstoneStatus[] MODES = values();

        private final ILangEntry name;

        RedstoneStatus(ILangEntry name) {
            this.name = name;
        }

        @Override
        public String getTranslationKey() {
            return name.getTranslationKey();
        }

        public static RedstoneStatus byIndexStatic(int index) {
            return MathUtils.getByIndexMod(MODES, index);
        }
    }
}
