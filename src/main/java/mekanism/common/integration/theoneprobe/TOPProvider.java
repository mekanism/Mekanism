/*package mekanism.common.integration.theoneprobe;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.theoneprobe.api.CompoundText;
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
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.IStructuralMultiblock;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.tile.base.TileEntityUpdateable;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
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
    static int ENERGY_ELEMENT_ID;
    static int FLUID_ELEMENT_ID;
    static int GAS_ELEMENT_ID;
    static int INFUSION_ELEMENT_ID;
    static int PIGMENT_ELEMENT_ID;
    static int SLURRY_ELEMENT_ID;

    @Override
    public Void apply(ITheOneProbe probe) {
        probe.registerProvider(this);
        probe.registerProbeConfigProvider(ProbeConfigProvider.INSTANCE);
        ENERGY_ELEMENT_ID = probe.registerElementFactory(EnergyElement::new);
        FLUID_ELEMENT_ID = probe.registerElementFactory(FluidElement::new);
        GAS_ELEMENT_ID = probe.registerElementFactory(GasElement::new);
        INFUSION_ELEMENT_ID = probe.registerElementFactory(InfuseTypeElement::new);
        PIGMENT_ELEMENT_ID = probe.registerElementFactory(PigmentElement::new);
        SLURRY_ELEMENT_ID = probe.registerElementFactory(SlurryElement::new);
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
                addInfo(tile, structure, Capabilities.GAS_HANDLER_CAPABILITY, multiblock -> multiblock.getGasTanks(null), info, GasElement::new, MekanismLang.GAS);
                addInfo(tile, structure, Capabilities.INFUSION_HANDLER_CAPABILITY, multiblock -> multiblock.getInfusionTanks(null), info, InfuseTypeElement::new, MekanismLang.INFUSE_TYPE);
                addInfo(tile, structure, Capabilities.PIGMENT_HANDLER_CAPABILITY, multiblock -> multiblock.getPigmentTanks(null), info, PigmentElement::new, MekanismLang.PIGMENT);
                addInfo(tile, structure, Capabilities.SLURRY_HANDLER_CAPABILITY, multiblock -> multiblock.getSlurryTanks(null), info, SlurryElement::new, MekanismLang.SLURRY);
            }
        }
    }

    @Nullable
    private MultiblockData getMultiblock(@Nonnull TileEntity tile) {
        if (tile instanceof IMultiblock) {
            return ((IMultiblock<?>) tile).getMultiblock();
        } else if (tile instanceof IStructuralMultiblock) {
            for (Structure s : ((IStructuralMultiblock) tile).getStructureMap().values()) {
                if (s.isValid()) {
                    return s.getMultiblockData();
                }
            }
        }
        return null;
    }

    private ITextComponent getNameComponent(ITextComponent component) {
        return CompoundText.create().style(TextStyleClass.NAME).text(component).get();
    }

    private void displayFluid(IProbeInfo info, IFluidHandler fluidHandler) {
        int tanks = fluidHandler.getTanks();
        for (int tank = 0; tank < tanks; tank++) {
            FluidStack fluidInTank = fluidHandler.getFluidInTank(tank);
            if (!fluidInTank.isEmpty()) {
                info.text(getNameComponent(MekanismLang.LIQUID.translate(fluidInTank)));
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

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>,
          HANDLER extends IChemicalHandler<CHEMICAL, STACK>> void addInfo(TileEntity tile, @Nullable MultiblockData structure, Capability<HANDLER> capability,
          Function<MultiblockData, List<TANK>> multiBlockToTanks, IProbeInfo info, ElementCreator<CHEMICAL, STACK> elementCreator, ILangEntry langEntry) {
        Optional<HANDLER> cap = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, capability, null));
        if (cap.isPresent()) {
            HANDLER handler = cap.get();
            for (int i = 0; i < handler.getTanks(); i++) {
                STACK chemicalInTank = handler.getChemicalInTank(i);
                if (!chemicalInTank.isEmpty()) {
                    info.text(getNameComponent(langEntry.translate(chemicalInTank.getType())));
                }
                info.element(elementCreator.create(chemicalInTank, handler.getTankCapacity(i)));
            }
        } else if (structure != null && structure.isFormed()) {
            //Special handling to allow viewing the chemicals in a multiblock when looking at things other than the ports
            for (TANK tank : multiBlockToTanks.apply(structure)) {
                if (!tank.isEmpty()) {
                    info.text(getNameComponent(langEntry.translate(tank.getType())));
                }
                info.element(elementCreator.create(tank.getStack(), tank.getCapacity()));
            }
        }
    }

    @FunctionalInterface
    private interface ElementCreator<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> {

        IElementNew create(STACK stored, long capacity);
    }
}*/