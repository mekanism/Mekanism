package mekanism.api.recipes.display;

import java.util.List;
import mekanism.api.MekanismAPI;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

//TODO - 26.2: Docs
public record ReactionRecipeDisplay(SlotDisplay inputSolid, SlotDisplay inputFluid, SlotDisplay inputChemical, SlotDisplay itemOutput, SlotDisplay chemicalOutput, SlotDisplay craftingStation, SlotDisplay combinedResult) implements RecipeDisplay {

    private static final DeferredHolder<Type<?>, Type<ReactionRecipeDisplay>> TYPE = DeferredHolder.create(Registries.RECIPE_DISPLAY,
          Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "reaction"));

    public ReactionRecipeDisplay(SlotDisplay inputSolid, SlotDisplay inputFluid, SlotDisplay inputChemical, SlotDisplay itemOutput, SlotDisplay chemicalOutput, SlotDisplay craftingStation) {
        this(inputSolid, inputFluid, inputChemical, itemOutput, chemicalOutput, craftingStation, new SlotDisplay.Composite(List.of(itemOutput, chemicalOutput)));
    }

    @Override
    public SlotDisplay result() {
        return combinedResult;
    }

    @Override
    public Type<ReactionRecipeDisplay> type() {
        return TYPE.value();
    }
}