package mekanism.client.recipe_viewer.jei;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismAPITags;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.RegistryUtils;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

public class ChemicalStackHelper implements IIngredientHelper<ChemicalStack> {

    @Nullable
    private IColorHelper colorHelper;

    void setColorHelper(IColorHelper colorHelper) {
        this.colorHelper = colorHelper;
    }

    @Override
    public String getDisplayName(ChemicalStack ingredient) {
        return TextComponentUtil.build(ingredient).getString();
    }

    @Override
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "JEI version 19.9.0")
    public String getUniqueId(ChemicalStack ingredient, UidContext context) {
        return "chemical:" + ingredient.getChemical();
    }

    @Override
    public Object getUid(ChemicalStack ingredient, UidContext context) {
        //Note: We just return the registry element itself as we have no component data
        return ingredient.getChemical();
    }

    @Override
    public ResourceLocation getResourceLocation(ChemicalStack ingredient) {
        return RegistryUtils.getName(ingredient.getChemicalHolder(), MekanismAPI.CHEMICAL_REGISTRY);
    }

    @Override
    public ItemStack getCheatItemStack(ChemicalStack ingredient) {
        return ChemicalUtil.getFilledVariant(MekanismBlocks.CREATIVE_CHEMICAL_TANK.getItemHolder(), ingredient.getChemicalHolder());
    }

    @Override
    public ChemicalStack normalizeIngredient(ChemicalStack ingredient) {
        return ingredient.copyWithAmount(FluidType.BUCKET_VOLUME);
    }

    @Override
    public boolean isValidIngredient(ChemicalStack ingredient) {
        return !ingredient.isEmpty();
    }

    @Override
    public Iterable<Integer> getColors(ChemicalStack ingredient) {
        if (colorHelper == null) {
            return IIngredientHelper.super.getColors(ingredient);
        }
        return colorHelper.getColors(MekanismRenderer.getChemicalTexture(ingredient.getChemicalHolder()), ingredient.getChemicalTint(), 1);
    }

    @Override
    public IIngredientType<ChemicalStack> getIngredientType() {
        return MekanismJEI.TYPE_CHEMICAL;
    }

    @Override
    public ChemicalStack copyIngredient(ChemicalStack ingredient) {
        return ingredient.copy();
    }

    @Override
    public Stream<ResourceLocation> getTagStream(ChemicalStack ingredient) {
        return ingredient.getTags().map(TagKey::location);
    }

    @Override
    public boolean isHiddenFromRecipeViewersByTags(ChemicalStack ingredient) {
        return ingredient.is(MekanismAPITags.Chemicals.HIDDEN_FROM_RECIPE_VIEWERS);
    }

    @Override
    public Optional<TagKey<?>> getTagKeyEquivalent(Collection<ChemicalStack> stacks) {
        if (stacks.size() < 2) {
            return Optional.empty();
        }
        List<Holder<Chemical>> values = stacks.stream()
              .map(ChemicalStack::getChemicalHolder)
              .distinct()
              .toList();
        int expected = values.size();
        if (expected != stacks.size()) {
            //One of the chemicals is there more than once, definitely not a tag
            return Optional.empty();
        }
        for (TagKey<Chemical> tagKey : values.getFirst().tags().toList()) {
            Optional<Named<Chemical>> optionalTag = MekanismAPI.CHEMICAL_REGISTRY.getTag(tagKey);
            if (optionalTag.isPresent()) {
                Named<Chemical> tag = optionalTag.get();
                if (tag.size() == expected && values.stream().allMatch(tag::contains)) {
                    return Optional.of(tagKey);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public String getErrorInfo(@Nullable ChemicalStack ingredient) {
        if (ingredient == null) {
            ingredient = ChemicalStack.EMPTY;
        }
        ToStringHelper toStringHelper = MoreObjects.toStringHelper(ChemicalStack.class);
        Holder<Chemical> chemical = ingredient.getChemicalHolder();
        toStringHelper.add("Chemical", chemical.is(MekanismAPI.EMPTY_CHEMICAL_KEY) ? "none" : TextComponentUtil.build(chemical).getString());
        if (!ingredient.isEmpty()) {
            toStringHelper.add("Amount", ingredient.getAmount());
        }
        return toStringHelper.toString();
    }
}