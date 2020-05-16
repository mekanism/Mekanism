package mekanism.common.integration.theoneprobe;

import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.theoneprobe.api.IElementNew;
import mcjty.theoneprobe.api.IProbeConfig;
import mcjty.theoneprobe.api.IProbeConfig.ConfigMode;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandlerWrapper;
import mekanism.api.chemical.gas.GasHandlerWrapper;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IMekanismInfusionHandler;
import mekanism.api.chemical.infuse.InfusionHandlerWrapper;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.multiblock.IStructuralMultiblock;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.base.TileEntityUpdateable;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

//Registered via IMC
@SuppressWarnings("unused")
public class TOPProvider implements IProbeInfoProvider, Function<ITheOneProbe, Void> {

    private boolean displayFluidTanks;
    private ConfigMode tankMode = ConfigMode.EXTENDED;

    @Override
    public Void apply(ITheOneProbe probe) {
        probe.registerProvider(this);
        probe.registerProbeConfigProvider(ProbeConfigProvider.INSTANCE);
        EnergyElement.ID = probe.registerElementFactory(EnergyElement::new);
        FluidElement.ID = probe.registerElementFactory(FluidElement::new);
        GasElement.ID = probe.registerElementFactory(GasElement::new);
        InfuseTypeElement.ID = probe.registerElementFactory(InfuseTypeElement::new);
        //Grab the default view settings
        IProbeConfig probeConfig = probe.createProbeConfig();
        displayFluidTanks = probeConfig.getTankMode() > 0;
        tankMode = probeConfig.getShowTankSetting();
        return null;
    }

    @Override
    public String getID() {
        return Mekanism.rl("chemicals").toString();
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo info, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        TileEntity tile = MekanismUtils.getTileEntity(world, data.getPos());
        if (tile != null) {
            MultiblockData structure = getMultiblock(tile);
            Optional<IStrictEnergyHandler> energyCapability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, Capabilities.STRICT_ENERGY_CAPABILITY, null));
            if (energyCapability.isPresent()) {
                displayEnergy(info, energyCapability.get());
            } else if (structure.isFormed()) {
                //Special handling to allow viewing the energy of multiblock's when looking at things other than the ports
                displayEnergy(info, structure);
            }
            if (tankMode == ConfigMode.NOT) {
                //Don't display tanks
                return;
            }
            ProbeMode requiredMode = tankMode == ConfigMode.NORMAL ? ProbeMode.NORMAL : ProbeMode.EXTENDED;
            if (mode == requiredMode) {
                //Fluid - only add it to our own tiles in which we disable the default display for
                if (displayFluidTanks && tile instanceof TileEntityUpdateable) {
                    Optional<IFluidHandler> fluidCapability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null));
                    if (fluidCapability.isPresent()) {
                        displayFluid(info, fluidCapability.get());
                    } else if (structure.isFormed()) {
                        //Special handling to allow viewing the fluid in a multiblock when looking at things other than the ports
                        displayFluid(info, structure);
                    }
                }
                //Gas
                Optional<IGasHandler> gasCapability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, Capabilities.GAS_HANDLER_CAPABILITY, null));
                if (gasCapability.isPresent()) {
                    addInfo(new GasHandlerWrapper(gasCapability.get()), info, GasElement::new, MekanismLang.GAS);
                } else if (structure.isFormed()) {
                    //Special handling to allow viewing the gas in a multiblock when looking at things other than the ports
                    addInfo(new GasHandlerWrapper(structure), info, GasElement::new, MekanismLang.GAS);
                }
                //Infuse Type
                Optional<IInfusionHandler> infusionCapability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, Capabilities.INFUSION_HANDLER_CAPABILITY, null));
                if (infusionCapability.isPresent()) {
                    addInfo(new InfusionHandlerWrapper(infusionCapability.get()), info, InfuseTypeElement::new, MekanismLang.INFUSE_TYPE);
                } else if (structure.isFormed()) {
                    //Special handling to allow viewing the infusion types in a multiblock when looking at things other than the ports
                    addInfo(new InfusionHandlerWrapper((IMekanismInfusionHandler) structure), info, InfuseTypeElement::new, MekanismLang.INFUSE_TYPE);
                }
            }
        }
    }

    @Nullable
    private MultiblockData getMultiblock(@Nonnull TileEntity tile) {
        if (tile instanceof TileEntityMultiblock<?>) {
            return ((TileEntityMultiblock<?>) tile).getMultiblock();
        } else if (tile instanceof IStructuralMultiblock) {
            IStructuralMultiblock structuralMultiblock = (IStructuralMultiblock) tile;
            return structuralMultiblock.getMultiblock();
        }
        return null;
    }

    private void displayFluid(IProbeInfo info, IFluidHandler fluidHandler) {
        int tanks = fluidHandler.getTanks();
        for (int tank = 0; tank < tanks; tank++) {
            FluidStack fluidInTank = fluidHandler.getFluidInTank(tank);
            if (!fluidInTank.isEmpty()) {
                //Mimic TOP's liquid text, but localized
                info.text(TextStyleClass.NAME + MekanismLang.LIQUID.translate(fluidInTank).getFormattedText());
            }
            info.element(new FluidElement(fluidInTank, fluidHandler.getTankCapacity(tank)));
        }
    }

    private void displayEnergy(IProbeInfo info, IStrictEnergyHandler energyHandler) {
        int containers = energyHandler.getEnergyContainerCount();
        for (int container = 0; container < containers; container++) {
            info.element(new EnergyElement(energyHandler.getEnergy(container), energyHandler.getMaxEnergy(container)));
        }
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void addInfo(IChemicalHandlerWrapper<CHEMICAL, STACK> wrapper,
          IProbeInfo info, ElementCreator<CHEMICAL, STACK> elementCreator, ILangEntry langEntry) {
        for (int i = 0; i < wrapper.getTanks(); i++) {
            STACK chemicalInTank = wrapper.getChemicalInTank(i);
            if (!chemicalInTank.isEmpty()) {
                info.text(TextStyleClass.NAME + langEntry.translate(chemicalInTank.getType()).getFormattedText());
            }
            info.element(elementCreator.create(chemicalInTank, wrapper.getTankCapacity(i)));
        }
    }

    @FunctionalInterface
    private interface ElementCreator<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> {

        IElementNew create(STACK stored, long capacity);
    }
}