package mekanism.common.recipe.ingredients.gas;

import com.mojang.serialization.MapCodec;
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

    public static final MapCodec<TagGasIngredient> CODEC = codec(MekanismAPI.GAS_REGISTRY_NAME, TagGasIngredient::new);

    TagGasIngredient(TagKey<Gas> tag) {
        super(tag);
    }

    @Override
    public MapCodec<TagGasIngredient> codec() {
        return MekanismGasIngredientTypes.TAG.value();
    }

    @Override
    protected Registry<Gas> registry() {
        return MekanismAPI.GAS_REGISTRY;
    }
}
