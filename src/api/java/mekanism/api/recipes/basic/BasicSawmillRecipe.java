package mekanism.api.recipes.basic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.book.SawingBookCategory.SawingBookInfo;
import mekanism.api.recipes.display.slot.ChanceSlotDisplay;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.TypedInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public class BasicSawmillRecipe extends SawmillRecipe {

    protected final ItemStackIngredient input;
    @Nullable
    protected final ItemStackTemplate mainOutput;
    @Nullable
    protected final ItemStackTemplate secondaryOutput;
    protected final double secondaryChance;

    /// @param commonInfo      Common recipe info.
    /// @param bookInfo        Recipe book info.
    /// @param input           Input.
    /// @param mainOutput      Main Output.
    /// @param secondaryOutput Secondary Output (chance based).
    /// @param secondaryChance Chance of the secondary output being produced. This must be at least zero and at most one.
    ///
    /// @apiNote At least one output must not be empty.
    public BasicSawmillRecipe(CommonInfo commonInfo, SawingBookInfo bookInfo, ItemStackIngredient input, @Nullable ItemStackTemplate mainOutput,
          @Nullable ItemStackTemplate secondaryOutput, double secondaryChance) {
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        if (mainOutput == null && secondaryOutput == null) {
            throw new IllegalArgumentException("At least one output must not be empty.");
        } else if (secondaryChance < 0 || secondaryChance > 1) {
            throw new IllegalArgumentException("Secondary output chance must be at least zero and at most one.");
        } else if (mainOutput == null) {
            if (secondaryChance == 0 || secondaryChance == 1) {
                throw new IllegalArgumentException("Secondary output must have a chance greater than zero and less than one.");
            }
        } else if (secondaryOutput == null && secondaryChance != 0) {
            throw new IllegalArgumentException("If there is no secondary output, the chance of getting the secondary output should be zero.");
        }
        this.mainOutput = mainOutput;
        this.secondaryOutput = secondaryOutput;
        this.secondaryChance = secondaryChance;
        super(commonInfo, bookInfo);
    }

    @Override
    @Contract("_ -> new")
    public ChanceOutput getOutput(TypedInstance<Item> input) {
        return new BasicChanceOutput(secondaryChance > 0 ? RANDOM.nextDouble() : 0);
    }

    @Override
    public List<ItemStackTemplate> getMainOutputDefinition(ContextMap contextMap) {
        return mainOutput == null ? Collections.emptyList() : Collections.singletonList(mainOutput);
    }

    @Override
    public List<ItemStackTemplate> getSecondaryOutputDefinition(ContextMap contextMap) {
        return secondaryOutput == null ? Collections.emptyList() : Collections.singletonList(secondaryOutput);
    }

    @Override
    @Deprecated
    public List<ChanceOutput> getOutputDefinition(ContextMap contextMap) {
        return Collections.singletonList(new BasicChanceOutput(secondaryChance));
    }

    @Override
    public SlotDisplay getOutputDisplay() {
        SlotDisplay secondaryDisplay;
        if (secondaryOutput == null || secondaryChance <= 0) {
            secondaryDisplay = null;
        } else {
            secondaryDisplay = ChanceSlotDisplay.create(new SlotDisplay.ItemStackSlotDisplay(secondaryOutput), secondaryChance);
        }
        if (mainOutput != null && secondaryDisplay != null) {
            return new SlotDisplay.Composite(List.of(new SlotDisplay.ItemStackSlotDisplay(mainOutput), secondaryDisplay));
        } else if (mainOutput != null) {
            return new SlotDisplay.ItemStackSlotDisplay(mainOutput);
        } else if (secondaryDisplay != null) {
            return secondaryDisplay;
        }
        throw new IllegalStateException("At least one output should always be present");
    }

    @Override
    public double getSecondaryChance() {
        return secondaryChance;
    }

    @Override
    public ItemStackIngredient getInput() {
        return input;
    }

    @Override
    public RecipeSerializer<BasicSawmillRecipe> getSerializer() {
        return MekanismRecipeSerializers.SAWING.get();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicSawmillRecipe other = (BasicSawmillRecipe) o;
        return secondaryChance == other.secondaryChance && input.equals(other.input) && Objects.equals(mainOutput, other.mainOutput) &&
               Objects.equals(secondaryOutput, other.secondaryOutput);
    }

    @Override
    public int hashCode() {
        int hash = 31 * input.hashCode() + Double.hashCode(secondaryChance);
        hash = 31 * hash + (mainOutput == null ? 0 : mainOutput.hashCode());
        if (secondaryOutput != null) {
            hash = 31 * hash + secondaryOutput.hashCode();
        }
        return hash;
    }

    /// Helper to create a [MapCodec] for a simple [BasicSawmillRecipe].
    ///
    /// @param factory Recipe constructor
    ///
    /// @since 10.8.0
    public static <RECIPE extends BasicSawmillRecipe> MapCodec<RECIPE> simpleMapCodec(SawmillRecipe.Factory<RECIPE> factory) {
        Codec<Double> chanceCodec = Codec.DOUBLE.validate(d -> d > 0 && d <= 1 ? DataResult.success(d) : DataResult.error(() -> "Expected secondaryChance to be greater than zero, and less than or equal to one. Found " + d));
        RecordCodecBuilder<RECIPE, Optional<ItemStackTemplate>> secondaryOutputField = ItemStackTemplate.CODEC.optionalFieldOf(SerializationConstants.SECONDARY_OUTPUT)
              .forGetter(recipe -> Optional.ofNullable(recipe.secondaryOutput));
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
              Recipe.CommonInfo.MAP_CODEC.forGetter(MekanismRecipe::commonInfo),
              SawingBookInfo.MAP_CODEC.forGetter(MekanismRecipe::bookInfo),
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(SawmillRecipe::getInput),
              SerializerHelper.oneRequired(
                    secondaryOutputField,
                    ItemStackTemplate.CODEC.optionalFieldOf(SerializationConstants.MAIN_OUTPUT), recipe -> Optional.ofNullable(recipe.mainOutput)
              ),
              secondaryOutputField,
              SerializerHelper.dependentOptionality(secondaryOutputField, chanceCodec.optionalFieldOf(SerializationConstants.SECONDARY_CHANCE), sawmillRecipe -> {
                  double secondaryChance = sawmillRecipe.getSecondaryChance();
                  return secondaryChance == 0 ? Optional.empty() : Optional.of(secondaryChance);
              })
        ).apply(instance, (commonInfo, bookInfo, input, mainOutput, secondaryOutput, secondChance) ->
              factory.create(commonInfo, bookInfo, input, mainOutput.orElse(null), secondaryOutput.orElse(null), secondChance.orElse(0D))
        ));
    }

    /// Helper to create a [StreamCodec] for a simple [BasicSawmillRecipe].
    ///
    /// @param factory Recipe constructor
    ///
    /// @since 10.8.0
    public static <RECIPE extends BasicSawmillRecipe> StreamCodec<RegistryFriendlyByteBuf, RECIPE> simpleStreamCodec(SawmillRecipe.Factory<RECIPE> factory) {
        return StreamCodec.composite(
              CommonInfo.STREAM_CODEC, MekanismRecipe::commonInfo,
              SawingBookInfo.STREAM_CODEC, MekanismRecipe::bookInfo,
              ItemStackIngredient.STREAM_CODEC, SawmillRecipe::getInput,
              ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC), recipe -> Optional.ofNullable(recipe.mainOutput),
              ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC), recipe -> Optional.ofNullable(recipe.secondaryOutput),
              ByteBufCodecs.DOUBLE, SawmillRecipe::getSecondaryChance,
              (commonInfo, bookInfo, input, mainOutput, secondaryOutput, secondChance) ->
                    factory.create(commonInfo, bookInfo, input, mainOutput.orElse(null), secondaryOutput.orElse(null), secondChance)
        );
    }

    public class BasicChanceOutput implements ChanceOutput {

        protected final double rand;

        protected BasicChanceOutput(double rand) {
            this.rand = rand;
        }

        @Override
        @Nullable
        public ItemStackTemplate getMainOutput() {
            return mainOutput;
        }

        @Override
        @Nullable
        public ItemStackTemplate getMaxSecondaryOutput() {
            return secondaryChance > 0 ? secondaryOutput : null;
        }

        @Override
        @Nullable
        public ItemStackTemplate getSecondaryOutput() {
            if (secondaryOutput != null && rand <= secondaryChance) {
                return secondaryOutput;
            }
            return null;
        }

        @Override
        @Nullable
        public ItemStackTemplate nextSecondaryOutput() {
            if (secondaryOutput != null && secondaryChance > 0) {
                double rand = RANDOM.nextDouble();
                if (rand <= secondaryChance) {
                    return secondaryOutput;
                }
            }
            return null;
        }
    }
}