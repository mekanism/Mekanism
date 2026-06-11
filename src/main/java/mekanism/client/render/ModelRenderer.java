package mekanism.client.render;

import mekanism.common.util.MekanismUtils;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public final class ModelRenderer {

    private ModelRenderer() {
    }

    private static final int BLOCK_STAGES = FluidType.BUCKET_VOLUME;

    public static int getStage(FluidResource fluidType, int stages, double scale) {
        return getStage(MekanismUtils.lighterThanAirGas(fluidType), stages, scale);
    }

    public static int getStage(boolean gaseous, int stages, double scale) {
        if (gaseous) {
            return stages - 1;
        }
        return Math.min(stages - 1, (int) (scale * (stages - 1)));
    }

    /// @apiNote If the data is gaseous then scale is ignored
    public static float getMaxY(int multiblockHeight, double scale, boolean gaseous) {
        int maxStages = getMaxStages(multiblockHeight);
        int stage = getStage(scale, maxStages, multiblockHeight, gaseous);
        return Math.max(0.02F, multiblockHeight * ((float) stage / maxStages) - 0.02F);
    }

    public static int getMaxStages(int multiblockHeight) {
        return Math.max(multiblockHeight * BLOCK_STAGES, 1);
    }

    public static int getStage(double scale, int maxStages, int multiblockHeight, boolean gaseous) {
        int stage;
        if (multiblockHeight == 0) {
            //If there is no height set it to 1 for the stage as max stages is going to be one as well
            stage = 1;
        } else if (gaseous) {
            stage = maxStages;
        } else {
            stage = Math.min(maxStages, (int) (scale * maxStages));
        }
        return stage;
    }

    public static boolean shouldSkipValveRender(float mainFluidHeight, Direction valveSide, int valveHeight, int valveFluidHeight) {
        return switch (valveSide) {
            case DOWN -> mainFluidHeight >= 0.49F;
            case UP -> mainFluidHeight >= valveHeight;
            default -> mainFluidHeight - valveFluidHeight >= 0.69F;
        };
    }
}