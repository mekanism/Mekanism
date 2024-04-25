package mekanism.client.recipe_viewer.jei;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IEmptyStackProvider;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IEmptyGasProvider;
import mekanism.api.chemical.infuse.IEmptyInfusionProvider;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.IEmptyPigmentProvider;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.IEmptySlurryProvider;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.ChemicalUtil;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

public abstract class ChemicalStackHelper<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> implements IIngredientHelper<STACK>,
      IEmptyStackProvider<CHEMICAL, STACK> {

    @Nullable
    private IColorHelper colorHelper;

    void setColorHelper(IColorHelper colorHelper) {
        this.colorHelper = colorHelper;
    }

    protected abstract String getType();

    @Override
    public String getDisplayName(STACK ingredient) {
        return TextComponentUtil.build(ingredient).getString();
    }

    @Override
    public String getUniqueId(STACK ingredient, UidContext context) {
        return getType().toLowerCase(Locale.ROOT) + ":" + ingredient.getTypeRegistryName();
    }

    @Override
    public ResourceLocation getResourceLocation(STACK ingredient) {
        return ingredient.getTypeRegistryName();
    }

    @Override
    public ItemStack getCheatItemStack(STACK ingredient) {
        return ChemicalUtil.getFilledVariant(MekanismBlocks.CREATIVE_CHEMICAL_TANK.getItemStack(), ingredient.getChemical());
    }

    @Override
    public STACK normalizeIngredient(STACK ingredient) {
        return ChemicalUtil.copyWithAmount(ingredient, FluidType.BUCKET_VOLUME);
    }

    @Override
    public boolean isValidIngredient(STACK ingredient) {
        return !ingredient.isEmpty();
    }

    @Override
    public Iterable<Integer> getColors(STACK ingredient) {
        if (colorHelper == null) {
            return IIngredientHelper.super.getColors(ingredient);
        }
        CHEMICAL chemical = ingredient.getChemical();
        return colorHelper.getColors(MekanismRenderer.getChemicalTexture(chemical), chemical.getTint(), 1);
    }

    @Override
    public STACK copyIngredient(STACK ingredient) {
        return ChemicalUtil.copy(ingredient);
    }

    @Override
    public Stream<ResourceLocation> getTagStream(STACK ingredient) {
        return ingredient.getChemical().getTags().map(TagKey::location);
    }

    protected abstract Registry<CHEMICAL> getRegistry();

    @Override
    public Optional<ResourceLocation> getTagEquivalent(Collection<STACK> stacks) {
        if (stacks.size() < 2) {
            return Optional.empty();
        }
        Set<CHEMICAL> values = stacks.stream()
              .map(ChemicalStack::getChemical)
              .collect(Collectors.toSet());
        int expected = values.size();
        if (expected != stacks.size()) {
            //One of the chemicals is there more than once, definitely not a tag
            return Optional.empty();
        }
        return getRegistry().getTags()
              .filter(pair -> {
                  Named<CHEMICAL> tag = pair.getSecond();
                  return tag.size() == expected && tag.stream().allMatch(tag::contains);
              }).map(pair -> pair.getFirst().location())
              .findFirst();
    }

    @Override
    public String getErrorInfo(@Nullable STACK ingredient) {
        if (ingredient == null) {
            ingredient = getEmptyStack();
        }
        ToStringHelper toStringHelper = MoreObjects.toStringHelper(GasStack.class);
        CHEMICAL chemical = ingredient.getChemical();
        toStringHelper.add(getType(), chemical.isEmptyType() ? "none" : TextComponentUtil.build(chemical).getString());
        if (!ingredient.isEmpty()) {
            toStringHelper.add("Amount", ingredient.getAmount());
        }
        return toStringHelper.toString();
    }

    public static class GasStackHelper extends ChemicalStackHelper<Gas, GasStack> implements IEmptyGasProvider {

        @Override
        protected String getType() {
            return "Gas";
        }

        @Override
        protected Registry<Gas> getRegistry() {
            return MekanismAPI.GAS_REGISTRY;
        }

        @Override
        public IIngredientType<GasStack> getIngredientType() {
            return MekanismJEI.TYPE_GAS;
        }
    }

    public static class InfusionStackHelper extends ChemicalStackHelper<InfuseType, InfusionStack> implements IEmptyInfusionProvider {

        @Override
        protected String getType() {
            return "Infuse Type";
        }

        @Override
        protected Registry<InfuseType> getRegistry() {
            return MekanismAPI.INFUSE_TYPE_REGISTRY;
        }

        @Override
        public IIngredientType<InfusionStack> getIngredientType() {
            return MekanismJEI.TYPE_INFUSION;
        }
    }

    public static class PigmentStackHelper extends ChemicalStackHelper<Pigment, PigmentStack> implements IEmptyPigmentProvider {

        @Override
        protected Registry<Pigment> getRegistry() {
            return MekanismAPI.PIGMENT_REGISTRY;
        }

        @Override
        protected String getType() {
            return "Pigment";
        }

        @Override
        public IIngredientType<PigmentStack> getIngredientType() {
            return MekanismJEI.TYPE_PIGMENT;
        }
    }

    public static class SlurryStackHelper extends ChemicalStackHelper<Slurry, SlurryStack> implements IEmptySlurryProvider {

        @Override
        protected Registry<Slurry> getRegistry() {
            return MekanismAPI.SLURRY_REGISTRY;
        }

        @Override
        protected String getType() {
            return "Slurry";
        }

        @Override
        public IIngredientType<SlurryStack> getIngredientType() {
            return MekanismJEI.TYPE_SLURRY;
        }
    }
}