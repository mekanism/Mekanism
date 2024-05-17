package mekanism.common.recipe.ingredients.creator;

import com.mojang.serialization.Codec;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.creator.IFluidStackIngredientCreator;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

@NothingNullByDefault
public class FluidStackIngredientCreator implements IFluidStackIngredientCreator {

    public static final FluidStackIngredientCreator INSTANCE = new FluidStackIngredientCreator();

    private FluidStackIngredientCreator() {
    }

    @Override
    public Codec<FluidStackIngredient> codec() {
        return FluidStackIngredient.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, FluidStackIngredient> streamCodec() {
        return FluidStackIngredient.STREAM_CODEC;
    }
}