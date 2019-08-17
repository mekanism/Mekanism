package mekanism.common.temporary;

import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

//TODO: Remove this when Forge adds back fluid support.
// This class mainly exists to remove the compile errors due to forge not having a fluid system yet in 1.14
public class FluidRegistry {

    public static Fluid WATER = new Fluid("water", new ResourceLocation("blocks/water_still"), new ResourceLocation("blocks/water_flow"),
          new ResourceLocation("blocks/water_overlay")).setBlock(Blocks.WATER).setUnlocalizedName(Blocks.WATER.getTranslationKey());
    public static Fluid LAVA = new Fluid("lava", new ResourceLocation("blocks/lava_still"), new ResourceLocation("blocks/lava_flow"))
          .setBlock(Blocks.LAVA).setLuminosity(15).setDensity(3000).setViscosity(6000).setTemperature(1300).setUnlocalizedName(Blocks.LAVA.getTranslationKey());

    public static Fluid getFluid(String name) {
        //Note: always return water as a temporary so that there is no crash
        return WATER;
    }

    public static FluidStack getFluidStack(String fluidName, int amount) {
        //Note: always return water as a temporary so that there is no crash
        return new FluidStack(WATER, amount);
    }

    public static boolean isFluidRegistered(String name) {
        return false;
    }

    public static String getFluidName(Fluid fluid) {
        return "FIXME";
    }
}