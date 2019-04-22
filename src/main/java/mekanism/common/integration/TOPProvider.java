package mekanism.common.integration;

import java.util.function.Function;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.IProgressStyle;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

@SuppressWarnings("unused")//IMC bound
public class TOPProvider implements Function<ITheOneProbe, Void>, IProbeInfoProvider {

    @Override
    public Void apply(ITheOneProbe iTheOneProbe) {
        iTheOneProbe.registerProvider(this);
        return null;
    }

    @Override
    public String getID() {
        return Mekanism.MODID;
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world,
          IBlockState blockState, IProbeHitData data) {
        if (mode != ProbeMode.EXTENDED) {
            return;
        }

        final TileEntity tile = world.getTileEntity(data.getPos());

        if (tile != null) {
            if (tile.hasCapability(Capabilities.GAS_HANDLER_CAPABILITY, null)) {
                IGasHandler handler = tile.getCapability(Capabilities.GAS_HANDLER_CAPABILITY, null);

                if (handler != null) {
                    GasTankInfo[] tanks = handler.getTankInfo();

                    for (GasTankInfo tank : tanks) {
                        IProgressStyle style = probeInfo.defaultProgressStyle().suffix("mB");
                        if (tank.getGas() != null) {
                            Gas gas = tank.getGas().getGas();
                            probeInfo.text(TextStyleClass.NAME + "Gas: " + gas.getLocalizedName());
                            int tint = gas.getTint();
                            //TOP respects transparency so we need to filter out the transparent layer
                            // if the gas has one. (Currently they are all fully transparent)
                            if ((tint & 0xFF000000) == 0) {
                                tint = 0xFF000000 | tint;
                            }
                            //TODO: Fix Hydrogen not displaying the color properly
                            style = style.filledColor(tint).alternateFilledColor(tint);
                        }
                        probeInfo.progress(tank.getStored(), tank.getMaxGas(), style);
                    }
                }
            }
        }
    }
}
