package mekanism.client.recipe_viewer.type;

import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.TextComponentUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public record VanillaRVRecipeType<RECIPE extends Recipe<?>>(
      ResourceLocation id, RecipeType<RECIPE> vanillaType, Class<? extends RECIPE> recipeClass, ItemStack iconStack, List<ItemLike> workstations
) implements IRecipeViewerRecipeType<RECIPE> {

    public VanillaRVRecipeType(RecipeType<RECIPE> vanillaType, Class<? extends RECIPE> recipeClass, Item item, ItemLike... altWorkstations) {
        this(Objects.requireNonNull(BuiltInRegistries.RECIPE_TYPE.getKey(vanillaType)), vanillaType, recipeClass, new ItemStack(item), List.of(altWorkstations));
    }

    @Override
    public Component getTextComponent() {
        //Fake value, doesn't really matter as we don't use it
        return TextComponentUtil.getString(vanillaType.toString());
    }

    @Override
    public boolean requiresHolder() {
        return true;
    }

    @Nullable
    @Override
    public ResourceLocation icon() {
        //Handled by the icon stack
        return null;
    }

    @Override
    public int xOffset() {
        return 0;
    }

    @Override
    public int yOffset() {
        return 0;
    }

    @Override
    public int width() {
        //Fake value, doesn't really matter as we don't use it
        return 100;
    }

    @Override
    public int height() {
        //Fake value, doesn't really matter as we don't use it
        return 100;
    }
}