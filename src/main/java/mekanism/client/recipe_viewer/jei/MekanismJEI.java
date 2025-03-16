package mekanism.client.recipe_viewer.jei;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.robit.GuiRobitRepair;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.alias.MekanismAliasMapping;
import mekanism.client.recipe_viewer.jei.machine.BoilerRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.ChemicalChemicalToChemicalRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.ChemicalCrystallizerRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.ChemicalDissolutionRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.ChemicalToChemicalRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.CombinerRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.ElectrolysisRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.FluidChemicalToChemicalRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.FluidToFluidRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.ItemStackChemicalToItemStackRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.ItemStackToChemicalRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.ItemStackToEnergyRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.ItemStackToFluidOptionalItemRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.ItemStackToItemStackRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.MetallurgicInfuserRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.NucleosynthesizingRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.PaintingRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.PigmentExtractingRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.PigmentMixerRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.PressurizedReactionRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.RotaryCondensentratorRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.SPSRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.SawmillRecipeCategory;
import mekanism.client.recipe_viewer.recipe.BoilerRecipeViewerRecipe;
import mekanism.client.recipe_viewer.recipe.SPSRecipeViewerRecipe;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.entity.robit.CraftingRobitContainer;
import mekanism.common.inventory.container.item.PortableQIODashboardContainer;
import mekanism.common.inventory.container.tile.QIODashboardContainer;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class MekanismJEI implements IModPlugin {

    public static final IIngredientType<ChemicalStack> TYPE_CHEMICAL = () -> ChemicalStack.class;

    public static final ChemicalStackHelper CHEMICAL_STACK_HELPER = new ChemicalStackHelper();
    private static final ISubtypeInterpreter<ItemStack> MEKANISM_DATA_INTERPRETER = new MekanismSubtypeInterpreter();
    private static final Map<IRecipeViewerRecipeType<?>, RecipeType<?>> recipeTypeInstanceCache = new HashMap<>();

    public static boolean shouldLoad() {
        //Skip handling if both EMI and JEI are loaded as otherwise some things behave strangely
        return !Mekanism.hooks.emi.isLoaded();
    }

    public static RecipeType<?> genericRecipeType(IRecipeViewerRecipeType<?> recipeType) {
        return recipeTypeInstanceCache.computeIfAbsent(recipeType, r -> {
            if (r.requiresHolder()) {
                return RecipeType.createRecipeHolderType(r.id());
            }
            return new RecipeType<>(r.id(), r.recipeClass());
        });
    }

    @SuppressWarnings("unchecked")
    public static <TYPE> RecipeType<TYPE> recipeType(IRecipeViewerRecipeType<TYPE> recipeType) {
        if (recipeType.requiresHolder()) {
            throw new IllegalStateException("Basic recipe type requested for a recipe that uses holders");
        }
        return (RecipeType<TYPE>) genericRecipeType(recipeType);
    }

    @SuppressWarnings("unchecked")
    public static <TYPE extends Recipe<?>> RecipeType<RecipeHolder<TYPE>> holderRecipeType(IRecipeViewerRecipeType<TYPE> recipeType) {
        if (!recipeType.requiresHolder()) {
            throw new IllegalStateException("Holder recipe type requested for a recipe that doesn't use holders");
        }
        return (RecipeType<RecipeHolder<TYPE>>) genericRecipeType(recipeType);
    }

    public static RecipeType<?>[] recipeType(IRecipeViewerRecipeType<?>... recipeTypes) {
        return Arrays.stream(recipeTypes).map(MekanismJEI::genericRecipeType).toArray(RecipeType[]::new);
    }

    @NotNull
    @Override
    public ResourceLocation getPluginUid() {
        //Note: Can't use Mekanism.rl, as JEI needs this in the constructor and the class may not be loaded yet.
        // we can still reference the modid though because of constant inlining
        return ResourceLocation.fromNamespaceAndPath(Mekanism.MODID, "jei_plugin");
    }

    public static void registerItemSubtypes(ISubtypeRegistration registry, Collection<? extends Holder<Item>> items) {
        for (Holder<Item> item : items) {
            //Handle items
            ItemStack stack = new ItemStack(item);
            if (Capabilities.STRICT_ENERGY.hasCapability(stack) || Capabilities.CHEMICAL.hasCapability(stack) || Capabilities.FLUID.hasCapability(stack)) {
                registry.registerSubtypeInterpreter(stack.getItem(), MEKANISM_DATA_INTERPRETER);
            }
        }
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registry) {
        if (shouldLoad()) {
            registerItemSubtypes(registry, MekanismItems.ITEMS.getEntries());
            registerItemSubtypes(registry, MekanismBlocks.BLOCKS.getSecondaryEntries());
        }
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {
        //Note: We register the ingredient types regardless of if EMI is loaded so that we don't crash any addons that are trying to reference them
        List<ChemicalStack> types = MekanismAPI.CHEMICAL_REGISTRY.holders()
              //Don't add the empty type. We will allow JEI to filter out any that are hidden from recipe viewers
              .filter(chemical -> !chemical.is(MekanismAPI.EMPTY_CHEMICAL_KEY))
              .map(chemical -> new ChemicalStack(chemical, FluidType.BUCKET_VOLUME))
              .toList();
        CHEMICAL_STACK_HELPER.setColorHelper(registry.getColorHelper());
        registry.register(TYPE_CHEMICAL, types, CHEMICAL_STACK_HELPER, new ChemicalStackRenderer(), Chemical.HOLDER_CODEC.xmap(
              chemical -> new ChemicalStack(chemical, FluidType.BUCKET_VOLUME),
              ChemicalStack::getChemicalHolder
        ));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        if (!shouldLoad()) {
            return;
        }
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();

        registry.addRecipeCategories(new ChemicalCrystallizerRecipeCategory(guiHelper, RecipeViewerRecipeType.CRYSTALLIZING));
        registry.addRecipeCategories(new ChemicalDissolutionRecipeCategory(guiHelper, RecipeViewerRecipeType.DISSOLUTION));
        registry.addRecipeCategories(new ChemicalChemicalToChemicalRecipeCategory(guiHelper, RecipeViewerRecipeType.CHEMICAL_INFUSING));
        registry.addRecipeCategories(new FluidChemicalToChemicalRecipeCategory(guiHelper, RecipeViewerRecipeType.WASHING));
        registry.addRecipeCategories(new ElectrolysisRecipeCategory(guiHelper, RecipeViewerRecipeType.SEPARATING));
        registry.addRecipeCategories(new MetallurgicInfuserRecipeCategory(guiHelper, RecipeViewerRecipeType.METALLURGIC_INFUSING));
        registry.addRecipeCategories(new PressurizedReactionRecipeCategory(guiHelper, RecipeViewerRecipeType.REACTION));
        registry.addRecipeCategories(new PigmentExtractingRecipeCategory(guiHelper, RecipeViewerRecipeType.PIGMENT_EXTRACTING));
        registry.addRecipeCategories(new PigmentMixerRecipeCategory(guiHelper, RecipeViewerRecipeType.PIGMENT_MIXING));
        registry.addRecipeCategories(new PaintingRecipeCategory(guiHelper, RecipeViewerRecipeType.PAINTING));

        //Register both methods of rotary condensentrator recipes
        registry.addRecipeCategories(new RotaryCondensentratorRecipeCategory(guiHelper, true));
        registry.addRecipeCategories(new RotaryCondensentratorRecipeCategory(guiHelper, false));

        registry.addRecipeCategories(new ItemStackToChemicalRecipeCategory<>(guiHelper, RecipeViewerRecipeType.OXIDIZING, false));
        registry.addRecipeCategories(new ItemStackToFluidOptionalItemRecipeCategory(guiHelper, RecipeViewerRecipeType.NUTRITIONAL_LIQUIFICATION, false));

        registry.addRecipeCategories(new ChemicalToChemicalRecipeCategory(guiHelper, RecipeViewerRecipeType.ACTIVATING));
        registry.addRecipeCategories(new ChemicalToChemicalRecipeCategory(guiHelper, RecipeViewerRecipeType.CENTRIFUGING));

        registry.addRecipeCategories(new CombinerRecipeCategory(guiHelper, RecipeViewerRecipeType.COMBINING));

        registry.addRecipeCategories(new ItemStackChemicalToItemStackRecipeCategory(guiHelper, RecipeViewerRecipeType.PURIFYING));
        registry.addRecipeCategories(new ItemStackChemicalToItemStackRecipeCategory(guiHelper, RecipeViewerRecipeType.COMPRESSING));
        registry.addRecipeCategories(new ItemStackChemicalToItemStackRecipeCategory(guiHelper, RecipeViewerRecipeType.INJECTING));

        registry.addRecipeCategories(new NucleosynthesizingRecipeCategory(guiHelper, RecipeViewerRecipeType.NUCLEOSYNTHESIZING));

        registry.addRecipeCategories(new SPSRecipeCategory(guiHelper, RecipeViewerRecipeType.SPS));
        registry.addRecipeCategories(new BoilerRecipeCategory(guiHelper, RecipeViewerRecipeType.BOILER));

        registry.addRecipeCategories(new SawmillRecipeCategory(guiHelper, RecipeViewerRecipeType.SAWING));

        registry.addRecipeCategories(new ItemStackToItemStackRecipeCategory(guiHelper, RecipeViewerRecipeType.ENRICHING));
        registry.addRecipeCategories(new ItemStackToItemStackRecipeCategory(guiHelper, RecipeViewerRecipeType.CRUSHING));
        registry.addRecipeCategories(new ItemStackToItemStackRecipeCategory(guiHelper, RecipeViewerRecipeType.SMELTING));

        registry.addRecipeCategories(new FluidToFluidRecipeCategory(guiHelper, RecipeViewerRecipeType.EVAPORATING));

        //Conversion recipes
        registry.addRecipeCategories(new ItemStackToEnergyRecipeCategory(guiHelper, RecipeViewerRecipeType.ENERGY_CONVERSION));
        registry.addRecipeCategories(new ItemStackToChemicalRecipeCategory<>(guiHelper, RecipeViewerRecipeType.CHEMICAL_CONVERSION, true));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registry) {
        if (!shouldLoad()) {
            return;
        }
        registry.addRecipeClickArea(GuiRobitRepair.class, 102, 48, 22, 15, RecipeTypes.ANVIL);
        registry.addGenericGuiContainerHandler(GuiMekanism.class, new JeiGuiElementHandler(registry.getJeiHelpers().getIngredientManager()));
        registry.addGhostIngredientHandler(GuiMekanism.class, new JeiGhostIngredientHandler<>());
    }

    @Override
    public void registerIngredientAliases(IIngredientAliasRegistration registration) {
        new MekanismAliasMapping().addAliases(new JEIAliasHelper(registration));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        if (!shouldLoad()) {
            return;
        }
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.SMELTING, MekanismRecipeType.SMELTING);
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.ENRICHING, MekanismRecipeType.ENRICHING);
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.CRUSHING, MekanismRecipeType.CRUSHING);
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.COMBINING, MekanismRecipeType.COMBINING);
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.PURIFYING, MekanismRecipeType.PURIFYING);
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.COMPRESSING, MekanismRecipeType.COMPRESSING);
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.INJECTING, MekanismRecipeType.INJECTING);
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.SAWING, MekanismRecipeType.SAWING);
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.METALLURGIC_INFUSING, MekanismRecipeType.METALLURGIC_INFUSING);
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.CRYSTALLIZING, MekanismRecipeType.CRYSTALLIZING);
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.DISSOLUTION, MekanismRecipeType.DISSOLUTION);
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.CHEMICAL_INFUSING, MekanismRecipeType.CHEMICAL_INFUSING);
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.OXIDIZING, MekanismRecipeType.OXIDIZING);
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.WASHING, MekanismRecipeType.WASHING);
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.ACTIVATING, MekanismRecipeType.ACTIVATING);
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.CENTRIFUGING, MekanismRecipeType.CENTRIFUGING);
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.SEPARATING, MekanismRecipeType.SEPARATING);
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.EVAPORATING, MekanismRecipeType.EVAPORATING);
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.REACTION, MekanismRecipeType.REACTION);
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.NUCLEOSYNTHESIZING, MekanismRecipeType.NUCLEOSYNTHESIZING);
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.PIGMENT_EXTRACTING, MekanismRecipeType.PIGMENT_EXTRACTING);
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.PIGMENT_MIXING, MekanismRecipeType.PIGMENT_MIXING);
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.PAINTING, MekanismRecipeType.PAINTING);
        RecipeRegistryHelper.registerCondensentrator(registry);
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.NUTRITIONAL_LIQUIFICATION, RecipeViewerUtils.getLiquificationRecipes());
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.SPS, SPSRecipeViewerRecipe.getSPSRecipes());
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.BOILER, BoilerRecipeViewerRecipe.getBoilerRecipes());
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.ENERGY_CONVERSION, MekanismRecipeType.ENERGY_CONVERSION);
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.CHEMICAL_CONVERSION, MekanismRecipeType.CHEMICAL_CONVERSION);
        RecipeRegistryHelper.addAnvilRecipes(registry, MekanismItems.HDPE_REINFORCED_ELYTRA, item -> new ItemStack[]{MekanismItems.HDPE_SHEET.asStack()});
        //Note: Use a "full" bucket's worth of heavy water, so that JEI renders it as desired in the info page
        registry.addIngredientInfo(MekanismFluids.HEAVY_WATER.asStack(FluidType.BUCKET_VOLUME), NeoForgeTypes.FLUID_STACK,
              MekanismLang.RECIPE_VIEWER_INFO_HEAVY_WATER.translate(MekanismConfig.general.pumpHeavyWaterAmount.get()));
        registry.addIngredientInfo(MekanismAPI.MODULE_REGISTRY.stream().map(data -> new ItemStack(data.getItemHolder())).toList(),
              VanillaTypes.ITEM_STACK, MekanismLang.RECIPE_VIEWER_INFO_MODULE_INSTALLATION.translate());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
        if (!shouldLoad()) {
            return;
        }
        //TODO: Eventually we may want to look into trying to make output definitions be invisibly added to categories, and then
        // have the output get calculated in draw, except it would also need to override getTooltip related stuff which won't be
        // super straightforward.

        CatalystRegistryHelper.register(registry, RecipeViewerRecipeType.ENRICHING, RecipeViewerRecipeType.CRUSHING, RecipeViewerRecipeType.COMBINING,
              RecipeViewerRecipeType.PURIFYING, RecipeViewerRecipeType.COMPRESSING, RecipeViewerRecipeType.INJECTING, RecipeViewerRecipeType.SAWING,
              RecipeViewerRecipeType.METALLURGIC_INFUSING, RecipeViewerRecipeType.CRYSTALLIZING, RecipeViewerRecipeType.DISSOLUTION, RecipeViewerRecipeType.CHEMICAL_INFUSING,
              RecipeViewerRecipeType.OXIDIZING, RecipeViewerRecipeType.WASHING, RecipeViewerRecipeType.ACTIVATING, RecipeViewerRecipeType.SEPARATING, RecipeViewerRecipeType.SPS,
              RecipeViewerRecipeType.EVAPORATING, RecipeViewerRecipeType.BOILER, RecipeViewerRecipeType.REACTION, RecipeViewerRecipeType.CENTRIFUGING,
              RecipeViewerRecipeType.NUTRITIONAL_LIQUIFICATION, RecipeViewerRecipeType.PIGMENT_EXTRACTING, RecipeViewerRecipeType.PIGMENT_MIXING,
              RecipeViewerRecipeType.PAINTING, RecipeViewerRecipeType.NUCLEOSYNTHESIZING, RecipeViewerRecipeType.CONDENSENTRATING, RecipeViewerRecipeType.DECONDENSENTRATING,
              RecipeViewerRecipeType.SMELTING, RecipeViewerRecipeType.ENERGY_CONVERSION, RecipeViewerRecipeType.CHEMICAL_CONVERSION);

        CatalystRegistryHelper.register(registry, RecipeTypes.SMELTING, RecipeViewerRecipeType.VANILLA_SMELTING.workstations());
        CatalystRegistryHelper.register(registry, RecipeTypes.CRAFTING, RecipeViewerRecipeType.VANILLA_CRAFTING.workstations());
        CatalystRegistryHelper.register(registry, RecipeTypes.ANVIL, List.of(MekanismItems.ROBIT));
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registry) {
        if (!shouldLoad()) {
            return;
        }
        IRecipeTransferHandlerHelper transferHelper = registry.getTransferHelper();
        IStackHelper stackHelper = registry.getJeiHelpers().getStackHelper();
        registry.addRecipeTransferHandler(CraftingRobitContainer.class, MekanismContainerTypes.CRAFTING_ROBIT.get(), RecipeTypes.CRAFTING, 1, 9, 10, 36);
        registry.addRecipeTransferHandler(new FormulaicRecipeTransferInfo());
        registry.addRecipeTransferHandler(new JeiQIOCraftingTransferHandler<>(transferHelper, stackHelper, MekanismContainerTypes.QIO_DASHBOARD.get(), QIODashboardContainer.class), RecipeTypes.CRAFTING);
        registry.addRecipeTransferHandler(new JeiQIOCraftingTransferHandler<>(transferHelper, stackHelper, MekanismContainerTypes.PORTABLE_QIO_DASHBOARD.get(), PortableQIODashboardContainer.class), RecipeTypes.CRAFTING);
    }
}