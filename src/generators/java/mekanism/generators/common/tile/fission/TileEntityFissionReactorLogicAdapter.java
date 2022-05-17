package mekanism.generators.common.tile.fission;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
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
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter.FissionReactorLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

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
                world.updateNeighborsAt(getBlockPos(), getBlockType());
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

    @ComputerMethod(nameOverride = "getRedstoneLogicStatus")
    public RedstoneStatus getStatus() {
        if (isRemote()) {
            return prevStatus;
        }
        FissionReactorMultiblockData multiblock = getMultiblock();
        if (multiblock.isFormed()) {
            switch (logicType) {
                case ACTIVATION:
                    return isPowered() ? RedstoneStatus.POWERED : RedstoneStatus.IDLE;
                case TEMPERATURE:
                    return multiblock.heatCapacitor.getTemperature() >= FissionReactorMultiblockData.MIN_DAMAGE_TEMPERATURE ? RedstoneStatus.OUTPUTTING : RedstoneStatus.IDLE;
                case EXCESS_WASTE:
                    return multiblock.wasteTank.getNeeded() == 0 ? RedstoneStatus.OUTPUTTING : RedstoneStatus.IDLE;
                case DAMAGED:
                    return multiblock.reactorDamage >= FissionReactorMultiblockData.MAX_DAMAGE ? RedstoneStatus.OUTPUTTING : RedstoneStatus.IDLE;
                case DEPLETED:
                    return multiblock.fuelTank.isEmpty() ? RedstoneStatus.OUTPUTTING : RedstoneStatus.IDLE;
                default:
                    break;
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
    public void onPowerChange() {
        super.onPowerChange();
        if (!isRemote()) {
            FissionReactorMultiblockData multiblock = getMultiblock();
            if (multiblock.isFormed()) {
                if (logicType == FissionReactorLogic.ACTIVATION) {
                    multiblock.setActive(isPowered());
                }
            }
        }
    }

    @Override
    public void load(@Nonnull CompoundTag nbt) {
        super.load(nbt);
        NBTUtils.setEnumIfPresent(nbt, NBTConstants.LOGIC_TYPE, FissionReactorLogic::byIndexStatic, logicType -> this.logicType = logicType);
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag nbtTags) {
        super.saveAdditional(nbtTags);
        NBTUtils.writeEnum(nbtTags, NBTConstants.LOGIC_TYPE, logicType);
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
