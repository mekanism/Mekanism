package mekanism.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class ItemStackToEnergyRecipeSerializer<RECIPE extends ItemStackToEnergyRecipe> implements RecipeSerializer<RECIPE> {

    private final IFactory<RECIPE> factory;
    private Codec<RECIPE> codec;

    public ItemStackToEnergyRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    @NotNull
    @Override
    public Codec<RECIPE> codec() {
        if (codec == null) {
            codec = RecordCodecBuilder.create(instance -> instance.group(
                  IngredientCreatorAccess.item().codec().fieldOf(JsonConstants.INPUT).forGetter(ItemStackToEnergyRecipe::getInput),
                  FloatingLong.NONZERO_CODEC.fieldOf(JsonConstants.OUTPUT).forGetter(r -> r.getOutput(ItemStack.EMPTY))
            ).apply(instance, factory::create));
        }
        return codec;
    }

    @NotNull
    @Override
    public RECIPE fromNetwork(@NotNull FriendlyByteBuf buffer) {
        ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().read(buffer);
        FloatingLong output = FloatingLong.readFromBuffer(buffer);
        return this.factory.create(inputIngredient, output);
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
        recipe.getInput().write(buffer);
        recipe.getOutput(ItemStack.EMPTY).writeToBuffer(buffer);
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends ItemStackToEnergyRecipe> {

        RECIPE create(ItemStackIngredient input, FloatingLong output);
    }
}