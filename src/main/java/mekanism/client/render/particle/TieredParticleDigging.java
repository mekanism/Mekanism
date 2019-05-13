package mekanism.client.render.particle;

import javax.annotation.Nullable;
import mekanism.api.EnumColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TieredParticleDigging extends ParticleDigging {

    @Nullable
    private final EnumColor color;

    public TieredParticleDigging(World world, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, IBlockState state, @Nullable EnumColor color) {
        super(world, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, state);
        this.color = color;
    }

    @Override
    protected void multiplyColor(@Nullable BlockPos pos) {
        if (color == null) {
            return;
        }
        this.particleRed *= color.getColor(0);
        this.particleGreen *= color.getColor(1);
        this.particleBlue *= color.getColor(2);
    }
}