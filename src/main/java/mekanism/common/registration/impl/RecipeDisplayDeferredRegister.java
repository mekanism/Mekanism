package mekanism.common.registration.impl;

import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import com.mojang.datafixers.util.Function6;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import mekanism.api.SerializationConstants;
import mekanism.api.recipes.display.recipe.MekanismRecipeDisplay;
import mekanism.api.recipes.display.recipe.OneInputRecipeDisplay;
import mekanism.api.recipes.display.recipe.TwoInputPerTickRecipeDisplay;
import mekanism.api.recipes.display.recipe.TwoInputRecipeDisplay;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public class RecipeDisplayDeferredRegister extends MekanismDeferredRegister<RecipeDisplay.Type<?>> {

    public RecipeDisplayDeferredRegister(String modid) {
        super(Registries.RECIPE_DISPLAY, modid);
    }

    public <DISPLAY extends OneInputRecipeDisplay> MekanismDeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<DISPLAY>> registerOneInput(Identifier recipeName,
          Function4<SlotDisplay, SlotDisplay, SlotDisplay, Float, DISPLAY> displayCreator) {
        return register(recipeName, () -> RecordCodecBuilder.mapCodec(i -> i.group(
              SlotDisplay.CODEC.fieldOf(SerializationConstants.INGREDIENT).forGetter(OneInputRecipeDisplay::ingredient),
              SlotDisplay.CODEC.fieldOf(SerializationConstants.RESULT).forGetter(RecipeDisplay::result),
              SlotDisplay.CODEC.fieldOf(SerializationConstants.CRAFTING_STATION).forGetter(RecipeDisplay::craftingStation),
              ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf(SerializationConstants.EXPERIENCE, 0F).forGetter(MekanismRecipeDisplay::experience)
        ).apply(i, displayCreator)), () -> StreamCodec.composite(
              SlotDisplay.STREAM_CODEC, OneInputRecipeDisplay::ingredient,
              SlotDisplay.STREAM_CODEC, RecipeDisplay::result,
              SlotDisplay.STREAM_CODEC, RecipeDisplay::craftingStation,
              ByteBufCodecs.FLOAT, MekanismRecipeDisplay::experience,
              displayCreator
        ));
    }

    public <DISPLAY extends TwoInputRecipeDisplay> MekanismDeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<DISPLAY>> registerTwoInput(Identifier recipeName,
          Function5<SlotDisplay, SlotDisplay, SlotDisplay, SlotDisplay, Float, DISPLAY> displayCreator) {
        return register(recipeName, () -> RecordCodecBuilder.mapCodec(i -> i.group(
                SlotDisplay.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(TwoInputRecipeDisplay::ingredient),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.EXTRA_INPUT).forGetter(TwoInputRecipeDisplay::secondIngredient),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.RESULT).forGetter(RecipeDisplay::result),
                SlotDisplay.CODEC.fieldOf(SerializationConstants.CRAFTING_STATION).forGetter(RecipeDisplay::craftingStation),
              ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf(SerializationConstants.EXPERIENCE, 0F).forGetter(MekanismRecipeDisplay::experience)
          ).apply(i, displayCreator)), () -> StreamCodec.composite(
              SlotDisplay.STREAM_CODEC, TwoInputRecipeDisplay::ingredient,
              SlotDisplay.STREAM_CODEC, TwoInputRecipeDisplay::secondIngredient,
              SlotDisplay.STREAM_CODEC, RecipeDisplay::result,
              SlotDisplay.STREAM_CODEC, RecipeDisplay::craftingStation,
              ByteBufCodecs.FLOAT, MekanismRecipeDisplay::experience,
              displayCreator
        ));
    }

    public <DISPLAY extends TwoInputPerTickRecipeDisplay> MekanismDeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<DISPLAY>> registerTwoPerTickInput(Identifier recipeName,
          boolean defaultPerTick, Function6<SlotDisplay, SlotDisplay, Boolean, Float, SlotDisplay, SlotDisplay, DISPLAY> displayCreator) {
        return register(recipeName, () -> RecordCodecBuilder.mapCodec(i -> i.group(
              SlotDisplay.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(TwoInputRecipeDisplay::ingredient),
              SlotDisplay.CODEC.fieldOf(SerializationConstants.EXTRA_INPUT).forGetter(TwoInputRecipeDisplay::secondIngredient),
              Codec.BOOL.optionalFieldOf(SerializationConstants.PER_TICK_USAGE, defaultPerTick).forGetter(TwoInputPerTickRecipeDisplay::perTickSecondary),
              ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf(SerializationConstants.EXPERIENCE, 0F).forGetter(TwoInputPerTickRecipeDisplay::experience),
              SlotDisplay.CODEC.fieldOf(SerializationConstants.RESULT).forGetter(RecipeDisplay::result),
              SlotDisplay.CODEC.fieldOf(SerializationConstants.CRAFTING_STATION).forGetter(RecipeDisplay::craftingStation)
        ).apply(i, displayCreator)), () -> StreamCodec.composite(
              SlotDisplay.STREAM_CODEC, TwoInputRecipeDisplay::ingredient,
              SlotDisplay.STREAM_CODEC, TwoInputRecipeDisplay::secondIngredient,
              ByteBufCodecs.BOOL, TwoInputPerTickRecipeDisplay::perTickSecondary,
              ByteBufCodecs.FLOAT, MekanismRecipeDisplay::experience,
              SlotDisplay.STREAM_CODEC, RecipeDisplay::result,
              SlotDisplay.STREAM_CODEC, RecipeDisplay::craftingStation,
              displayCreator
        ));
    }

    public <DISPLAY extends RecipeDisplay> MekanismDeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<DISPLAY>> register(Identifier recipeName, Supplier<MapCodec<DISPLAY>> codec,
          Supplier<StreamCodec<RegistryFriendlyByteBuf, DISPLAY>> streamCodec) {
        return register(recipeName.getPath(), codec, streamCodec);
    }

    public <DISPLAY extends RecipeDisplay> MekanismDeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<DISPLAY>> register(String name, Supplier<MapCodec<DISPLAY>> codec,
          Supplier<StreamCodec<RegistryFriendlyByteBuf, DISPLAY>> streamCodec) {
        return register(name, () -> new RecipeDisplay.Type<>(codec.get(), streamCodec.get()));
    }
}