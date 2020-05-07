package mekanism.common.tile;

import java.util.Collections;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.IConfigurable;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.energy.ProxiedEnergyContainerHolder;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.util.CableUtils;
import mekanism.common.util.text.BooleanStateDisplay.InputOutput;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;

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
    public boolean renderUpdate() {
        return true;
    }

    @Override
    public int getRedstoneLevel() {
        return structure == null ? 0 : structure.getCurrentRedstoneLevel();
    }
}