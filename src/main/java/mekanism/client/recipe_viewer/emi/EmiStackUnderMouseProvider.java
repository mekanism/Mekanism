package mekanism.client.recipe_viewer.emi;

import dev.emi.emi.api.EmiStackProvider;
import dev.emi.emi.api.neoforge.NeoForgeEmiStack;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import mekanism.api.chemical.ChemicalStack;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.recipe_viewer.GuiElementHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public class EmiStackUnderMouseProvider implements EmiStackProvider<Screen> {

    @Override
    public EmiStackInteraction getStackAt(Screen screen, int x, int y) {
        if (screen instanceof GuiMekanism<?> gui) {
            return GuiElementHandler.getClickableIngredientUnderMouse(gui, x, y, (helper, ingredient) -> {
                EmiStack emiStack;
                switch (ingredient) {
                    case ItemStack stack -> emiStack = EmiStack.of(stack);
                    case FluidStack stack -> emiStack = NeoForgeEmiStack.of(stack);
                    case ChemicalStack stack -> emiStack = new ChemicalEmiStack(stack);
                    default -> {
                        return null;
                    }
                }
                return new EmiStackInteraction(emiStack, null, false);
            }).orElse(EmiStackInteraction.EMPTY);
        }
        return EmiStackInteraction.EMPTY;
    }
}