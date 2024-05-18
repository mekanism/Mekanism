package mekanism.common.recipe.ingredients.infusion;

import com.mojang.serialization.MapCodec;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.recipes.ingredients.chemical.IInfusionIngredient;
import mekanism.api.recipes.ingredients.chemical.TagChemicalIngredient;
import mekanism.common.registries.MekanismInfusionIngredientTypes;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;

@NothingNullByDefault
public final class TagInfusionIngredient extends TagChemicalIngredient<InfuseType, IInfusionIngredient> implements IInfusionIngredient {

    public static final MapCodec<TagInfusionIngredient> CODEC = codec(MekanismAPI.INFUSE_TYPE_REGISTRY_NAME, TagInfusionIngredient::new);

    TagInfusionIngredient(TagKey<InfuseType> tag) {
        super(tag);
    }

    @Override
    public MapCodec<TagInfusionIngredient> codec() {
        return MekanismInfusionIngredientTypes.TAG.value();
    }

    @Override
    protected Registry<InfuseType> registry() {
        return MekanismAPI.INFUSE_TYPE_REGISTRY;
    }
}
