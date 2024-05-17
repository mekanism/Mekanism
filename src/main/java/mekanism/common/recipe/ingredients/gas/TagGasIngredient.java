package mekanism.common.recipe.ingredients.gas;

import com.mojang.serialization.MapCodec;
import mekanism.api.JsonConstants;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.recipes.ingredients.chemical.IGasIngredient;
import mekanism.api.recipes.ingredients.chemical.TagChemicalIngredient;
import mekanism.common.registries.MekanismGasIngredientTypes;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;

@NothingNullByDefault
public final class TagGasIngredient extends TagChemicalIngredient<Gas, IGasIngredient> implements IGasIngredient {

    public static final MapCodec<TagGasIngredient> CODEC = TagKey.codec(MekanismAPI.GAS_REGISTRY_NAME).xmap(TagGasIngredient::new, TagGasIngredient::tag)
          .fieldOf(JsonConstants.TAG);

    TagGasIngredient(TagKey<Gas> tag) {
        super(tag);
    }

    @Override
    public MapCodec<TagGasIngredient> codec() {
        return MekanismGasIngredientTypes.TAG.value();
    }

    @Override
    public Registry<Gas> registry() {
        return MekanismAPI.GAS_REGISTRY;
    }
}
