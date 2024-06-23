package mekanism.client.recipe_viewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedSet;
import java.util.Set;
import java.util.stream.Collectors;
import mekanism.api.MekanismAPITags;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.ItemStackToFluidRecipe;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.SlurryStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.progress.IProgressInfoHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.NutritionalLiquifierIRecipe;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.RegistryUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

public class RecipeViewerUtils {

    private RecipeViewerUtils() {
    }

    public static final IProgressInfoHandler CONSTANT_PROGRESS = () -> 1;
    public static final IBarInfoHandler FULL_BAR = () -> 1;

    public static IProgressInfoHandler progressHandler(int processTime) {
        int time = SharedConstants.MILLIS_PER_TICK * processTime;
        return () -> {
            double subTime = System.currentTimeMillis() % (long) time;
            return subTime / time;
        };
    }

    public static IBarInfoHandler barProgressHandler(int processTime) {
        Component tooltip = MekanismLang.TICKS_REQUIRED.translate(processTime);
        int time = SharedConstants.MILLIS_PER_TICK * processTime;
        return new IBarInfoHandler() {
            @Override
            public Component getTooltip() {
                return tooltip;
            }

            @Override
            public double getLevel() {
                double subTime = System.currentTimeMillis() % (long) time;
                return subTime / time;
            }
        };
    }

    public static ResourceLocation synthetic(ResourceLocation id, String prefix, String namespace) {
        return synthetic(ResourceLocation.fromNamespaceAndPath(namespace, id.toString().replace(':', '_')), prefix);
    }

    public static ResourceLocation synthetic(ResourceLocation id, String prefix) {
        return id.withPrefix("/" + prefix + "/");
    }

    public static <T> T getCurrent(List<T> elements) {
        return elements.get(getIndex(elements));
    }

    public static int getIndex(List<?> elements) {
        return (int) (System.currentTimeMillis() / TimeUtil.MILLISECONDS_PER_SECOND % elements.size());
    }

    @SuppressWarnings("unchecked")
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> List<ItemStack> getStacksFor(
          ChemicalStackIngredient<CHEMICAL, STACK, ?> ingredient, boolean displayConversions) {
        Set<CHEMICAL> chemicals = ingredient.getRepresentations().stream().map(ChemicalStack::getChemical).collect(Collectors.toSet());
        if (!displayConversions) {
            return getStacksFor(chemicals, null);
        }
        ChemicalType chemicalType = ChemicalType.getTypeFor(ingredient);
        return getStacksFor(chemicals, (IMekanismRecipeTypeProvider<?, ? extends ItemStackToChemicalRecipe<CHEMICAL, ?>, ?>)  switch (chemicalType) {
            case GAS -> MekanismRecipeType.GAS_CONVERSION;
            case INFUSION -> MekanismRecipeType.INFUSION_CONVERSION;
            default -> null;
        });
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>> List<ItemStack> getStacksFor(Set<CHEMICAL> supportedTypes,
          @Nullable IMekanismRecipeTypeProvider<?, ? extends ItemStackToChemicalRecipe<CHEMICAL, ?>, ?> recipeType) {
        List<ItemStack> stacks = new ArrayList<>();
        //Always include the chemical tank of the type to portray that we accept items
        for (CHEMICAL type : supportedTypes) {
            stacks.add(ChemicalUtil.getFullChemicalTank(ChemicalTankTier.BASIC, type));
        }
        //See if there are any chemical to item mappings
        if (recipeType != null) {
            for (RecipeHolder<? extends ItemStackToChemicalRecipe<CHEMICAL, ?>> recipeHolder : recipeType.getRecipes(null)) {
                ItemStackToChemicalRecipe<CHEMICAL, ?> recipe = recipeHolder.value();
                for (ChemicalStack<CHEMICAL> output : recipe.getOutputDefinition()) {
                    if (supportedTypes.contains(output.getChemical())) {
                        stacks.addAll(recipe.getInput().getRepresentations());
                        break;
                    }
                }
            }
        }
        return stacks;
    }

    public static Map<ResourceLocation, ItemStackToFluidRecipe> getLiquificationRecipes() {
        Map<ResourceLocation, ItemStackToFluidRecipe> liquification = new HashMap<>();
        //TODO: Do we want to loop creative tabs or something instead?
        // In theory recipe loaders should init the creative tabs before we are called so we wouldn't need to call
        // CreativeModeTab#buildContents, and in theory we only need to care about things in search so could use:
        // CreativeModeTabs.searchTab().getDisplayItems(). The bigger issue is how to come up with unique synthetic
        // names for the recipes as EMI requires they be unique. (Maybe index them?)
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = new ItemStack(item);
            if (stack.has(DataComponents.FOOD)) {
                FoodProperties food = stack.getFoodProperties(null);
                //Only display consuming foods that provide healing as otherwise no paste will be made
                if (food != null && food.nutrition() > 0) {
                    ResourceLocation id = RecipeViewerUtils.synthetic(RegistryUtils.getName(stack.getItem()), "liquification", Mekanism.MODID);
                    liquification.put(id, new NutritionalLiquifierIRecipe(IngredientCreatorAccess.item().from(stack), MekanismFluids.NUTRITIONAL_PASTE.getFluidStack(food.nutrition() * 50)));
                }
            }
        }
        return liquification;
    }

    public static List<ItemStack> getDisplayItems(SlurryStackIngredient ingredient) {
        SequencedSet<Named<Item>> tags = new LinkedHashSet<>();
        for (SlurryStack slurryStack : ingredient.getRepresentations()) {
            Slurry slurry = slurryStack.getChemical();
            if (!slurry.is(MekanismAPITags.Slurries.DIRTY)) {
                TagKey<Item> oreTag = slurry.getOreTag();
                if (oreTag != null) {
                    BuiltInRegistries.ITEM.getTag(oreTag).ifPresent(tags::add);
                }
            }
        }
        if (tags.size() == 1) {
            //TODO: Eventually come up with a better way to do this to allow for if there outputs based on the input and multiple input types
            return tags.getFirst().stream().map(ItemStack::new).toList();
        }
        return Collections.emptyList();
    }
}