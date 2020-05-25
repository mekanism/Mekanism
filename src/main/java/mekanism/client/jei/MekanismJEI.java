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
import mekanism.client.gui.GuiSPS;
import mekanism.client.gui.GuiThermalEvaporationController;
import mekanism.client.gui.machine.GuiAntiprotonicNucleosynthesizer;
import mekanism.client.gui.machine.GuiChemicalCrystallizer;
import mekanism.client.gui.machine.GuiChemicalDissolutionChamber;
import mekanism.client.gui.machine.GuiChemicalInfuser;
import mekanism.client.gui.machine.GuiChemicalOxidizer;
import mekanism.client.gui.machine.GuiChemicalWasher;
import mekanism.client.gui.machine.GuiCombiner;
import mekanism.client.gui.machine.GuiElectrolyticSeparator;
import mekanism.client.gui.machine.GuiFormulaicAssemblicator;
import mekanism.client.gui.machine.GuiIsotopicCentrifuge;
import mekanism.client.gui.machine.GuiMetallurgicInfuser;
import mekanism.client.gui.machine.GuiNutritionalLiquifier;
import mekanism.client.gui.machine.GuiPRC;
import mekanism.client.gui.machine.GuiPrecisionSawmill;
import mekanism.client.gui.machine.GuiSolarNeutronActivator;
import mekanism.client.gui.robit.GuiRobitCrafting;
import mekanism.client.jei.chemical.ChemicalStackHelper;
import mekanism.client.jei.chemical.ChemicalStackRenderer;
import mekanism.client.jei.chemical.GasStackHelper;
import mekanism.client.jei.chemical.GasStackRenderer;
import mekanism.client.jei.chemical.InfusionStackHelper;
import mekanism.client.jei.chemical.InfusionStackRenderer;
import mekanism.client.jei.chemical.PigmentStackHelper;
import mekanism.client.jei.chemical.PigmentStackRenderer;
import mekanism.client.jei.chemical.SlurryStackHelper;
import mekanism.client.jei.chemical.SlurryStackRenderer;
import mekanism.client.jei.machine.ChemicalCrystallizerRecipeCategory;
import mekanism.client.jei.machine.ChemicalInfuserRecipeCategory;
import mekanism.client.jei.machine.CombinerRecipeCategory;
import mekanism.client.jei.machine.ElectrolysisRecipeCategory;
import mekanism.client.jei.machine.FluidGasToGasRecipeCategory;
import mekanism.client.jei.machine.FluidToFluidRecipeCategory;
import mekanism.client.jei.machine.GasToGasRecipeCategory;
import mekanism.client.jei.machine.ItemStackGasToGasRecipeCategory;
import mekanism.client.jei.machine.ItemStackGasToItemStackRecipeCategory;
import mekanism.client.jei.machine.ItemStackToGasRecipeCategory;
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
import mekanism.common.util.MekanismUtils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
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
import net.minecraftforge.registries.IForgeRegistry;

@JeiPlugin
public class MekanismJEI implements IModPlugin {

    public static final IIngredientType<GasStack> TYPE_GAS = () -> GasStack.class;
    public static final IIngredientType<InfusionStack> TYPE_INFUSION = () -> InfusionStack.class;
    public static final IIngredientType<PigmentStack> TYPE_PIGMENT = () -> PigmentStack.class;
    public static final IIngredientType<SlurryStack> TYPE_SLURRY = () -> SlurryStack.class;

    private static final ISubtypeInterpreter MEKANISM_NBT_INTERPRETER = stack -> {
        if (!stack.hasTag()) {
            return ISubtypeInterpreter.NONE;
        }
        String nbtRepresentation = addInterpretation("", getChemicalComponent(stack, Capabilities.GAS_HANDLER_CAPABILITY));
        nbtRepresentation = addInterpretation(nbtRepresentation, getChemicalComponent(stack, Capabilities.INFUSION_HANDLER_CAPABILITY));
        nbtRepresentation = addInterpretation(nbtRepresentation, getChemicalComponent(stack, Capabilities.PIGMENT_HANDLER_CAPABILITY));
        nbtRepresentation = addInterpretation(nbtRepresentation, getChemicalComponent(stack, Capabilities.SLURRY_HANDLER_CAPABILITY));
        nbtRepresentation = addInterpretation(nbtRepresentation, getEnergyComponent(stack));
        return nbtRepresentation;
    };

    private static String addInterpretation(String nbtRepresentation, String component) {
        if (nbtRepresentation.isEmpty()) {
            return component;
        }
        return nbtRepresentation + ":" + component;
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>>
    String getChemicalComponent(ItemStack stack, Capability<HANDLER> capability) {
        Optional<HANDLER> cap = MekanismUtils.toOptional(stack.getCapability(capability));
        if (cap.isPresent()) {
            HANDLER handler = cap.get();
            String component = "";
            for (int tank = 0, tanks = handler.getTanks(); tank < tanks; tank++) {
                STACK chemical = handler.getChemicalInTank(tank);
                if (!chemical.isEmpty()) {
                    component = addInterpretation(component, chemical.getTypeRegistryName().toString());
                } else if (tanks > 1) {
                    component = addInterpretation(component, "empty");
                }
            }
            return component;
        }
        return ISubtypeInterpreter.NONE;
    }

    private static String getEnergyComponent(ItemStack stack) {
        Optional<IStrictEnergyHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.STRICT_ENERGY_CAPABILITY));
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
                itemStack.getCapability(Capabilities.SLURRY_HANDLER_CAPABILITY).isPresent()) {
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
        this.<Gas, GasStack>registerIngredientType(registry, MekanismAPI.GAS_REGISTRY, TYPE_GAS, new GasStackHelper(), new GasStackRenderer(), GasStack::new);
        this.<InfuseType, InfusionStack>registerIngredientType(registry, MekanismAPI.INFUSE_TYPE_REGISTRY, TYPE_INFUSION, new InfusionStackHelper(), new InfusionStackRenderer(), InfusionStack::new);
        this.<Pigment , PigmentStack > registerIngredientType(registry, MekanismAPI.PIGMENT_REGISTRY, TYPE_PIGMENT, new PigmentStackHelper(), new PigmentStackRenderer(), PigmentStack::new);
        this.<Slurry, SlurryStack>registerIngredientType(registry, MekanismAPI.SLURRY_REGISTRY, TYPE_SLURRY, new SlurryStackHelper(), new SlurryStackRenderer(), SlurryStack::new);
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void registerIngredientType(IModIngredientRegistration registry,
          IForgeRegistry<CHEMICAL> forgeRegistry, IIngredientType<STACK> ingredientType, ChemicalStackHelper<CHEMICAL, STACK> stackHelper,
          ChemicalStackRenderer<CHEMICAL, STACK> stackRenderer, ChemicalToStackCreator<CHEMICAL, STACK> stackCreator) {
        List<STACK> types = forgeRegistry.getValues().stream()
              .filter(chemical -> !chemical.isEmptyType() && !chemical.isHidden())
              .map(chemical -> stackCreator.createStack(chemical, FluidAttributes.BUCKET_VOLUME))
              .collect(Collectors.toList());
        registry.register(ingredientType, types, stackHelper, stackRenderer);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();

        registry.addRecipeCategories(new ChemicalCrystallizerRecipeCategory(guiHelper));
        registry.addRecipeCategories(new ItemStackGasToGasRecipeCategory(guiHelper));
        registry.addRecipeCategories(new ChemicalInfuserRecipeCategory(guiHelper));
        registry.addRecipeCategories(new FluidGasToGasRecipeCategory(guiHelper));
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
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registry) {
        GuiHandlerRegistryHelper.registerElectricMachines(registry);
        GuiHandlerRegistryHelper.registerAdvancedElectricMachines(registry);
        GuiHandlerRegistryHelper.register(registry, MekanismBlocks.COMBINER, GuiCombiner.class, 87, 41, 24, 7);
        GuiHandlerRegistryHelper.register(registry, MekanismBlocks.PRECISION_SAWMILL, GuiPrecisionSawmill.class, 79, 40, 24, 7);
        GuiHandlerRegistryHelper.register(registry, MekanismBlocks.METALLURGIC_INFUSER, GuiMetallurgicInfuser.class, 72, 47, 32, 8);
        GuiHandlerRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_CRYSTALLIZER, GuiChemicalCrystallizer.class, 53, 62, 48, 8);
        GuiHandlerRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER, GuiChemicalDissolutionChamber.class, 64, 40, 48, 8);
        GuiHandlerRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_INFUSER, GuiChemicalInfuser.class, 47, 39, 28, 8);
        GuiHandlerRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_INFUSER, GuiChemicalInfuser.class, 101, 39, 28, 8);
        GuiHandlerRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_OXIDIZER, GuiChemicalOxidizer.class, 64, 40, 48, 8);
        GuiHandlerRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_WASHER, GuiChemicalWasher.class, 61, 39, 55, 8);
        GuiHandlerRegistryHelper.register(registry, MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR, GuiSolarNeutronActivator.class, 64, 39, 48, 8);
        GuiHandlerRegistryHelper.register(registry, MekanismBlocks.ELECTROLYTIC_SEPARATOR, GuiElectrolyticSeparator.class, 80, 30, 16, 6);
        GuiHandlerRegistryHelper.register(registry, MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, GuiThermalEvaporationController.class, 49, 20, 78, 38);
        GuiHandlerRegistryHelper.register(registry, MekanismBlocks.PRESSURIZED_REACTION_CHAMBER, GuiPRC.class, 75, 37, 36, 10);
        GuiHandlerRegistryHelper.register(registry, MekanismBlocks.ISOTOPIC_CENTRIFUGE, GuiIsotopicCentrifuge.class, 64, 39, 48, 8);
        GuiHandlerRegistryHelper.register(registry, MekanismBlocks.NUTRITIONAL_LIQUIFIER, GuiNutritionalLiquifier.class, 64, 40, 48, 8);
        GuiHandlerRegistryHelper.register(registry, MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER, GuiAntiprotonicNucleosynthesizer.class, 45, 18, 104, 68);
        GuiHandlerRegistryHelper.register(registry, MekanismBlocks.SPS_CASING, GuiSPS.class, 27, 17, 122, 60);
        GuiHandlerRegistryHelper.registerCondensentrator(registry);

        GuiHandlerRegistryHelper.register(registry, GuiRobitCrafting.class, VanillaRecipeCategoryUid.CRAFTING, 90, 35, 22, 15);
        GuiHandlerRegistryHelper.register(registry, GuiFormulaicAssemblicator.class, VanillaRecipeCategoryUid.CRAFTING, 86, 43, 20, 15);

        registry.addGuiContainerHandler(GuiMekanism.class, new GuiElementHandler());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        //Register the recipes and their catalysts if enabled
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
        RecipeRegistryHelper.registerSmelter(registry);
        RecipeRegistryHelper.registerNutritionalLiquifier(registry);
        RecipeRegistryHelper.registerSPS(registry);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
        CatalystRegistryHelper.register(registry, MekanismBlocks.ENRICHMENT_CHAMBER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.CRUSHER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.COMBINER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.PURIFICATION_CHAMBER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.OSMIUM_COMPRESSOR);
        CatalystRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_INJECTION_CHAMBER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.PRECISION_SAWMILL);
        CatalystRegistryHelper.register(registry, MekanismBlocks.METALLURGIC_INFUSER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_CRYSTALLIZER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_INFUSER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_OXIDIZER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_WASHER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR);
        CatalystRegistryHelper.register(registry, MekanismBlocks.ELECTROLYTIC_SEPARATOR);
        CatalystRegistryHelper.register(registry, MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.PRESSURIZED_REACTION_CHAMBER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.ISOTOPIC_CENTRIFUGE);
        CatalystRegistryHelper.register(registry, MekanismBlocks.NUTRITIONAL_LIQUIFIER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER);
        CatalystRegistryHelper.registerCondensentrator(registry);
        CatalystRegistryHelper.registerSmelter(registry);
        CatalystRegistryHelper.registerRecipeItem(registry, MekanismBlocks.FORMULAIC_ASSEMBLICATOR, VanillaRecipeCategoryUid.CRAFTING);
        CatalystRegistryHelper.registerRecipeItem(registry, MekanismItems.ROBIT, VanillaRecipeCategoryUid.CRAFTING);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registry) {
        registry.addRecipeTransferHandler(CraftingRobitContainer.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36);
        registry.addRecipeTransferHandler(FormulaicAssemblicatorContainer.class, VanillaRecipeCategoryUid.CRAFTING, 19, 9, 35, 36);
    }
}