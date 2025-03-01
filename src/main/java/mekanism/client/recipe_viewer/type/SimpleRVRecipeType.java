package mekanism.client.recipe_viewer.type;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import mekanism.common.registration.impl.RecipeTypeRegistryObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.ItemLike;

@NothingNullByDefault
public record SimpleRVRecipeType<VANILLA_INPUT extends RecipeInput, RECIPE extends MekanismRecipe<VANILLA_INPUT>, INPUT_CACHE extends IInputRecipeCache>(
      ResourceLocation id, ResourceLocation icon, IHasTranslationKey name, Class<? extends RECIPE> recipeClass,
      IMekanismRecipeTypeProvider<VANILLA_INPUT, RECIPE, INPUT_CACHE> vanillaProvider,
      int xOffset, int yOffset, int width, int height, List<ItemLike> workstations
) implements IRecipeViewerRecipeType<RECIPE>, IMekanismRecipeTypeProvider<VANILLA_INPUT, RECIPE, INPUT_CACHE> {

    public SimpleRVRecipeType(RecipeTypeRegistryObject<VANILLA_INPUT, RECIPE, INPUT_CACHE> type, Class<? extends RECIPE> recipeClass, IHasTranslationKey name,
          ResourceLocation icon, int xOffset, int yOffset, int width, int height, ItemLike... altWorkstations) {
        this(type.getId(), icon, name, recipeClass, type, xOffset, yOffset, width, height, List.of(altWorkstations));
    }

    @Override
    public Component getTextComponent() {
        return TextComponentUtil.build(name);
    }

    @Override
    public boolean requiresHolder() {
        return true;
    }

    @Override
    public ItemStack iconStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public MekanismRecipeType<VANILLA_INPUT, RECIPE, INPUT_CACHE> getRecipeType() {
        return vanillaProvider.getRecipeType();
    }
}