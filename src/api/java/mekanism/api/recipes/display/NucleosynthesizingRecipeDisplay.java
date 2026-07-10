package mekanism.api.recipes.display;

import mekanism.api.MekanismAPI;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

//TODO - 26.2: Docs
public record NucleosynthesizingRecipeDisplay(SlotDisplay input, SlotDisplay secondaryInput, int duration, boolean perTickUsage, SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay {

    private static final DeferredHolder<Type<?>, Type<NucleosynthesizingRecipeDisplay>> TYPE = DeferredHolder.create(Registries.RECIPE_DISPLAY,
          Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "nucleosynthesizing"));

    @Override
    public Type<NucleosynthesizingRecipeDisplay> type() {
        return TYPE.value();
    }
}