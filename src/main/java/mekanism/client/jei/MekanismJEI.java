package mekanism.client.jei;

import java.util.List;
import java.util.stream.Collectors;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.jei.gas.GasStackRenderer;
import mekanism.client.jei.machine.AdvancedMachineRecipeCategory;
import mekanism.client.jei.machine.ChanceMachineRecipeCategory;
import mekanism.client.jei.machine.DoubleMachineRecipeCategory;
import mekanism.client.jei.machine.MachineRecipeCategory;
import mekanism.client.jei.machine.chemical.ChemicalCrystallizerRecipeCategory;
import mekanism.client.jei.machine.chemical.ChemicalDissolutionChamberRecipeCategory;
import mekanism.client.jei.machine.chemical.ChemicalInfuserRecipeCategory;
import mekanism.client.jei.machine.chemical.ChemicalOxidizerRecipeCategory;
import mekanism.client.jei.machine.chemical.ChemicalWasherRecipeCategory;
import mekanism.client.jei.machine.other.ElectrolyticSeparatorRecipeCategory;
import mekanism.client.jei.machine.other.MetallurgicInfuserRecipeCategory;
import mekanism.client.jei.machine.other.PRCRecipeCategory;
import mekanism.client.jei.machine.other.RotaryCondensentratorRecipeCategory;
import mekanism.client.jei.machine.other.SolarNeutronRecipeCategory;
import mekanism.client.jei.machine.other.ThermalEvaporationRecipeCategory;
import mekanism.common.MekanismBlock;
import mekanism.common.MekanismItem;
import mekanism.common.block.states.MachineType;
import mekanism.common.inventory.container.robit.ContainerRobitInventory;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraftforge.fluids.Fluid;

@JEIPlugin
public class MekanismJEI implements IModPlugin {

    public static final IIngredientType<GasStack> TYPE_GAS = () -> GasStack.class;

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {
        List<GasStack> list = GasRegistry.getRegisteredGasses().stream().filter(Gas::isVisible).map(g -> new GasStack(g, Fluid.BUCKET_VOLUME)).collect(Collectors.toList());
        registry.register(MekanismJEI.TYPE_GAS, list, new GasStackHelper(), new GasStackRenderer());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();

        addRecipeCategory(registry, MachineType.CHEMICAL_CRYSTALLIZER, new ChemicalCrystallizerRecipeCategory(guiHelper));
        addRecipeCategory(registry, MachineType.CHEMICAL_DISSOLUTION_CHAMBER, new ChemicalDissolutionChamberRecipeCategory(guiHelper));
        addRecipeCategory(registry, MachineType.CHEMICAL_INFUSER, new ChemicalInfuserRecipeCategory(guiHelper));
        addRecipeCategory(registry, MachineType.CHEMICAL_OXIDIZER, new ChemicalOxidizerRecipeCategory(guiHelper));
        addRecipeCategory(registry, MachineType.CHEMICAL_WASHER, new ChemicalWasherRecipeCategory(guiHelper));
        addRecipeCategory(registry, MachineType.ELECTROLYTIC_SEPARATOR, new ElectrolyticSeparatorRecipeCategory(guiHelper));
        addRecipeCategory(registry, MachineType.METALLURGIC_INFUSER, new MetallurgicInfuserRecipeCategory(guiHelper));
        addRecipeCategory(registry, MachineType.PRESSURIZED_REACTION_CHAMBER, new PRCRecipeCategory(guiHelper));

        addRecipeCategory(registry, MachineType.ROTARY_CONDENSENTRATOR, new RotaryCondensentratorRecipeCategory(guiHelper, true));
        addRecipeCategory(registry, MachineType.ROTARY_CONDENSENTRATOR, new RotaryCondensentratorRecipeCategory(guiHelper, false));

        addRecipeCategory(registry, MachineType.SOLAR_NEUTRON_ACTIVATOR, new SolarNeutronRecipeCategory(guiHelper));

        addRecipeCategory(registry, MachineType.COMBINER, new DoubleMachineRecipeCategory(guiHelper, Recipe.COMBINER.getJEICategory(),
              "tile.MachineBlock.Combiner.name", ProgressBar.STONE));

        addRecipeCategory(registry, MachineType.PURIFICATION_CHAMBER, new AdvancedMachineRecipeCategory(guiHelper, Recipe.PURIFICATION_CHAMBER.getJEICategory(),
              "tile.MachineBlock.PurificationChamber.name", ProgressBar.RED));
        addRecipeCategory(registry, MachineType.OSMIUM_COMPRESSOR, new AdvancedMachineRecipeCategory(guiHelper, Recipe.OSMIUM_COMPRESSOR.getJEICategory(),
              "tile.MachineBlock.OsmiumCompressor.name", ProgressBar.RED));
        addRecipeCategory(registry, MachineType.CHEMICAL_INJECTION_CHAMBER, new AdvancedMachineRecipeCategory(guiHelper, Recipe.CHEMICAL_INJECTION_CHAMBER.getJEICategory(),
              "tile.MachineBlock2.ChemicalInjectionChamber.name", ProgressBar.YELLOW));

        addRecipeCategory(registry, MachineType.PRECISION_SAWMILL, new ChanceMachineRecipeCategory(guiHelper, Recipe.PRECISION_SAWMILL.getJEICategory(),
              "tile.MachineBlock2.PrecisionSawmill.name", ProgressBar.PURPLE));

        addRecipeCategory(registry, MachineType.ENRICHMENT_CHAMBER, new MachineRecipeCategory(guiHelper, Recipe.ENRICHMENT_CHAMBER.getJEICategory(),
              "tile.MachineBlock.EnrichmentChamber.name", ProgressBar.BLUE));
        addRecipeCategory(registry, MachineType.CRUSHER, new MachineRecipeCategory(guiHelper, Recipe.CRUSHER.getJEICategory(), "tile.MachineBlock.Crusher.name",
              ProgressBar.CRUSH));
        addRecipeCategory(registry, MachineType.ENERGIZED_SMELTER, new MachineRecipeCategory(guiHelper, Recipe.ENERGIZED_SMELTER.getJEICategory(),
              "tile.MachineBlock.EnergizedSmelter.name", ProgressBar.BLUE));

        //There is no config option to disable the thermal evaporation plant
        registry.addRecipeCategories(new ThermalEvaporationRecipeCategory(guiHelper));
    }

    private void addRecipeCategory(IRecipeCategoryRegistration registry, MachineType type, BaseRecipeCategory category) {
        if (type.isEnabled()) {
            registry.addRecipeCategories(category);
        }
    }

    @Override
    public void register(IModRegistry registry) {
        registry.addAdvancedGuiHandlers(new GuiElementHandler());

        //Blacklist
        IIngredientBlacklist ingredientBlacklist = registry.getJeiHelpers().getIngredientBlacklist();
        //TODO: Why do these still show up in JEI (Is it due to an error with category types?)
        ingredientBlacklist.addIngredientToBlacklist(MekanismItem.ITEM_PROXY.getItemStack());
        ingredientBlacklist.addIngredientToBlacklist(MekanismBlock.BOUNDING_BLOCK.getItemStack());

        //Register the recipes and their catalysts if enabled
        RecipeRegistryHelper.registerEnrichmentChamber(registry);
        RecipeRegistryHelper.registerCrusher(registry);
        RecipeRegistryHelper.registerCombiner(registry);
        RecipeRegistryHelper.registerPurification(registry);
        RecipeRegistryHelper.registerCompressor(registry);
        RecipeRegistryHelper.registerInjection(registry);
        RecipeRegistryHelper.registerSawmill(registry);
        RecipeRegistryHelper.registerMetallurgicInfuser(registry);
        RecipeRegistryHelper.registerCrystallizer(registry);
        RecipeRegistryHelper.registerDissolution(registry);
        RecipeRegistryHelper.registerChemicalInfuser(registry);
        RecipeRegistryHelper.registerOxidizer(registry);
        RecipeRegistryHelper.registerWasher(registry);
        RecipeRegistryHelper.registerNeutronActivator(registry);
        RecipeRegistryHelper.registerSeparator(registry);
        RecipeRegistryHelper.registerEvaporationPlant(registry);
        RecipeRegistryHelper.registerReactionChamber(registry);
        RecipeRegistryHelper.registerCondensentrator(registry);
        RecipeRegistryHelper.registerSmelter(registry);
        RecipeRegistryHelper.registerFormulaicAssemblicator(registry);
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(ContainerRobitInventory.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36);
    }
}