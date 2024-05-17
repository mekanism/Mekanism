package mekanism.common.recipe.ingredients.creator;

import com.mojang.serialization.Codec;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ingredients.chemical.IGasIngredient;
import mekanism.api.recipes.ingredients.GasStackIngredient;
import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

@NothingNullByDefault
public class GasStackIngredientCreator implements IChemicalStackIngredientCreator<Gas, GasStack, IGasIngredient, GasStackIngredient> {

    public static final GasStackIngredientCreator INSTANCE = new GasStackIngredientCreator();

    private GasStackIngredientCreator() {
    }

    @Override
    public Codec<GasStackIngredient> codec() {
        return GasStackIngredient.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, GasStackIngredient> streamCodec() {
        return GasStackIngredient.STREAM_CODEC;
    }

    @Override
    public IChemicalIngredientCreator<Gas, IGasIngredient> chemicalCreator() {
        return IngredientCreatorAccess.gas();
    }

    @Override
    public GasStackIngredient from(IGasIngredient ingredient, long amount) {
        Objects.requireNonNull(ingredient, "GasStackIngredients cannot be created from a null ingredient.");
        return GasStackIngredient.of(ingredient, amount);
    }
}