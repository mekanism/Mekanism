package mekanism.client.render.ctm;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;

public class BlockRenderTypeCTM {

    public ChiselTextureCTM makeTexture(EnumWorldBlockLayer layer, TextureSpriteCallback... sprites) {
      return new ChiselTextureCTM(this, layer, sprites);
    }

    public CTMBlockRenderContext getBlockRenderContext(IBlockAccess world, BlockPos pos) {
        return new CTMBlockRenderContext(world, pos);
    }

    public int getQuadsPerSide() {
        return 4;
    }

    public int requiredTextures() {
        return 2;
    }
}
