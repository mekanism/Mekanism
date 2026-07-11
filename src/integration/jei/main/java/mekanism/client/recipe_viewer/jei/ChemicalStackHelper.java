package mekanism.client.recipe_viewer.jei;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import mekanism.api.MekanismAPITags;
import mekanism.api.MekanismRegistries;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.registries.MekanismBlocks;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jspecify.annotations.Nullable;

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
    public Object getUid(ChemicalStack ingredient, UidContext context) {
        //Note: We just return the registry element itself as we have no component data
        return ingredient.getChemical();
    }

    @Override
    public Identifier getIdentifier(ChemicalStack ingredient) {
        Optional<ResourceKey<Chemical>> key = ingredient.typeHolder().unwrapKey();
        if (key.isPresent()) {
            return key.get().identifier();
        }
        Optional<Registry<Chemical>> optionalRegistry = RecipeViewerUtils.getRegistry(MekanismRegistries.Keys.CHEMICAL);
        if (optionalRegistry.isEmpty()) {
            //Something went horribly wrong, bail
            Mekanism.logger.warn("Failed to find chemical registry when looking up ingredient id");
            return ChemicalIds.EMPTY.identifier();
        }
        Identifier identifier = optionalRegistry.get().getKey(ingredient.getChemical());
        return identifier == null ? ChemicalIds.EMPTY.identifier() : identifier;
    }

    @Override
    public ItemStack getCheatItemStack(ChemicalStack ingredient) {
        return ContainerType.CHEMICAL.getFilledVariant(MekanismBlocks.CREATIVE_CHEMICAL_TANK.getItemHolder(), ingredient.typeHolder(), null);
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
        return colorHelper.getColors(MekanismRenderer.getChemicalTexture(ingredient.typeHolder()), ingredient.getChemical().tint(), 1);
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
    public Stream<Identifier> getTagStream(ChemicalStack ingredient) {
        return ingredient.tags().map(TagKey::location);
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
              .map(ChemicalStack::typeHolder)
              .distinct()
              .toList();
        int expected = values.size();
        if (expected != stacks.size()) {
            //One of the chemicals is there more than once, definitely not a tag
            return Optional.empty();
        }
        Optional<Registry<Chemical>> optionalRegistry = RecipeViewerUtils.getRegistry(MekanismRegistries.Keys.CHEMICAL);
        if (optionalRegistry.isEmpty()) {
            //Something went horribly wrong, bail
            Mekanism.logger.warn("Failed to find chemical registry when calculating tag key equivalents");
            return Optional.empty();
        }
        Registry<Chemical> registry = optionalRegistry.get();
        for (TagKey<Chemical> tagKey : values.getFirst().tags().toList()) {
            Optional<Named<Chemical>> optionalTag = registry.get(tagKey);
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
        toStringHelper.add("Chemical", ingredient.isEmpty() ? "none" : TextComponentUtil.build(ingredient).getString());
        if (!ingredient.isEmpty()) {
            toStringHelper.add("Amount", ingredient.amount());
        }
        return toStringHelper.toString();
    }
}