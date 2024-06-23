package mekanism.client.recipe_viewer.jei;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.robit.GuiRobitRepair;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.jei.ChemicalStackHelper.GasStackHelper;
import mekanism.client.recipe_viewer.jei.ChemicalStackHelper.InfusionStackHelper;
import mekanism.client.recipe_viewer.jei.ChemicalStackHelper.PigmentStackHelper;
import mekanism.client.recipe_viewer.jei.ChemicalStackHelper.SlurryStackHelper;
import mekanism.client.recipe_viewer.jei.machine.BoilerRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.ChemicalCrystallizerRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.ChemicalDissolutionRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.ChemicalInfuserRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.CombinerRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.ElectrolysisRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.FluidSlurryToSlurryRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.FluidToFluidRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.GasToGasRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.ItemStackGasToItemStackRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.ItemStackToEnergyRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.ItemStackToFluidRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.ItemStackToGasRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.ItemStackToInfuseTypeRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.ItemStackToItemStackRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.ItemStackToPigmentRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.MetallurgicInfuserRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.NucleosynthesizingRecipeCategory;
import mekanism.client.recipe_viewer.jei.machine.PaintingRecipeCategory;
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
import mekanism.common.attachments.containers.ContainerType;
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
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.RegistryUtils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class MekanismJEI implements IModPlugin {

    public static final IIngredientType<GasStack> TYPE_GAS = () -> GasStack.class;
    public static final IIngredientType<InfusionStack> TYPE_INFUSION = () -> InfusionStack.class;
    public static final IIngredientType<PigmentStack> TYPE_PIGMENT = () -> PigmentStack.class;
    public static final IIngredientType<SlurryStack> TYPE_SLURRY = () -> SlurryStack.class;

    public static final GasStackHelper GAS_STACK_HELPER = new GasStackHelper();
    public static final InfusionStackHelper INFUSION_STACK_HELPER = new InfusionStackHelper();
    public static final PigmentStackHelper PIGMENT_STACK_HELPER = new PigmentStackHelper();
    public static final SlurryStackHelper SLURRY_STACK_HELPER = new SlurryStackHelper();

    private static final Map<IRecipeViewerRecipeType<?>, RecipeType<?>> recipeTypeInstanceCache = new HashMap<>();

    public static boolean shouldLoad() {
        //Skip handling if both EMI and JEI are loaded as otherwise some things behave strangely
        return !Mekanism.hooks.EmiLoaded;
    }

    public static RecipeType<?> genericRecipeType(IRecipeViewerRecipeType<?> recipeType) {
        return recipeTypeInstanceCache.computeIfAbsent(recipeType, r -> {
            if (r.requiresHolder()) {
                return new RecipeType<>(r.id(), RecipeHolder.class);
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

    private static final IIngredientSubtypeInterpreter<ItemStack> MEKANISM_NBT_INTERPRETER = (stack, context) -> {
        if (context == UidContext.Ingredient) {
            String representation = getChemicalComponent(stack, ContainerType.GAS, Capabilities.GAS.item());
            representation = addInterpretation(representation, getChemicalComponent(stack, ContainerType.INFUSION, Capabilities.INFUSION.item()));
            representation = addInterpretation(representation, getChemicalComponent(stack, ContainerType.PIGMENT, Capabilities.PIGMENT.item()));
            representation = addInterpretation(representation, getChemicalComponent(stack, ContainerType.SLURRY, Capabilities.SLURRY.item()));
            representation = addInterpretation(representation, getFluidComponent(stack));
            representation = addInterpretation(representation, getEnergyComponent(stack));
            return representation;
        }
        return IIngredientSubtypeInterpreter.NONE;
    };

    private static String addInterpretation(String nbtRepresentation, String component) {
        return nbtRepresentation.isEmpty() ? component : nbtRepresentation + ":" + component;
    }

    private static String getChemicalComponent(ItemStack stack, ContainerType<?, ?, ? extends IChemicalHandler<?, ?>> containerType,
          ItemCapability<? extends IChemicalHandler<?, ?>, Void> capability) {
        IChemicalHandler<?, ?> handler = containerType.createHandlerIfData(stack);
        if (handler == null) {
            handler = stack.getCapability(capability);
        }
        if (handler != null) {
            String component = "";
            for (int tank = 0, tanks = handler.getTanks(); tank < tanks; tank++) {
                ChemicalStack<?> chemicalStack = handler.getChemicalInTank(tank);
                if (!chemicalStack.isEmpty()) {
                    component = addInterpretation(component, chemicalStack.getTypeRegistryName().toString());
                } else if (tanks > 1) {
                    component = addInterpretation(component, "empty");
                }
            }
            return component;
        }
        return IIngredientSubtypeInterpreter.NONE;
    }

    private static String getFluidComponent(ItemStack stack) {
        IFluidHandler handler = ContainerType.FLUID.createHandlerIfData(stack);
        if (handler == null) {
            handler = Capabilities.FLUID.getCapability(stack);
        }
        if (handler != null) {
            String component = "";
            for (int tank = 0, tanks = handler.getTanks(); tank < tanks; tank++) {
                FluidStack fluidStack = handler.getFluidInTank(tank);
                if (!fluidStack.isEmpty()) {
                    component = addInterpretation(component, RegistryUtils.getName(fluidStack.getFluid()).toString());
                } else if (tanks > 1) {
                    component = addInterpretation(component, "empty");
                }
            }
            return component;
        }
        return IIngredientSubtypeInterpreter.NONE;
    }

    private static String getEnergyComponent(ItemStack stack) {
        IStrictEnergyHandler energyHandlerItem = ContainerType.ENERGY.createHandlerIfData(stack);
        if (energyHandlerItem == null) {
            energyHandlerItem = Capabilities.STRICT_ENERGY.getCapability(stack);
        }
        if (energyHandlerItem != null) {
            String component = "";
            int containers = energyHandlerItem.getEnergyContainerCount();
            for (int container = 0; container < containers; container++) {
                FloatingLong neededEnergy = energyHandlerItem.getNeededEnergy(container);
                if (neededEnergy.isZero()) {
                    component = addInterpretation(component, "filled");
                } else if (containers > 1) {
                    component = addInterpretation(component, "empty");
                }
            }
            return component;
        }
        return IIngredientSubtypeInterpreter.NONE;
    }

    public static IIngredientType<? extends ChemicalStack<?>> getIngredientType(ChemicalType chemicalType) {
        return switch (chemicalType) {
            case GAS -> TYPE_GAS;
            case INFUSION -> TYPE_INFUSION;
            case PIGMENT -> TYPE_PIGMENT;
            case SLURRY -> TYPE_SLURRY;
        };
    }

    @NotNull
    @Override
    public ResourceLocation getPluginUid() {
        return Mekanism.rl("jei_plugin");
    }

    public static void registerItemSubtypes(ISubtypeRegistration registry, Collection<? extends Holder<? extends ItemLike>> itemProviders) {
        for (Holder<? extends ItemLike> itemProvider : itemProviders) {
            //Handle items
            ItemStack stack = new ItemStack(itemProvider.value());
            if (Capabilities.STRICT_ENERGY.hasCapability(stack) || Capabilities.GAS.hasCapability(stack) ||
                Capabilities.INFUSION.hasCapability(stack) || Capabilities.PIGMENT.hasCapability(stack) ||
                Capabilities.SLURRY.hasCapability(stack) || Capabilities.FLUID.hasCapability(stack)) {
                registry.registerSubtypeInterpreter(stack.getItem(), MEKANISM_NBT_INTERPRETER);
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
    @SuppressWarnings("RedundantTypeArguments")
    public void registerIngredients(IModIngredientRegistration registry) {
        //Note: We register the ingredient types regardless of if EMI is loaded so that we don't crash any addons that are trying to reference them
        //The types cannot properly be inferred at runtime
        this.<Gas, GasStack>registerIngredientType(registry, MekanismAPI.GAS_REGISTRY, TYPE_GAS, GAS_STACK_HELPER);
        this.<InfuseType, InfusionStack>registerIngredientType(registry, MekanismAPI.INFUSE_TYPE_REGISTRY, TYPE_INFUSION, INFUSION_STACK_HELPER);
        this.<Pigment, PigmentStack>registerIngredientType(registry, MekanismAPI.PIGMENT_REGISTRY, TYPE_PIGMENT, PIGMENT_STACK_HELPER);
        this.<Slurry, SlurryStack>registerIngredientType(registry, MekanismAPI.SLURRY_REGISTRY, TYPE_SLURRY, SLURRY_STACK_HELPER);
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void registerIngredientType(IModIngredientRegistration registration,
          Registry<CHEMICAL> registry, IIngredientType<STACK> ingredientType, ChemicalStackHelper<CHEMICAL, STACK> stackHelper) {
        List<STACK> types = registry.stream()
              .filter(chemical -> !chemical.isEmptyType())//Don't add the empty type. We will allow JEI to filter out any that are hidden from recipe viewers
              .map(chemical -> ChemicalUtil.<CHEMICAL, STACK>withAmount(chemical, FluidType.BUCKET_VOLUME))
              .toList();
        stackHelper.setColorHelper(registration.getColorHelper());
        registration.register(ingredientType, types, stackHelper, new ChemicalStackRenderer<>());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        if (!shouldLoad()) {
            return;
        }
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();

        registry.addRecipeCategories(new ChemicalCrystallizerRecipeCategory(guiHelper, RecipeViewerRecipeType.CRYSTALLIZING));
        registry.addRecipeCategories(new ChemicalDissolutionRecipeCategory(guiHelper, RecipeViewerRecipeType.DISSOLUTION));
        registry.addRecipeCategories(new ChemicalInfuserRecipeCategory(guiHelper, RecipeViewerRecipeType.CHEMICAL_INFUSING));
        registry.addRecipeCategories(new FluidSlurryToSlurryRecipeCategory(guiHelper, RecipeViewerRecipeType.WASHING));
        registry.addRecipeCategories(new ElectrolysisRecipeCategory(guiHelper, RecipeViewerRecipeType.SEPARATING));
        registry.addRecipeCategories(new MetallurgicInfuserRecipeCategory(guiHelper, RecipeViewerRecipeType.METALLURGIC_INFUSING));
        registry.addRecipeCategories(new PressurizedReactionRecipeCategory(guiHelper, RecipeViewerRecipeType.REACTION));
        registry.addRecipeCategories(new ItemStackToPigmentRecipeCategory(guiHelper, RecipeViewerRecipeType.PIGMENT_EXTRACTING));
        registry.addRecipeCategories(new PigmentMixerRecipeCategory(guiHelper, RecipeViewerRecipeType.PIGMENT_MIXING));
        registry.addRecipeCategories(new PaintingRecipeCategory(guiHelper, RecipeViewerRecipeType.PAINTING));

        //Register both methods of rotary condensentrator recipes
        registry.addRecipeCategories(new RotaryCondensentratorRecipeCategory(guiHelper, true));
        registry.addRecipeCategories(new RotaryCondensentratorRecipeCategory(guiHelper, false));

        registry.addRecipeCategories(new ItemStackToGasRecipeCategory(guiHelper, RecipeViewerRecipeType.OXIDIZING, false));
        registry.addRecipeCategories(new ItemStackToFluidRecipeCategory(guiHelper, RecipeViewerRecipeType.NUTRITIONAL_LIQUIFICATION, false));

        registry.addRecipeCategories(new GasToGasRecipeCategory(guiHelper, RecipeViewerRecipeType.ACTIVATING));
        registry.addRecipeCategories(new GasToGasRecipeCategory(guiHelper, RecipeViewerRecipeType.CENTRIFUGING));

        registry.addRecipeCategories(new CombinerRecipeCategory(guiHelper, RecipeViewerRecipeType.COMBINING));

        registry.addRecipeCategories(new ItemStackGasToItemStackRecipeCategory(guiHelper, RecipeViewerRecipeType.PURIFYING));
        registry.addRecipeCategories(new ItemStackGasToItemStackRecipeCategory(guiHelper, RecipeViewerRecipeType.COMPRESSING));
        registry.addRecipeCategories(new ItemStackGasToItemStackRecipeCategory(guiHelper, RecipeViewerRecipeType.INJECTING));

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
        registry.addRecipeCategories(new ItemStackToGasRecipeCategory(guiHelper, RecipeViewerRecipeType.GAS_CONVERSION, true));
        registry.addRecipeCategories(new ItemStackToInfuseTypeRecipeCategory(guiHelper, RecipeViewerRecipeType.INFUSION_CONVERSION));
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
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.GAS_CONVERSION, MekanismRecipeType.GAS_CONVERSION);
        RecipeRegistryHelper.register(registry, RecipeViewerRecipeType.INFUSION_CONVERSION, MekanismRecipeType.INFUSION_CONVERSION);
        RecipeRegistryHelper.addAnvilRecipes(registry, MekanismItems.HDPE_REINFORCED_ELYTRA, item -> new ItemStack[]{MekanismItems.HDPE_SHEET.getItemStack()});
        //Note: Use a "full" bucket's worth of heavy water, so that JEI renders it as desired in the info page
        registry.addIngredientInfo(MekanismFluids.HEAVY_WATER.getFluidStack(FluidType.BUCKET_VOLUME), NeoForgeTypes.FLUID_STACK,
              MekanismLang.RECIPE_VIEWER_INFO_HEAVY_WATER.translate(MekanismConfig.general.pumpHeavyWaterAmount.get()));
        registry.addIngredientInfo(MekanismAPI.MODULE_REGISTRY.stream().map(data -> data.getItemProvider().getItemStack()).toList(),
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
              RecipeViewerRecipeType.SMELTING, RecipeViewerRecipeType.ENERGY_CONVERSION, RecipeViewerRecipeType.GAS_CONVERSION, RecipeViewerRecipeType.INFUSION_CONVERSION);

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