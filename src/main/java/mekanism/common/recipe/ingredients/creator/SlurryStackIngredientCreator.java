package mekanism.common.recipe.ingredients.creator;

import com.mojang.serialization.Codec;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.ingredients.chemical.ISlurryIngredient;
import mekanism.api.recipes.ingredients.SlurryStackIngredient;
import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

@NothingNullByDefault
public class SlurryStackIngredientCreator implements IChemicalStackIngredientCreator<Slurry, SlurryStack, ISlurryIngredient, SlurryStackIngredient> {

    public static final SlurryStackIngredientCreator INSTANCE = new SlurryStackIngredientCreator();

    private SlurryStackIngredientCreator() {
    }

    @Override
    public Codec<SlurryStackIngredient> codec() {
        return SlurryStackIngredient.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, SlurryStackIngredient> streamCodec() {
        return SlurryStackIngredient.STREAM_CODEC;
    }

    @Override
    public IChemicalIngredientCreator<Slurry, ISlurryIngredient> chemicalCreator() {
        return IngredientCreatorAccess.slurry();
    }

    @Override
    public SlurryStackIngredient from(ISlurryIngredient ingredient, long amount) {
        Objects.requireNonNull(ingredient, "SlurryStackIngredients cannot be created from a null ingredient.");
        return SlurryStackIngredient.of(ingredient, amount);
    }
}