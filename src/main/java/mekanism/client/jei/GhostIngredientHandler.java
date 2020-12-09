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
import java.util.stream.Collectors;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.jei.interfaces.IJEIGhostTarget;
import mekanism.client.jei.interfaces.IJEIGhostTarget.IGhostIngredientConsumer;
import mekanism.common.lib.LRU;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;

public class GhostIngredientHandler<GUI extends GuiMekanism<?>> implements IGhostIngredientHandler<GUI> {

    @Override
    public <INGREDIENT> List<Target<INGREDIENT>> getTargets(GUI gui, INGREDIENT ingredient, boolean doStart) {
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
                areas.add(new Rectangle2d(window.x, window.y, window.getWidth(), window.getHeightRealms()));
                areas.addAll(GuiElementHandler.getAreasFor(window.x, window.y, window.getWidth(), window.getHeightRealms(), window.children()));
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
                    ghostTargets.add(new TargetInfo<>(ghostTarget, ghostHandler, element.x, element.y, element.getWidth(), element.getHeightRealms()));
                }
            }
        }
        return ghostTargets;
    }

    @Override
    public void onComplete() {
    }

    private static void addVisibleAreas(List<Rectangle2d> visible, Rectangle2d area, List<Rectangle2d> coveredArea) {
        boolean intersected = false;
        int x = area.getX();
        int x2 = x + area.getWidth();
        int y = area.getY();
        int y2 = y + area.getHeight();
        int size = coveredArea.size();
        for (int i = 0; i < size; i++) {
            Rectangle2d covered = coveredArea.get(i);
            int cx = covered.getX();
            int cx2 = cx + covered.getWidth();
            int cy = covered.getY();
            int cy2 = cy + covered.getHeight();
            //Check if the covered area intersects the area we are checking against
            if (x < cx2 && x2 > cx && y < cy2 && y2 > cy) {
                intersected = true;
                if (x < cx || y < cy || x2 > cx2 || y2 > cy2) {
                    //If the area is not fully covered then get the parts of it that are not covered
                    List<Rectangle2d> uncoveredArea = getVisibleArea(area, covered);
                    if (i + 1 == size) {
                        //If there are no more elements left, just add all the remaining visible parts
                        visible.addAll(uncoveredArea);
                    } else {
                        //Otherwise grab the remaining unchecked elements from the covering layer
                        List<Rectangle2d> coveredAreas = coveredArea.subList(i + 1, size);
                        //And check each of our sub visible areas
                        for (Rectangle2d visibleArea : uncoveredArea) {
                            addVisibleAreas(visible, visibleArea, coveredAreas);
                        }
                    }
                }
                //If it is covered at all exit, we either added the uncovered parts or it is fully covered
                break;
            }
        }
        if (!intersected) {
            //If we didn't intersect it at all, just add the area itself
            visible.add(area);
        }
    }

    private static List<Rectangle2d> getVisibleArea(Rectangle2d area, Rectangle2d coveredArea) {
        //Useful tool for visualizing overlaps: https://silentmatt.com/rectangle-intersection/
        //TODO: Look into further cleaning this up so that it is less "hardcoded" manner for adding the various components
        // started out as more hardcoded to actually figure out the different pieces
        int x = area.getX();
        int x2 = x + area.getWidth();
        int y = area.getY();
        int y2 = y + area.getHeight();
        int cx = coveredArea.getX();
        int cx2 = cx + coveredArea.getWidth();
        int cy = coveredArea.getY();
        int cy2 = cy + coveredArea.getHeight();
        //Given we know it intersects we can use a simplified check for seeing which sides get intersected
        boolean intersectsTop = y >= cy && y <= cy2;
        boolean intersectsLeft = x >= cx && x <= cx2;
        boolean intersectsBottom = y2 >= cy && y2 <= cy2;
        boolean intersectsRight = x2 >= cx && x2 <= cx2;
        List<Rectangle2d> areas = new ArrayList<>();
        if (intersectsTop && intersectsBottom) {
            //Intersects three sides (even if the perpendicular one may only have the top and bottom point intersected), we have one rectangle
            if (intersectsLeft) {
                //Right section
                areas.add(new Rectangle2d(cx2, y, x2 - cx2, area.getHeight()));
            } else if (intersectsRight) {
                //Left section
                areas.add(new Rectangle2d(x, y, cx - x, area.getHeight()));
            } else {
                //Intersects two parallel sides, we have two rectangles
                //Left section
                areas.add(new Rectangle2d(x, y, cx - x, area.getHeight()));
                //Right section
                areas.add(new Rectangle2d(cx2, y, x2 - cx2, area.getHeight()));
            }
        } else if (intersectsLeft && intersectsRight) {
            //Intersects three sides (even if the perpendicular one may only have the top and bottom point intersected), we have one rectangle
            if (intersectsTop) {
                //Bottom section
                areas.add(new Rectangle2d(x, cy2, area.getWidth(), y2 - cy2));
            } else if (intersectsBottom) {
                //Top section
                areas.add(new Rectangle2d(x, y, area.getWidth(), cy - y));
            } else {
                //Intersects two parallel sides, we have two rectangles
                //Top section
                areas.add(new Rectangle2d(x, y, area.getWidth(), cy - y));
                //Bottom section
                areas.add(new Rectangle2d(x, cy2, area.getWidth(), y2 - cy2));
            }
        }
        //Intersects two perpendicular sides, we have two rectangles
        else if (intersectsTop && intersectsLeft) {
            //Bottom section
            areas.add(new Rectangle2d(x, cy2, area.getWidth(), y2 - cy2));
            //Right section
            areas.add(new Rectangle2d(cx2, y, x2 - cx2, cy2 - y));
        } else if (intersectsTop && intersectsRight) {
            //Left section
            areas.add(new Rectangle2d(x, y, cx - x, cy2 - y));
            //Bottom section
            areas.add(new Rectangle2d(x, cy2, area.getWidth(), y2 - cy2));
        } else if (intersectsBottom && intersectsLeft) {
            //Top section
            areas.add(new Rectangle2d(x, y, area.getWidth(), cy - y));
            //Right section
            areas.add(new Rectangle2d(cx2, cy, x2 - cx2, y2 - cy));
        } else if (intersectsBottom && intersectsRight) {
            //Top section
            areas.add(new Rectangle2d(x, y, area.getWidth(), cy - y));
            //Left section
            areas.add(new Rectangle2d(x, cy, cx - x, y2 - cy));
        }
        //Intersects a single side, we have three rectangles
        else if (intersectsTop) {
            //Left section
            areas.add(new Rectangle2d(x, y, cx - x, cy2 - y));
            //Bottom section
            areas.add(new Rectangle2d(x, cy2, area.getWidth(), y2 - cy2));
            //Right section
            areas.add(new Rectangle2d(cx2, y, x2 - cx2, cy2 - y));
        } else if (intersectsLeft) {
            //Top section
            areas.add(new Rectangle2d(x, y, area.getWidth(), cy - y));
            //Bottom section
            areas.add(new Rectangle2d(x, cy2, area.getWidth(), y2 - cy2));
            //Right section
            areas.add(new Rectangle2d(cx2, cy, x2 - cx2, coveredArea.getHeight()));
        } else if (intersectsBottom) {
            //Top section
            areas.add(new Rectangle2d(x, y, area.getWidth(), cy - y));
            //Left section
            areas.add(new Rectangle2d(x, cy, cx - x, y2 - cy));
            //Right section
            areas.add(new Rectangle2d(cx2, cy, x2 - cx2, y2 - cy));
        } else if (intersectsRight) {
            //Top section
            areas.add(new Rectangle2d(x, y, area.getWidth(), cy - y));
            //Left section
            areas.add(new Rectangle2d(x, cy, cx - x, coveredArea.getHeight()));
            //Bottom section
            areas.add(new Rectangle2d(x, cy2, area.getWidth(), y2 - cy2));
        } else {
            //The covered area is entirely contained by the main area, we have four rectangles
            //Top section
            areas.add(new Rectangle2d(x, y, area.getWidth(), cy - y));
            //Left section
            areas.add(new Rectangle2d(x, cy, cx - x, coveredArea.getHeight()));
            //Bottom section
            areas.add(new Rectangle2d(x, cy2, area.getWidth(), y2 - cy2));
            //Right section
            areas.add(new Rectangle2d(cx2, cy, x2 - cx2, coveredArea.getHeight()));
        }
        return areas;
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
            List<Rectangle2d> visibleAreas = new ArrayList<>();
            addVisibleAreas(visibleAreas, new Rectangle2d(x, y, width, height), coveredArea);
            return visibleAreas.stream().map(visibleArea -> new Target<INGREDIENT>() {
                @Override
                public Rectangle2d getArea() {
                    return visibleArea;
                }

                @Override
                public void accept(INGREDIENT ingredient) {
                    ghostHandler.accept(ingredient);
                }
            }).collect(Collectors.toList());
        }
    }
}