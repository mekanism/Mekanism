package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.infuse.InfusionStack;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemStackToInfuseTypeRecipeBuilder extends MekanismRecipeBuilder<ItemStackToInfuseTypeRecipeBuilder> {

    private final ItemStackIngredient input;
    private final InfusionStack output;

    protected ItemStackToInfuseTypeRecipeBuilder(ItemStackIngredient input, InfusionStack output) {
        super(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "infusion_conversion"));
        this.input = input;
        this.output = output;
    }

    public static ItemStackToInfuseTypeRecipeBuilder infusionConversion(ItemStackIngredient input, InfusionStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This infusion conversion recipe requires a non empty infusion output.");
        }
        return new ItemStackToInfuseTypeRecipeBuilder(input, output);
    }

    @Override
    protected ItemStackToInfuseTypeRecipeResult getResult(ResourceLocation id) {
        return new ItemStackToInfuseTypeRecipeResult(id);
    }

    public class ItemStackToInfuseTypeRecipeResult extends RecipeResult {

        protected ItemStackToInfuseTypeRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add("input", input.serialize());
            json.add("output", SerializerHelper.serializeInfusionStack(output));
        }
    }
}