package mekanism.client;

import mekanism.api.Pos3D;
import mekanism.common.config.MekanismConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.world.World;

public class ClientLaserManager {

    //TODO: Remove, have server tell client about the effects
    // Is this even possible to do, I think it has to be calculated client side
    @Deprecated
    public static BlockRayTraceResult fireLaserClient(TileEntity source, Direction direction, World world) {
        Pos3D from = new Pos3D(source).centre().translate(direction, 0.501);
        Pos3D to = from.translate(direction, MekanismConfig.general.laserRange.get() - 0.002);
        //TODO: Verify this is correct
        return world.rayTraceBlocks(new RayTraceContext(from, to, BlockMode.COLLIDER, FluidMode.NONE, Minecraft.getInstance().player));
    }
}