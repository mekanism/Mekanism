package mekanism.client.recipe_viewer.jei;

import java.util.List;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.recipe_viewer.GhostIngredientHandler;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerGhostTarget.IGhostIngredientConsumer;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;

public class JeiGhostIngredientHandler<GUI extends GuiMekanism<?>> implements IGhostIngredientHandler<GUI> {

    @Override
    public <INGREDIENT> List<Target<INGREDIENT>> getTargetsTyped(GUI gui, ITypedIngredient<INGREDIENT> ingredient, boolean doStart) {
        return GhostIngredientHandler.getTargetsTyped(gui, ingredient, (handler, typed) -> handler.supportedTarget(typed.getIngredient()), JeiTarget::new);
    }

    @Override
    public void onComplete() {
    }

    private record JeiTarget<INGREDIENT>(IGhostIngredientConsumer handler, Object ingredient, Rect2i getArea) implements Target<INGREDIENT> {

        @Override
        public void accept(INGREDIENT ignored) {
            //Ignore the passed in ingredient and use the one we cached as valid for our target
            handler.accept(ingredient);
        }
    }
}