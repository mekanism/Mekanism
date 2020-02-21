package mekanism.client.jei.chemical;

import javax.annotation.Nullable;
import mekanism.api.infuse.InfuseType;
import mekanism.api.infuse.InfusionStack;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraftforge.fluids.FluidAttributes;

public class InfusionStackRenderer extends ChemicalStackRenderer<InfuseType, InfusionStack> {

    public InfusionStackRenderer() {
        this(FluidAttributes.BUCKET_VOLUME, TooltipMode.ITEM_LIST, TEX_WIDTH, TEX_HEIGHT, null);
    }

    public InfusionStackRenderer(int capacityMb, int width, int height, @Nullable IDrawable overlay) {
        this(capacityMb, TooltipMode.SHOW_AMOUNT_NO_UNITS, width, height, overlay);
    }

    public InfusionStackRenderer(int capacityMb, TooltipMode tooltipMode, int width, int height, @Nullable IDrawable overlay) {
        super(capacityMb, tooltipMode, width, height, overlay);
    }
}