package mekanism.client.render.data;

import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalStack;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public abstract class ChemicalRenderData<STACK extends ChemicalStack<?>> extends RenderData {

    @Nonnull
    public final STACK chemicalType;

    protected ChemicalRenderData(@Nonnull STACK chemicalType) {
        this.chemicalType = chemicalType;
    }

    @Override
    public int getColorARGB(float scale) {
        return MekanismRenderer.getColorARGB(chemicalType, scale, isGaseous());
    }

    @Override
    public TextureAtlasSprite getTexture() {
        return MekanismRenderer.getChemicalTexture(chemicalType.getType());
    }

    @Override
    public int hashCode() {
        int code = super.hashCode();
        code = 31 * code + chemicalType.getTypeRegistryName().hashCode();
        return code;
    }
}