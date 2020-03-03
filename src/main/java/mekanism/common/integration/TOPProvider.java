package mekanism.common.integration;

import java.util.function.Function;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.IProgressStyle;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandlerWrapper;
import mekanism.api.chemical.gas.GasHandlerWrapper;
import mekanism.api.chemical.infuse.InfusionHandlerWrapper;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

//Registered via IMC
@SuppressWarnings("unused")
public class TOPProvider implements IProbeInfoProvider, Function<ITheOneProbe, Void> {

    @Override
    public Void apply(ITheOneProbe probe) {
        probe.registerProvider(this);
        return null;
    }

    @Override
    public String getID() {
        return Mekanism.rl("chemicals").toString();
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        if (mode != ProbeMode.EXTENDED) {
            return;
        }
        TileEntity tile = MekanismUtils.getTileEntity(world, data.getPos());
        if (tile != null) {
            CapabilityUtils.getCapability(tile, Capabilities.GAS_HANDLER_CAPABILITY, null).ifPresent(handler ->
                  addInfo(new GasHandlerWrapper(handler), probeInfo, true, MekanismLang.GAS));
            CapabilityUtils.getCapability(tile, Capabilities.INFUSION_HANDLER_CAPABILITY, null).ifPresent(handler ->
                  addInfo(new InfusionHandlerWrapper(handler), probeInfo, false, MekanismLang.INFUSE_TYPE));
        }
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void addInfo(IChemicalHandlerWrapper<CHEMICAL, STACK> wrapper,
          IProbeInfo probeInfo, boolean hasSuffix, ILangEntry langEntry) {
        for (int i = 0; i < wrapper.getTanks(); i++) {
            STACK chemicalInTank = wrapper.getChemicalInTank(i);
            IProgressStyle style = probeInfo.defaultProgressStyle();
            if (hasSuffix) {
                style = style.suffix("mB");
            }
            if (!chemicalInTank.isEmpty()) {
                CHEMICAL chemical = chemicalInTank.getType();
                probeInfo.text(TextStyleClass.NAME + langEntry.translate(chemical).getFormattedText());
                int tint = chemical.getTint();
                //TOP respects transparency so we need to filter out the transparent layer
                // if the chemical has one. (Currently they are all fully transparent)
                if ((tint & 0xFF000000) == 0) {
                    tint = 0xFF000000 | tint;
                }
                if (tint != 0xFFFFFFFF) {
                    //TOP bugs out with full white background so just use default instead
                    // The default is a slightly off white color so is better for readability
                    style = style.filledColor(tint).alternateFilledColor(tint);
                }
            }
            probeInfo.progress(chemicalInTank.getAmount(), wrapper.getTankCapacity(i), style);
        }
    }
}