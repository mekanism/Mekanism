package mekanism.common.integration.lookingat.theoneprobe;

import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.IProbeConfig;
import mcjty.theoneprobe.api.IProbeConfig.ConfigMode;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;
import mekanism.common.Mekanism;
import mekanism.common.integration.lookingat.ChemicalElement;
import mekanism.common.integration.lookingat.EnergyElement;
import mekanism.common.integration.lookingat.FluidElement;
import mekanism.common.integration.lookingat.LookingAtHelper;
import mekanism.common.integration.lookingat.LookingAtUtils;
import mekanism.common.integration.lookingat.theoneprobe.TOPChemicalElement.ChemicalElementFactory;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

//Registered via IMC
public class TOPProvider implements IProbeInfoProvider, Function<ITheOneProbe, Void> {

    private BooleanSupplier displayFluidTanks;
    private Supplier<ConfigMode> tankMode = () -> ConfigMode.EXTENDED;

    @Override
    public Void apply(ITheOneProbe probe) {
        probe.registerProvider(this);
        probe.registerEntityProvider(TOPEntityProvider.INSTANCE);
        probe.registerProbeConfigProvider(ProbeConfigProvider.INSTANCE);
        probe.registerElementFactory(new TOPEnergyElement.Factory());
        probe.registerElementFactory(new TOPFluidElement.Factory());
        probe.registerElementFactory(new ChemicalElementFactory());
        //Grab the default view settings
        IProbeConfig probeConfig = probe.createProbeConfig();
        displayFluidTanks = () -> probeConfig.getTankMode() > 0;
        tankMode = probeConfig::getShowTankSetting;
        return null;
    }

    @Override
    public ResourceLocation getID() {
        return Mekanism.rl("data");
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo info, Player player, Level world, BlockState blockState, IProbeHitData data) {
        BlockPos pos = data.getPos();
        BlockEntity tile = WorldUtils.getTileEntity(world, pos);
        LookingAtUtils.addInfoOrRedirect(new TOPLookingAtHelper(info), world, pos, blockState, tile, displayTanks(mode), displayFluidTanks.getAsBoolean());
    }

    private boolean displayTanks(ProbeMode mode) {
        return switch (tankMode.get()) {
            case NOT -> false;//Don't display tanks
            case NORMAL -> mode == ProbeMode.NORMAL;
            case EXTENDED -> mode == ProbeMode.EXTENDED;
        };
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
        public void addEnergyElement(EnergyElement element) {
            info.element(new TOPEnergyElement(element));
        }

        @Override
        public void addFluidElement(FluidElement element) {
            info.element(new TOPFluidElement(element));
        }

        @Override
        public void addChemicalElement(ChemicalElement element) {
            info.element(new TOPChemicalElement(element));
        }
    }
}