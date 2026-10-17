package mekanism.api.recipes.basic;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.book.CrystallizingBookCategory.CrystallizingBookInfo;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.core.TypedInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;


public class BasicChemicalCrystallizerRecipe extends ChemicalCrystallizerRecipe {

    protected final ChemicalStackIngredient input;
    protected final ItemStackTemplate output;
    protected final SlotDisplay typeDisplay;

    /// @param commonInfo Common recipe info.
    /// @param bookInfo   Recipe book info.
    /// @param input      Input.
    /// @param output     Output.
    public BasicChemicalCrystallizerRecipe(CommonInfo commonInfo, CrystallizingBookInfo bookInfo, ChemicalStackIngredient input, SlotDisplay typeDisplay,
          ItemStackTemplate output) {
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        this.output = Objects.requireNonNull(output, "Output cannot be null.");
        this.typeDisplay = Objects.requireNonNull(typeDisplay, "Type display cannot be null, to not have a type display pass empty.");
        super(commonInfo, bookInfo);
    }

    @Override
    @Contract(pure = true)
    public ItemStackTemplate getOutput(TypedInstance<Chemical> input) {
        return output;
    }

    @Override
    public List<ItemStackTemplate> getOutputDefinition(ContextMap contextMap) {
        return Collections.singletonList(output);
    }

    @Override
    public SlotDisplay getOutputDisplay() {
        return new SlotDisplay.ItemStackSlotDisplay(output);
    }

    @Override
    public ChemicalStackIngredient getInput() {
        return input;
    }

    /// For Serializer usage only. Do not modify the returned stack!
    ///
    /// @return the uncopied output definition
    public ItemStackTemplate getOutputRaw() {
        return this.output;
    }

    @Override
    public SlotDisplay getTypeDisplay() {
        return typeDisplay;
    }

    @Override
    public RecipeSerializer<BasicChemicalCrystallizerRecipe> getSerializer() {
        return MekanismRecipeSerializers.CRYSTALLIZING.get();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicChemicalCrystallizerRecipe other = (BasicChemicalCrystallizerRecipe) o;
        return input.equals(other.input) && output.equals(other.output) && typeDisplay.equals(other.typeDisplay);
    }

    @Override
    public int hashCode() {
        int hash = input.hashCode();
        hash = 31 * hash + output.hashCode();
        hash = 31 * hash + typeDisplay.hashCode();
        return hash;
    }

    /// Helper to create a [MapCodec] for a simple [BasicChemicalCrystallizerRecipe].
    ///
    /// @param factory Recipe constructor
    ///
    /// @since 10.8.0
    public static <RECIPE extends BasicChemicalCrystallizerRecipe> MapCodec<RECIPE> simpleMapCodec(ChemicalCrystallizerRecipe.Factory<RECIPE> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
              Recipe.CommonInfo.MAP_CODEC.forGetter(MekanismRecipe::commonInfo),
              CrystallizingBookInfo.MAP_CODEC.forGetter(MekanismRecipe::bookInfo),
              ChemicalStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(ChemicalCrystallizerRecipe::getInput),
              SlotDisplay.CODEC.optionalFieldOf(SerializationConstants.TYPE_DISPLAY, SlotDisplay.Empty.INSTANCE).forGetter(ChemicalCrystallizerRecipe::getTypeDisplay),
              ItemStackTemplate.CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(recipe -> recipe.output)
        ).apply(instance, factory::create));
    }

    /// Helper to create a [StreamCodec] for a simple [BasicChemicalCrystallizerRecipe].
    ///
    /// @param factory Recipe constructor
    ///
    /// @since 10.8.0
    public static <RECIPE extends BasicChemicalCrystallizerRecipe> StreamCodec<RegistryFriendlyByteBuf, RECIPE> simpleStreamCodec(
          ChemicalCrystallizerRecipe.Factory<RECIPE> factory) {
        return StreamCodec.composite(
              CommonInfo.STREAM_CODEC, MekanismRecipe::commonInfo,
              CrystallizingBookInfo.STREAM_CODEC, MekanismRecipe::bookInfo,
              ChemicalStackIngredient.STREAM_CODEC, ChemicalCrystallizerRecipe::getInput,
              SlotDisplay.STREAM_CODEC, ChemicalCrystallizerRecipe::getTypeDisplay,
              ItemStackTemplate.STREAM_CODEC, recipe -> recipe.output,
              factory::create
        );
    }
}