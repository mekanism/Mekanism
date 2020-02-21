package mekanism.client.jei.chemical;

import mekanism.api.infuse.InfuseType;
import mekanism.api.infuse.InfusionStack;
import net.minecraftforge.fluids.FluidAttributes;

public class InfusionStackRenderer extends ChemicalStackRenderer<InfuseType, InfusionStack> {

    public InfusionStackRenderer() {
        super(FluidAttributes.BUCKET_VOLUME, TooltipMode.ITEM_LIST, TEX_WIDTH, TEX_HEIGHT, null);
    }

    public InfusionStackRenderer(int capacityMb, int width, int height) {
        super(capacityMb, TooltipMode.SHOW_AMOUNT_NO_UNITS, width, height, null);
    }
}