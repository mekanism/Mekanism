package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ChemicalCrystallizerRecipeBuilder extends MekanismRecipeBuilder<ChemicalCrystallizerRecipeBuilder> {

    private final ChemicalType chemicalType;
    private final ChemicalStackIngredient<?, ?> input;
    private final ItemStack output;

    protected ChemicalCrystallizerRecipeBuilder(ResourceLocation serializerName, ChemicalStackIngredient<?, ?> input, ItemStack output) {
        super(serializerName);
        this.input = input;
        this.chemicalType = ChemicalType.getTypeFor(input);
        this.output = output;
    }

    /**
     * Creates a Chemical Crystallizing recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ChemicalCrystallizerRecipeBuilder crystallizing(ChemicalStackIngredient<?, ?> input, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This crystallizing recipe requires a non empty item output.");
        }
        return new ChemicalCrystallizerRecipeBuilder(mekSerializer("crystallizing"), input, output);
    }

    @Override
    protected ChemicalCrystallizerRecipeResult getResult(ResourceLocation id) {
        return new ChemicalCrystallizerRecipeResult(id);
    }

    /**
     * Builds this recipe using the output item's name as the recipe name.
     *
     * @param consumer Finished Recipe Consumer.
     */
    public void build(Consumer<FinishedRecipe> consumer) {
        build(consumer, output.getItem().getRegistryName());
    }

    public class ChemicalCrystallizerRecipeResult extends RecipeResult {

        protected ChemicalCrystallizerRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(@Nonnull JsonObject json) {
            json.addProperty(JsonConstants.CHEMICAL_TYPE, chemicalType.getSerializedName());
            json.add(JsonConstants.INPUT, input.serialize());
            json.add(JsonConstants.OUTPUT, SerializerHelper.serializeItemStack(output));
        }
    }
}