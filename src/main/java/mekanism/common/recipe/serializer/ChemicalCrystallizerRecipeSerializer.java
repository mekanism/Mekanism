package mekanism.common.recipe.serializer;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiFunction;
import mekanism.api.SerializationConstants;
import mekanism.api.recipes.basic.BasicChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class ChemicalCrystallizerRecipeSerializer implements RecipeSerializer<BasicChemicalCrystallizerRecipe> {

    private final StreamCodec<RegistryFriendlyByteBuf, BasicChemicalCrystallizerRecipe> streamCodec;
    private final MapCodec<BasicChemicalCrystallizerRecipe> codec;

    public ChemicalCrystallizerRecipeSerializer(BiFunction<ChemicalStackIngredient, ItemStack, BasicChemicalCrystallizerRecipe> factory) {
        this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
              IngredientCreatorAccess.chemicalStack().codec().fieldOf(SerializationConstants.INPUT).forGetter(BasicChemicalCrystallizerRecipe::getInput),
              ItemStack.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(BasicChemicalCrystallizerRecipe::getOutputRaw)
        ).apply(instance, factory));
        this.streamCodec = StreamCodec.composite(
              IngredientCreatorAccess.chemicalStack().streamCodec(), BasicChemicalCrystallizerRecipe::getInput,
              ItemStack.STREAM_CODEC, BasicChemicalCrystallizerRecipe::getOutputRaw,
              factory
        );
    }

    @NotNull
    @Override
    public MapCodec<BasicChemicalCrystallizerRecipe> codec() {
        return codec;
    }

    @NotNull
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, BasicChemicalCrystallizerRecipe> streamCodec() {
        return streamCodec;
    }
}