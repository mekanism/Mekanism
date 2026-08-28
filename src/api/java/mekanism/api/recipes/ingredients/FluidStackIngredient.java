package mekanism.api.recipes.ingredients;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializerHelper;
import mekanism.api.recipes.display.slot.WithAmountSlotDisplay;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.core.TypedInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SimpleFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackContentsFactory;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.Nullable;

/// Implementation for how Mekanism handle's FluidStack Ingredients.
///
/// Create instances of this using [mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#fluid()].
///
/// @implNote This is a wrapper around [SizedFluidIngredient]
public final class FluidStackIngredient implements InputIngredient<Fluid, FluidStack> {

    /// A codec which can (de)encode fluid stack ingredients.
    ///
    /// @since 10.6.0
    public static final Codec<FluidStackIngredient> CODEC = SizedFluidIngredient.CODEC.xmap(FluidStackIngredient::new, FluidStackIngredient::ingredient);
    /// A stream codec which can be used to encode and decode fluid stack ingredients over the network.
    ///
    /// @since 10.6.0
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStackIngredient> STREAM_CODEC = SizedFluidIngredient.STREAM_CODEC
          .map(FluidStackIngredient::new, FluidStackIngredient::ingredient);

    /// Creates a Fluid Stack Ingredient that matches a given ingredient and amount. Prefer calling via
    /// [mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#fluid()] and
    /// [mekanism.api.recipes.ingredients.creator.IFluidStackIngredientCreator#from(SizedFluidIngredient)].
    ///
    /// @param ingredient Sized ingredient to match.
    ///
    /// @throws NullPointerException     if the given instance is null.
    /// @throws IllegalArgumentException if the given instance is empty.
    /// @since 10.6.0
    public static FluidStackIngredient of(SizedFluidIngredient ingredient) {
        Objects.requireNonNull(ingredient, "FluidStackIngredients cannot be created from a null ingredient.");
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
    public boolean testType(TypedInstance<Fluid> instance) {
        return ingredient.ingredient().test(IngredientCreatorAccess.fluid().createStack(instance));
    }

    @Override
    public FluidStack getMatchingInstance(FluidStack stack) {
        return test(stack) ? stack.copyWithAmount(ingredient.amount()) : FluidStack.EMPTY;
    }

    @Override
    public int getNeededAmount(TypedInstance<Fluid> instance) {
        return testType(instance) ? ingredient.amount() : 0;
    }

    @Override
    public boolean hasNoMatchingInstances() {
        //TODO - 26.2: Figure out how to reimplement this, or if the entire concept should go away
        //return ingredient.ingredient().hasNoFluids();
        return false;
    }

    @Override
    public void logMissingTags() {
        //TODO - 26.2: Re-evaluate this implementation
        if (hasNoMatchingInstances()) {
            FluidIngredient fluidIngredient = ingredient.ingredient();
            if (fluidIngredient instanceof SimpleFluidIngredient simple) {
                Optional<TagKey<Fluid>> fluidTagKey = simple.fluidSet().unwrapKey();
                if (fluidTagKey.isPresent()) {
                    MekanismAPI.logger.error("Empty tag: {}", fluidTagKey.get());
                } else {
                    MekanismAPI.logger.error("Empty FluidStackIngredient: {}", SerializerHelper.stringify(FluidIngredient.CODEC, fluidIngredient));
                }
            } else {
                MekanismAPI.logger.error("Empty FluidStackIngredient: {}", SerializerHelper.stringify(FluidIngredient.CODEC, fluidIngredient));
            }
        }
    }

    @Override
    public List<FluidStack> getRepresentations(ContextMap context) {
        if (this.representations == null) {
            this.representations = display().resolve(context, FluidStackContentsFactory.INSTANCE).toList();
        }
        return representations;
    }

    @Override
    public WithAmountSlotDisplay display() {
        return new WithAmountSlotDisplay(ingredient.ingredient().display(), ingredient.amount());
    }

    /// For use in recipe input caching. Gets the internal Neo Sized Fluid Ingredient.
    ///
    /// @since 10.6.0
    @Internal
    public SizedFluidIngredient ingredient() {
        return ingredient;
    }

    @Override
    public boolean equals(@Nullable Object o) {
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