package mekanism.client.jei;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiWindow;
import mekanism.client.jei.interfaces.IJEIGhostTarget;
import mekanism.client.jei.interfaces.IJEIGhostTarget.IGhostIngredientConsumer;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;

public class GhostIngredientHandler implements IGhostIngredientHandler<GuiMekanism> {

    @Override
    public <I> List<Target<I>> getTargets(GuiMekanism genericGui, I ingredient, boolean doStart) {
        //TODO: Improve the handling of this for when gui window "popups" are involved
        // This does not have proper support for windows blocking and making targets inaccessible,
        // we currently don't have any cases where this would be a problem so it low priority but eventually
        // we could write a system to do a "brief" visibility check to try and see if an element is entirely
        // behind a gui window. Additionally, the coloring JEI does also probably happens regardless of the
        // layer so if we end up with a window covering an element that supports jei ghost targeting then it
        // will probably end up rendering improperly. That will either need a PR to JEI, or more likely the
        // better solution will be to only add the "visible" parts as individual sub targets, and to then
        // have the initial looping go through gathering position information "z level" information filtering
        // out any elements that targeting isn't possible with the current ingredient
        GuiMekanism<?> gui = (GuiMekanism<?>) genericGui;
        List<Target<I>> targets = new ArrayList<>();
        addTargets(targets, gui.children(), ingredient);
        for (GuiWindow window : gui.getWindows()) {
            addTargets(targets, window.children(), ingredient);
        }
        return targets;
    }

    private <I> void addTargets(List<Target<I>> targets, List<? extends IGuiEventListener> children, I ingredient) {
        for (IGuiEventListener child : children) {
            if (child instanceof IJEIGhostTarget && child instanceof Widget) {
                IJEIGhostTarget target = (IJEIGhostTarget) child;
                IGhostIngredientConsumer ghostHandler = target.getGhostHandler();
                if (ghostHandler != null && ghostHandler.supportsIngredient(ingredient)) {
                    Widget element = (Widget) child;
                    int borderSize = target.borderSize();
                    Rectangle2d area = new Rectangle2d(element.x + borderSize, element.y + borderSize,
                          element.getWidth() - 2 * borderSize, element.getHeight() - 2 * borderSize);
                    targets.add(new Target<I>() {
                        @Override
                        public Rectangle2d getArea() {
                            return area;
                        }

                        @Override
                        public void accept(I targetIngredient) {
                            ghostHandler.accept(targetIngredient);
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onComplete() {
    }
}