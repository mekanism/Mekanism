package mekanism.api.recipes.display;

import java.util.List;
import mekanism.api.MekanismAPI;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

//TODO - 26.2: Docs
public record SawingRecipeDisplay(SlotDisplay input, SlotDisplay mainOutput, SlotDisplay secondaryOutput, double secondaryChance, SlotDisplay craftingStation, SlotDisplay combinedResult) implements RecipeDisplay {

    private static final DeferredHolder<Type<?>, Type<SawingRecipeDisplay>> TYPE = DeferredHolder.create(Registries.RECIPE_DISPLAY,
          Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "sawing"));

    public SawingRecipeDisplay(SlotDisplay input, SlotDisplay mainOutput, SlotDisplay secondaryOutput, double secondaryChance, SlotDisplay craftingStation) {
        this(input, mainOutput, secondaryOutput, secondaryChance, craftingStation, new SlotDisplay.Composite(List.of(mainOutput, secondaryOutput)));
    }

    @Override
    public SlotDisplay result() {
        return combinedResult;
    }

    @Override
    public Type<SawingRecipeDisplay> type() {
        return TYPE.value();
    }
}