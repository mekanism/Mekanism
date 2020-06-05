package mekanism.common.tile.multiblock;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.inventory.AutomationType;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.text.BooleanStateDisplay.InputOutput;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;

public class TileEntitySPSPort extends TileEntitySPSCasing {

    private MachineEnergyContainer<TileEntitySPSPort> energyContainer;

    public TileEntitySPSPort() {
        super(MekanismBlocks.SPS_PORT);
        delaySupplier = () -> 0;
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (getMultiblock().isFormed()) {
            if (getActive()) {
                ChemicalUtil.emit(Capabilities.GAS_HANDLER_CAPABILITY, getMultiblock().getDirectionsToEmit(getPos()), getMultiblock().outputTank, this);
            }

            if (!energyContainer.isEmpty() && getMultiblock().canSupplyCoilEnergy(this)) {
                getMultiblock().supplyCoilEnergy(this, energyContainer.extract(energyContainer.getEnergy(), Action.EXECUTE, AutomationType.INTERNAL));
            }
        }
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this));
        return builder.build();
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks() {
        //Note: We can just use a proxied holder as the input/output restrictions are done in the tanks themselves
        return side -> getMultiblock().getGasTanks(side);
    }

    @Override
    public boolean persists(SubstanceType type) {
        if (type == SubstanceType.GAS) {
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
                  MekanismLang.SPS_PORT_MODE.translateColored(EnumColor.GRAY, InputOutput.of(oldMode, true))));
        }
        return ActionResultType.SUCCESS;
    }
}
