package mekanism.api.recipes.basic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.book.DissolutionBookCategory.DissolutionBookInfo;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalStackSlotDisplay;
import net.minecraft.core.TypedInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

public class BasicChemicalDissolutionRecipe extends ChemicalDissolutionRecipe {

    protected final ItemStackIngredient itemInput;
    protected final ChemicalStackIngredient chemicalInput;
    protected final ChemicalStackTemplate output;
    private final boolean perTickUsage;

    /// @param commonInfo    Common recipe info.
    /// @param bookInfo      Recipe book info.
    /// @param itemInput     Item input.
    /// @param chemicalInput Chemical input.
    /// @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
    /// @param output        Output.
    public BasicChemicalDissolutionRecipe(CommonInfo commonInfo, DissolutionBookInfo bookInfo, ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput,
          boolean perTickUsage, ChemicalStackTemplate output) {
        this.itemInput = Objects.requireNonNull(itemInput, "Item input cannot be null.");
        this.chemicalInput = Objects.requireNonNull(chemicalInput, "Chemical input cannot be null.");
        this.output = Objects.requireNonNull(output, "Output cannot be null.");
        this.perTickUsage = perTickUsage;
        super(commonInfo, bookInfo);
    }

    @Override
    public boolean perTickUsage() {
        return perTickUsage;
    }

    @Override
    public ItemStackIngredient getItemInput() {
        return itemInput;
    }

    @Override
    public ChemicalStackIngredient getChemicalInput() {
        return chemicalInput;
    }

    @Override
    public ChemicalStackTemplate getOutput(TypedInstance<Item> inputItem, TypedInstance<Chemical> inputChemical) {
        return output;
    }

    @Override
    public List<ChemicalStackTemplate> getOutputDefinition(ContextMap contextMap) {
        return Collections.singletonList(output);
    }

    @Override
    public SlotDisplay getOutputDisplay() {
        return new ChemicalStackSlotDisplay(output);
    }

    /// For Serializer usage only. Do not modify the returned stack!
    ///
    /// @return the uncopied output definition
    public ChemicalStackTemplate getOutputRaw() {
        return output;
    }

    @Override
    public RecipeSerializer<BasicChemicalDissolutionRecipe> getSerializer() {
        return MekanismRecipeSerializers.DISSOLUTION.get();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicChemicalDissolutionRecipe other = (BasicChemicalDissolutionRecipe) o;
        return perTickUsage == other.perTickUsage && itemInput.equals(other.itemInput) && chemicalInput.equals(other.chemicalInput) && output.equals(other.output);
    }

    @Override
    public int hashCode() {
        int result = itemInput.hashCode();
        result = 31 * result + chemicalInput.hashCode();
        result = 31 * result + output.hashCode();
        result = 31 * result + Boolean.hashCode(perTickUsage);
        return result;
    }

    /// Helper to create a [MapCodec] for a simple [BasicChemicalDissolutionRecipe].
    ///
    /// @param factory Recipe constructor
    ///
    /// @since 10.8.0
    public static <RECIPE extends BasicChemicalDissolutionRecipe> MapCodec<RECIPE> simpleMapCodec(ChemicalDissolutionRecipe.Factory<RECIPE> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
              Recipe.CommonInfo.MAP_CODEC.forGetter(MekanismRecipe::commonInfo),
              DissolutionBookInfo.MAP_CODEC.forGetter(MekanismRecipe::bookInfo),
              ItemStackIngredient.CODEC.fieldOf(SerializationConstants.ITEM_INPUT).forGetter(ChemicalDissolutionRecipe::getItemInput),
              ChemicalStackIngredient.CODEC.fieldOf(SerializationConstants.CHEMICAL_INPUT).forGetter(ChemicalDissolutionRecipe::getChemicalInput),
              Codec.BOOL.fieldOf(SerializationConstants.PER_TICK_USAGE).forGetter(ChemicalDissolutionRecipe::perTickUsage),
              ChemicalStackTemplate.MAP_CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(recipe -> recipe.output)
        ).apply(instance, factory::create));
    }

    /// Helper to create a [StreamCodec] for a simple [BasicChemicalDissolutionRecipe].
    ///
    /// @param factory Recipe constructor
    ///
    /// @since 10.8.0
    public static <RECIPE extends BasicChemicalDissolutionRecipe> StreamCodec<RegistryFriendlyByteBuf, RECIPE> simpleStreamCodec(ChemicalDissolutionRecipe.Factory<RECIPE> factory) {
        return StreamCodec.composite(
              CommonInfo.STREAM_CODEC, MekanismRecipe::commonInfo,
              DissolutionBookInfo.STREAM_CODEC, MekanismRecipe::bookInfo,
              ItemStackIngredient.STREAM_CODEC, ChemicalDissolutionRecipe::getItemInput,
              ChemicalStackIngredient.STREAM_CODEC, ChemicalDissolutionRecipe::getChemicalInput,
              ByteBufCodecs.BOOL, ChemicalDissolutionRecipe::perTickUsage,
              ChemicalStackTemplate.STREAM_CODEC, recipe -> recipe.output,
              factory::create
        );
    }
}