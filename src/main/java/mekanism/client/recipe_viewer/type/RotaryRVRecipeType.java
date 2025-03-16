package mekanism.client.recipe_viewer.type;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.vanilla_input.RotaryRecipeInput;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.RotaryInputRecipeCache;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public record RotaryRVRecipeType(ResourceLocation id, IHasTranslationKey name, List<ItemLike> workstations) implements IRecipeViewerRecipeType<RotaryRecipe>,
      IMekanismRecipeTypeProvider<RotaryRecipeInput, RotaryRecipe, RotaryInputRecipeCache> {

    public RotaryRVRecipeType(ResourceLocation id, IHasTranslationKey name) {
        this(id, name, List.of(MekanismBlocks.ROTARY_CONDENSENTRATOR));
    }

    @Override
    public Component getTextComponent() {
        return TextComponentUtil.build(name);
    }

    @Override
    public Class<? extends RotaryRecipe> recipeClass() {
        return RotaryRecipe.class;
    }

    @Override
    public boolean requiresHolder() {
        return true;
    }

    @Override
    public ItemStack iconStack() {
        return new ItemStack(MekanismBlocks.ROTARY_CONDENSENTRATOR);
    }

    @Nullable
    @Override
    public ResourceLocation icon() {
        //Handled by the icon stack
        return null;
    }

    @Override
    public int xOffset() {
        return -3;
    }

    @Override
    public int yOffset() {
        return -12;
    }

    @Override
    public int width() {
        return 170;
    }

    @Override
    public int height() {
        return 64;
    }

    @Override
    public MekanismRecipeType<RotaryRecipeInput, RotaryRecipe, RotaryInputRecipeCache> getRecipeType() {
        return MekanismRecipeType.ROTARY.getRecipeType();
    }
}