package mekanism.client.render.particle;

import javax.annotation.Nullable;
import mekanism.api.EnumColor;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tier.BaseTier;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TieredParticleDigging extends ParticleDigging {

    @Nullable
    private final EnumColor color;

    public TieredParticleDigging(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, IBlockState state,
          BaseTier tier) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D, state);
        if (particleTexture.getIconName().endsWith(BaseTier.BASIC.getName())) {
            this.color = null;
            //Change the texture instead
            String newTexture = particleTexture.getIconName().replace(BaseTier.BASIC.getName(), tier.getName());
            TextureAtlasSprite textureAtlasSprite = MekanismRenderer
                  .getTextureAtlasSprite(new ResourceLocation(newTexture));
            setParticleTexture(textureAtlasSprite);
        } else {
            this.color = tier.getColor();
        }
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