package mekanism.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiFunction;
import mekanism.api.JsonConstants;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.codec.DependentMapCodec;
import mekanism.api.recipes.basic.BasicChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class ChemicalCrystallizerRecipeSerializer implements RecipeSerializer<BasicChemicalCrystallizerRecipe> {

    private static final MapCodec<ChemicalType> chemicalTypeMapCodec = ChemicalType.CODEC.fieldOf(JsonConstants.CHEMICAL_TYPE);
    @SuppressWarnings("unchecked")
    private static final MapCodec<ChemicalStackIngredient<?, ?>> chemicalStackIngredientMapEncoder = new DependentMapCodec<>(JsonConstants.INPUT, type -> (Codec<ChemicalStackIngredient<?, ?>>) IngredientCreatorAccess.getCreatorForType(type).codec(), chemicalTypeMapCodec, ChemicalType::getTypeFor);

    private final StreamCodec<RegistryFriendlyByteBuf, BasicChemicalCrystallizerRecipe> streamCodec;
    private final MapCodec<BasicChemicalCrystallizerRecipe> codec;

    public ChemicalCrystallizerRecipeSerializer(BiFunction<ChemicalStackIngredient<?, ?>, ItemStack, BasicChemicalCrystallizerRecipe> factory) {
        this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
              chemicalStackIngredientMapEncoder.forGetter(BasicChemicalCrystallizerRecipe::getInput),
              ItemStack.CODEC.fieldOf(JsonConstants.OUTPUT).forGetter(BasicChemicalCrystallizerRecipe::getOutputRaw)
        ).apply(instance, factory));
        //TODO - 1.20.5: Figure this out. I think this technically works but it is messy
        this.streamCodec = StreamCodec.composite(
              ChemicalType.STREAM_CODEC.<RegistryFriendlyByteBuf>cast().dispatch(ChemicalType::getTypeFor,
                    chemicalType -> IngredientCreatorAccess.getCreatorForType(chemicalType).streamCodec()), BasicChemicalCrystallizerRecipe::getInput,
              ItemStack.STREAM_CODEC, BasicChemicalCrystallizerRecipe::getOutputRaw,
              factory
        );
    }

    @NotNull
    @Override
    public MapCodec<BasicChemicalCrystallizerRecipe> codec() {
        return codec;
    }

    @NotNull
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, BasicChemicalCrystallizerRecipe> streamCodec() {
        return streamCodec;
    }
}