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
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.multiblock.IMultiblockBase;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.base.TileEntityUpdateable;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
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
        PigmentElement.ID = probe.registerElementFactory(PigmentElement::new);
        SlurryElement.ID = probe.registerElementFactory(SlurryElement::new);
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
            } else if (structure != null && structure.isFormed()) {
                //Special handling to allow viewing the energy of multiblock's when looking at things other than the ports
                displayEnergy(info, structure);
            }
            if (tankMode == ConfigMode.NOT) {
                //Don't display tanks
                return;
            }
            ProbeMode requiredMode = tankMode == ConfigMode.NORMAL ? ProbeMode.NORMAL : ProbeMode.EXTENDED;
            if (mode == requiredMode) {
                //TODO: Improve handling for displaying merged tanks/potentially hiding the alternate tank types
                //Fluid - only add it to our own tiles in which we disable the default display for
                if (displayFluidTanks && tile instanceof TileEntityUpdateable) {
                    Optional<IFluidHandler> fluidCapability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null));
                    if (fluidCapability.isPresent()) {
                        displayFluid(info, fluidCapability.get());
                    } else if (structure != null && structure.isFormed()) {
                        //Special handling to allow viewing the fluid in a multiblock when looking at things other than the ports
                        displayFluid(info, structure);
                    }
                }
                //Chemicals
                addInfo(tile, structure, Capabilities.GAS_HANDLER_CAPABILITY, info, GasElement::new, MekanismLang.GAS);
                addInfo(tile, structure, Capabilities.INFUSION_HANDLER_CAPABILITY, info, InfuseTypeElement::new, MekanismLang.INFUSE_TYPE);
                addInfo(tile, structure, Capabilities.PIGMENT_HANDLER_CAPABILITY, info, PigmentElement::new, MekanismLang.PIGMENT);
                addInfo(tile, structure, Capabilities.SLURRY_HANDLER_CAPABILITY, info, SlurryElement::new, MekanismLang.SLURRY);
            }
        }
    }

    @Nullable
    private MultiblockData getMultiblock(@Nonnull TileEntity tile) {
        if (tile instanceof IMultiblockBase) {
            return ((IMultiblockBase) tile).getMultiblockData();
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

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>> void addInfo(TileEntity tile,
          @Nullable MultiblockData structure, Capability<HANDLER> capability, IProbeInfo info, ElementCreator<CHEMICAL, STACK> elementCreator, ILangEntry langEntry) {
        Optional<HANDLER> cap = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, capability, null));
        HANDLER handler;
        if (cap.isPresent()) {
            handler = cap.get();
        } else if (structure != null && structure.isFormed()) {
            //Special handling to allow viewing the chemicals in a multiblock when looking at things other than the ports
            //TODO: ChemicalHandlers - FIXME (it won't be directly implementing the handler anymore)
            handler = (HANDLER) structure;
        } else {
            return;
        }
        for (int i = 0; i < handler.getTanks(); i++) {
            STACK chemicalInTank = handler.getChemicalInTank(i);
            if (!chemicalInTank.isEmpty()) {
                info.text(TextStyleClass.NAME + langEntry.translate(chemicalInTank.getType()).getFormattedText());
            }
            info.element(elementCreator.create(chemicalInTank, handler.getTankCapacity(i)));
        }
    }

    @FunctionalInterface
    private interface ElementCreator<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> {

        IElementNew create(STACK stored, long capacity);
    }
}