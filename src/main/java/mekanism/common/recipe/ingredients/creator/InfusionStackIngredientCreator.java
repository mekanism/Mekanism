package mekanism.common.recipe.ingredients.creator;

import com.mojang.serialization.Codec;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.ingredients.chemical.IInfusionIngredient;
import mekanism.api.recipes.ingredients.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

@NothingNullByDefault
public class InfusionStackIngredientCreator implements IChemicalStackIngredientCreator<InfuseType, InfusionStack, IInfusionIngredient, InfusionStackIngredient> {

    public static final InfusionStackIngredientCreator INSTANCE = new InfusionStackIngredientCreator();

    private InfusionStackIngredientCreator() {
    }

    @Override
    public Codec<InfusionStackIngredient> codec() {
        return InfusionStackIngredient.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, InfusionStackIngredient> streamCodec() {
        return InfusionStackIngredient.STREAM_CODEC;
    }

    @Override
    public IChemicalIngredientCreator<InfuseType, IInfusionIngredient> chemicalCreator() {
        return IngredientCreatorAccess.basicInfusion();
    }

    @Override
    public InfusionStackIngredient from(IInfusionIngredient ingredient, long amount) {
        Objects.requireNonNull(ingredient, "InfusionStackIngredients cannot be created from a null ingredient.");
        return InfusionStackIngredient.of(ingredient, amount);
    }
}