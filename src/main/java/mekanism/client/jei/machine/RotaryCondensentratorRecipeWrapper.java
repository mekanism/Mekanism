//TODO: Fix this
/*package mekanism.client.jei.machine;

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
import net.minecraftforge.fluids.FluidStack;

public class RotaryCondensentratorRecipeWrapper implements IRecipeWrapper {

    public static final int GAS_AMOUNT = 1;
    public static final int FLUID_AMOUNT = 1;
    @Nonnull
    private Fluid fluidType;
    @Nonnull
    private Gas gasType;
    private boolean condensentrating;

    public RotaryCondensentratorRecipeWrapper(@Nonnull Fluid fluid, @Nonnull Gas gas, boolean b) {
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
            component = TextComponentUtil.translate("gui.mekanism.condensentrating");
        } else {
            component = TextComponentUtil.translate("gui.mekanism.decondensentrating");
        }
        minecraft.fontRenderer.drawString(component.getFormattedText(), 3, 62, 0x404040);
    }

    @Nonnull
    public Gas getGasType() {
        return gasType;
    }

    @Nonnull
    public Fluid getFluidType() {
        return fluidType;
    }
}*/