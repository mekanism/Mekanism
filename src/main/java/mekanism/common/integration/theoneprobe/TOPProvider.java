package mekanism.common.integration.theoneprobe;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.IElement;
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
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.chemical.merged.ChemicalTankWrapper;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.merged.MergedChemicalTank.Current;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.block.BlockBounding;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.fluid.FluidTankWrapper;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.capabilities.merged.MergedTank.CurrentType;
import mekanism.common.capabilities.proxy.ProxyChemicalHandler;
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
import net.minecraft.util.math.BlockPos;
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
        BlockPos pos = data.getPos();
        if (blockState.getBlock() instanceof BlockBounding) {
            //If we are a bounding block that has a position set, redirect the probe to the main location
            BlockPos mainPos = BlockBounding.getMainBlockPos(world, pos);
            if (mainPos != null) {
                pos = mainPos;
                blockState = world.getBlockState(mainPos);
            }
        }
        TileEntity tile = MekanismUtils.getTileEntity(world, pos);
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
                addInfo(tile, structure, Capabilities.GAS_HANDLER_CAPABILITY, multiblock -> multiblock.getGasTanks(null), info, GasElement::new, MekanismLang.GAS, Current.GAS, CurrentType.GAS);
                addInfo(tile, structure, Capabilities.INFUSION_HANDLER_CAPABILITY, multiblock -> multiblock.getInfusionTanks(null), info, InfuseTypeElement::new, MekanismLang.INFUSE_TYPE, Current.INFUSION, CurrentType.INFUSION);
                addInfo(tile, structure, Capabilities.PIGMENT_HANDLER_CAPABILITY, multiblock -> multiblock.getPigmentTanks(null), info, PigmentElement::new, MekanismLang.PIGMENT, Current.PIGMENT, CurrentType.PIGMENT);
                addInfo(tile, structure, Capabilities.SLURRY_HANDLER_CAPABILITY, multiblock -> multiblock.getSlurryTanks(null), info, SlurryElement::new, MekanismLang.SLURRY, Current.SLURRY, CurrentType.SLURRY);
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
        if (fluidHandler instanceof IMekanismFluidHandler) {
            IMekanismFluidHandler mekFluidHandler = (IMekanismFluidHandler) fluidHandler;
            for (IExtendedFluidTank fluidTank : mekFluidHandler.getFluidTanks(null)) {
                if (fluidTank instanceof FluidTankWrapper) {
                    MergedTank mergedTank = ((FluidTankWrapper) fluidTank).getMergedTank();
                    CurrentType currentType = mergedTank.getCurrentType();
                    if (currentType != CurrentType.EMPTY && currentType != CurrentType.FLUID) {
                        //Skip if the tank is on a chemical
                        continue;
                    }
                }
                addFluidInfo(info, fluidTank.getFluid(), fluidTank.getCapacity());
            }
        } else {
            //Fallback handling if it is not our fluid handler (probably never gets used)
            for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
                addFluidInfo(info, fluidHandler.getFluidInTank(tank), fluidHandler.getTankCapacity(tank));
            }
        }
    }

    private void addFluidInfo(IProbeInfo info, FluidStack fluidInTank, int capacity) {
        if (!fluidInTank.isEmpty()) {
            info.text(getNameComponent(MekanismLang.LIQUID.translate(fluidInTank)));
        }
        info.element(new FluidElement(fluidInTank, capacity));
    }

    private void displayEnergy(IProbeInfo info, IStrictEnergyHandler energyHandler) {
        int containers = energyHandler.getEnergyContainerCount();
        for (int container = 0; container < containers; container++) {
            info.element(new EnergyElement(energyHandler.getEnergy(container), energyHandler.getMaxEnergy(container)));
        }
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>,
          HANDLER extends IChemicalHandler<CHEMICAL, STACK>> void addInfo(TileEntity tile, @Nullable MultiblockData structure, Capability<HANDLER> capability,
          Function<MultiblockData, List<TANK>> multiBlockToTanks, IProbeInfo info, ElementCreator<CHEMICAL, STACK> elementCreator, ILangEntry langEntry,
          Current matchingCurrent, CurrentType matchingCurrentType) {
        Optional<HANDLER> cap = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, capability, null));
        if (cap.isPresent()) {
            HANDLER handler = cap.get();
            if (handler instanceof ProxyChemicalHandler) {
                List<TANK> tanks = ((ProxyChemicalHandler<CHEMICAL, STACK, ?>) handler).getTanksIfMekanism();
                if (!tanks.isEmpty()) {
                    //If there are any tanks add them and then exit, otherwise continue on assuming it is not a mekanism handler that is wrapped
                    for (TANK tank : tanks) {
                        addChemicalTankInfo(info, elementCreator, langEntry, tank, matchingCurrent, matchingCurrentType);
                    }
                    return;
                }
            }
            if (handler instanceof IMekanismChemicalHandler) {
                IMekanismChemicalHandler<CHEMICAL, STACK, TANK> mekHandler = (IMekanismChemicalHandler<CHEMICAL, STACK, TANK>) handler;
                for (TANK tank : mekHandler.getChemicalTanks(null)) {
                    addChemicalTankInfo(info, elementCreator, langEntry, tank, matchingCurrent, matchingCurrentType);
                }
            } else {
                for (int i = 0; i < handler.getTanks(); i++) {
                    addChemicalInfo(info, elementCreator, langEntry, handler.getChemicalInTank(i), handler.getTankCapacity(i));
                }
            }
        } else if (structure != null && structure.isFormed()) {
            //Special handling to allow viewing the chemicals in a multiblock when looking at things other than the ports
            for (TANK tank : multiBlockToTanks.apply(structure)) {
                addChemicalTankInfo(info, elementCreator, langEntry, tank, matchingCurrent, matchingCurrentType);
            }
        }
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> void addChemicalTankInfo(
          IProbeInfo info, ElementCreator<CHEMICAL, STACK> elementCreator, ILangEntry langEntry, TANK chemicalTank, Current matchingCurrent,
          CurrentType matchingCurrentType) {
        if (chemicalTank instanceof ChemicalTankWrapper) {
            MergedChemicalTank mergedTank = ((ChemicalTankWrapper<CHEMICAL, STACK>) chemicalTank).getMergedTank();
            if (mergedTank instanceof MergedTank) {
                //If we are also support fluid, only show if we are the correct type
                CurrentType currentType = ((MergedTank) mergedTank).getCurrentType();
                if (((MergedTank) mergedTank).getCurrentType() != matchingCurrentType) {
                    //Skip if the tank is not the correct chemical type (fluid is default for merged tanks when empty)
                    return;
                }
            } else {
                Current current = mergedTank.getCurrent();
                if (current == Current.EMPTY) {
                    if (matchingCurrent != Current.GAS) {
                        //Skip tanks if overall it is empty and it is not the gas tank
                        return;
                    }
                } else if (current != matchingCurrent) {
                    //Skip if the tank is on the wrong type of chemical
                    return;
                }
            }
        }
        addChemicalInfo(info, elementCreator, langEntry, chemicalTank.getStack(), chemicalTank.getCapacity());
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void addChemicalInfo(IProbeInfo info,
          ElementCreator<CHEMICAL, STACK> elementCreator, ILangEntry langEntry, STACK chemicalInTank, long capacity) {
        if (!chemicalInTank.isEmpty()) {
            info.text(getNameComponent(langEntry.translate(chemicalInTank.getType())));
        }
        info.element(elementCreator.create(chemicalInTank, capacity));
    }

    @FunctionalInterface
    private interface ElementCreator<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> {

        IElement create(STACK stored, long capacity);
    }
}