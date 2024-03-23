package mekanism.client.recipe_viewer.type;

import java.util.List;
import java.util.stream.Stream;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.providers.IItemProvider;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public record RVRecipeTypeWrapper<RECIPE extends MekanismRecipe, INPUT_CACHE extends IInputRecipeCache>(
      ResourceLocation id, IItemProvider item, Class<? extends RECIPE> recipeClass, IMekanismRecipeTypeProvider<RECIPE, INPUT_CACHE> vanillaProvider,
      int xOffset, int yOffset, int width, int height, List<IItemProvider> workstations
) implements IRecipeViewerRecipeType<RECIPE>, IMekanismRecipeTypeProvider<RECIPE, INPUT_CACHE> {

    public RVRecipeTypeWrapper(IMekanismRecipeTypeProvider<RECIPE, INPUT_CACHE> vanillaProvider, Class<? extends RECIPE> recipeClass, int xOffset, int yOffset,
          int width, int height, IItemProvider icon, IItemProvider... altWorkstations) {
        this(vanillaProvider.getRegistryName(), icon, recipeClass, vanillaProvider, xOffset, yOffset, width, height, List.of(altWorkstations));
    }

    public RVRecipeTypeWrapper {
        if (workstations.isEmpty()) {
            workstations = List.of(item);
        } else {
            workstations = Stream.concat(Stream.of(item), workstations.stream()).toList();
        }
    }

    @Override
    public Component getTextComponent() {
        return item.getTextComponent();
    }

    @Override
    public boolean requiresHolder() {
        return true;
    }

    @Override
    public ItemStack iconStack() {
        return item.getItemStack();
    }

    @Nullable
    @Override
    public ResourceLocation icon() {
        //Handled by the icon stack
        return null;
    }

    @Override
    public MekanismRecipeType<RECIPE, INPUT_CACHE> getRecipeType() {
        return vanillaProvider.getRecipeType();
    }
}