package mekanism.common.tile;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IConfigurable;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.energy.ProxiedEnergyContainerHolder;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.util.CableUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.BooleanStateDisplay.InputOutput;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityInductionPort extends TileEntityInductionCasing implements IConfigurable {

    public TileEntityInductionPort() {
        super(MekanismBlocks.INDUCTION_PORT);
        delaySupplier = () -> 0;
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        //Don't allow inserting if we are on output mode, or extracting if we are on input mode
        return ProxiedEnergyContainerHolder.create(side -> !getActive(), side -> getActive(),
              side -> structure == null ? Collections.emptyList() : structure.getEnergyContainers(side));
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (structure != null && getActive()) {
            CableUtils.emit(structure.getDirectionsToEmit(Coord4D.get(this)), structure.getEnergyContainer(), this);
        }
    }

    @Override
    public boolean persists(SubstanceType type) {
        //Do not handle energy when it comes to syncing it/saving this tile to disk
        if (type == SubstanceType.ENERGY) {
            return false;
        }
        return super.persists(type);
    }

    @Override
    public ActionResultType onSneakRightClick(PlayerEntity player, Direction side) {
        if (!isRemote()) {
            boolean oldMode = getActive();
            setActive(!oldMode);
            player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                  MekanismLang.INDUCTION_PORT_MODE.translateColored(EnumColor.GRAY, InputOutput.of(oldMode, true))));
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        return ActionResultType.PASS;
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapabilityIfEnabled(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.CONFIGURABLE_CAPABILITY) {
            return Capabilities.CONFIGURABLE_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapabilityIfEnabled(capability, side);
    }

    @Nonnull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        if (side == null) {
            //Allow internal queries to view/see both all slots, we only limit the slots that can be seen
            // to either input or output if we are querying this with a specific side (as then we are
            // in the world)
            return super.getInventorySlots(null);
        }
        if (!hasInventory() || structure == null) {
            //If we don't have a structure then return that we have no slots accessible
            return Collections.emptyList();
        }
        //TODO: Cache this??
        return Collections.singletonList(getActive() ? structure.energyInputSlot : structure.energyOutputSlot);
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(getEnergy(), getMaxEnergy());
    }
}