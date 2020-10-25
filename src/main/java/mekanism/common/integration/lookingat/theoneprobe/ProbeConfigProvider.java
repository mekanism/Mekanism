package mekanism.common.integration.lookingat.theoneprobe;

import mcjty.theoneprobe.api.IProbeConfig;
import mcjty.theoneprobe.api.IProbeConfigProvider;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeHitEntityData;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.base.TileEntityUpdateable;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ProbeConfigProvider implements IProbeConfigProvider {

    public static final ProbeConfigProvider INSTANCE = new ProbeConfigProvider();

    @Override
    public void getProbeConfig(IProbeConfig config, PlayerEntity player, World world, Entity entity, IProbeHitEntityData data) {
    }

    @Override
    public void getProbeConfig(IProbeConfig config, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        TileEntity tile = WorldUtils.getTileEntity(world, data.getPos());
        if (CapabilityUtils.getCapability(tile, Capabilities.STRICT_ENERGY_CAPABILITY, null).isPresent()) {
            config.setRFMode(0);
        }
        if (tile instanceof TileEntityUpdateable) {
            //Disable the default fluid view for our own tiles
            config.setTankMode(0);
        }
    }
}