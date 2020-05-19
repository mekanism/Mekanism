package mekanism.generators.common.tile.fusion;

import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.tile.base.SubstanceType;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityFusionReactorController extends TileEntityFusionReactorBlock {

    public TileEntityFusionReactorController() {
        super(GeneratorsBlocks.FUSION_REACTOR_CONTROLLER);
        //Never allow the gas handler, fluid handler, or energy cap to be enabled here even though internally we can handle both of them
        addDisabledCapabilities(Capabilities.GAS_HANDLER_CAPABILITY, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Capabilities.HEAT_HANDLER_CAPABILITY);
        addDisabledCapabilities(EnergyCompatUtils.getEnabledEnergyCapabilities());
        addSemiDisabledCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, () -> !getMultiblock().isFormed());
        delaySupplier = () -> 0;
    }

    @Override
    public void onUpdateServer() {
        super.onUpdateServer();
        setActive(getMultiblock().isFormed());
    }

    @Override
    protected boolean canPlaySound() {
        return getMultiblock().isFormed() && getMultiblock().isBurning();
    }

    @Override
    public boolean handles(SubstanceType type) {
        if (type == SubstanceType.GAS || type == SubstanceType.FLUID || type == SubstanceType.HEAT) {
            return false;
        }
        return super.handles(type);
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }
}