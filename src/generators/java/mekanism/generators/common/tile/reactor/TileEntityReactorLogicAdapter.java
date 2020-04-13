package mekanism.generators.common.tile.reactor;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.math.MathUtils;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.base.ILangEntry;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.tile.interfaces.IHasMode;
import mekanism.common.util.NBTUtils;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class TileEntityReactorLogicAdapter extends TileEntityReactorBlock implements IHasMode {

    public ReactorLogic logicType = ReactorLogic.DISABLED;
    public boolean activeCooled;
    private boolean prevOutputting;

    public TileEntityReactorLogicAdapter() {
        super(GeneratorsBlocks.REACTOR_LOGIC_ADAPTER);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        boolean outputting = checkMode();
        if (outputting != prevOutputting) {
            World world = getWorld();
            if (world != null) {
                world.notifyNeighborsOfStateChange(getPos(), getBlockType());
            }
        }
        prevOutputting = outputting;
    }

    @Override
    public boolean isFrame() {
        return false;
    }

    public boolean checkMode() {
        if (isRemote()) {
            return prevOutputting;
        }
        if (getReactor() == null || !getReactor().isFormed()) {
            return false;
        }
        switch (logicType) {
            case READY:
                return getReactor().getLastPlasmaTemp() >= getReactor().getIgnitionTemperature(activeCooled);
            case CAPACITY:
                return getReactor().getLastPlasmaTemp() >= getReactor().getMaxPlasmaTemperature(activeCooled);
            case DEPLETED:
                return (getReactor().getDeuteriumTank().getStored() < getReactor().getInjectionRate() / 2) ||
                       (getReactor().getTritiumTank().getStored() < getReactor().getInjectionRate() / 2);
            case DISABLED:
            default:
                return false;
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.LOGIC_TYPE, ReactorLogic::byIndexStatic, logicType -> this.logicType = logicType);
        activeCooled = nbtTags.getBoolean(NBTConstants.ACTIVE_COOLED);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt(NBTConstants.LOGIC_TYPE, logicType.ordinal());
        nbtTags.putBoolean(NBTConstants.ACTIVE_COOLED, activeCooled);
        return nbtTags;
    }

    @Override
    public void nextMode() {
        activeCooled = !activeCooled;
        markDirty(false);
    }

    public void setLogicTypeFromPacket(ReactorLogic logicType) {
        this.logicType = logicType;
        markDirty(false);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableEnum.create(ReactorLogic::byIndexStatic, ReactorLogic.DISABLED, () -> logicType, value -> logicType = value));
        container.track(SyncableBoolean.create(() -> activeCooled, value -> activeCooled = value));
        container.track(SyncableBoolean.create(() -> prevOutputting, value -> prevOutputting = value));
    }

    public enum ReactorLogic implements IHasTranslationKey {
        DISABLED(GeneratorsLang.REACTOR_LOGIC_DISABLED, GeneratorsLang.DESCRIPTION_REACTOR_DISABLED, new ItemStack(Items.GUNPOWDER)),
        READY(GeneratorsLang.REACTOR_LOGIC_READY, GeneratorsLang.DESCRIPTION_REACTOR_READY, new ItemStack(Items.REDSTONE)),
        CAPACITY(GeneratorsLang.REACTOR_LOGIC_CAPACITY, GeneratorsLang.DESCRIPTION_REACTOR_CAPACITY, new ItemStack(Items.REDSTONE)),
        DEPLETED(GeneratorsLang.REACTOR_LOGIC_DEPLETED, GeneratorsLang.DESCRIPTION_REACTOR_DEPLETED, new ItemStack(Items.REDSTONE));

        private static final ReactorLogic[] MODES = values();

        private final ILangEntry name;
        private final ILangEntry description;
        private final ItemStack renderStack;

        ReactorLogic(ILangEntry name, ILangEntry description, ItemStack stack) {
            this.name = name;
            this.description = description;
            renderStack = stack;
        }

        public ItemStack getRenderStack() {
            return renderStack;
        }

        @Override
        public String getTranslationKey() {
            return name.getTranslationKey();
        }

        public ITextComponent getDescription() {
            return description.translate();
        }

        public static ReactorLogic byIndexStatic(int index) {
            return MathUtils.getByIndexMod(MODES, index);
        }
    }
}