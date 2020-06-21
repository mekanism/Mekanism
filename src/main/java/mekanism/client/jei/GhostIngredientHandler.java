package mekanism.client.jei;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap.FastSortedEntrySet;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiWindow;
import mekanism.client.jei.interfaces.IJEIGhostTarget;
import mekanism.client.jei.interfaces.IJEIGhostTarget.IGhostIngredientConsumer;
import mekanism.common.lib.LRU;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;

public class GhostIngredientHandler implements IGhostIngredientHandler<GuiMekanism> {

    @Override
    public <INGREDIENT> List<Target<INGREDIENT>> getTargets(GuiMekanism genericGui, INGREDIENT ingredient, boolean doStart) {
        GuiMekanism<?> gui = (GuiMekanism<?>) genericGui;
        boolean hasTargets = false;
        int depth = 0;
        Int2ObjectLinkedOpenHashMap<List<TargetInfo<INGREDIENT>>> depthBasedTargets = new Int2ObjectLinkedOpenHashMap<>();
        Int2ObjectMap<List<Rectangle2d>> layerIntersections = new Int2ObjectOpenHashMap<>();
        List<TargetInfo<INGREDIENT>> ghostTargets = getTargets(gui.children(), ingredient);
        if (!ghostTargets.isEmpty()) {
            //If we found any targets increment the layer count and add them to our depth based target list
            depthBasedTargets.put(depth, ghostTargets);
            hasTargets = true;
        }
        //Now gather the targets for the windows in reverse-order (i.e. back to front)
        for (LRU<GuiWindow>.LRUIterator iter = gui.getWindowsDescendingIterator(); iter.hasNext(); ) {
            GuiWindow window = iter.next();
            depth++;
            if (hasTargets) {
                //If we have at least one layer with targets grab the intersection information for this window's layer
                List<Rectangle2d> areas = new ArrayList<>();
                areas.add(new Rectangle2d(window.x, window.y, window.getWidth(), window.getHeight()));
                areas.addAll(GuiElementHandler.getAreasFor(window.x, window.y, window.getWidth(), window.getHeight(), window.children()));
                layerIntersections.put(depth, areas);
            }
            ghostTargets = getTargets(window.children(), ingredient);
            if (!ghostTargets.isEmpty()) {
                //If we found any targets increment the layer count and add them to our depth based target list
                depthBasedTargets.put(depth, ghostTargets);
                hasTargets = true;
            }
        }
        if (!hasTargets) {
            //If we don't have any layers with elements in them just return
            return Collections.emptyList();
        }
        List<Target<INGREDIENT>> targets = new ArrayList<>();
        List<Rectangle2d> coveredArea = new ArrayList<>();
        //Note: we iterate the target info in reverse so that we are able to more easily build up a list of the area that is covered
        // in front of the level of targets we are currently adding to
        FastSortedEntrySet<List<TargetInfo<INGREDIENT>>> depthEntries = depthBasedTargets.int2ObjectEntrySet();
        for (ObjectBidirectionalIterator<Entry<List<TargetInfo<INGREDIENT>>>> iter = depthEntries.fastIterator(depthEntries.last()); iter.hasPrevious(); ) {
            Entry<List<TargetInfo<INGREDIENT>>> entry = iter.previous();
            int targetDepth = entry.getIntKey();
            for (; depth > targetDepth; depth--) {
                //If we are at a lower depth than the max depth we have things for add all the ones of higher depth
                coveredArea.addAll(layerIntersections.get(depth));
            }
            for (TargetInfo<INGREDIENT> ghostTarget : entry.getValue()) {
                targets.addAll(ghostTarget.convertToTargets(coveredArea));
            }
        }
        return targets;
    }

    private <INGREDIENT> List<TargetInfo<INGREDIENT>> getTargets(List<? extends IGuiEventListener> children, INGREDIENT ingredient) {
        List<TargetInfo<INGREDIENT>> ghostTargets = new ArrayList<>();
        for (IGuiEventListener child : children) {
            if (child instanceof IJEIGhostTarget && child instanceof Widget) {
                IJEIGhostTarget ghostTarget = (IJEIGhostTarget) child;
                IGhostIngredientConsumer ghostHandler = ghostTarget.getGhostHandler();
                if (ghostHandler != null && ghostHandler.supportsIngredient(ingredient)) {
                    Widget element = (Widget) child;
                    ghostTargets.add(new TargetInfo<>(ghostTarget, ghostHandler, element.x, element.y, element.getWidth(), element.getHeight()));
                }
            }
        }
        return ghostTargets;
    }

    @Override
    public void onComplete() {
    }

    private static class TargetInfo<INGREDIENT> {

        private final IGhostIngredientConsumer ghostHandler;
        private final int x, y, width, height;

        public TargetInfo(IJEIGhostTarget ghostTarget, IGhostIngredientConsumer ghostHandler, int x, int y, int width, int height) {
            this.ghostHandler = ghostHandler;
            int borderSize = ghostTarget.borderSize();
            this.x = x + borderSize;
            this.y = y + borderSize;
            this.width = width - 2 * borderSize;
            this.height = height - 2 * borderSize;
        }

        public List<Target<INGREDIENT>> convertToTargets(List<Rectangle2d> coveredArea) {
            //TODO: Further improve the filtering that is performed. Currently we only are removing elements that
            // are fully blocked by a singular element. Ideally we want to remove any that are blocked by multiple
            // individual components, and also only show the portions that are visible, as JEI will color the entire
            // target's area even if parts of it are not visible.
            // Maybe we can do this by keeping track of Rectangle2ds that are for the target areas, and then compare those
            // to the covered area
            boolean isCovered = false;
            for (Rectangle2d area : coveredArea) {
                if (x >= area.getX() && y >= area.getY() && x + width <= area.getX() + area.getWidth() && y + height <= area.getY() + area.getHeight()) {
                    //If one of the area components fully covers the area exit
                    isCovered = true;
                    break;
                }
            }
            if (isCovered) {
                return Collections.emptyList();
            }
            Rectangle2d area = new Rectangle2d(x, y, width, height);
            return Collections.singletonList(new Target<INGREDIENT>() {
                @Override
                public Rectangle2d getArea() {
                    return area;
                }

                @Override
                public void accept(INGREDIENT ingredient) {
                    ghostHandler.accept(ingredient);
                }
            });
        }
    }
}