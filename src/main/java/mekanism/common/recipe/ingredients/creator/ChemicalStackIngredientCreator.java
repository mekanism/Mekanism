package mekanism.common.recipe.ingredients.creator;

import com.mojang.serialization.Codec;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ChemicalStackIngredientCreator implements IChemicalStackIngredientCreator {

    public static final ChemicalStackIngredientCreator INSTANCE = new ChemicalStackIngredientCreator();

    private ChemicalStackIngredientCreator() {
    }

    @Override
    public Codec<ChemicalStackIngredient> codec() {
        return ChemicalStackIngredient.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ChemicalStackIngredient> streamCodec() {
        return ChemicalStackIngredient.STREAM_CODEC;
    }
}