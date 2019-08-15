//TODO: Fix this
/*package mekanism.client.jei.machine.other;

import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
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
        ITextComponent component;
        if (condensentrating) {
            component = TextComponentUtil.build(Translation.of("gui.condensentrating"));
        } else {
            component = TextComponentUtil.build(Translation.of("gui.decondensentrating"));
        }
        minecraft.fontRenderer.drawString(component.getFormattedText(), 3, 62, 0x404040);
    }

    public Gas getGasType() {
        return gasType;
    }

    public Fluid getFluidType() {
        return fluidType;
    }
}*/