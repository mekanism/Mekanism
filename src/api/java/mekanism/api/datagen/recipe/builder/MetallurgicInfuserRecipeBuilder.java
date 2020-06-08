package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MetallurgicInfuserRecipeBuilder extends MekanismRecipeBuilder<MetallurgicInfuserRecipeBuilder> {

    private final ItemStackIngredient itemInput;
    private final InfusionStackIngredient infusionInput;
    private final ItemStack output;

    protected MetallurgicInfuserRecipeBuilder(ItemStackIngredient itemInput, InfusionStackIngredient infusionInput, ItemStack output) {
        super(mekSerializer("metallurgic_infusing"));
        this.itemInput = itemInput;
        this.infusionInput = infusionInput;
        this.output = output;
    }

    public static MetallurgicInfuserRecipeBuilder metallurgicInfusing(ItemStackIngredient itemInput, InfusionStackIngredient infusionInput, ItemStack output) {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("This metallurgic infusing recipe requires a non empty output.");
        }
        return new MetallurgicInfuserRecipeBuilder(itemInput, infusionInput, output);
    }

    @Override
    protected MetallurgicInfuserRecipeResult getResult(ResourceLocation id) {
        return new MetallurgicInfuserRecipeResult(id);
    }

    public void build(Consumer<IFinishedRecipe> consumer) {
        build(consumer, output.getItem().getRegistryName());
    }

    public class MetallurgicInfuserRecipeResult extends RecipeResult {

        protected MetallurgicInfuserRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add(JsonConstants.ITEM_INPUT, itemInput.serialize());
            json.add(JsonConstants.INFUSION_INPUT, infusionInput.serialize());
            json.add(JsonConstants.OUTPUT, SerializerHelper.serializeItemStack(output));
        }
    }
}