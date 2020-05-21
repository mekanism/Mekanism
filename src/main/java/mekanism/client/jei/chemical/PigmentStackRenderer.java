package mekanism.client.jei.chemical;

import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import net.minecraftforge.fluids.FluidAttributes;

public class PigmentStackRenderer extends ChemicalStackRenderer<Pigment, PigmentStack> {

    public PigmentStackRenderer() {
        super(FluidAttributes.BUCKET_VOLUME, TooltipMode.ITEM_LIST, TEX_WIDTH, TEX_HEIGHT, null);
    }

    public PigmentStackRenderer(long capacityMb, int width, int height) {
        super(capacityMb, TooltipMode.SHOW_AMOUNT_NO_UNITS, width, height, null);
    }
}