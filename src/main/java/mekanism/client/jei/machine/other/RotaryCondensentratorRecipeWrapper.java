package mekanism.client.jei.machine.other;

import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.util.LangUtils;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class RotaryCondensentratorRecipeWrapper implements IRecipeWrapper {

    public static final int GAS_AMOUNT = 1;
    public static final int FLUID_AMOUNT = 1;
    private Fluid fluidType;
    private Gas gasType;
    private boolean condensentrating;

    public RotaryCondensentratorRecipeWrapper(Fluid fluid, Gas gas, boolean b) {
        fluidType = fluid;
        gasType = gas;
        condensentrating = b;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        if (condensentrating) {
            ingredients.setInput(MekanismJEI.TYPE_GAS, new GasStack(gasType, GAS_AMOUNT));
            ingredients.setOutput(VanillaTypes.FLUID, new FluidStack(fluidType, FLUID_AMOUNT));
        } else {
            ingredients.setInput(VanillaTypes.FLUID, new FluidStack(fluidType, FLUID_AMOUNT));
            ingredients.setOutput(MekanismJEI.TYPE_GAS, new GasStack(gasType, GAS_AMOUNT));
        }
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        minecraft.fontRenderer.drawString(condensentrating ? LangUtils.localize("gui.condensentrating")
              : LangUtils.localize("gui.decondensentrating"), 6 - 3, 74 - 12, 0x404040, false);
    }

    public Gas getGasType() {
        return gasType;
    }

    public Fluid getFluidType() {
        return fluidType;
    }
}