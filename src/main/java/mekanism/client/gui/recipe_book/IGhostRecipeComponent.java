package mekanism.client.gui.recipe_book;

import java.util.List;
import net.minecraft.client.gui.screens.recipebook.SlotSelectTime;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public interface IGhostRecipeComponent {

    void setGhostData(SlotDisplay contents, ContextMap context, SlotSelectTime slotSelectTime, boolean isResultSlot);

    void clearGhostData();

    record GhostSlot<STACK>(List<STACK> elements, boolean isResultSlot, SlotSelectTime slotSelectTime) {

        public STACK getCurrent() {
            return get(slotSelectTime.currentIndex());
        }

        public STACK get(int itemIndex) {
            return this.elements.get(itemIndex % this.elements.size());
        }
    }
}