package mekanism.client.jei;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.api.providers.IItemProvider;
import mekanism.client.gui.GuiElectrolyticSeparator;
import mekanism.client.gui.GuiEnrichmentChamber;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.GuiMetallurgicInfuser;
import mekanism.client.gui.GuiPRC;
import mekanism.client.gui.GuiSolarNeutronActivator;
import mekanism.client.gui.GuiThermalEvaporationController;
import mekanism.client.gui.chemical.GuiChemicalCrystallizer;
import mekanism.client.gui.chemical.GuiChemicalDissolutionChamber;
import mekanism.client.gui.chemical.GuiChemicalInfuser;
import mekanism.client.gui.chemical.GuiChemicalOxidizer;
import mekanism.client.gui.chemical.GuiChemicalWasher;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.jei.gas.GasStackRenderer;
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
import mekanism.common.inventory.container.entity.robit.InventoryRobitContainer;
import mekanism.common.inventory.container.tile.FormulaicAssemblicatorContainer;
import mekanism.common.item.IItemEnergized;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismMachines;
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
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidAttributes;

@JeiPlugin
public class MekanismJEI implements IModPlugin {

    public static final IIngredientType<GasStack> TYPE_GAS = () -> GasStack.class;

    private static final ISubtypeInterpreter GAS_TANK_NBT_INTERPRETER = itemStack -> {
        if (!itemStack.hasTag() || !(itemStack.getItem() instanceof IGasItem)) {
            return ISubtypeInterpreter.NONE;
        }
        GasStack gasStack = ((IGasItem) itemStack.getItem()).getGas(itemStack);
        if (gasStack.isEmpty()) {
            return ISubtypeInterpreter.NONE;
        }
        return gasStack.getType().getRegistryName().toString();
    };
    private static final ISubtypeInterpreter ENERGY_INTERPRETER = itemStack -> {
        if (!itemStack.hasTag() || !(itemStack.getItem() instanceof IItemEnergized)) {
            return ISubtypeInterpreter.NONE;
        }
        IItemEnergized energized = (IItemEnergized) itemStack.getItem();
        double energy = energized.getEnergy(itemStack);
        if (energy == 0) {
            return "empty";
        } else if (energy == energized.getMaxEnergy(itemStack)) {
            return "filled";
        }
        return ISubtypeInterpreter.NONE;
    };

    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return Mekanism.rl("jei_plugin");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registry) {
        for (IItemProvider itemProvider : MekanismItems.ITEMS.getAllItems()) {
            Item item = itemProvider.getItem();
            //Handle items
            if (item instanceof IGasItem) {
                registry.registerSubtypeInterpreter(item, GAS_TANK_NBT_INTERPRETER);
            } else if (item instanceof IItemEnergized) {
                registry.registerSubtypeInterpreter(item, ENERGY_INTERPRETER);
            }
        }
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
        List<GasStack> list = MekanismAPI.GAS_REGISTRY.getValues().stream().filter(g -> !g.isHidden()).map(g -> new GasStack(g, FluidAttributes.BUCKET_VOLUME)).collect(Collectors.toList());
        registry.register(MekanismJEI.TYPE_GAS, list, new GasStackHelper(), new GasStackRenderer());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();

        //TODO: Fix adding the recipes to this as I think it probably grabs it from all the matching recipe types
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

        registry.addRecipeCategories(new GasToGasRecipeCategory(guiHelper));

        registry.addRecipeCategories(new CombinerRecipeCategory(guiHelper, MekanismMachines.COMBINER, ProgressBar.STONE));

        registry.addRecipeCategories(new ItemStackGasToItemStackRecipeCategory(guiHelper, MekanismMachines.PURIFICATION_CHAMBER, ProgressBar.RED));
        registry.addRecipeCategories(new ItemStackGasToItemStackRecipeCategory(guiHelper, MekanismMachines.OSMIUM_COMPRESSOR, ProgressBar.RED));
        registry.addRecipeCategories(new ItemStackGasToItemStackRecipeCategory(guiHelper, MekanismMachines.CHEMICAL_INJECTION_CHAMBER, ProgressBar.YELLOW));

        registry.addRecipeCategories(new SawmillRecipeCategory(guiHelper, MekanismBlocks.PRECISION_SAWMILL, ProgressBar.PURPLE));

        registry.addRecipeCategories(new ItemStackToItemStackRecipeCategory(guiHelper, MekanismMachines.ENRICHMENT_CHAMBER, ProgressBar.BLUE));
        registry.addRecipeCategories(new ItemStackToItemStackRecipeCategory(guiHelper, MekanismMachines.CRUSHER, ProgressBar.CRUSH));
        registry.addRecipeCategories(new ItemStackToItemStackRecipeCategory(guiHelper, MekanismMachines.ENERGIZED_SMELTER, ProgressBar.BLUE));

        registry.addRecipeCategories(new FluidToFluidRecipeCategory(guiHelper));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registry) {
        GuiHandlerRegistryHelper.register(registry, MekanismMachines.ENRICHMENT_CHAMBER, GuiEnrichmentChamber.class, 79, 40, 24, 7);
        GuiHandlerRegistryHelper.register(registry, MekanismMachines.CRUSHER, GuiCrusher.class, 79, 40, 24, 7);
        GuiHandlerRegistryHelper.register(registry, MekanismMachines.COMBINER, GuiCombiner.class, 79, 40, 24, 7);
        GuiHandlerRegistryHelper.register(registry, MekanismMachines.PURIFICATION_CHAMBER, GuiPurificationChamber.class, 79, 40, 24, 7);
        GuiHandlerRegistryHelper.register(registry, MekanismMachines.OSMIUM_COMPRESSOR, GuiOsmiumCompressor.class, 79, 40, 24, 7);
        GuiHandlerRegistryHelper.register(registry, MekanismMachines.CHEMICAL_INJECTION_CHAMBER, GuiChemicalInjectionChamber.class, 79, 40, 24, 7);
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
        GuiHandlerRegistryHelper.registerSmelter(registry);

        registry.addGuiContainerHandler(GuiMekanism.class, new GuiElementHandler());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        //Register the recipes and their catalysts if enabled
        RecipeRegistryHelper.register(registry, MekanismMachines.ENRICHMENT_CHAMBER, MekanismRecipeType.ENRICHING);
        RecipeRegistryHelper.register(registry, MekanismMachines.CRUSHER, MekanismRecipeType.CRUSHING);
        RecipeRegistryHelper.register(registry, MekanismMachines.COMBINER, MekanismRecipeType.COMBINING);
        RecipeRegistryHelper.register(registry, MekanismMachines.PURIFICATION_CHAMBER, MekanismRecipeType.PURIFYING);
        RecipeRegistryHelper.register(registry, MekanismMachines.OSMIUM_COMPRESSOR, MekanismRecipeType.COMPRESSING);
        RecipeRegistryHelper.register(registry, MekanismMachines.CHEMICAL_INJECTION_CHAMBER, MekanismRecipeType.INJECTING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.PRECISION_SAWMILL, MekanismRecipeType.SAWING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.METALLURGIC_INFUSER, MekanismRecipeType.METALLURGIC_INFUSING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_CRYSTALLIZER, MekanismRecipeType.CRYSTALLIZING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER, MekanismRecipeType.DISSOLUTION);
        RecipeRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_INFUSER, MekanismRecipeType.CHEMICAL_INFUSING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_OXIDIZER, MekanismRecipeType.OXIDIZING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.CHEMICAL_WASHER, MekanismRecipeType.WASHING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR, MekanismRecipeType.ACTIVATING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.ELECTROLYTIC_SEPARATOR, MekanismRecipeType.SEPARATING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, MekanismRecipeType.EVAPORATING);
        RecipeRegistryHelper.register(registry, MekanismBlocks.PRESSURIZED_REACTION_CHAMBER, MekanismRecipeType.REACTION);
        RecipeRegistryHelper.registerCondensentrator(registry);
        RecipeRegistryHelper.registerSmelter(registry);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
        CatalystRegistryHelper.register(registry, MekanismMachines.ENRICHMENT_CHAMBER);
        CatalystRegistryHelper.register(registry, MekanismMachines.CRUSHER);
        CatalystRegistryHelper.register(registry, MekanismMachines.COMBINER);
        CatalystRegistryHelper.register(registry, MekanismMachines.PURIFICATION_CHAMBER);
        CatalystRegistryHelper.register(registry, MekanismMachines.OSMIUM_COMPRESSOR);
        CatalystRegistryHelper.register(registry, MekanismMachines.CHEMICAL_INJECTION_CHAMBER);
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
        CatalystRegistryHelper.registerCondensentrator(registry);
        CatalystRegistryHelper.registerSmelter(registry);
        CatalystRegistryHelper.registerRecipeItem(registry, MekanismBlocks.FORMULAIC_ASSEMBLICATOR, VanillaRecipeCategoryUid.CRAFTING);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registry) {
        registry.addRecipeTransferHandler(InventoryRobitContainer.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36);
        registry.addRecipeTransferHandler(FormulaicAssemblicatorContainer.class, VanillaRecipeCategoryUid.CRAFTING, 20, 9, 35, 36);
    }
}