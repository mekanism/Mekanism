package mekanism.client.render.data;

import java.util.Objects;
import mekanism.api.MekanismAPITags;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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
        return Objects.hash(super.hashCode(), chemical);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass() || !super.equals(o)) {
            return false;
        }
        return chemical.is(((ChemicalRenderData) o).chemical);
    }
}