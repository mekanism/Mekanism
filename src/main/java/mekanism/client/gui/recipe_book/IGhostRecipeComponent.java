package mekanism.client.gui.recipe_book;

import java.util.List;
import java.util.stream.Stream;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.chemical.display.ForChemicalStacks;
import net.minecraft.client.gui.screens.recipebook.SlotSelectTime;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.display.ForFluidStacks;

public interface IGhostRecipeComponent {

    void setGhostData(SlotDisplay contents, Iterable<SlotDisplay> recipeContext, ContextMap context, SlotSelectTime slotSelectTime, boolean isResultSlot);

    void clearGhostData();

    record GhostSlot<STACK>(List<STACK> elements, boolean isResultSlot, SlotSelectTime slotSelectTime, int extraData) {

        private static final ForChemicalStacks<Integer> CHEMICAL_SIZE = ChemicalStack::amount;
        private static final ForFluidStacks<Integer> FLUID_SIZE = FluidStack::amount;

        public static <STACK> GhostSlot<STACK> createWithMaxTank(List<STACK> elements, Iterable<SlotDisplay> recipeContext, ContextMap context,
              SlotSelectTime slotSelectTime, boolean isResultSlot) {
            int maxCapacity = 0;
            for (SlotDisplay display : recipeContext) {
                int capacity = Stream.concat(
                      display.resolve(context, CHEMICAL_SIZE),
                      display.resolve(context, FLUID_SIZE)
                ).max(Integer::compareTo).orElse(0);
                if (capacity > maxCapacity) {
                    maxCapacity = capacity;
                }
            }
            return new GhostSlot<>(elements, isResultSlot, slotSelectTime, maxCapacity);
        }

        public STACK getCurrent() {
            return get(slotSelectTime.currentIndex());
        }

        public STACK get(int itemIndex) {
            return this.elements.get(itemIndex % this.elements.size());
        }
    }
}