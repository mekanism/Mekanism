package mekanism.client.jei;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.providers.IItemProvider;
import mekanism.client.gui.GuiChemicalCrystallizer;
import mekanism.client.gui.GuiChemicalDissolutionChamber;
import mekanism.client.gui.GuiChemicalInfuser;
import mekanism.client.gui.GuiChemicalOxidizer;
import mekanism.client.gui.GuiChemicalWasher;
import mekanism.client.gui.GuiCombiner;
import mekanism.client.gui.GuiElectrolyticSeparator;
import mekanism.client.gui.GuiFormulaicAssemblicator;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.GuiMetallurgicInfuser;
import mekanism.client.gui.GuiPRC;
import mekanism.client.gui.GuiPrecisionSawmill;
import mekanism.client.gui.GuiSolarNeutronActivator;
import mekanism.client.gui.GuiThermalEvaporationController;
import mekanism.client.gui.robit.GuiRobitCrafting;
import mekanism.client.jei.chemical.GasStackHelper;
import mekanism.client.jei.chemical.GasStackRenderer;
import mekanism.client.jei.chemical.InfusionStackHelper;
import mekanism.client.jei.chemical.InfusionStackRenderer;
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
import mekanism.client.jei.machine.PressurizedReactionRecipeCategory;
import mekanism.client.jei.machine.RotaryCondensentratorRecipeCategory;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidAttributes;

@JeiPlugin
public class MekanismJEI implements IModPlugin {

    public static final IIngredientType<GasStack> TYPE_GAS = () -> GasStack.class;
    public static final IIngredientType<InfusionStack> TYPE_INFUSION = () -> InfusionStack.class;

    private static final ISubtypeInterpreter GAS_TANK_NBT_INTERPRETER = itemStack -> {
        Optional<IGasHandler> capability = MekanismUtils.toOptional(itemStack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
        if (!itemStack.hasTag() || !capability.isPresent()) {
            return ISubtypeInterpreter.NONE;
        }
        IGasHandler gasHandlerItem = capability.get();
        if (gasHandlerItem.getGasTankCount() == 1) {
            //TODO: Eventually figure out a good way to do this with multiple gas tanks
            return gasHandlerItem.getGasInTank(0).getType().getRegistryName().toString();
        }
        return ISubtypeInterpreter.NONE;
    };
    private static final ISubtypeInterpreter INFUSION_TANK_NBT_INTERPRETER = itemStack -> {
        Optional<IInfusionHandler> capability = MekanismUtils.toOptional(itemStack.getCapability(Capabilities.INFUSION_HANDLER_CAPABILITY));
        if (!itemStack.hasTag() || !capability.isPresent()) {
            return ISubtypeInterpreter.NONE;
        }
        IInfusionHandler infusionHandlerItem = capability.get();
        if (infusionHandlerItem.getInfusionTankCount() == 1) {
            //TODO: Eventually figure out a good way to do this with multiple infusion tanks
            return infusionHandlerItem.getInfusionInTank(0).getType().getRegistryName().toString();
        }
        return ISubtypeInterpreter.NONE;
    };
    private static final ISubtypeInterpreter ENERGY_INTERPRETER = itemStack -> {
        Optional<IStrictEnergyHandler> capability = MekanismUtils.toOptional(itemStack.getCapability(Capabilities.STRICT_ENERGY_CAPABILITY));
        if (!itemStack.hasTag() || !capability.isPresent()) {
            return ISubtypeInterpreter.NONE;
        }
        IStrictEnergyHandler energyHandlerItem = capability.get();
        if (energyHandlerItem.getEnergyContainerCount() == 1) {
            //TODO: Eventually figure out a good way to do this with multiple energy containers
            if (energyHandlerItem.getEnergy(0).isZero()) {
                return "empty";
            } else if (energyHandlerItem.getNeededEnergy(0).isZero()) {
                return "filled";
            }
        }
        return ISubtypeInterpreter.NONE;
    };

    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return Mekanism.rl("jei_plugin");
    }

    public static void registerItemSubtypes(ISubtypeRegistration registry, List<IItemProvider> itemProviders) {
        for (IItemProvider itemProvider : itemProviders) {
            Item item = itemProvider.getItem();
            //TODO: Is there some issue with the fact that maybe these override the other ones so need to be done differently, if there is one
            // that supports say both energy and gas
            //Handle items
            ItemStack itemStack = itemProvider.getItemStack();
            if (itemStack.getCapability(Capabilities.STRICT_ENERGY_CAPABILITY).isPresent()) {
                registry.registerSubtypeInterpreter(item, ENERGY_INTERPRETER);
            }
            if (itemStack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).isPresent()) {
                registry.registerSubtypeInterpreter(item, GAS_TANK_NBT_INTERPRETER);
            }
            if (itemStack.getCapability(Capabilities.INFUSION_HANDLER_CAPABILITY).isPresent()) {
                registry.registerSubtypeInterpreter(item, INFUSION_TANK_NBT_INTERPRETER);
            }
        }
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registry) {
        registerItemSubtypes(registry, MekanismItems.ITEMS.getAllItems());
        //We don't have a get all blocks so just manually add them
        registry.registerSubtypeInterpreter(MekanismBlocks.BASIC_GAS_TANK.getItem(), GAS_TANK_NBT_INTERPRETER);
        registry.registerSubtypeInterpreter(MekanismBlocks.ADVANCED_GAS_TANK.getItem(), GAS_TANK_NBT_INTERPRETER);
        registry.registerSubtypeInterpreter(MekanismBlocks.ELITE_GAS_TANK.getItem(), GAS_TANK_NBT_INTERPRETER);
        registry.registerSubtypeInterpreter(MekanismBlocks.ULTIMATE_GAS_TANK.getItem(), GAS_TANK_NBT_INTERPRETER);
        registry.registerSubtypeInterpreter(MekanismBlocks.CREATIVE_GAS_TANK.getItem(), GAS_TANK_NBT_INTERPRETER);
        registry.registerSubtypeInterpreter(MekanismBlocks.BASIC_ENERGY_CUBE.getItem(), ENERGY_INTERPRETER);
        registry.registerSubtypeInterpreter(MekanismBlocks.ADVANCED_ENERGY_CUBE.getItem(), ENERGY_INTERPRETER);
        registry.registerSubtypeInterpreter(MekanismBlocks.ELITE_ENERGY_CUBE.getItem(), ENERGY_INTERPRETER);
        registry.registerSubtypeInterpreter(MekanismBlocks.ULTIMATE_ENERGY_CUBE.getItem(), ENERGY_INTERPRETER);
        registry.registerSubtypeInterpreter(MekanismBlocks.CREATIVE_ENERGY_CUBE.getItem(), ENERGY_INTERPRETER);
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {
        List<GasStack> gases = MekanismAPI.GAS_REGISTRY.getValues().stream().filter(g -> !g.isEmptyType() && !g.isHidden()).map(g -> new GasStack(g, FluidAttributes.BUCKET_VOLUME)).collect(Collectors.toList());
        registry.register(MekanismJEI.TYPE_GAS, gases, new GasStackHelper(), new GasStackRenderer());
        List<InfusionStack> infuseTypes = MekanismAPI.INFUSE_TYPE_REGISTRY.getValues().stream().filter(g -> !g.isEmptyType()).map(g -> new InfusionStack(g, FluidAttributes.BUCKET_VOLUME)).collect(Collectors.toList());
        registry.register(MekanismJEI.TYPE_INFUSION, infuseTypes, new InfusionStackHelper(), new InfusionStackRenderer());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();

        registry.addRecipeCategories(new ChemicalCrystallizerRecipeCategory(guiHelper));
        registry.addRecipeCategories(new ItemStackGasToGasRecipeCategory(guiHelper));
        registry.addRecipeCategories(new ChemicalInfuserRecipeCategory(guiHelper));
        registry.addRecipeCategories(new ItemStackToGasRecipeCategory(guiHelper));
        registry.addRecipeCategories(new FluidGasToGasRecipeCategory(guiHelper));
        registry.addRecipeCategories(new ElectrolysisRecipeCategory(guiHelper));
        registry.addRecipeCategories(new MetallurgicInfuserRecipeCategory(guiHelper));
        registry.addRecipeCategories(new PressurizedReactionRecipeCategory(guiHelper));

        //Register both methods of rotary condensentrator recipes
        registry.addRecipeCategories(new RotaryCondensentratorRecipeCategory(guiHelper, true));
        registry.addRecipeCategories(new RotaryCondensentratorRecipeCategory(guiHelper, false));

        registry.addRecipeCategories(new GasToGasRecipeCategory(guiHelper, MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR));
        registry.addRecipeCategories(new GasToGasRecipeCategory(guiHelper, MekanismBlocks.ISOTOPIC_CENTRIFUGE));

        registry.addRecipeCategories(new CombinerRecipeCategory(guiHelper, MekanismBlocks.COMBINER));

        registry.addRecipeCategories(new ItemStackGasToItemStackRecipeCategory(guiHelper, MekanismBlocks.PURIFICATION_CHAMBER));
        registry.addRecipeCategories(new ItemStackGasToItemStackRecipeCategory(guiHelper, MekanismBlocks.OSMIUM_COMPRESSOR));
        registry.addRecipeCategories(new ItemStackGasToItemStackRecipeCategory(guiHelper, MekanismBlocks.CHEMICAL_INJECTION_CHAMBER));

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
        GuiHandlerRegistryHelper.register(registry, MekanismBlocks.COMBINER, GuiCombiner.class, 79, 40, 24, 7);
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
        RecipeRegistryHelper.registerCondensentrator(registry);
        RecipeRegistryHelper.registerSmelter(registry);
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
        CatalystRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_CRYSTALLIZER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_OXIDIZER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_WASHER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR);
        CatalystRegistryHelper.register(registry, MekanismBlocks.ELECTROLYTIC_SEPARATOR);
        CatalystRegistryHelper.register(registry, MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.PRESSURIZED_REACTION_CHAMBER);
        CatalystRegistryHelper.register(registry, MekanismBlocks.ISOTOPIC_CENTRIFUGE);
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