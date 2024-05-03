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
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.providers.IItemProvider;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.ChemicalEmiStack.GasEmiStack;
import mekanism.client.recipe_viewer.emi.ChemicalEmiStack.InfusionEmiStack;
import mekanism.client.recipe_viewer.emi.ChemicalEmiStack.PigmentEmiStack;
import mekanism.client.recipe_viewer.emi.ChemicalEmiStack.SlurryEmiStack;
import mekanism.client.recipe_viewer.emi.recipe.BoilerEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.ChemicalCrystallizerEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.ChemicalDissolutionEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.ChemicalInfuserEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.CombinerEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.ElectrolysisEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.FluidSlurryToSlurryEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.FluidToFluidEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.GasToGasEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.ItemStackGasToItemStackEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.ItemStackToEnergyEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.ItemStackToFluidEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.ItemStackToGasEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.ItemStackToInfuseTypeEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.ItemStackToItemStackEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.ItemStackToPigmentEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.MekanismEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.MetallurgicInfuserEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.NucleosynthesizingEmiRecipe;
import mekanism.client.recipe_viewer.emi.recipe.PaintingEmiRecipe;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

@EmiEntrypoint
public class MekanismEmi implements EmiPlugin {

    @SuppressWarnings("Convert2Diamond")//Can't be detected properly
    private static final ChemicalEmiIngredientSerializer<Gas, GasEmiStack> GAS_SERIALIZER = new ChemicalEmiIngredientSerializer<Gas, GasEmiStack>("Gas", MekanismAPI.GAS_REGISTRY, GasEmiStack::new);
    private static final EmiRegistryAdapter<Gas> GAS_REGISTRY_ADAPTER = EmiRegistryAdapter.simple(Gas.class, MekanismAPI.GAS_REGISTRY, GasEmiStack::new);
    @SuppressWarnings("Convert2Diamond")//Can't be detected properly
    private static final ChemicalEmiIngredientSerializer<InfuseType, InfusionEmiStack> INFUSION_SERIALIZER = new ChemicalEmiIngredientSerializer<InfuseType, InfusionEmiStack>("Infuse Type", MekanismAPI.INFUSE_TYPE_REGISTRY, InfusionEmiStack::new);
    private static final EmiRegistryAdapter<InfuseType> INFUSE_TYPE_REGISTRY_ADAPTER = EmiRegistryAdapter.simple(InfuseType.class, MekanismAPI.INFUSE_TYPE_REGISTRY, InfusionEmiStack::new);
    @SuppressWarnings("Convert2Diamond")//Can't be detected properly
    private static final ChemicalEmiIngredientSerializer<Pigment, PigmentEmiStack> PIGMENT_SERIALIZER = new ChemicalEmiIngredientSerializer<Pigment, PigmentEmiStack>("Pigment", MekanismAPI.PIGMENT_REGISTRY, PigmentEmiStack::new);
    private static final EmiRegistryAdapter<Pigment> PIGMENT_REGISTRY_ADAPTER = EmiRegistryAdapter.simple(Pigment.class, MekanismAPI.PIGMENT_REGISTRY, PigmentEmiStack::new);
    @SuppressWarnings("Convert2Diamond")//Can't be detected properly
    private static final ChemicalEmiIngredientSerializer<Slurry, SlurryEmiStack> SLURRY_SERIALIZER = new ChemicalEmiIngredientSerializer<Slurry, SlurryEmiStack>("Slurry", MekanismAPI.SLURRY_REGISTRY, SlurryEmiStack::new);
    private static final EmiRegistryAdapter<Slurry> SLURRY_REGISTRY_ADAPTER = EmiRegistryAdapter.simple(Slurry.class, MekanismAPI.SLURRY_REGISTRY, SlurryEmiStack::new);

    private static final Comparison MEKANISM_COMPARISON = Comparison.compareData(emiStack -> {
        //TODO - 1.20.5: Re-evaluate if we want the components check or if it should check it slightly differently?
        if (!emiStack.getComponentChanges().isEmpty()) {
            Set<Object> representation = new HashSet<>();
            ItemStack stack = emiStack.getItemStack();
            addChemicalComponent(representation, stack, ContainerType.GAS, Capabilities.GAS.item());
            addChemicalComponent(representation, stack, ContainerType.INFUSION, Capabilities.INFUSION.item());
            addChemicalComponent(representation, stack, ContainerType.PIGMENT, Capabilities.PIGMENT.item());
            addChemicalComponent(representation, stack, ContainerType.SLURRY, Capabilities.SLURRY.item());
            addFluidComponent(representation, stack);
            addEnergyComponent(representation, stack);
            if (!representation.isEmpty()) {

                return representation;
            }
        }
        return null;
    });

    private static void addChemicalComponent(Set<Object> representation, ItemStack stack, ContainerType<?, ?, ? extends IChemicalHandler<?, ?>> containerType,
          ItemCapability<? extends IChemicalHandler<?, ?>, Void> capability) {
        IChemicalHandler<?, ?> handler = containerType.createHandlerIfData(stack);
        if (handler == null) {
            handler = stack.getCapability(capability);
        }
        if (handler != null) {
            int tanks = handler.getTanks();
            if (tanks == 1) {
                ChemicalStack<?> chemicalStack = handler.getChemicalInTank(0);
                if (!chemicalStack.isEmpty()) {
                    representation.add(chemicalStack.getChemical());
                }
            } else if (tanks > 1) {
                List<Chemical<?>> chemicals = new ArrayList<>(tanks);
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
                FloatingLong neededEnergy = energyHandlerItem.getNeededEnergy(0);
                if (neededEnergy.isZero()) {
                    representation.add("filled");
                }
            } else if (containers > 1) {
                StringBuilder component = new StringBuilder();
                for (int container = 0; container < containers; container++) {
                    FloatingLong neededEnergy = energyHandlerItem.getNeededEnergy(container);
                    if (neededEnergy.isZero()) {
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
        registry.addIngredientSerializer(GasEmiStack.class, GAS_SERIALIZER);
        registry.addIngredientSerializer(InfusionEmiStack.class, INFUSION_SERIALIZER);
        registry.addIngredientSerializer(PigmentEmiStack.class, PIGMENT_SERIALIZER);
        registry.addIngredientSerializer(SlurryEmiStack.class, SLURRY_SERIALIZER);
        //TODO - 1.20.5: Test this works properly for getting things to display as tags
        registry.addRegistryAdapter(GAS_REGISTRY_ADAPTER);
        registry.addRegistryAdapter(INFUSE_TYPE_REGISTRY_ADAPTER);
        registry.addRegistryAdapter(PIGMENT_REGISTRY_ADAPTER);
        registry.addRegistryAdapter(SLURRY_REGISTRY_ADAPTER);
    }

    private <CHEMICAL extends Chemical<CHEMICAL>> void addEmiStacks(EmiRegistry emiRegistry, ChemicalEmiIngredientSerializer<CHEMICAL, ?> serializer) {
        for (CHEMICAL chemical : serializer.registry) {
            if (!chemical.isHidden()) {
                emiRegistry.addEmiStack(serializer.create(chemical));
            }
        }
    }

    @Override
    public void register(EmiRegistry registry) {
        addEmiStacks(registry, GAS_SERIALIZER);
        addEmiStacks(registry, INFUSION_SERIALIZER);
        addEmiStacks(registry, PIGMENT_SERIALIZER);
        addEmiStacks(registry, SLURRY_SERIALIZER);

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

    public static void registerItemSubtypes(EmiRegistry registry, Collection<? extends Holder<? extends ItemLike>> itemProviders) {
        for (Holder<? extends ItemLike> itemProvider : itemProviders) {
            //Handle items
            ItemStack stack = new ItemStack(itemProvider.value());
            if (Capabilities.STRICT_ENERGY.hasCapability(stack) || Capabilities.GAS.hasCapability(stack) ||
                Capabilities.INFUSION.hasCapability(stack) || Capabilities.PIGMENT.hasCapability(stack) ||
                Capabilities.SLURRY.hasCapability(stack) || Capabilities.FLUID.hasCapability(stack)) {
                registry.setDefaultComparison(stack.getItem(), MEKANISM_COMPARISON);
            }
        }
    }

    private void addCategories(EmiRegistry registry) {
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.CRYSTALLIZING, ChemicalCrystallizerEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.DISSOLUTION, ChemicalDissolutionEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.CHEMICAL_INFUSING, ChemicalInfuserEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.WASHING, FluidSlurryToSlurryEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.SEPARATING, ElectrolysisEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.METALLURGIC_INFUSING, MetallurgicInfuserEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.REACTION, PressurizedReactionEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.PIGMENT_EXTRACTING, ItemStackToPigmentEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.PIGMENT_MIXING, PigmentMixerEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.PAINTING, PaintingEmiRecipe::new);

        //Register both methods of rotary condensentrator recipes
        MekanismEmiRecipeCategory condensentratingCategory = addCategory(registry, RecipeViewerRecipeType.CONDENSENTRATING);
        MekanismEmiRecipeCategory decondensentratingCategory = addCategory(registry, RecipeViewerRecipeType.DECONDENSENTRATING);
        for (RecipeHolder<RotaryRecipe> recipeHolder : MekanismRecipeType.ROTARY.getRecipes(registry.getRecipeManager(), null)) {
            RotaryRecipe recipe = recipeHolder.value();
            if (recipe.hasGasToFluid()) {
                if (recipe.hasFluidToGas()) {
                    //Note: If the recipe is bidirectional, we prefix the recipe id so that they don't clash as duplicates
                    // as we return the proper recipe holder regardless
                    registry.addRecipe(new RotaryEmiRecipe(condensentratingCategory, RecipeViewerUtils.synthetic(recipeHolder.id(), "condensentrating"), recipeHolder, true));
                    registry.addRecipe(new RotaryEmiRecipe(decondensentratingCategory, RecipeViewerUtils.synthetic(recipeHolder.id(), "decondensentrating"), recipeHolder, false));
                } else {
                    registry.addRecipe(new RotaryEmiRecipe(condensentratingCategory, recipeHolder.id(), recipeHolder, true));
                }
            } else if (recipe.hasFluidToGas()) {
                registry.addRecipe(new RotaryEmiRecipe(decondensentratingCategory, recipeHolder.id(), recipeHolder, false));
            }
        }

        addCategoryAndRecipes(registry, RecipeViewerRecipeType.OXIDIZING, (category, recipeHolder) -> new ItemStackToGasEmiRecipe(category, recipeHolder, TileEntityChemicalOxidizer.BASE_TICKS_REQUIRED));

        addCategoryAndRecipes(registry, RecipeViewerRecipeType.NUTRITIONAL_LIQUIFICATION, (category, id, recipe) -> new ItemStackToFluidEmiRecipe(category, id, recipe, TileEntityNutritionalLiquifier.BASE_TICKS_REQUIRED), RecipeViewerUtils.getLiquificationRecipes());

        addCategoryAndRecipes(registry, RecipeViewerRecipeType.ACTIVATING, GasToGasEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.CENTRIFUGING, GasToGasEmiRecipe::new);

        addCategoryAndRecipes(registry, RecipeViewerRecipeType.COMBINING, CombinerEmiRecipe::new);

        addCategoryAndRecipes(registry, RecipeViewerRecipeType.PURIFYING, ItemStackGasToItemStackEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.COMPRESSING, ItemStackGasToItemStackEmiRecipe::new);
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.INJECTING, ItemStackGasToItemStackEmiRecipe::new);

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
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.GAS_CONVERSION, (category, recipeHolder) -> new ItemStackToGasEmiRecipe(category, recipeHolder, 0));
        addCategoryAndRecipes(registry, RecipeViewerRecipeType.INFUSION_CONVERSION, ItemStackToInfuseTypeEmiRecipe::new);

        registry.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(MekanismFluids.HEAVY_WATER.getFluid())), List.of(
              MekanismLang.RECIPE_VIEWER_INFO_HEAVY_WATER.translate(MekanismConfig.general.pumpHeavyWaterAmount.get())
        ), Mekanism.rl("info/heavy_water")));
        registry.addRecipe(new EmiInfoRecipe(MekanismAPI.MODULE_REGISTRY.stream().<EmiIngredient>map(data -> EmiStack.of(data.getItemProvider())).toList(), List.of(
              MekanismLang.RECIPE_VIEWER_INFO_MODULE_INSTALLATION.translate()
        ), Mekanism.rl("info/module_installation")));
    }

    public static <RECIPE extends MekanismRecipe, TYPE extends IRecipeViewerRecipeType<RECIPE> & IMekanismRecipeTypeProvider<RECIPE, ?>> void addCategoryAndRecipes(
          EmiRegistry registry, TYPE recipeType, BiFunction<MekanismEmiRecipeCategory, RecipeHolder<RECIPE>, MekanismEmiRecipe<RECIPE>> recipeCreator) {
        MekanismEmiRecipeCategory category = addCategory(registry, recipeType);
        for (RecipeHolder<RECIPE> recipe : recipeType.getRecipes(registry.getRecipeManager(), null)) {
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

    private static MekanismEmiRecipeCategory addCategory(EmiRegistry registry, IRecipeViewerRecipeType<?> recipeType) {
        MekanismEmiRecipeCategory category = MekanismEmiRecipeCategory.create(recipeType);
        registry.addCategory(category);
        addWorkstations(registry, category, recipeType.workstations());
        return category;
    }

    private static void addWorkstations(EmiRegistry registry, EmiRecipeCategory category, List<IItemProvider> workstations) {
        for (IItemProvider workstation : workstations) {
            registry.addWorkstation(category, EmiStack.of(workstation));
            if (workstation instanceof IBlockProvider mekanismBlock) {
                AttributeFactoryType factoryType = Attribute.get(mekanismBlock.getBlock(), AttributeFactoryType.class);
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