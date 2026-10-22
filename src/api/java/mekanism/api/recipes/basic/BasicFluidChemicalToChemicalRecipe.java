
package mekanism.api.recipes.basic;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalStackSlotDisplay;
import net.minecraft.core.TypedInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

/// @since 10.8.0
public abstract class BasicFluidChemicalToChemicalRecipe<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>>
      extends FluidChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO> {

    private static final PlacementInfo IS_PLACEABLE = PlacementInfo.createFromOptionals(List.of(Optional.empty(), Optional.empty()));

    private final RecipeType<FluidChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> recipeType;
    protected final FluidStackIngredient fluidInput;
    protected final ChemicalStackIngredient chemicalInput;
    protected final ChemicalStackTemplate output;
    @Nullable
    private PlacementInfo placementInfo;

    /// @param commonInfo    Common recipe info.
    /// @param bookInfo      Recipe book info.
    /// @param fluidInput    Fluid input.
    /// @param chemicalInput Chemical input.
    /// @param output        Output.
    protected BasicFluidChemicalToChemicalRecipe(RecipeType<FluidChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> recipeType, CommonInfo commonInfo, BOOK_INFO bookInfo,
          FluidStackIngredient fluidInput, ChemicalStackIngredient chemicalInput, ChemicalStackTemplate output) {
        this.recipeType = Objects.requireNonNull(recipeType, "Recipe type cannot be null");
        this.fluidInput = Objects.requireNonNull(fluidInput, "Fluid input cannot be null.");
        this.chemicalInput = Objects.requireNonNull(chemicalInput, "Chemical input cannot be null.");
        this.output = Objects.requireNonNull(output, "Output cannot be null.");
        super(commonInfo, bookInfo);
    }

    @Override
    public final RecipeType<FluidChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>> getType() {
        return recipeType;
    }

    @Override
    public FluidStackIngredient getFluidInput() {
        return fluidInput;
    }

    @Override
    public ChemicalStackIngredient getChemicalInput() {
        return chemicalInput;
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = getInputA().hasNoMatchingInstances() || getInputB().hasNoMatchingInstances() ? PlacementInfo.NOT_PLACEABLE : IS_PLACEABLE;
        }
        return this.placementInfo;
    }

    @Override
    public List<ChemicalStackTemplate> getOutputDefinition(ContextMap contextMap) {
        return Collections.singletonList(output);
    }

    @Override
    public SlotDisplay getOutputDisplay() {
        return new ChemicalStackSlotDisplay(output);
    }

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public ChemicalStackTemplate getOutput(TypedInstance<Fluid> fluidStack, TypedInstance<Chemical> chemicalStack) {
        return output;
    }

    public ChemicalStackTemplate getOutputRaw() {
        return output;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicFluidChemicalToChemicalRecipe<?, ?> other = (BasicFluidChemicalToChemicalRecipe<?, ?>) o;
        return fluidInput.equals(other.fluidInput) && chemicalInput.equals(other.chemicalInput) && output.equals(other.output);
    }

    @Override
    public int hashCode() {
        int result = fluidInput.hashCode();
        result = 31 * result + chemicalInput.hashCode();
        result = 31 * result + output.hashCode();
        return result;
    }

    /// Helper to create a [MapCodec] for a simple [BasicFluidChemicalToChemicalRecipe].
    ///
    /// @param factory   Recipe constructor
    /// @param bookCodec Map codec for serializing recipe book data
    ///
    /// @since 10.8.0
    public static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends BookInfo<BOOK_CATEGORY>, RECIPE extends BasicFluidChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>>
    MapCodec<RECIPE> simpleMapCodec(FluidChemicalToChemicalRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory, MapCodec<BOOK_INFO> bookCodec) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
              CommonInfo.MAP_CODEC.forGetter(MekanismRecipe::commonInfo),
              bookCodec.forGetter(MekanismRecipe::bookInfo),
              FluidStackIngredient.CODEC.fieldOf(SerializationConstants.FLUID_INPUT).forGetter(FluidChemicalToChemicalRecipe::getFluidInput),
              ChemicalStackIngredient.CODEC.fieldOf(SerializationConstants.CHEMICAL_INPUT).forGetter(FluidChemicalToChemicalRecipe::getChemicalInput),
              ChemicalStackTemplate.MAP_CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(recipe -> recipe.output)
        ).apply(instance, factory::create));
    }

    /// Helper to create a [StreamCodec] for a simple [BasicFluidChemicalToChemicalRecipe].
    ///
    /// @param factory   Recipe constructor
    /// @param bookCodec Stream codec for serializing recipe book data
    ///
    /// @since 10.8.0
    public static <BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends BookInfo<BOOK_CATEGORY>, RECIPE extends BasicFluidChemicalToChemicalRecipe<BOOK_CATEGORY, BOOK_INFO>>
    StreamCodec<RegistryFriendlyByteBuf, RECIPE> simpleStreamCodec(FluidChemicalToChemicalRecipe.Factory<BOOK_CATEGORY, BOOK_INFO, RECIPE> factory,
          StreamCodec<RegistryFriendlyByteBuf, BOOK_INFO> bookCodec) {
        return StreamCodec.composite(
              CommonInfo.STREAM_CODEC, MekanismRecipe::commonInfo,
              bookCodec, MekanismRecipe::bookInfo,
              FluidStackIngredient.STREAM_CODEC, FluidChemicalToChemicalRecipe::getFluidInput,
              ChemicalStackIngredient.STREAM_CODEC, FluidChemicalToChemicalRecipe::getChemicalInput,
              ChemicalStackTemplate.STREAM_CODEC, recipe -> recipe.output,
              factory::create
        );
    }
}