package mekanism.api.recipes.display;

import java.util.List;
import mekanism.api.MekanismAPI;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

//TODO - 26.2: Docs
public record MultiOutputRecipeDisplay(SlotDisplay input, SlotDisplay output, SlotDisplay secondaryOutput, SlotDisplay craftingStation, SlotDisplay combinedResult) implements RecipeDisplay {

    private static final DeferredHolder<Type<?>, Type<MultiOutputRecipeDisplay>> TYPE = DeferredHolder.create(Registries.RECIPE_DISPLAY,
          Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "multi_output"));

    public MultiOutputRecipeDisplay(SlotDisplay input, SlotDisplay output, SlotDisplay secondaryOutput, SlotDisplay craftingStation) {
        this(input, output, secondaryOutput, craftingStation, new SlotDisplay.Composite(List.of(output, secondaryOutput)));
    }

    @Override
    public SlotDisplay result() {
        return combinedResult;
    }

    @Override
    public Type<MultiOutputRecipeDisplay> type() {
        return TYPE.value();
    }
}