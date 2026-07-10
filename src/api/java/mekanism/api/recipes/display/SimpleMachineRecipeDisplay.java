package mekanism.api.recipes.display;

import mekanism.api.MekanismAPI;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

//TODO - 26.2: Docs
public record SimpleMachineRecipeDisplay(SlotDisplay input, SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay {

    private static final DeferredHolder<Type<?>, Type<SimpleMachineRecipeDisplay>> TYPE = DeferredHolder.create(Registries.RECIPE_DISPLAY,
          Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "simple_machine"));

    @Override
    public Type<SimpleMachineRecipeDisplay> type() {
        return TYPE.value();
    }
}