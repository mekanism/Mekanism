package mekanism.client.recipe_viewer.jei;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import mekanism.api.MekanismAPITags;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalStackContentsFactory;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.registries.MekanismBlocks;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.neoforged.neoforge.fluids.FluidType;
import org.jspecify.annotations.Nullable;

public class ChemicalStackHelper implements IIngredientHelper<ChemicalStack> {

    private final IColorHelper colorHelper;
    private final HolderLookup.RegistryLookup<Chemical> chemicalLookup;

    public ChemicalStackHelper(IColorHelper colorHelper, HolderLookup.RegistryLookup<Chemical> chemicalLookup) {
        this.colorHelper = colorHelper;
        this.chemicalLookup = chemicalLookup;
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
        ResourceKey<Chemical> key = ingredient.typeHolder().getKey();
        if (key != null) {
            return key.identifier();
        } else if (chemicalLookup instanceof Registry<Chemical> registry) {
            //Simple attempt to try and figure out the id for a direct holder. In general we never should get here
            // so we don't bother with trying to unwrap any delegating registry lookups
            Identifier identifier = registry.getKey(ingredient.getChemical());
            if (identifier != null) {
                return identifier;
            }
        }
        return ChemicalIds.EMPTY.identifier();
    }

    @Override
    public ItemStack getCheatItemStack(ChemicalStack ingredient) {
        return ContainerType.CHEMICAL.getFilledVariant(MekanismBlocks.CREATIVE_CHEMICAL_TANK.getItemHolder(), ingredient.typeHolder(), null);
    }

    @Override
    public ChemicalStack normalizeIngredient(ChemicalStack ingredient) {
        return copyWithAmount(ingredient, FluidType.BUCKET_VOLUME);
    }

    @Override
    public boolean isValidIngredient(ChemicalStack ingredient) {
        return !ingredient.isEmpty();
    }

    @Override
    public Iterable<Integer> getColors(ChemicalStack ingredient) {
        return colorHelper.getColors(MekanismRenderer.getChemicalTexture(ingredient.typeHolder()), ingredient.getChemical().tint(), 1);
    }

    @Override
    public IIngredientType<ChemicalStack> getIngredientType() {
        return MekanismJEI.TYPE_CHEMICAL;
    }

    @Override
    public long getAmount(ChemicalStack ingredient) {
        return ingredient.amount();
    }

    @Override
    public ChemicalStack copyWithAmount(ChemicalStack ingredient, long amount) {
        if (ingredient.isEmpty() || amount <= 0) {
            return ChemicalStack.EMPTY;
        }
        int intAmount = Math.toIntExact(amount);
        return ingredient.copyWithAmount(intAmount);
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
        for (TagKey<Chemical> tagKey : values.getFirst().tags().toList()) {
            Optional<Named<Chemical>> optionalTag = chemicalLookup.get(tagKey);
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
    public Optional<DisplayContentsFactory<ChemicalStack>> getDisplayContentsFactory() {
        return Optional.of(ChemicalStackContentsFactory.INSTANCE);
    }

    @Override
    public String getErrorInfo(@Nullable ChemicalStack ingredient) {
        if (ingredient == null) {
            return "null";
        }
        ToStringHelper toStringHelper = MoreObjects.toStringHelper(ingredient.getClass());
        toStringHelper.add("Chemical", ingredient.isEmpty() ? "none" : TextComponentUtil.build(ingredient).getString());
        if (!ingredient.isEmpty()) {
            toStringHelper.add("Amount", ingredient.amount());
        }
        return toStringHelper.toString();
    }
}