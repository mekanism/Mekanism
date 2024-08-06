package mekanism.client.render.data;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

//TODO - 1.18: Make it possible for chemicals to define a "glow/light" value and then use that here
@NothingNullByDefault
public abstract class ChemicalRenderData<CHEMICAL extends Chemical> extends RenderData {

    public final CHEMICAL chemical;

    protected ChemicalRenderData(BlockPos renderLocation, int width, int height, int length, CHEMICAL chemical) {
        super(renderLocation, width, height, length);
        this.chemical = chemical;
    }

    @Override
    public int getColorARGB(float scale) {
        return MekanismRenderer.getColorARGB(chemical, scale, isGaseous());
    }

    @Override
    public TextureAtlasSprite getTexture() {
        return MekanismRenderer.getChemicalTexture(chemical);
    }

    @Override
    public boolean isGaseous() {
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), chemical);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass() || !super.equals(o)) {
            return false;
        }
        return chemical == ((ChemicalRenderData<?>) o).chemical;
    }

    public static class GasRenderData extends ChemicalRenderData<Chemical> {

        public GasRenderData(BlockPos renderLocation, int width, int height, int length, Chemical gas) {
            super(renderLocation, width, height, length, gas);
        }

        @Override
        public boolean isGaseous() {
            return true;
        }
    }

    public static class InfusionRenderData extends ChemicalRenderData<Chemical> {

        public InfusionRenderData(BlockPos renderLocation, int width, int height, int length, Chemical infuseType) {
            super(renderLocation, width, height, length, infuseType);
        }
    }

    public static class PigmentRenderData extends ChemicalRenderData<Chemical> {

        public PigmentRenderData(BlockPos renderLocation, int width, int height, int length, Chemical pigment) {
            super(renderLocation, width, height, length, pigment);
        }
    }

    public static class SlurryRenderData extends ChemicalRenderData<Chemical> {

        public SlurryRenderData(BlockPos renderLocation, int width, int height, int length, Chemical slurry) {
            super(renderLocation, width, height, length, slurry);
        }
    }
}