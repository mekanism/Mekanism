package mekanism.client.recipe_viewer.emi;

import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import java.util.Collections;
import java.util.List;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.GuiUtils;
import mekanism.client.recipe_viewer.GhostIngredientHandler;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerGhostTarget.IGhostIngredientConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

public class EmiGhostIngredientHandler implements EmiDragDropHandler<Screen> {

    @Override
    public boolean dropStack(Screen screen, EmiIngredient ingredient, int x, int y) {
        for (EmiTarget target : getTargets(screen, ingredient)) {
            if (target.handle(x, y)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(Screen screen, EmiIngredient dragged, GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        for (EmiTarget target : getTargets(screen, dragged)) {
            GuiUtils.fill(graphics, target.area.getX(), target.area.getY(), target.area.getWidth(), target.area.getHeight(), 0x8822BB33);
        }
    }

    private List<EmiTarget> getTargets(Screen screen, EmiIngredient stack) {
        if (screen instanceof GuiMekanism<?> gui) {
            return GhostIngredientHandler.getTargetsTyped(gui, stack, EmiGhostIngredientHandler::getFirstSupportedStack, EmiTarget::new);
        }
        return Collections.emptyList();
    }

    @Nullable
    private static Object getFirstSupportedStack(IGhostIngredientConsumer handler, EmiIngredient ingredient) {
        for (EmiStack emiStack : ingredient.getEmiStacks()) {
            Object raw = null;
            if (emiStack.getKey() instanceof Item) {
                raw = emiStack.getItemStack();
            } else if (emiStack.getKey() instanceof Fluid fluid) {
                raw = new FluidStack(fluid.builtInRegistryHolder(), FluidType.BUCKET_VOLUME, emiStack.getComponentChanges());
            } else if (emiStack instanceof ChemicalEmiStack chemicalEmiStack) {
                raw = chemicalEmiStack.getStack();
            }
            if (raw != null) {
                Object stack = handler.supportedTarget(raw);
                if (stack != null) {
                    return stack;
                }
            }
        }
        return null;
    }

    private record EmiTarget(IGhostIngredientConsumer handler, Object ingredient, Rect2i area) {

        public boolean handle(int x, int y) {
            if (area.contains(x, y)) {
                handler.accept(ingredient);
                return true;
            }
            return false;
        }
    }
}