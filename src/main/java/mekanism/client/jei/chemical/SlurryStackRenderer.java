package mekanism.client.jei.chemical;

import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import net.minecraftforge.fluids.FluidAttributes;

public class SlurryStackRenderer extends ChemicalStackRenderer<Slurry, SlurryStack> {

    public SlurryStackRenderer() {
        super(FluidAttributes.BUCKET_VOLUME, TooltipMode.ITEM_LIST, TEX_WIDTH, TEX_HEIGHT, null);
    }

    public SlurryStackRenderer(long capacityMb, int width, int height) {
        super(capacityMb, TooltipMode.SHOW_AMOUNT_NO_UNITS, width, height, null);
    }
}