package mekanism.client.jei;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalUtils.ChemicalToStackCreator;
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
import mekanism.api.providers.IItemProvider;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.robit.GuiRobitRepair;
import mekanism.client.jei.ChemicalStackHelper.GasStackHelper;
import mekanism.client.jei.ChemicalStackHelper.InfusionStackHelper;
import mekanism.client.jei.ChemicalStackHelper.PigmentStackHelper;
import mekanism.client.jei.ChemicalStackHelper.SlurryStackHelper;
import mekanism.client.jei.machine.ChemicalCrystallizerRecipeCategory;
import mekanism.client.jei.machine.ChemicalDissolutionRecipeCategory;
import mekanism.client.jei.machine.ChemicalInfuserRecipeCategory;
import mekanism.client.jei.machine.CombinerRecipeCategory;
import mekanism.client.jei.machine.ElectrolysisRecipeCategory;
import mekanism.client.jei.machine.FluidSlurryToSlurryRecipeCategory;
import mekanism.client.jei.machine.FluidToFluidRecipeCategory;
import mekanism.client.jei.machine.GasToGasRecipeCategory;
import mekanism.client.jei.machine.ItemStackGasToItemStackRecipeCategory;
import mekanism.client.jei.machine.ItemStackToEnergyRecipeCategory;
import mekanism.client.jei.machine.ItemStackToGasRecipeCategory;
import mekanism.client.jei.machine.ItemStackToInfuseTypeRecipeCategory;
import mekanism.client.jei.machine.ItemStackToItemStackRecipeCategory;
import mekanism.client.jei.machine.MetallurgicInfuserRecipeCategory;
import mekanism.client.jei.machine.NucleosynthesizingRecipeCategory;
import mekanism.client.jei.machine.PressurizedReactionRecipeCategory;
import mekanism.client.jei.machine.RotaryCondensentratorRecipeCategory;
import mekanism.client.jei.machine.SPSRecipeCategory;
import mekanism.client.jei.machine.SawmillRecipeCategory;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.container.entity.robit.CraftingRobitContainer;
import mekanism.common.inventory.container.tile.FormulaicAssemblicatorContainer;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.IForgeRegistry;

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

    private static final ResourceLocation ENERGY_CONVERSION = Mekanism.rl("energy_conversion");
    private static final ResourceLocation GAS_CONVERSION = Mekanism.rl("gas_conversion");
    private static final ResourceLocation INFUSION_CONVERSION = Mekanism.rl("infusion_conversion");

    private static final ISubtypeInterpreter MEKANISM_NBT_INTERPRETER = new ISubtypeInterpreter() {
        @Override
        public String apply(ItemStack stack) {
            if (!stack.hasTag()) {
                return ISubtypeInterpreter.NONE;
            }
            String nbtRepresentation = addInterpretation("", getChemicalComponent(stack, Capabilities.GAS_HANDLER_CAPABILITY));
            nbtRepresentation = addInterpretation(nbtRepresentation, getChemicalComponent(stack, Capabilities.INFUSION_HANDLER_CAPABILITY));
            nbtRepresentation = addInterpretation(nbtRepresentation, getChemicalComponent(stack, Capabilities.PIGMENT_HANDLER_CAPABILITY));
            nbtRepresentation = addInterpretation(nbtRepresentation, getChemicalComponent(stack, Capabilities.SLURRY_HANDLER_CAPABILITY));
            nbtRepresentation = addInterpretation(nbtRepresentation, getFluidComponent(stack));
            nbtRepresentation = addInterpretation(nbtRepresentation, getEnergyComponent(stack));
            return nbtRepresentation;
        }

        @Override
        public String apply(ItemStack stack, UidContext context) {
            //Only use the old method if we are an ingredient, for recipes ignore all the NBT
            return context == UidContext.Ingredient ? apply(stack) : ISubtypeInterpreter.NONE;
        }
    };

    private static String addInterpretation(String nbtRepresentation, String component) {
        if (nbtRepresentation.isEmpty()) {
            return component;
        }
        return nbtRepresentation + ":" + component;
    }

    private static String getChemicalComponent(ItemStack stack, Capability<? extends IChemicalHandler<?, ?>> capability) {
        Optional<? extends IChemicalHandler<?, ?>> cap = stack.getCapability(capability).resolve();
        if (cap.isPresent()) {
            IChemicalHandler<?, ?> handler = cap.get();
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
        return ISubtypeInterpreter.NONE;
    }

    private static String getFluidComponent(ItemStack stack) {
        Optional<IFluidHandlerItem> cap = FluidUtil.getFluidHandler(stack).resolve();
        if (cap.isPresent()) {
            IFluidHandlerItem handler = cap.get();
            String component = "";
            for (int tank = 0, tanks = handler.getTanks(); tank < tanks; tank++) {
                FluidStack fluidStack = handler.getFluidInTank(tank);
                if (!fluidStack.isEmpty()) {
                    component = addInterpretation(component, fluidStack.getFluid().getRegistryName().toString());
                } else if (tanks > 1) {
                    component = addInterpretation(component, "empty");
                }
            }
            return component;
        }
        return ISubtypeInterpreter.NONE;
    }

    private static String getEnergyComponent(ItemStack stack) {
        Optional<IStrictEnergyHandler> capability = stack.getCapability(Capabilities.STRICT_ENERGY_CAPABILITY).resolve();
        if (capability.isPresent()) {
            IStrictEnergyHandler energyHandlerItem = capability.get();
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
        return ISubtypeInterpreter.NONE;
    }

    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return Mekanism.rl("jei_plugin");
    }

    public static void registerItemSubtypes(ISubtypeRegistration registry, List<? extends IItemProvider> itemProviders) {
        for (IItemProvider itemProvider : itemProviders) {
            //Handle items
            ItemStack itemStack = itemProvider.getItemStack();
            if (itemStack.getCapability(Capabilities.STRICT_ENERGY_CAPABILITY).isPresent() || itemStack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).isPresent() ||
                itemStack.getCapability(Capabilities.INFUSION_HANDLER_CAPABILITY).isPresent() || itemStack.getCapability(Capabilities.PIGMENT_HANDLER_CAPABILITY).isPresent() ||
                itemStack.getCapability(Capabilities.SLURRY_HANDLER_CAPABILITY).isPresent() || FluidUtil.getFluidHandler(itemStack).isPresent()) {
                registry.registerSubtypeInterpreter(itemProvider.getItem(), MEKANISM_NBT_INTERPRETER);
            }
        }
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registry) {
        registerItemSubtypes(registry, MekanismItems.ITEMS.getAllItems());
        registerItemSubtypes(registry, MekanismBlocks.BLOCKS.getAllBlocks());
    }

    @Override
    @SuppressWarnings("RedundantTypeArguments")
    public void registerIngredients(IModIngredientRegistration registry) {
        //The types cannot properly be inferred at runtime
        this.<Gas, GasStack>registerIngredientType(registry, MekanismAPI.gasRegistry(), TYPE_GAS, GAS_STACK_HELPER, GasStack::new);
        this.<InfuseType, InfusionStack>registerIngredientType(registry, MekanismAPI.infuseTypeRegistry(), TYPE_INFUSION, INFUSION_STACK_HELPER, InfusionStack::new);
        this.<Pigment, PigmentStack>registerIngredientType(registry, MekanismAPI.pigmentRegistry(), TYPE_PIGMENT, PIGMENT_STACK_HELPER, PigmentStack::new);
        this.<Slurry, SlurryStack>registerIngredientType(registry, MekanismAPI.slurryRegistry(), TYPE_SLURRY, SLURRY_STACK_HELPER, SlurryStack::new);
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void registerIngredientType(IModIngredientRegistration registry,
          IForgeRegistry<CHEMICAL> forgeRegistry, IIngredientType<STACK> ingredientType, ChemicalStackHelper<CHEMICAL, STACK> stackHelper,
          ChemicalToStackCreator<CHEMICAL, STACK> stackCreator) {
        List<STACK> types = forgeRegistry.getValues().stream()
              .filter(chemical -> !chemical.isEmptyType() && !chemical.isHidden())
              .map(chemical -> stackCreator.createStack(chemical, FluidAttributes.BUCKET_VOLUME))
              .collect(Collectors.toList());
        registry.register(ingredientType, types, stackHelper, new ChemicalStackRenderer<>());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();

        registry.addRecipeCategories(new ChemicalCrystallizerRecipeCategory(guiHelper));
        registry.addRecipeCategories(new ChemicalDissolutionRecipeCategory(guiHelper));
        registry.addRecipeCategories(new ChemicalInfuserRecipeCategory(guiHelper));
        registry.addRecipeCategories(new FluidSlurryToSlurryRecipeCategory(guiHelper));
        registry.addRecipeCategories(new ElectrolysisRecipeCategory(guiHelper));
        registry.addRecipeCategories(new MetallurgicInfuserRecipeCategory(guiHelper));
        registry.addRecipeCategories(new PressurizedReactionRecipeCategory(guiHelper));

        //Register both methods of rotary condensentrator recipes
        registry.addRecipeCategories(new RotaryCondensentratorRecipeCategory(guiHelper, true));
        registry.addRecipeCategories(new RotaryCondensentratorRecipeCategory(guiHelper, false));

        registry.addRecipeCategories(new ItemStackToGasRecipeCategory(guiHelper, MekanismBlocks.CHEMICAL_OXIDIZER));
        registry.addRecipeCategories(new ItemStackToGasRecipeCategory(guiHelper, MekanismBlocks.NUTRITIONAL_LIQUIFIER));

        registry.addRecipeCategories(new GasToGasRecipeCategory(guiHelper, MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR));
        registry.addRecipeCategories(new GasToGasRecipeCategory(guiHelper, MekanismBlocks.ISOTOPIC_CENTRIFUGE));

        registry.addRecipeCategories(new CombinerRecipeCategory(guiHelper, MekanismBlocks.COMBINER));

        registry.addRecipeCategories(new ItemStackGasToItemStackRecipeCategory(guiHelper, MekanismBlocks.PURIFICATION_CHAMBER));
        registry.addRecipeCategories(new ItemStackGasToItemStackRecipeCategory(guiHelper, MekanismBlocks.OSMIUM_COMPRESSOR));
        registry.addRecipeCategories(new ItemStackGasToItemStackRecipeCategory(guiHelper, MekanismBlocks.CHEMICAL_INJECTION_CHAMBER));

        registry.addRecipeCategories(new NucleosynthesizingRecipeCategory(guiHelper));

        registry.addRecipeCategories(new SPSRecipeCategory(guiHelper));

        registry.addRecipeCategories(new SawmillRecipeCategory(guiHelper, MekanismBlocks.PRECISION_SAWMILL));

        registry.addRecipeCategories(new ItemStackToItemStackRecipeCategory(guiHelper, MekanismBlocks.ENRICHMENT_CHAMBER));
        registry.addRecipeCategories(new ItemStackToItemStackRecipeCategory(guiHelper, MekanismBlocks.CRUSHER));
        registry.addRecipeCategories(new ItemStackToItemStackRecipeCategory(guiHelper, MekanismBlocks.ENERGIZED_SMELTER));

        registry.addRecipeCategories(new FluidToFluidRecipeCategory(guiHelper));

        //Conversion recipes
        registry.addRecipeCategories(new ItemStackToEnergyRecipeCategory(guiHelper, ENERGY_CONVERSION));
        registry.addRecipeCategories(new ItemStackToGasRecipeCategory(guiHelper, GAS_CONVERSION));
        registry.addRecipeCategories(new ItemStackToInfuseTypeRecipeCategory(guiHelper, INFUSION_CONVERSION));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registry) {
        registry.addRecipeClickArea(GuiRobitRepair.class, 102, 48, 22, 15, VanillaRecipeCategoryUid.ANVIL);
        registry.addGenericGuiContainerHandler(GuiMekanism.class, new GuiElementHandler());
        registry.addGhostIngredientHandler(GuiMekanism.class, new GhostIngredientHandler<>());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        //Register the recipes and their catalysts if enabled
        RecipeRegistryHelper.register(registry, MekanismBlocks.ENERGIZED_SMELTER, MekanismRecipeType.SMELTING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.ENRICHMENT_CHAMBER, MekanismRecipeType.ENRICHING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.CRUSHER, MekanismRecipeType.CRUSHING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.COMBINER, MekanismRecipeType.COMBINING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.PURIFICATION_CHAMBER, MekanismRecipeType.PURIFYING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.OSMIUM_COMPRESSOR, MekanismRecipeType.COMPRESSING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_INJECTION_CHAMBER, MekanismRecipeType.INJECTING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.PRECISION_SAWMILL, MekanismRecipeType.SAWING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.METALLURGIC_INFUSER, MekanismRecipeType.METALLURGIC_INFUSING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_CRYSTALLIZER, MekanismRecipeType.CRYSTALLIZING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER, MekanismRecipeType.DISSOLUTION);
        RecipeRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_INFUSER, MekanismRecipeType.CHEMICAL_INFUSING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_OXIDIZER, MekanismRecipeType.OXIDIZING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_WASHER, MekanismRecipeType.WASHING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR, MekanismRecipeType.ACTIVATING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.ISOTOPIC_CENTRIFUGE, MekanismRecipeType.CENTRIFUGING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.ELECTROLYTIC_SEPARATOR, MekanismRecipeType.SEPARATING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, MekanismRecipeType.EVAPORATING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.PRESSURIZED_REACTION_CHAMBER, MekanismRecipeType.REACTION);
        RecipeRegistryHelper.register(registry, MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER, MekanismRecipeType.NUCLEOSYNTHESIZING);
        RecipeRegistryHelper.registerCondensentrator(registry);
        RecipeRegistryHelper.registerNutritionalLiquifier(registry);
        RecipeRegistryHelper.registerSPS(registry);
        RecipeRegistryHelper.register(registry, ENERGY_CONVERSION, MekanismRecipeType.ENERGY_CONVERSION);
        RecipeRegistryHelper.register(registry, GAS_CONVERSION, MekanismRecipeType.GAS_CONVERSION);
        RecipeRegistryHelper.register(registry, INFUSION_CONVERSION, MekanismRecipeType.INFUSION_CONVERSION);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
        CatalystRegistryHelper.register(registry, MekanismBlocks.ENRICHMENT_CHAMBER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.CRUSHER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.COMBINER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.PURIFICATION_CHAMBER, GAS_CONVERSION);
        CatalystRegistryHelper.register(registry, MekanismBlocks.OSMIUM_COMPRESSOR, GAS_CONVERSION);
        CatalystRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_INJECTION_CHAMBER, GAS_CONVERSION);
        CatalystRegistryHelper.register(registry, MekanismBlocks.PRECISION_SAWMILL);
        CatalystRegistryHelper.register(registry, MekanismBlocks.METALLURGIC_INFUSER, INFUSION_CONVERSION);
        CatalystRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_CRYSTALLIZER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER, GAS_CONVERSION);
        CatalystRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_INFUSER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_OXIDIZER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_WASHER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR);
        CatalystRegistryHelper.register(registry, MekanismBlocks.ELECTROLYTIC_SEPARATOR);
        CatalystRegistryHelper.register(registry, MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.PRESSURIZED_REACTION_CHAMBER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.ISOTOPIC_CENTRIFUGE);
        CatalystRegistryHelper.register(registry, MekanismBlocks.NUTRITIONAL_LIQUIFIER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER, GAS_CONVERSION);
        CatalystRegistryHelper.registerCondensentrator(registry);
        CatalystRegistryHelper.register(registry, MekanismBlocks.ENERGIZED_SMELTER, VanillaRecipeCategoryUid.FURNACE);
        CatalystRegistryHelper.registerRecipeItem(registry, MekanismBlocks.FORMULAIC_ASSEMBLICATOR, VanillaRecipeCategoryUid.CRAFTING);
        CatalystRegistryHelper.registerRecipeItem(registry, MekanismItems.ROBIT, MekanismBlocks.ENERGIZED_SMELTER.getRegistryName(), VanillaRecipeCategoryUid.ANVIL,
              VanillaRecipeCategoryUid.CRAFTING, VanillaRecipeCategoryUid.FURNACE);
        //TODO: Decide if we want to make it so all mekanism energy supporting blocks that have gui's are added as catalysts?
        registry.addRecipeCatalyst(MekanismBlocks.BASIC_ENERGY_CUBE.getItemStack(), ENERGY_CONVERSION);
        registry.addRecipeCatalyst(MekanismBlocks.ADVANCED_ENERGY_CUBE.getItemStack(), ENERGY_CONVERSION);
        registry.addRecipeCatalyst(MekanismBlocks.ELITE_ENERGY_CUBE.getItemStack(), ENERGY_CONVERSION);
        registry.addRecipeCatalyst(MekanismBlocks.ULTIMATE_ENERGY_CUBE.getItemStack(), ENERGY_CONVERSION);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registry) {
        registry.addRecipeTransferHandler(CraftingRobitContainer.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36);
        //TODO - 10.1: Validate we are properly allowing for searching all slots in the formulaic assemblicator and the player's inventory
        registry.addRecipeTransferHandler(FormulaicAssemblicatorContainer.class, VanillaRecipeCategoryUid.CRAFTING, 19, 9, 35, 36);
        registry.addRecipeTransferHandler(new QIOCraftingTransferHandler(registry.getTransferHelper()), VanillaRecipeCategoryUid.CRAFTING);
    }
}