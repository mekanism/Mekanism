package mekanism.generators.common.tile.fission;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.EnumSet;
import java.util.Locale;
import java.util.function.IntFunction;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTranslationKey.IHasEnumNameTranslationKey;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.text.TextUtils;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.base.IReactorLogic;
import mekanism.generators.common.base.IReactorLogicMode;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsDataComponents;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter.FissionReactorLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
                BlockState state = getBlockState();
                if (side == null) {
                    //Not formed, just update all sides
                    world.updateNeighborsAt(getBlockPos(), state.getBlock());
                } else if (!EventHooks.onNeighborNotify(world, worldPosition, state, EnumSet.of(side), false).isCanceled()) {
                    world.neighborChanged(worldPosition.relative(side), state.getBlock(), worldPosition);
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
                case CRITICAL_WASTE_LEVEL -> {
                    long target = MathUtils.clampToLong(multiblock.wasteTank.getCapacity() * MekanismGeneratorsConfig.generators.fissionExcessWasteRatio.get());
                    if (multiblock.wasteTank.getStored() >= target) {
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
    public void readSustainedData(HolderLookup.Provider provider, @NotNull CompoundTag nbt) {
        super.readSustainedData(provider, nbt);
        NBTUtils.setEnumIfPresent(nbt, SerializationConstants.LOGIC_TYPE, FissionReactorLogic.BY_ID, logicType -> this.logicType = logicType);
    }

    @Override
    public void writeSustainedData(HolderLookup.Provider provider, @NotNull CompoundTag nbtTags) {
        super.writeSustainedData(provider, nbtTags);
        NBTUtils.writeEnum(nbtTags, SerializationConstants.LOGIC_TYPE, logicType);
    }

    @Override
    protected void collectImplicitComponents(@NotNull DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(GeneratorsDataComponents.FISSION_LOGIC_TYPE, logicType);
    }

    @Override
    protected void applyImplicitComponents(@NotNull BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        logicType = input.getOrDefault(GeneratorsDataComponents.FISSION_LOGIC_TYPE, logicType);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableEnum.create(FissionReactorLogic.BY_ID, FissionReactorLogic.DISABLED, this::getMode, value -> logicType = value));
        container.track(SyncableEnum.create(RedstoneStatus.BY_ID, RedstoneStatus.IDLE, () -> prevStatus, value -> prevStatus = value));
    }

    @Override
    public boolean canBeMaster() {
        return false;
    }

    @NothingNullByDefault
    public enum FissionReactorLogic implements IReactorLogicMode<FissionReactorLogic>, IHasEnumNameTranslationKey, StringRepresentable {
        DISABLED(GeneratorsLang.REACTOR_LOGIC_DISABLED, GeneratorsLang.DESCRIPTION_REACTOR_DISABLED, new ItemStack(Items.GUNPOWDER), EnumColor.DARK_GRAY),
        ACTIVATION(GeneratorsLang.REACTOR_LOGIC_ACTIVATION, GeneratorsLang.DESCRIPTION_REACTOR_ACTIVATION, new ItemStack(Items.FLINT_AND_STEEL), EnumColor.AQUA),
        TEMPERATURE(GeneratorsLang.REACTOR_LOGIC_TEMPERATURE, GeneratorsLang.DESCRIPTION_REACTOR_TEMPERATURE, new ItemStack(Items.REDSTONE), EnumColor.RED),
        CRITICAL_WASTE_LEVEL(GeneratorsLang.REACTOR_LOGIC_CRITICAL_WASTE_LEVEL, GeneratorsLang.DESCRIPTION_REACTOR_CRITICAL_WASTE_LEVEL, new ItemStack(Items.REDSTONE), EnumColor.RED) {
            @NotNull
            @Override
            public Component getDescription() {
                return description.translate(TextUtils.getPercent(MekanismGeneratorsConfig.generators.fissionExcessWasteRatio.get()));
            }
        },
        DAMAGED(GeneratorsLang.REACTOR_LOGIC_DAMAGED, GeneratorsLang.DESCRIPTION_REACTOR_DAMAGED, new ItemStack(Items.REDSTONE), EnumColor.RED),
        DEPLETED(GeneratorsLang.REACTOR_LOGIC_DEPLETED, GeneratorsLang.DESCRIPTION_REACTOR_DEPLETED, new ItemStack(Items.REDSTONE), EnumColor.RED);

        public static final Codec<FissionReactorLogic> CODEC = StringRepresentable.fromEnum(FissionReactorLogic::values);
        public static final IntFunction<FissionReactorLogic> BY_ID = ByIdMap.continuous(FissionReactorLogic::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, FissionReactorLogic> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, FissionReactorLogic::ordinal);

        private final ILangEntry name;
        protected final ILangEntry description;
        private final ItemStack renderStack;
        private final EnumColor color;
        private final String serializedName;

        FissionReactorLogic(ILangEntry name, ILangEntry description, ItemStack stack, EnumColor color) {
            this.name = name;
            this.description = description;
            this.renderStack = stack;
            this.color = color;
            this.serializedName = name().toLowerCase(Locale.ROOT);
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

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }

    @NothingNullByDefault
    public enum RedstoneStatus implements IHasEnumNameTranslationKey {
        IDLE(MekanismLang.IDLE),
        OUTPUTTING(GeneratorsLang.REACTOR_LOGIC_OUTPUTTING),
        POWERED(GeneratorsLang.REACTOR_LOGIC_POWERED);

        public static final IntFunction<RedstoneStatus> BY_ID = ByIdMap.continuous(RedstoneStatus::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, RedstoneStatus> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, RedstoneStatus::ordinal);

        private final ILangEntry name;

        RedstoneStatus(ILangEntry name) {
            this.name = name;
        }

        @Override
        public String getTranslationKey() {
            return name.getTranslationKey();
        }
    }
}
