package mekanism.common.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.codec.DependentMapCodec;
import mekanism.api.recipes.basic.BasicChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class ChemicalCrystallizerRecipeSerializer implements RecipeSerializer<BasicChemicalCrystallizerRecipe> {

    private final IFactory<BasicChemicalCrystallizerRecipe> factory;
    private Codec<BasicChemicalCrystallizerRecipe> codec;

    public ChemicalCrystallizerRecipeSerializer(IFactory<BasicChemicalCrystallizerRecipe> factory) {
        this.factory = factory;
    }

    private static final MapCodec<ChemicalType> chemicalTypeMapCodec = ChemicalType.CODEC.fieldOf(JsonConstants.CHEMICAL_TYPE);
    @SuppressWarnings("unchecked")
    private static final MapCodec<ChemicalStackIngredient<?, ?>> chemicalStackIngredientMapEncoder = new DependentMapCodec<>(JsonConstants.INPUT, type -> (Codec<ChemicalStackIngredient<?, ?>>) IngredientCreatorAccess.getCreatorForType(type).codec(), chemicalTypeMapCodec, ChemicalType::getTypeFor);

    @NotNull
    @Override
    public Codec<BasicChemicalCrystallizerRecipe> codec() {
        if (codec == null) {
            codec = RecordCodecBuilder.create(instance -> instance.group(
                  chemicalStackIngredientMapEncoder.forGetter(BasicChemicalCrystallizerRecipe::getInput),
                  ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf(JsonConstants.OUTPUT).forGetter(BasicChemicalCrystallizerRecipe::getOutputRaw)
            ).apply(instance, factory::create));
        }
        return codec;
    }

    @NotNull
    @Override
    public BasicChemicalCrystallizerRecipe fromNetwork(@NotNull FriendlyByteBuf buffer) {
        ChemicalType chemicalType = buffer.readEnum(ChemicalType.class);
        ChemicalStackIngredient<?, ?> inputIngredient = IngredientCreatorAccess.getCreatorForType(chemicalType).read(buffer);
        ItemStack output = buffer.readItem();
        return this.factory.create(inputIngredient, output);
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull BasicChemicalCrystallizerRecipe recipe) {
        buffer.writeEnum(recipe.getChemicalType());
        recipe.getInput().write(buffer);
        buffer.writeItem(recipe.getOutputRaw());
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends BasicChemicalCrystallizerRecipe> {

        RECIPE create(ChemicalStackIngredient<?, ?> input, ItemStack output);
    }
}