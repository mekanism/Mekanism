package mekanism.client.render.data;

import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class GasRenderData extends RenderData {

    @Nonnull
    public GasStack gasType = GasStack.EMPTY;

    @Override
    public boolean isGaseous() {
        return true;
    }

    @Override
    public int getColorARGB(float scale) {
        return MekanismRenderer.getColorARGB(gasType, scale);
    }

    @Override
    public TextureAtlasSprite getTexture() {
        return MekanismRenderer.getChemicalTexture(gasType.getType());
    }

    @Override
    public int hashCode() {
        int code = super.hashCode();
        code = 31 * code + gasType.getType().getRegistryName().hashCode();
        return code;
    }

    @Override
    public boolean equals(Object data) {
        return super.equals(data) && data instanceof GasRenderData && gasType.isTypeEqual(((GasRenderData) data).gasType);
    }
}