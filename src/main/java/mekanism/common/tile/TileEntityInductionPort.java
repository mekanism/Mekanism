package mekanism.common.tile;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.IConfigurable;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.CableUtils;
import mekanism.common.util.EnumUtils;
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
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (structure != null && getActive()) {
            //TODO: We may want to look into caching the directionsToEmit, and updating it on neighbor updates
            Set<Direction> directionsToEmit = EnumSet.noneOf(Direction.class);
            for (Direction direction : EnumUtils.DIRECTIONS) {
                if (!structure.locations.contains(Coord4D.get(this).offset(direction))) {
                    directionsToEmit.add(direction);
                }
            }
            CableUtils.emit(directionsToEmit, structure.getEnergyContainer(), this);
        }
    }

    @Override
    public boolean canHandleEnergy() {
        //Mark that we can handle energy
        return true;
    }

    @Override
    public boolean persistEnergy() {
        //But that we do not handle energy when it comes to syncing it/saving this tile to disk
        return false;
    }

    @Nonnull
    @Override
    public FloatingLong insertEnergy(int container, @Nonnull FloatingLong amount, @Nullable Direction side, @Nonnull Action action) {
        //Don't allow inserting if we are on output mode
        return getActive() ? amount : super.insertEnergy(container, amount, side, action);
    }

    @Nonnull
    @Override
    public FloatingLong extractEnergy(int container, @Nonnull FloatingLong amount, @Nullable Direction side, @Nonnull Action action) {
        //Don't allow extracting if we are on input mode
        return getActive() ? super.extractEnergy(container, amount, side, action) : FloatingLong.ZERO;
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
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return canHandleEnergy() && structure != null ? structure.getEnergyContainers(side) : Collections.emptyList();
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
            //TODO: Previously we had a check like !isRemote() ? structure == null : !clientHasStructure
            // Do we still need this if we ever actually needed it?
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