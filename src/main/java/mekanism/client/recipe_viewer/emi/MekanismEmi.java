package mekanism.client.recipe_viewer.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiInitRegistry;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiRegistryAdapter;
import dev.emi.emi.api.stack.EmiStack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.gear.ModuleData;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.recipe.BoilerEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.ChemicalChemicalToChemicalEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.ChemicalCrystallizerEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.ChemicalDissolutionEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.ChemicalToChemicalEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.CombinerEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.ElectrolysisEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.FluidChemicalToChemicalEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.FluidToFluidEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.ItemStackChemicalToItemStackEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.ItemStackToChemicalEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.ItemStackToEnergyEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.ItemStackToFluidOptionalItemEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.ItemStackToItemStackEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.MekanismEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.MetallurgicInfuserEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.NucleosynthesizingEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.PaintingEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.PigmentExtractingEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.PigmentMixerEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.PressurizedReactionEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.RotaryEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.SPSEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.SawmillEmiRecipe;
import mekanism.client.recipe_viewer.emi.transfer.EmiQIOCraftingTransferHandler;
import mekanism.client.recipe_viewer.emi.transfer.FormulaicAssemblicatorTransferHandler;
import mekanism.client.recipe_viewer.recipe.BoilerRecipeViewerRecipe;
import mekanism.client.recipe_viewer.recipe.SPSRecipeViewerRecipe;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.machine.TileEntityChemicalOxidizer;
import mekanism.common.tile.machine.TileEntityNutritionalLiquifier;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

@EmiEntrypoint
public class MekanismEmi implements EmiPlugin {

    private static final ChemicalEmiIngredientSerializer CHEMICAL_SERIALIZER = new ChemicalEmiIngredientSerializer();
    private static final EmiRegistryAdapter<Chemical> CHEMICAL_REGISTRY_ADAPTER = EmiRegistryAdapter.simple(Chemical.class, MekanismAPI.CHEMICAL_REGISTRY, ChemicalEmiStack::new);

    private static final Comparison MEKANISM_COMPARISON = Comparison.compareData(emiStack -> {
        Set<Object> representation = new HashSet<>();
        ItemStack stack = emiStack.getItemStack();
        addChemicalComponent(representation, stack);
        addFluidComponent(representation, stack);
        addEnergyComponent(representation, stack);
        if (!representation.isEmpty()) {
            return representation;
        }
        return null;
    });

    private static void addChemicalComponent(Set<Object> representation, ItemStack stack) {
        IChemicalHandler handler = ContainerType.CHEMICAL.createHandlerIfData(stack);
        if (handler == null) {
            handler = stack.getCapability(Capabilities.CHEMICAL.item());
        }
        if (handler != null) {
            int tanks = handler.getChemicalTanks();
            if (tanks == 1) {
                ChemicalStack chemicalStack = handler.getChemicalInTank(0);
                if (!chemicalStack.isEmpty()) {
                    representation.add(chemicalStack.getChemical());
                }
            } else if (tanks > 1) {
                List<Chemical> chemicals = new ArrayList<>(tanks);
                for (int tank = 0; tank < tanks; tank++) {
                    chemicals.add(handler.getChemicalInTank(tank).getChemical());
                }
                representation.add(chemicals);
            }
        }
    }

    private static void addFluidComponent(Set<Object> representation, ItemStack stack) {
        IFluidHandler handler = ContainerType.FLUID.createHandlerIfData(stack);
        if (handler == null) {
            handler = Capabilities.FLUID.getCapability(stack);
        }
        if (handler != null) {
            int tanks = handler.getTanks();
            if (tanks == 1) {
                FluidStack fluidStack = handler.getFluidInTank(0);
                if (!fluidStack.isEmpty()) {
                    //Equals and hashcode ignore the count, so we can just add the fluid stack
                    representation.add(fluidStack);
                }
            } else if (tanks > 1) {
                List<FluidStack> fluids = new ArrayList<>(tanks);
                for (int tank = 0; tank < tanks; tank++) {
                    //Equals and hashcode ignore the count, so we can just add the fluid stack
                    fluids.add(handler.getFluidInTank(tank));
                }
                representation.add(fluids);
            }
        }
    }

    private static void addEnergyComponent(Set<Object> representation, ItemStack stack) {
        IStrictEnergyHandler energyHandlerItem = ContainerType.ENERGY.createHandlerIfData(stack);
        if (energyHandlerItem == null) {
            energyHandlerItem = Capabilities.STRICT_ENERGY.getCapability(stack);
        }
        if (energyHandlerItem != null) {
            int containers = energyHandlerItem.getEnergyContainerCount();
            if (containers == 1) {
                long neededEnergy = energyHandlerItem.getNeededEnergy(0);
                if (neededEnergy == 0L) {
                    representation.add("filled");
                }
            } else if (containers > 1) {
                StringBuilder component = new StringBuilder();
                for (int container = 0; container < containers; container++) {
                    long neededEnergy = energyHandlerItem.getNeededEnergy(container);
                    if (neededEnergy == 0L) {
                        component.append("filled");
                    } else {
                        component.append("empty");
                    }
                }
                representation.add(component.toString());
            }
        }
    }

    @Override
    public void initialize(EmiInitRegistry registry) {
        registry.addIngredientSerializer(ChemicalEmiStack.class, CHEMICAL_SERIALIZER);
        registry.addRegistryAdapter(CHEMICAL_REGISTRY_ADAPTER);
    }

    @Override
    public void register(EmiRegistry registry) {
        CHEMICAL_SERIALIZER.addEmiStacks(registry);

        //Note: We have to add these as generic and then instance check the class so that we can have them be generic across our classes
        registry.addGenericExclusionArea(new EmiExclusionHandler());
        registry.addGenericDragDropHandler(new EmiGhostIngredientHandler());
        registry.addGenericStackProvider(new EmiStackUnderMouseProvider());

        registry.addRecipeHandler(MekanismContainerTypes.FORMULAIC_ASSEMBLICATOR.get(), new FormulaicAssemblicatorTransferHandler());
        registry.addRecipeHandler(MekanismContainerTypes.QIO_DASHBOARD.get(), new EmiQIOCraftingTransferHandler<>());
        registry.addRecipeHandler(MekanismContainerTypes.PORTABLE_QIO_DASHBOARD.get(), new EmiQIOCraftingTransferHandler<>());

        addCategories(registry);
        //Workstations for vanilla categories
        addWorkstations(registry, VanillaEmiRecipeCategories.SMELTING, RecipeViewerRecipeType.VANILLA_SMELTING.workstations());
        addWorkstations(registry, VanillaEmiRecipeCategories.CRAFTING, RecipeViewerRecipeType.VANILLA_CRAFTING.workstations());
        addWorkstations(registry, VanillaEmiRecipeCategories.ANVIL_REPAIRING, List.of(MekanismItems.ROBIT));

        registerItemSubtypes(registry, MekanismItems.ITEMS.getEntries());
        registerItemSubtypes(registry, MekanismBlocks.BLOCKS.getSecondaryEntries());
    }

    public static void registerItemSubtypes(EmiRegistry registry, Collection<? extends Holder<Item>> items) {
        for (Holder<Item> item : items) {
            //Handle items
            ItemStack stack = new ItemStack(item);
            if (Capabilities.STRICT_ENERGY.hasCapability(stack) || Capabilities.CHEMICAL.hasCapability(stack) || Capabilities.FLUID.hasCapability(stack)) {
                registry.setDefaultComparison(stack.getItem(), MEKANISM_COMPARISON);
            }
        }
    }

    private void addCategories(EmiRegistry registry) {
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.CRYSTALLIZING, ChemicalCrystallizerEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.DISSOLUTION, ChemicalDissolutionEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.CHEMICAL_INFUSING, ChemicalChemicalToChemicalEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.WASHING, FluidChemicalToChemicalEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.SEPARATING, ElectrolysisEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.METALLURGIC_INFUSING, MetallurgicInfuserEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.REACTION, PressurizedReactionEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.PIGMENT_EXTRACTING, PigmentExtractingEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.PIGMENT_MIXING, PigmentMixerEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.PAINTING, PaintingEmiRecipe::new);

        //Register both methods of rotary condensentrator recipes
        MekanismEmiRecipeCategory condensentratingCategory = addCategory(registry, RecipeViewerRecipeType.CONDENSENTRATING);
        MekanismEmiRecipeCategory decondensentratingCategory = addCategory(registry, RecipeViewerRecipeType.DECONDENSENTRATING);
        for (RecipeHolder<RotaryRecipe> recipeHolder : MekanismRecipeType.ROTARY.getRecipes(registry.getRecipeManager())) {
            RotaryRecipe recipe = recipeHolder.value();
            if (recipe.hasChemicalToFluid()) {
                if (recipe.hasFluidToChemical()) {
                    //Note: If the recipe is bidirectional, we prefix the recipe id so that they don't clash as duplicates
                    // as we return the proper recipe holder regardless
                    registry.addRecipe(new RotaryEmiRecipe(condensentratingCategory, RecipeViewerUtils.synthetic(recipeHolder.id(), "condensentrating"), recipeHolder, true));
                    registry.addRecipe(new RotaryEmiRecipe(decondensentratingCategory, RecipeViewerUtils.synthetic(recipeHolder.id(), "decondensentrating"), recipeHolder, false));
                } else {
                    registry.addRecipe(new RotaryEmiRecipe(condensentratingCategory, recipeHolder.id(), recipeHolder, true));
                }
            } else if (recipe.hasFluidToChemical()) {
                registry.addRecipe(new RotaryEmiRecipe(decondensentratingCategory, recipeHolder.id(), recipeHolder, false));
            }
        }

        addCategoryAndRecipes(registry, RecipeViewerRecipeType.OXIDIZING, (category, recipeHolder) -> new ItemStackToChemicalEmiRecipe<>(category, recipeHolder, TileEntityChemicalOxidizer.BASE_TICKS_REQUIRED));

        addCategoryAndRecipes(registry, RecipeViewerRecipeType.NUTRITIONAL_LIQUIFICATION, (category, id, recipe) -> new ItemStackToFluidOptionalItemEmiRecipe(category, id, recipe, TileEntityNutritionalLiquifier.BASE_TICKS_REQUIRED), RecipeViewerUtils.getLiquificationRecipes());

        addCategoryAndRecipes(registry, RecipeViewerRecipeType.ACTIVATING, ChemicalToChemicalEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.CENTRIFUGING, ChemicalToChemicalEmiRecipe::new);

        addCategoryAndRecipes(registry, RecipeViewerRecipeType.COMBINING, CombinerEmiRecipe::new);

        addCategoryAndRecipes(registry, RecipeViewerRecipeType.PURIFYING, ItemStackChemicalToItemStackEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.COMPRESSING, ItemStackChemicalToItemStackEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.INJECTING, ItemStackChemicalToItemStackEmiRecipe::new);

        addCategoryAndRecipes(registry, RecipeViewerRecipeType.NUCLEOSYNTHESIZING, NucleosynthesizingEmiRecipe::new);

        addCategoryAndRecipes(registry, RecipeViewerRecipeType.SPS, SPSEmiRecipe::new, SPSRecipeViewerRecipe.getSPSRecipes());
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.BOILER, BoilerEmiRecipe::new, BoilerRecipeViewerRecipe.getBoilerRecipes());

        addCategoryAndRecipes(registry, RecipeViewerRecipeType.SAWING, SawmillEmiRecipe::new);

        addCategoryAndRecipes(registry, RecipeViewerRecipeType.ENRICHING, ItemStackToItemStackEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.CRUSHING, ItemStackToItemStackEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.SMELTING, ItemStackToItemStackEmiRecipe::new);

        addCategoryAndRecipes(registry, RecipeViewerRecipeType.EVAPORATING, FluidToFluidEmiRecipe::new);

        //Conversion recipes
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.ENERGY_CONVERSION, ItemStackToEnergyEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.CHEMICAL_CONVERSION, (category, recipeHolder) -> new ItemStackToChemicalEmiRecipe<>(category, recipeHolder, 0));

        registry.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(MekanismFluids.HEAVY_WATER.value())), List.of(
              MekanismLang.RECIPE_VIEWER_INFO_HEAVY_WATER.translate(MekanismConfig.general.pumpHeavyWaterAmount.get())
        ), Mekanism.rl("info/heavy_water")));
        registry.addRecipe(new EmiInfoRecipe(MekanismAPI.MODULE_REGISTRY.stream()
              .map(ModuleData::getItemHolder)
              .<EmiIngredient>map(item -> EmiStack.of(item.value()))
              .toList(), List.of(
              MekanismLang.RECIPE_VIEWER_INFO_MODULE_INSTALLATION.translate()
        ), Mekanism.rl("info/module_installation")));
    }

    public static <RECIPE extends MekanismRecipe<?>, TYPE extends IRecipeViewerRecipeType<RECIPE> & IMekanismRecipeTypeProvider<?, RECIPE, ?>> void addCategoryAndRecipes(
          EmiRegistry registry, TYPE recipeType, BiFunction<MekanismEmiRecipeCategory, RecipeHolder<RECIPE>, MekanismEmiRecipe<RECIPE>> recipeCreator) {
        MekanismEmiRecipeCategory category = addCategory(registry, recipeType);
        for (RecipeHolder<RECIPE> recipe : recipeType.getRecipes(registry.getRecipeManager())) {
            registry.addRecipe(recipeCreator.apply(category, recipe));
        }
    }

    public static <RECIPE> void addCategoryAndRecipes(EmiRegistry registry, IRecipeViewerRecipeType<RECIPE> recipeType, BasicRecipeCreator<RECIPE> recipeCreator,
          Map<ResourceLocation, RECIPE> recipes) {
        MekanismEmiRecipeCategory category = addCategory(registry, recipeType);
        for (Map.Entry<ResourceLocation, RECIPE> entry : recipes.entrySet()) {
            registry.addRecipe(recipeCreator.create(category, entry.getKey(), entry.getValue()));
        }
    }

    public static <RECIPE extends INamedRVRecipe> void addCategoryAndRecipes(EmiRegistry registry, IRecipeViewerRecipeType<RECIPE> recipeType,
          BasicRecipeCreator<RECIPE> recipeCreator, List<RECIPE> recipes) {
        MekanismEmiRecipeCategory category = addCategory(registry, recipeType);
        for (RECIPE recipe : recipes) {
            registry.addRecipe(recipeCreator.create(category, recipe.id(), recipe));
        }
    }

    private static MekanismEmiRecipeCategory addCategory(EmiRegistry registry, IRecipeViewerRecipeType<?> recipeType) {
        MekanismEmiRecipeCategory category = MekanismEmiRecipeCategory.create(recipeType);
        registry.addCategory(category);
        addWorkstations(registry, category, recipeType.workstations());
        return category;
    }

    private static void addWorkstations(EmiRegistry registry, EmiRecipeCategory category, List<ItemLike> workstations) {
        for (ItemLike workstation : workstations) {
            Item item = workstation.asItem();
            registry.addWorkstation(category, EmiStack.of(item));
            if (item instanceof BlockItem blockItem) {
                AttributeFactoryType factoryType = Attribute.get(blockItem.getBlock(), AttributeFactoryType.class);
                if (factoryType != null) {
                    for (FactoryTier tier : EnumUtils.FACTORY_TIERS) {
                        registry.addWorkstation(category, EmiStack.of(MekanismBlocks.getFactory(tier, factoryType.getFactoryType())));
                    }
                }
            }
        }
    }

    public interface BasicRecipeCreator<RECIPE> {

        MekanismEmiRecipe<RECIPE> create(MekanismEmiRecipeCategory category, ResourceLocation id, RECIPE recipe);
    }
}