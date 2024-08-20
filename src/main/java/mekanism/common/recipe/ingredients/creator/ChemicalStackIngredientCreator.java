package mekanism.common.recipe.ingredients.creator;

import com.mojang.serialization.Codec;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.chemical.ChemicalIngredient;
import mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

@NothingNullByDefault
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

    @Override
    public ChemicalStackIngredient from(ChemicalIngredient ingredient, long amount) {
        Objects.requireNonNull(ingredient, "ChemicalStackIngredients cannot be created from a null ingredient.");
        return ChemicalStackIngredient.of(ingredient, amount);
    }
}