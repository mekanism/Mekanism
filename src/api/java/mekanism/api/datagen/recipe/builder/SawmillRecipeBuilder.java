package mekanism.api.datagen.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.advancements.Advancement;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SawmillRecipeBuilder extends MekanismRecipeBuilder<SawmillRecipeBuilder> {

    private final OutputType outputType;
    private final ItemStackIngredient input;
    private final ItemStack mainOutput;
    private final ItemStack secondaryOutput;
    private final double secondaryChance;

    protected SawmillRecipeBuilder(ItemStackIngredient input, ItemStack mainOutput, ItemStack secondaryOutput, double secondaryChance, OutputType outputType) {
        super(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "sawing"));
        this.outputType = outputType;
        this.input = input;
        this.mainOutput = mainOutput;
        this.secondaryOutput = secondaryOutput;
        this.secondaryChance = secondaryChance;
    }

    public static SawmillRecipeBuilder sawing(ItemStackIngredient input, ItemStack mainOutput) {
        if (mainOutput.isEmpty()) {
            throw new IllegalArgumentException("This sawing recipe requires a non empty output.");
        }
        return new SawmillRecipeBuilder(input, mainOutput, ItemStack.EMPTY, 0, OutputType.PRIMARY);
    }

    public static SawmillRecipeBuilder sawing(ItemStackIngredient input, ItemStack secondaryOutput, double secondaryChance) {
        if (secondaryOutput.isEmpty()) {
            throw new IllegalArgumentException("This sawing recipe requires a non empty secondary output.");
        }
        if (secondaryChance <= 0 || secondaryChance > 1) {
            throw new IllegalArgumentException("This sawing recipe requires a secondary output chance greater than zero and at most one.");
        }
        return new SawmillRecipeBuilder(input, ItemStack.EMPTY, secondaryOutput, secondaryChance, OutputType.SECONDARY);
    }

    public static SawmillRecipeBuilder sawing(ItemStackIngredient input, ItemStack mainOutput, ItemStack secondaryOutput, double secondaryChance) {
        if (mainOutput.isEmpty() || secondaryOutput.isEmpty()) {
            throw new IllegalArgumentException("This sawing recipe requires a non empty primary, and secondary output.");
        }
        if (secondaryChance <= 0 || secondaryChance > 1) {
            throw new IllegalArgumentException("This sawing recipe requires a secondary output chance greater than zero and at most one.");
        }
        return new SawmillRecipeBuilder(input, mainOutput, secondaryOutput, secondaryChance, OutputType.BOTH);
    }

    @Override
    public SawmillRecipeResult getResult(ResourceLocation id) {
        return new SawmillRecipeResult(id, input, mainOutput, secondaryOutput, secondaryChance, outputType, advancementBuilder,
              new ResourceLocation(id.getNamespace(), "recipes/" + id.getPath()), serializerName);
    }

    public static class SawmillRecipeResult extends RecipeResult {

        private final OutputType outputType;
        private final ItemStackIngredient input;
        private final ItemStack mainOutput;
        private final ItemStack secondaryOutput;
        private final double secondaryChance;

        public SawmillRecipeResult(ResourceLocation id, ItemStackIngredient input, ItemStack mainOutput, ItemStack secondaryOutput, double secondaryChance,
              OutputType outputType, Advancement.Builder advancementBuilder, ResourceLocation advancementId, ResourceLocation serializerName) {
            super(id, advancementBuilder, advancementId, serializerName);
            this.outputType = outputType;
            this.input = input;
            this.mainOutput = mainOutput;
            this.secondaryOutput = secondaryOutput;
            this.secondaryChance = secondaryChance;
        }

        @Override
        public void serialize(@Nonnull JsonObject json) {
            json.add("input", input.serialize());
            if (outputType.hasPrimary) {
                json.add("mainOutput", SerializerHelper.serializeItemStack(mainOutput));
            }
            if (outputType.hasSecondary) {
                json.add("secondaryOutput", SerializerHelper.serializeItemStack(secondaryOutput));
                json.addProperty("secondaryChance", secondaryChance);
            }
        }
    }

    private enum OutputType {
        PRIMARY(true, false),
        SECONDARY(false, true),
        BOTH(true, true);

        private final boolean hasPrimary;
        private final boolean hasSecondary;

        OutputType(boolean hasPrimary, boolean hasSecondary) {
            this.hasPrimary = hasPrimary;
            this.hasSecondary = hasSecondary;
        }
    }
}