package mekanism.common.integration;

import java.util.Optional;
import java.util.function.Function;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.IProgressStyle;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
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
        return Mekanism.MODID + ":gas";
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        if (mode != ProbeMode.EXTENDED) {
            return;
        }
        TileEntity tile = MekanismUtils.getTileEntity(world, data.getPos());
        if (tile != null) {
            //TODO: Show infusion type info via this also?
            Optional<IGasHandler> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, Capabilities.GAS_HANDLER_CAPABILITY, null));
            if (capability.isPresent()) {
                IGasHandler gasHandler = capability.get();
                for (int i = 0; i < gasHandler.getGasTankCount(); i++) {
                    GasStack gasInTank = gasHandler.getGasInTank(i);
                    IProgressStyle style = probeInfo.defaultProgressStyle().suffix("mB");
                    if (!gasInTank.isEmpty()) {
                        Gas gas = gasInTank.getType();
                        probeInfo.text(TextStyleClass.NAME + MekanismLang.GAS.translate(gas).getFormattedText());
                        int tint = gas.getTint();
                        //TOP respects transparency so we need to filter out the transparent layer
                        // if the gas has one. (Currently they are all fully transparent)
                        if ((tint & 0xFF000000) == 0) {
                            tint = 0xFF000000 | tint;
                        }
                        if (tint != 0xFFFFFFFF) {
                            //TOP bugs out with full white background so just use default instead
                            // The default is a slightly off white color so is better for readability
                            style = style.filledColor(tint).alternateFilledColor(tint);
                        }
                    }
                    probeInfo.progress(gasInTank.getAmount(), gasHandler.getGasTankCapacity(i), style);
                }
            }
        }
    }
}