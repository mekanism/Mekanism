package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ItemStackToEnergyRecipeBuilder extends MekanismRecipeBuilder<ItemStackToEnergyRecipeBuilder> {

    private final ItemStackIngredient input;
    private final FloatingLong output;

    protected ItemStackToEnergyRecipeBuilder(ItemStackIngredient input, FloatingLong output, ResourceLocation serializerName) {
        super(serializerName);
        this.input = input;
        this.output = output;
    }

    /**
     * Creates an Energy Conversion recipe builder.
     *
     * @param input  Input.
     * @param output Output.
     */
    public static ItemStackToEnergyRecipeBuilder energyConversion(ItemStackIngredient input, FloatingLong output) {
        if (output.isZero()) {
            throw new IllegalArgumentException("This energy conversion recipe requires an energy output greater than zero");
        }
        return new ItemStackToEnergyRecipeBuilder(input, output, mekSerializer("energy_conversion"));
    }

    @Override
    protected MekanismRecipeBuilder<ItemStackToEnergyRecipeBuilder>.RecipeResult getResult(ResourceLocation id, @Nullable AdvancementHolder advancementHolder) {
        return new ItemStackToEnergyRecipeResult(id, advancementHolder);
    }

    public class ItemStackToEnergyRecipeResult extends RecipeResult {

        protected ItemStackToEnergyRecipeResult(ResourceLocation id, @Nullable AdvancementHolder advancementHolder) {
            super(id, advancementHolder);
        }

        @Override
        public void serializeRecipeData(@NotNull JsonObject json) {
            json.add(JsonConstants.INPUT, input.serialize());
            json.addProperty(JsonConstants.OUTPUT, output);
        }
    }
}