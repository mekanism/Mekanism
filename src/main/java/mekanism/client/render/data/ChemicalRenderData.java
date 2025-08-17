package mekanism.client.render.data;

import mekanism.api.MekanismAPITags;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO - 1.18: Make it possible for chemicals to define a "glow/light" value and then use that here
@NothingNullByDefault
public class ChemicalRenderData extends RenderData {

    public final Holder<Chemical> chemical;

    public ChemicalRenderData(BlockPos renderLocation, int width, int height, int length, Holder<Chemical> chemical) {
        super(renderLocation, width, height, length);
        this.chemical = chemical;
    }

    @Override
    public int getColorARGB(float scale) {
        return MekanismRenderer.getColorARGB(chemical, scale);
    }

    @Override
    public TextureAtlasSprite getTexture() {
        return MekanismRenderer.getChemicalTexture(chemical);
    }

    @Override
    @SuppressWarnings("removal")
    public boolean isGaseous() {
        //TODO - 1.22: Remove the legacy check
        return chemical.is(MekanismAPITags.Chemicals.GASEOUS) || chemical.value().isGaseousLegacy();
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + chemical.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null) {
            return false;
        }
        return o.getClass() == ChemicalRenderData.class && equalsCommonChemical(o);
    }

    protected boolean equalsCommonChemical(@NotNull Object o) {
        return super.equals(o) && chemical.is(((ChemicalRenderData) o).chemical);
    }

    public static class Scaled extends ChemicalRenderData implements ScaledRenderData {

        private final float scale;

        public Scaled(BlockPos renderLocation, int width, int height, int length, Holder<Chemical> chemical, float scale) {
            super(renderLocation, width, height, length, chemical);
            this.scale = scale;
        }

        @Override
        public float scale() {
            return scale;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (o == this) {
                return true;
            } else if (o == null) {
                return false;
            }
            return o.getClass() == Scaled.class && equalsCommonChemical(o) && scale == ((Scaled) o).scale;
        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + Float.hashCode(scale);
        }
    }
}