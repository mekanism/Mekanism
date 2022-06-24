package mekanism.client.render.data;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.NotNull;

//TODO - 1.18: Make it possible for chemicals to define a "glow/light" value and then use that here
public abstract class ChemicalRenderData<STACK extends ChemicalStack<?>> extends RenderData {

    @NotNull
    public final STACK chemicalType;

    protected ChemicalRenderData(@NotNull STACK chemicalType) {
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
    public boolean isGaseous() {
        return false;
    }

    @Override
    public int hashCode() {
        int code = super.hashCode();
        code = 31 * code + chemicalType.getType().hashCode();
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || getClass() != o.getClass() || !super.equals(o)) {
            return false;
        }
        return chemicalType.getType() == ((ChemicalRenderData<?>) o).chemicalType.getType();
    }

    public static class GasRenderData extends ChemicalRenderData<GasStack> {

        public GasRenderData(@NotNull GasStack chemicalType) {
            super(chemicalType);
        }

        @Override
        public boolean isGaseous() {
            return true;
        }
    }

    public static class InfusionRenderData extends ChemicalRenderData<InfusionStack> {

        public InfusionRenderData(@NotNull InfusionStack chemicalType) {
            super(chemicalType);
        }
    }

    public static class PigmentRenderData extends ChemicalRenderData<PigmentStack> {

        public PigmentRenderData(@NotNull PigmentStack chemicalType) {
            super(chemicalType);
        }
    }

    public static class SlurryRenderData extends ChemicalRenderData<SlurryStack> {

        public SlurryRenderData(@NotNull SlurryStack chemicalType) {
            super(chemicalType);
        }
    }
}