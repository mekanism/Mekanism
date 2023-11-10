package mekanism.common.recipe.serializer;

import com.google.common.collect.Streams;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapEncoder.Implementation;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class ChemicalCrystallizerRecipeSerializer<RECIPE extends ChemicalCrystallizerRecipe> implements RecipeSerializer<RECIPE> {

    private final IFactory<RECIPE> factory;
    private Codec<RECIPE> codec;

    public ChemicalCrystallizerRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    @SuppressWarnings("unchecked")
    private static MapCodec<ChemicalStackIngredient<?, ?>> codecByType(ChemicalType chemicalType) {
        return ((Codec<ChemicalStackIngredient<?, ?>>) IngredientCreatorAccess.getCreatorForType(chemicalType).codec()).fieldOf(JsonConstants.INPUT);
    }

    private static final MapCodec<ChemicalType> chemicalTypeMapCodec = ChemicalType.CODEC.fieldOf(JsonConstants.CHEMICAL_TYPE);

    private static final MapEncoder<ChemicalStackIngredient<?,?>> chemicalStackIngredientEncoder = new Implementation<>() {
        @Override
        public <T> RecordBuilder<T> encode(ChemicalStackIngredient<?, ?> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            ChemicalType type = ChemicalType.getTypeFor(input);
            return codecByType(type).encode(input, ops, chemicalTypeMapCodec.encode(type, ops, prefix));
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Streams.concat(chemicalTypeMapCodec.keys(ops), Stream.of(ops.createString(JsonConstants.INPUT)));
        }
    };

    @NotNull
    @Override
    public Codec<RECIPE> codec() {
        if (codec == null) {
            codec = RecordCodecBuilder.create(instance-> {
                RecordCodecBuilder<RECIPE, ChemicalType> typeField = chemicalTypeMapCodec.forGetter(r -> ChemicalType.getTypeFor(r.getInput()));
                return instance.group(
                      typeField.dependent(ChemicalCrystallizerRecipe::getInput, chemicalStackIngredientEncoder, ChemicalCrystallizerRecipeSerializer::codecByType),
                      //SerializerHelper.BOXED_CHEMICALSTACK_INGREDIENT_CODEC.fieldOf(JsonConstants.INPUT).forGetter(ChemicalCrystallizerRecipe::getInput),
                      SerializerHelper.ITEMSTACK_CODEC.fieldOf(JsonConstants.OUTPUT).forGetter(ChemicalCrystallizerRecipe::getOutputRaw)
                ).apply(instance, factory::create);
            });
        }
        return codec;
    }

    @Override
    public RECIPE fromNetwork(@NotNull FriendlyByteBuf buffer) {
        try {
            ChemicalType chemicalType = buffer.readEnum(ChemicalType.class);
            ChemicalStackIngredient<?, ?> inputIngredient = IngredientCreatorAccess.getCreatorForType(chemicalType).read(buffer);
            ItemStack output = buffer.readItem();
            return this.factory.create(inputIngredient, output);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading boxed chemical to itemstack recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing boxed chemical to itemstack recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends ChemicalCrystallizerRecipe> {

        RECIPE create(ChemicalStackIngredient<?, ?> input, ItemStack output);
    }
}