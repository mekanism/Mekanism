package mekanism.client.recipe_viewer;

import java.util.ArrayList;
import java.util.Collection;
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
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.chemical.ChemicalSolidTag;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.api.recipes.basic.BasicItemStackToFluidOptionalItemRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.client.MekanismClient;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.progress.IProgressInfoHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tile.machine.TileEntityNutritionalLiquifier;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class RecipeViewerUtils {

    private RecipeViewerUtils() {
    }

    public static final IProgressInfoHandler CONSTANT_PROGRESS = () -> 1;
    public static final IBarInfoHandler FULL_BAR = () -> 1;

    public static IProgressInfoHandler progressHandler(int processTime) {
        int time = SharedConstants.MILLIS_PER_TICK * processTime;
        return () -> {
            double subTime = System.currentTimeMillis() % time;
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
                double subTime = System.currentTimeMillis() % time;
                return subTime / time;
            }
        };
    }

    public static ResourceLocation synthetic(ResourceLocation id, String prefix, String namespace) {
        return synthetic(ResourceLocation.fromNamespaceAndPath(namespace, id.toString().replace(':', '_')), prefix);
    }

    public static ResourceLocation synthetic(String id, String prefix, String namespace) {
        if (id.equals("[unregistered]")) {
            return synthetic(ResourceLocation.fromNamespaceAndPath(namespace, "_unregistered_sad_face_"), prefix);
        }
        return synthetic(ResourceLocation.fromNamespaceAndPath(namespace, id.replace(':', '_')), prefix);
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

    public static long getCurrent(long[] elements) {
        return elements[getIndex(elements)];
    }

    public static <T> int getIndex(long[] elements) {
        return (int) (System.currentTimeMillis() / TimeUtil.MILLISECONDS_PER_SECOND % elements.length);
    }

    public static List<ItemStack> getStacksFor(ChemicalStackIngredient ingredient, boolean displayConversions) {
        Set<Holder<Chemical>> chemicals = ingredient.getRepresentations().stream().map(ChemicalStack::getChemicalHolder).collect(Collectors.toSet());
        return getStacksFor(chemicals, displayConversions ? MekanismRecipeType.CHEMICAL_CONVERSION : null);
    }

    private static List<ItemStack> getStacksFor(Set<Holder<Chemical>> supportedTypes, @Nullable IMekanismRecipeTypeProvider<?, ? extends ItemStackToChemicalRecipe, ?> recipeType) {
        List<ItemStack> stacks = new ArrayList<>();
        //Always include the chemical tank of the type to portray that we accept items
        for (Holder<Chemical> type : supportedTypes) {
            stacks.add(ChemicalUtil.getFullChemicalTank(ChemicalTankTier.BASIC, type));
        }
        //See if there are any chemical to item mappings
        if (recipeType != null) {
            for (RecipeHolder<? extends ItemStackToChemicalRecipe> recipeHolder : recipeType.getRecipes()) {
                ItemStackToChemicalRecipe recipe = recipeHolder.value();
                for (ChemicalStack output : recipe.getOutputDefinition()) {
                    if (anyMatch(supportedTypes, output.getChemicalHolder())) {
                        stacks.addAll(recipe.getInput().getRepresentations());
                        break;
                    }
                }
            }
        }
        return stacks;
    }

    private static <T> boolean anyMatch(Collection<Holder<T>> holders, Holder<T> holder) {
        for (Holder<T> toCheck : holders) {
            //noinspection deprecation
            if (toCheck.is(holder)) {
                return true;
            }
        }
        return false;
    }

    public static Map<ResourceLocation, BasicItemStackToFluidOptionalItemRecipe> getLiquificationRecipes() {
        Map<ResourceLocation, BasicItemStackToFluidOptionalItemRecipe> liquification = new HashMap<>();
        //TODO: Do we want to loop creative tabs or something instead?
        // In theory recipe loaders should init the creative tabs before we are called so we wouldn't need to call
        // CreativeModeTab#buildContents, and in theory we only need to care about things in search so could use:
        // CreativeModeTabs.searchTab().getDisplayItems(). The bigger issue is how to come up with unique synthetic
        // names for the recipes as EMI requires they be unique. (Maybe index them?)
        for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
            BasicItemStackToFluidOptionalItemRecipe recipe = TileEntityNutritionalLiquifier.getRecipe(entry.getValue().getDefaultInstance());
            if (recipe != null) {
                liquification.put(synthetic(entry.getKey().location(), "liquification", Mekanism.MODID), recipe);
            }
        }
        return liquification;
    }

    public static List<ItemStack> getDisplayItems(ChemicalStackIngredient ingredient) {
        SequencedSet<Named<Item>> tags = new LinkedHashSet<>();
        for (ChemicalStack chemicalStack : ingredient.getRepresentations()) {
            ChemicalSolidTag tag = chemicalStack.getData(IMekanismDataMapTypes.INSTANCE.chemicalSolidTag());
            if (tag != null) {
                tag.lookupTag().ifPresent(tags::add);
            }
            //TODO - 1.22: Remove this legacy branch
            else if (!chemicalStack.is(MekanismAPITags.Chemicals.DIRTY)) {
                @SuppressWarnings("removal") TagKey<Item> oreTag = chemicalStack.getChemical().getOreTag();
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

    public static TooltipContext getRVTooltipContext() {
        //Similar to how ItemEmiStack works
        Level level = MekanismClient.tryGetClientWorld();
        if (level == null) {
            return TooltipContext.EMPTY;
        } else if (Minecraft.getInstance().isSameThread()) {
            return Item.TooltipContext.of(level);
        }
        // Don't provide world as context, as it is not thread safe
        return Item.TooltipContext.of(level.registryAccess());
    }
}