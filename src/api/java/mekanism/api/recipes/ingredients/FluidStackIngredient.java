package mekanism.api.recipes.ingredients;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Objects;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.TagFluidIngredient;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation for how Mekanism handle's FluidStack Ingredients.
 * <p>
 * Create instances of this using {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#fluid()}.
 *
 * @implNote This is a wrapper around {@link SizedFluidIngredient}
 */
@NothingNullByDefault
public final class FluidStackIngredient implements InputIngredient<@NotNull FluidStack> {

    /**
     * A codec which can (de)encode fluid stack ingredients.
     *
     * @since 10.6.0
     */
    public static final Codec<FluidStackIngredient> CODEC = SizedFluidIngredient.FLAT_CODEC.xmap(FluidStackIngredient::new, FluidStackIngredient::ingredient);
    /**
     * A stream codec which can be used to encode and decode fluid stack ingredients over the network.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStackIngredient> STREAM_CODEC = SizedFluidIngredient.STREAM_CODEC
          .map(FluidStackIngredient::new, FluidStackIngredient::ingredient);

    /**
     * Creates a Fluid Stack Ingredient that matches a given ingredient and amount. Prefer calling via
     * {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#fluid()} and
     * {@link mekanism.api.recipes.ingredients.creator.IFluidStackIngredientCreator#from(SizedFluidIngredient)}.
     *
     * @param ingredient Sized ingredient to match.
     *
     * @throws NullPointerException     if the given instance is null.
     * @throws IllegalArgumentException if the given instance is empty.
     * @since 10.6.0
     */
    public static FluidStackIngredient of(SizedFluidIngredient ingredient) {
        Objects.requireNonNull(ingredient, "FluidStackIngredients cannot be created from a null ingredient.");
        if (ingredient.ingredient().isEmpty()) {
            throw new IllegalArgumentException("FluidStackIngredients cannot be created using the empty ingredient.");
        }
        return new FluidStackIngredient(ingredient);
    }

    private final SizedFluidIngredient ingredient;
    @Nullable
    private List<FluidStack> representations;

    private FluidStackIngredient(SizedFluidIngredient ingredient) {
        this.ingredient = ingredient;
    }

    @Override
    public boolean test(FluidStack stack) {
        Objects.requireNonNull(stack);
        return ingredient.test(stack);
    }

    @Override
    public boolean testType(FluidStack stack) {
        Objects.requireNonNull(stack);
        return ingredient.ingredient().test(stack);
    }

    @Override
    public FluidStack getMatchingInstance(FluidStack stack) {
        return test(stack) ? stack.copyWithAmount(ingredient.amount()) : FluidStack.EMPTY;
    }

    @Override
    public long getNeededAmount(FluidStack stack) {
        return testType(stack) ? ingredient.amount() : 0;
    }

    @Override
    public boolean hasNoMatchingInstances() {
        return ingredient.ingredient().hasNoFluids();
    }

    @Override
    public void logMissingTags() {
        if (hasNoMatchingInstances()) {
            FluidIngredient fluidIngredient = ingredient.ingredient();
            if (fluidIngredient instanceof TagFluidIngredient tagged) {
                MekanismAPI.logger.error("Empty tag: {}", tagged.tag());
            } else {
                MekanismAPI.logger.error("Empty FluidStackIngredient: {}", SerializerHelper.stringify(FluidIngredient.CODEC, fluidIngredient));
            }
        }
    }

    @Override
    public List<@NotNull FluidStack> getRepresentations() {
        if (this.representations == null) {
            this.representations = List.of(ingredient.getFluids());
        }
        return representations;
    }

    /**
     * For use in recipe input caching. Gets the internal Neo Sized Fluid Ingredient.
     *
     * @since 10.6.0
     */
    @Internal
    public SizedFluidIngredient ingredient() {
        return ingredient;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return ingredient.equals(((FluidStackIngredient) o).ingredient);
    }

    @Override
    public int hashCode() {
        return ingredient.hashCode();
    }

    @Override
    public String toString() {
        return ingredient.toString();
    }
}