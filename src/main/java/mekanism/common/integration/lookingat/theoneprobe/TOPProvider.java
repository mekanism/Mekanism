package mekanism.common.integration.lookingat.theoneprobe;

import java.util.Optional;
import java.util.function.Function;
import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.IProbeConfig;
import mcjty.theoneprobe.api.IProbeConfig.ConfigMode;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.MergedChemicalTank.Current;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.block.BlockBounding;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.merged.MergedTank.CurrentType;
import mekanism.common.integration.lookingat.LookingAtHelper;
import mekanism.common.integration.lookingat.LookingAtUtils;
import mekanism.common.integration.lookingat.theoneprobe.TOPChemicalElement.GasElement;
import mekanism.common.integration.lookingat.theoneprobe.TOPChemicalElement.InfuseTypeElement;
import mekanism.common.integration.lookingat.theoneprobe.TOPChemicalElement.PigmentElement;
import mekanism.common.integration.lookingat.theoneprobe.TOPChemicalElement.SlurryElement;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.base.TileEntityUpdateable;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

//Registered via IMC
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
        ENERGY_ELEMENT_ID = probe.registerElementFactory(TOPEnergyElement::new);
        FLUID_ELEMENT_ID = probe.registerElementFactory(TOPFluidElement::new);
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
            TOPLookingAtHelper helper = new TOPLookingAtHelper(info);
            MultiblockData structure = LookingAtUtils.getMultiblock(tile);
            Optional<IStrictEnergyHandler> energyCapability = CapabilityUtils.getCapability(tile, Capabilities.STRICT_ENERGY_CAPABILITY, null).resolve();
            if (energyCapability.isPresent()) {
                LookingAtUtils.displayEnergy(helper, energyCapability.get());
            } else if (structure != null && structure.isFormed()) {
                //Special handling to allow viewing the energy of multiblock's when looking at things other than the ports
                LookingAtUtils.displayEnergy(helper, structure);
            }
            if (tankMode == ConfigMode.NOT) {
                //Don't display tanks
                return;
            }
            ProbeMode requiredMode = tankMode == ConfigMode.NORMAL ? ProbeMode.NORMAL : ProbeMode.EXTENDED;
            if (mode == requiredMode) {
                //Fluid - only add it to our own tiles in which we disable the default display for
                if (displayFluidTanks && tile instanceof TileEntityUpdateable) {
                    Optional<IFluidHandler> fluidCapability = CapabilityUtils.getCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null).resolve();
                    if (fluidCapability.isPresent()) {
                        LookingAtUtils.displayFluid(helper, fluidCapability.get());
                    } else if (structure != null && structure.isFormed()) {
                        //Special handling to allow viewing the fluid in a multiblock when looking at things other than the ports
                        LookingAtUtils.displayFluid(helper, structure);
                    }
                }
                //Chemicals
                LookingAtUtils.addInfo(tile, structure, Capabilities.GAS_HANDLER_CAPABILITY, multiblock -> multiblock.getGasTanks(null), helper, MekanismLang.GAS, Current.GAS, CurrentType.GAS);
                LookingAtUtils.addInfo(tile, structure, Capabilities.INFUSION_HANDLER_CAPABILITY, multiblock -> multiblock.getInfusionTanks(null), helper, MekanismLang.INFUSE_TYPE, Current.INFUSION, CurrentType.INFUSION);
                LookingAtUtils.addInfo(tile, structure, Capabilities.PIGMENT_HANDLER_CAPABILITY, multiblock -> multiblock.getPigmentTanks(null), helper, MekanismLang.PIGMENT, Current.PIGMENT, CurrentType.PIGMENT);
                LookingAtUtils.addInfo(tile, structure, Capabilities.SLURRY_HANDLER_CAPABILITY, multiblock -> multiblock.getSlurryTanks(null), helper, MekanismLang.SLURRY, Current.SLURRY, CurrentType.SLURRY);
            }
        }
    }

    private static class TOPLookingAtHelper implements LookingAtHelper {

        private final IProbeInfo info;

        public TOPLookingAtHelper(IProbeInfo info) {
            this.info = info;
        }

        @Override
        public void addText(ITextComponent text) {
            info.text(CompoundText.create().name(text).get());
        }

        @Override
        public void addEnergyElement(FloatingLong energy, FloatingLong maxEnergy) {
            info.element(new TOPEnergyElement(energy, maxEnergy));
        }

        @Override
        public void addFluidElement(FluidStack stored, int capacity) {
            info.element(new TOPFluidElement(stored, capacity));
        }

        @Override
        public void addChemicalElement(ChemicalStack<?> stored, long capacity) {
            if (stored instanceof GasStack) {
                info.element(new GasElement((GasStack) stored, capacity));
            } else if (stored instanceof InfusionStack) {
                info.element(new InfuseTypeElement((InfusionStack) stored, capacity));
            } else if (stored instanceof PigmentStack) {
                info.element(new PigmentElement((PigmentStack) stored, capacity));
            } else if (stored instanceof SlurryStack) {
                info.element(new SlurryElement((SlurryStack) stored, capacity));
            }
        }
    }
}