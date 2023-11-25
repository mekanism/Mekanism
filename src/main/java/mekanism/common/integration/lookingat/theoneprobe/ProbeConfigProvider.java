package mekanism.common.integration.lookingat.theoneprobe;

import mcjty.theoneprobe.api.IProbeConfig;
import mcjty.theoneprobe.api.IProbeConfigProvider;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeHitEntityData;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.base.TileEntityUpdateable;
import mekanism.common.util.WorldUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ProbeConfigProvider implements IProbeConfigProvider {

    public static final ProbeConfigProvider INSTANCE = new ProbeConfigProvider();

    @Override
    public void getProbeConfig(IProbeConfig config, Player player, Level world, Entity entity, IProbeHitEntityData data) {
    }

    @Override
    public void getProbeConfig(IProbeConfig config, Player player, Level world, BlockState blockState, IProbeHitData data) {
        BlockEntity tile = WorldUtils.getTileEntity(world, data.getPos());
        if (Capabilities.STRICT_ENERGY.getCapabilityIfLoaded(world, data.getPos(), null, tile, null) != null) {
            config.setRFMode(0);
        }
        if (tile instanceof TileEntityUpdateable) {
            //Disable the default fluid view for our own tiles
            config.setTankMode(0);
        }
    }
}