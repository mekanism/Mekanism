package mekanism.api.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.book.ReactingBookCategory;
import mekanism.api.recipes.book.ReactingBookCategory.ReactingBookInfo;
import mekanism.api.recipes.display.ReactionRecipeDisplay;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalStackSlotDisplay;
import mekanism.api.recipes.vanilla_input.ReactionRecipeInput;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.TriPredicate;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

/// Input: ItemStack
///
/// Input: FluidStack
///
/// Input: ChemicalStack
///
/// Item Output: ItemStack (can be empty if chemical output is not empty)
///
/// Chemical Output: ChemicalStack (can be empty if item output is not empty)
///
/// @apiNote Pressurized Reaction Chambers can process this recipe type.
public abstract class PressurizedReactionRecipe extends MekanismRecipe<ReactionRecipeInput, ReactingBookCategory, ReactingBookInfo> implements TriPredicate<ItemStack, FluidStack, ChemicalStack> {

    private static final Holder<Item> PRESSURIZED_REACTION_CHAMBER = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "pressurized_reaction_chamber"));

    /// @param commonInfo Common recipe info.
    /// @param bookInfo   Recipe book info.
    ///
    /// @since 10.8.0
    protected PressurizedReactionRecipe(Recipe.CommonInfo commonInfo, ReactingBookInfo bookInfo) {
        super(commonInfo, bookInfo);
    }

    /// Gets the item input ingredient.
    public abstract ItemStackIngredient getInputSolid();

    /// Gets the fluid input ingredient.
    public abstract FluidStackIngredient getInputFluid();

    /// Gets the chemical input ingredient.
    public abstract ChemicalStackIngredient getInputChemical();

    /// Gets the amount of "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the recipe.
    @Range(from = 0, to = Integer.MAX_VALUE)
    public abstract int getEnergyRequired();

    /// Gets the base duration in ticks that this recipe takes to complete.
    public abstract int getDuration();

    @Override
    public abstract boolean test(ItemStack solid, FluidStack liquid, ChemicalStack chemical);

    @Override
    public boolean matches(ReactionRecipeInput input, Level level) {
        //Don't match incomplete recipes or ones that don't match
        return !isIncomplete() && test(input.item(), input.fluid(), input.chemical());
    }

    /// For JEI, gets the output representations to display.
    ///
    /// @return Representation of the output, **MUST NOT** be modified.
    public abstract List<PressurizedReactionRecipeOutput> getOutputDefinition(ContextMap contextMap);

    /// {@return a slot display for the output of the recipe}
    ///
    /// @since 10.8.0
    public abstract SlotDisplay getOutputDisplay();

    /// Gets a new output based on the given inputs.
    ///
    /// @param solid    Specific item input.
    /// @param liquid   Specific fluid input.
    /// @param chemical Specific chemical input.
    ///
    /// @return New output.
    ///
    /// @apiNote While Mekanism does not currently make use of the inputs, it is important to support it and pass the proper value in case any addons define input based
    /// outputs where things like NBT may be different.
    /// @implNote The passed in inputs should **NOT** be modified.
    @Contract(value = "_, _, _ -> new", pure = true)
    public abstract PressurizedReactionRecipeOutput getOutput(ItemStack solid, FluidStack liquid, ChemicalStack chemical);

    @Override
    public boolean isIncomplete() {
        return getInputSolid().hasNoMatchingInstances() || getInputFluid().hasNoMatchingInstances() || getInputChemical().hasNoMatchingInstances();
    }

    @Override
    public void logMissingTags() {
        getInputSolid().logMissingTags();
        getInputFluid().logMissingTags();
        getInputChemical().logMissingTags();
    }

    @Override
    public final RecipeType<? extends PressurizedReactionRecipe> getType() {
        return MekanismRecipeTypes.TYPE_REACTION.value();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new ReactionRecipeDisplay(
              getInputSolid().display(),
              getInputFluid().display(),
              getInputChemical().display(),
              getOutputDisplay(),
              new SlotDisplay.ItemSlotDisplay(PRESSURIZED_REACTION_CHAMBER)
        ));
    }

    /// @apiNote Both item and chemical may be present or one may be empty.
    public record PressurizedReactionRecipeOutput(@Nullable ItemStackTemplate item, @Nullable ChemicalStackTemplate chemical) {

        private static final Codec<Optional<PressurizedReactionRecipeOutput>> UNCHECKED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
              ItemStackTemplate.CODEC.optionalFieldOf(SerializationConstants.ITEM).forGetter(output -> output.map(PressurizedReactionRecipeOutput::item)),
              ChemicalStackTemplate.MAP_CODEC.codec().optionalFieldOf(SerializationConstants.CHEMICAL).forGetter(output -> output.map(PressurizedReactionRecipeOutput::chemical))
        ).apply(instance, (item, chemical) -> {
            if (item.isEmpty() && chemical.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new PressurizedReactionRecipeOutput(item, chemical));
        }));
        public static final Codec<PressurizedReactionRecipeOutput> CODEC = UNCHECKED_CODEC.flatXmap(result ->
                    result.map(DataResult::success).orElseGet(() -> DataResult.error(() -> "No output specified, must have at least an Item or Chemical output")),
              output -> DataResult.success(Optional.of(output))
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, PressurizedReactionRecipeOutput> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public PressurizedReactionRecipeOutput decode(RegistryFriendlyByteBuf buffer) {
                byte type = buffer.readByte();
                ItemStackTemplate item = type == 0 || type == 1 ? ItemStackTemplate.STREAM_CODEC.decode(buffer) : null;
                ChemicalStackTemplate chemical = type == 0 || type == 2 ? ChemicalStackTemplate.STREAM_CODEC.decode(buffer) : null;
                return new PressurizedReactionRecipeOutput(item, chemical);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, PressurizedReactionRecipeOutput output) {
                byte type;
                if (output.item != null && output.chemical != null) {
                    type = 0;
                } else if (output.item != null) {
                    type = 1;
                } else {
                    type = 2;
                }
                buffer.writeByte(type);
                if (output.item != null) {
                    ItemStackTemplate.STREAM_CODEC.encode(buffer, output.item);
                }
                if (output.chemical != null) {
                    ChemicalStackTemplate.STREAM_CODEC.encode(buffer, output.chemical);
                }
            }
        };

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private PressurizedReactionRecipeOutput(Optional<ItemStackTemplate> item, Optional<ChemicalStackTemplate> chemical) {
            this(item.orElse(null), chemical.orElse(null));
        }

        public PressurizedReactionRecipeOutput {
            if (item == null && chemical == null) {
                throw new IllegalArgumentException("At least one output must be present.");
            }
        }

        /// @since 10.8.0
        public SlotDisplay asDisplay() {
            if (item != null && chemical != null) {
                return new SlotDisplay.Composite(List.of(new SlotDisplay.ItemStackSlotDisplay(item), new ChemicalStackSlotDisplay(chemical)));
            } else if (item != null) {
                return new SlotDisplay.ItemStackSlotDisplay(item);
            } else if (chemical != null) {
                return new ChemicalStackSlotDisplay(chemical);
            }
            throw new IllegalStateException("At least one output should always be present");
        }
    }

    /// Helper interface for constructing [PressurizedReactionRecipes][PressurizedReactionRecipe]
    ///
    /// @since 10.8.0
    @FunctionalInterface
    public interface Factory<RECIPE extends PressurizedReactionRecipe> {

        /// @param commonInfo     Common recipe info.
        /// @param bookInfo       Recipe book info.
        /// @param inputSolid     Item input.
        /// @param inputFluid     Fluid input.
        /// @param inputChemical  Chemical input.
        /// @param energyRequired Amount of "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the recipe.
        /// @param duration       Base duration in ticks that this recipe takes to complete. Must be greater than zero.
        /// @param output         Output.
        RECIPE create(CommonInfo commonInfo, ReactingBookInfo bookInfo, ItemStackIngredient inputSolid, FluidStackIngredient inputFluid,
              ChemicalStackIngredient inputChemical, int energyRequired, int duration, PressurizedReactionRecipeOutput output);
    }
}
