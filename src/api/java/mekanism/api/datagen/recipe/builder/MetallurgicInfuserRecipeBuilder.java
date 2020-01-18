package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.inputs.InfusionIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.advancements.Advancement;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MetallurgicInfuserRecipeBuilder extends MekanismRecipeBuilder<MetallurgicInfuserRecipeBuilder> {

    private final ItemStackIngredient itemInput;
    private final InfusionIngredient infusionInput;
    private final ItemStack output;

    protected MetallurgicInfuserRecipeBuilder(ItemStackIngredient itemInput, InfusionIngredient infusionInput, ItemStack output) {
        super(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "metallurgic_infusing"));
        this.itemInput = itemInput;
        this.infusionInput = infusionInput;
        this.output = output;
    }

    public static MetallurgicInfuserRecipeBuilder metallurgicInfusing(ItemStackIngredient itemInput, InfusionIngredient infusionInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This metallurgic infusing recipe requires a non empty output.");
        }
        return new MetallurgicInfuserRecipeBuilder(itemInput, infusionInput, output);
    }

    @Override
    public MetallurgicInfuserRecipeResult getResult(ResourceLocation id) {
        return new MetallurgicInfuserRecipeResult(id, itemInput, infusionInput, output, advancementBuilder,
              new ResourceLocation(id.getNamespace(), "recipes/" + output.getItem().getGroup().getPath() + "/" + id.getPath()), serializerName);
    }

    public static class MetallurgicInfuserRecipeResult extends RecipeResult {

        private final ItemStackIngredient itemInput;
        private final InfusionIngredient infusionInput;
        private final ItemStack output;

        public MetallurgicInfuserRecipeResult(ResourceLocation id, ItemStackIngredient itemInput, InfusionIngredient infusionInput, ItemStack output,
              Advancement.Builder advancementBuilder, ResourceLocation advancementId, ResourceLocation serializerName) {
            super(id, advancementBuilder, advancementId, serializerName);
            this.itemInput = itemInput;
            this.infusionInput = infusionInput;
            this.output = output;
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add("itemInput", itemInput.serialize());
            json.add("infusionInput", infusionInput.serialize());
            json.add("output", SerializerHelper.serializeItemStack(output));
        }
    }
}