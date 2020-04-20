package mekanism.generators.common.tile.reactor;

import java.util.Collections;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IConfigurable;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.chemical.ProxiedChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.energy.ProxiedEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.fluid.ProxiedFluidTankHolder;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.ProxiedInventorySlotHolder;
import mekanism.common.capabilities.resolver.basic.PersistentCapabilityResolver;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.util.CableUtils;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.BooleanStateDisplay.InputOutput;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.world.World;

public class TileEntityReactorPort extends TileEntityReactorBlock implements IConfigurable {

    public TileEntityReactorPort() {
        super(GeneratorsBlocks.REACTOR_PORT);
        delaySupplier = () -> 0;
        addCapabilityResolver(PersistentCapabilityResolver.configurable(() -> this));
    }

    @Nonnull
    @Override
    protected IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks() {
        //Note: We can just use a proxied holder as the input/output restrictions are done in the tanks themselves
        return ProxiedChemicalTankHolder.create(side -> true, side -> getActive(),
              side -> getReactor() == null ? Collections.emptyList() : getReactor().controller.getGasTanks(side));
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        return ProxiedFluidTankHolder.create(side -> true, side -> getActive(),
              side -> getReactor() == null ? Collections.emptyList() : getReactor().controller.getFluidTanks(side));
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        return ProxiedEnergyContainerHolder.create(side -> true, side -> getActive(),
              side -> getReactor() == null ? Collections.emptyList() : getReactor().controller.getEnergyContainers(side));
    }

    @Nonnull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors() {
        return side -> getReactor() == null ? Collections.emptyList() : getReactor().controller.getHeatCapacitors(side);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        return ProxiedInventorySlotHolder.create(side -> true, side -> getActive(),
              side -> getReactor() == null ? Collections.emptyList() : getReactor().controller.getInventorySlots(side));
    }

    @Override
    public boolean persists(SubstanceType type) {
        if (type == SubstanceType.GAS || type == SubstanceType.FLUID || type == SubstanceType.ENERGY || type == SubstanceType.HEAT) {
            return false;
        }
        return super.persists(type);
    }

    @Override
    public boolean persistInventory() {
        return false;
    }

    @Override
    public boolean isFrame() {
        return false;
    }

    @Override
    protected void resetChanged() {
        if (changed) {
            World world = getWorld();
            if (world != null) {
                world.notifyNeighborsOfStateChange(getPos(), getBlockType());
            }
        }
        super.resetChanged();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (getActive() && getReactor() != null) {
            GasUtils.emit(getReactor().getSteamTank(), this);
            CableUtils.emit(getReactor().controller.energyContainer, this);
        }
    }

    @Nullable
    @Override
    public IHeatHandler getAdjacent(Direction side) {
        TileEntity adj = MekanismUtils.getTileEntity(getWorld(), getPos().offset(side));
        if (adj instanceof TileEntityReactorBlock) {
            return null;
        }
        return MekanismUtils.toOptional(CapabilityUtils.getCapability(adj, Capabilities.HEAT_HANDLER_CAPABILITY, side.getOpposite())).orElse(null);
    }

    @Override
    public ActionResultType onSneakRightClick(PlayerEntity player, Direction side) {
        if (!isRemote()) {
            boolean oldMode = getActive();
            setActive(!oldMode);
            player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                  GeneratorsLang.REACTOR_PORT_EJECT.translateColored(EnumColor.GRAY, InputOutput.of(oldMode, true))));
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        return ActionResultType.PASS;
    }
}