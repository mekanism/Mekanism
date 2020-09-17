package mekanism.generators.common.tile.fusion;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.ILangEntry;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.tile.interfaces.IHasMode;
import mekanism.common.util.NBTUtils;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.base.IReactorLogic;
import mekanism.generators.common.base.IReactorLogicMode;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter.FusionReactorLogic;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class TileEntityFusionReactorLogicAdapter extends TileEntityFusionReactorBlock implements IReactorLogic<FusionReactorLogic>, IHasMode {

    public FusionReactorLogic logicType = FusionReactorLogic.DISABLED;
    public boolean activeCooled;
    private boolean prevOutputting;

    public TileEntityFusionReactorLogicAdapter() {
        super(GeneratorsBlocks.FUSION_REACTOR_LOGIC_ADAPTER);
    }

    @Override
    protected void onUpdateServer(FusionReactorMultiblockData multiblock) {
        super.onUpdateServer(multiblock);
        boolean outputting = checkMode();
        if (outputting != prevOutputting) {
            World world = getWorld();
            if (world != null) {
                world.notifyNeighborsOfStateChange(getPos(), getBlockType());
            }
        }
        prevOutputting = outputting;
    }

    public boolean checkMode() {
        if (isRemote()) {
            return prevOutputting;
        }
        FusionReactorMultiblockData multiblock = getMultiblock();
        if (multiblock.isFormed()) {
            switch (logicType) {
                case READY:
                    return multiblock.getLastPlasmaTemp() >= multiblock.getIgnitionTemperature(activeCooled);
                case CAPACITY:
                    return multiblock.getLastPlasmaTemp() >= multiblock.getMaxPlasmaTemperature(activeCooled);
                case DEPLETED:
                    return (multiblock.deuteriumTank.getStored() < multiblock.getInjectionRate() / 2) ||
                           (multiblock.tritiumTank.getStored() < multiblock.getInjectionRate() / 2);
                case DISABLED:
                default:
                    return false;
            }
        }
        return false;
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbtTags) {
        super.read(state, nbtTags);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.LOGIC_TYPE, FusionReactorLogic::byIndexStatic, logicType -> this.logicType = logicType);
        activeCooled = nbtTags.getBoolean(NBTConstants.ACTIVE_COOLED);
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt(NBTConstants.LOGIC_TYPE, logicType.ordinal());
        nbtTags.putBoolean(NBTConstants.ACTIVE_COOLED, activeCooled);
        return nbtTags;
    }

    @Override
    public boolean canBeMaster() {
        return false;
    }

    @Override
    public void nextMode() {
        activeCooled = !activeCooled;
        markDirty(false);
    }

    @Override
    public FusionReactorLogic getMode() {
        return logicType;
    }

    @Override
    public FusionReactorLogic[] getModes() {
        return FusionReactorLogic.values();
    }

    public void setLogicTypeFromPacket(FusionReactorLogic logicType) {
        this.logicType = logicType;
        markDirty(false);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableEnum.create(FusionReactorLogic::byIndexStatic, FusionReactorLogic.DISABLED, () -> logicType, value -> logicType = value));
        container.track(SyncableBoolean.create(() -> activeCooled, value -> activeCooled = value));
        container.track(SyncableBoolean.create(() -> prevOutputting, value -> prevOutputting = value));
    }

    public enum FusionReactorLogic implements IReactorLogicMode<FusionReactorLogic>, IHasTranslationKey {
        DISABLED(GeneratorsLang.REACTOR_LOGIC_DISABLED, GeneratorsLang.DESCRIPTION_REACTOR_DISABLED, new ItemStack(Items.GUNPOWDER)),
        READY(GeneratorsLang.REACTOR_LOGIC_READY, GeneratorsLang.DESCRIPTION_REACTOR_READY, new ItemStack(Items.REDSTONE)),
        CAPACITY(GeneratorsLang.REACTOR_LOGIC_CAPACITY, GeneratorsLang.DESCRIPTION_REACTOR_CAPACITY, new ItemStack(Items.REDSTONE)),
        DEPLETED(GeneratorsLang.REACTOR_LOGIC_DEPLETED, GeneratorsLang.DESCRIPTION_REACTOR_DEPLETED, new ItemStack(Items.REDSTONE));

        private static final FusionReactorLogic[] MODES = values();

        private final ILangEntry name;
        private final ILangEntry description;
        private final ItemStack renderStack;

        FusionReactorLogic(ILangEntry name, ILangEntry description, ItemStack stack) {
            this.name = name;
            this.description = description;
            renderStack = stack;
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
        public ITextComponent getDescription() {
            return description.translate();
        }

        @Override
        public EnumColor getColor() {
            return EnumColor.RED;
        }

        public static FusionReactorLogic byIndexStatic(int index) {
            return MathUtils.getByIndexMod(MODES, index);
        }
    }
}