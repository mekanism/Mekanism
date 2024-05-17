package mekanism.common.recipe.ingredients.creator;

import com.mojang.serialization.Codec;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ingredients.chemical.IPigmentIngredient;
import mekanism.api.recipes.ingredients.PigmentStackIngredient;
import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

@NothingNullByDefault
public class PigmentStackIngredientCreator implements IChemicalStackIngredientCreator<Pigment, PigmentStack, IPigmentIngredient, PigmentStackIngredient> {

    public static final PigmentStackIngredientCreator INSTANCE = new PigmentStackIngredientCreator();

    private PigmentStackIngredientCreator() {
    }

    @Override
    public Codec<PigmentStackIngredient> codec() {
        return PigmentStackIngredient.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, PigmentStackIngredient> streamCodec() {
        return PigmentStackIngredient.STREAM_CODEC;
    }

    @Override
    public IChemicalIngredientCreator<Pigment, IPigmentIngredient> chemicalCreator() {
        return IngredientCreatorAccess.basicPigment();
    }

    @Override
    public PigmentStackIngredient from(IPigmentIngredient ingredient, long amount) {
        Objects.requireNonNull(ingredient, "PigmentStackIngredients cannot be created from a null ingredient.");
        return PigmentStackIngredient.of(ingredient, amount);
    }
}