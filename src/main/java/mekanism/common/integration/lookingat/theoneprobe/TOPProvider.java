package mekanism.common.integration.lookingat.theoneprobe;

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
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockBounding;
import mekanism.common.integration.lookingat.LookingAtHelper;
import mekanism.common.integration.lookingat.LookingAtUtils;
import mekanism.common.integration.lookingat.theoneprobe.TOPChemicalElement.GasElementFactory;
import mekanism.common.integration.lookingat.theoneprobe.TOPChemicalElement.InfuseTypeElementFactory;
import mekanism.common.integration.lookingat.theoneprobe.TOPChemicalElement.PigmentElementFactory;
import mekanism.common.integration.lookingat.theoneprobe.TOPChemicalElement.SlurryElementFactory;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

//Registered via IMC
public class TOPProvider implements IProbeInfoProvider, Function<ITheOneProbe, Void> {

    private boolean displayFluidTanks;
    private ConfigMode tankMode = ConfigMode.EXTENDED;

    @Override
    public Void apply(ITheOneProbe probe) {
        probe.registerProvider(this);
        probe.registerEntityProvider(TOPEntityProvider.INSTANCE);
        probe.registerProbeConfigProvider(ProbeConfigProvider.INSTANCE);
        probe.registerElementFactory(new TOPEnergyElement.Factory());
        probe.registerElementFactory(new TOPFluidElement.Factory());
        probe.registerElementFactory(new GasElementFactory());
        probe.registerElementFactory(new InfuseTypeElementFactory());
        probe.registerElementFactory(new PigmentElementFactory());
        probe.registerElementFactory(new SlurryElementFactory());
        //Grab the default view settings
        IProbeConfig probeConfig = probe.createProbeConfig();
        displayFluidTanks = probeConfig.getTankMode() > 0;
        tankMode = probeConfig.getShowTankSetting();
        return null;
    }

    @Override
    public ResourceLocation getID() {
        return Mekanism.rl("data");
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo info, Player player, Level world, BlockState blockState, IProbeHitData data) {
        BlockPos pos = data.getPos();
        if (blockState.getBlock() instanceof BlockBounding) {
            //If we are a bounding block that has a position set, redirect the probe to the main location
            BlockPos mainPos = BlockBounding.getMainBlockPos(world, pos);
            if (mainPos != null) {
                pos = mainPos;
                //If we end up needing the blockstate at some point lower down, then uncomment this line
                // until we do though there is no point in bothering to query the world to get it
                //blockState = world.getBlockState(mainPos);
            }
        }
        BlockEntity tile = WorldUtils.getTileEntity(world, pos);
        if (tile != null) {
            LookingAtUtils.addInfo(new TOPLookingAtHelper(info), tile, displayTanks(mode), displayFluidTanks);
        }
    }

    private boolean displayTanks(ProbeMode mode) {
        if (tankMode == ConfigMode.NOT) {
            //Don't display tanks
            return false;
        }
        if (tankMode == ConfigMode.NORMAL) {
            return mode == ProbeMode.NORMAL;
        }
        return mode == ProbeMode.EXTENDED;
    }

    static class TOPLookingAtHelper implements LookingAtHelper {

        private final IProbeInfo info;

        public TOPLookingAtHelper(IProbeInfo info) {
            this.info = info;
        }

        @Override
        public void addText(Component text) {
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
            TOPChemicalElement element = TOPChemicalElement.create(stored, capacity);
            if (element != null) {
                info.element(element);
            }
        }
    }
}