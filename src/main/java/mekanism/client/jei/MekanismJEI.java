package mekanism.client.jei;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
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
import mekanism.client.jei.machine.CombinerRecipeCategory;
import mekanism.client.jei.machine.ItemStackGasToItemStackRecipeCategory;
import mekanism.client.jei.machine.ItemStackToItemStackRecipeCategory;
import mekanism.client.jei.machine.SawmillRecipeCategory;
import mekanism.client.jei.machine.ChemicalCrystallizerRecipeCategory;
import mekanism.client.jei.machine.ItemStackGasToGasRecipeCategory;
import mekanism.client.jei.machine.ChemicalInfuserRecipeCategory;
import mekanism.client.jei.machine.ItemStackToGasRecipeCategory;
import mekanism.client.jei.machine.FluidGasToGasRecipeCategory;
import mekanism.client.jei.machine.ElectrolysisRecipeCategory;
import mekanism.client.jei.machine.MetallurgicInfuserRecipeCategory;
import mekanism.client.jei.machine.PressurizedReactionRecipeCategory;
import mekanism.client.jei.machine.GasToGasRecipeCategory;
import mekanism.client.jei.machine.FluidToFluidRecipeCategory;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.inventory.container.entity.robit.InventoryRobitContainer;
import mekanism.common.inventory.container.tile.FormulaicAssemblicatorContainer;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidAttributes;

@JeiPlugin
public class MekanismJEI implements IModPlugin {

    public static final IIngredientType<GasStack> TYPE_GAS = () -> GasStack.class;

    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Mekanism.MODID, "jei_plugin");
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {
        List<GasStack> list = MekanismAPI.GAS_REGISTRY.getValues().stream().filter(Gas::isVisible).map(g -> new GasStack(g, FluidAttributes.BUCKET_VOLUME)).collect(Collectors.toList());
        registry.register(MekanismJEI.TYPE_GAS, list, new GasStackHelper(), new GasStackRenderer());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();

        //TODO: Fix adding the recipes to this as I think it probably grabs it from all the matching recipe types
        addRecipeCategory(registry, MekanismBlock.CHEMICAL_CRYSTALLIZER, new ChemicalCrystallizerRecipeCategory(guiHelper));
        addRecipeCategory(registry, MekanismBlock.CHEMICAL_DISSOLUTION_CHAMBER, new ItemStackGasToGasRecipeCategory(guiHelper));
        addRecipeCategory(registry, MekanismBlock.CHEMICAL_INFUSER, new ChemicalInfuserRecipeCategory(guiHelper));
        addRecipeCategory(registry, MekanismBlock.CHEMICAL_OXIDIZER, new ItemStackToGasRecipeCategory(guiHelper));
        addRecipeCategory(registry, MekanismBlock.CHEMICAL_WASHER, new FluidGasToGasRecipeCategory(guiHelper));
        addRecipeCategory(registry, MekanismBlock.ELECTROLYTIC_SEPARATOR, new ElectrolysisRecipeCategory(guiHelper));
        addRecipeCategory(registry, MekanismBlock.METALLURGIC_INFUSER, new MetallurgicInfuserRecipeCategory(guiHelper));
        addRecipeCategory(registry, MekanismBlock.PRESSURIZED_REACTION_CHAMBER, new PressurizedReactionRecipeCategory(guiHelper));

        //TODO
        //addRecipeCategory(registry, MekanismBlock.ROTARY_CONDENSENTRATOR, new RotaryCondensentratorRecipeCategory(guiHelper, true));
        //addRecipeCategory(registry, MekanismBlock.ROTARY_CONDENSENTRATOR, new RotaryCondensentratorRecipeCategory(guiHelper, false));

        addRecipeCategory(registry, MekanismBlock.SOLAR_NEUTRON_ACTIVATOR, new GasToGasRecipeCategory(guiHelper));

        addRecipeCategory(registry, MekanismBlock.COMBINER, new CombinerRecipeCategory(guiHelper, MekanismBlock.COMBINER, ProgressBar.STONE));

        addRecipeCategory(registry, MekanismBlock.PURIFICATION_CHAMBER, new ItemStackGasToItemStackRecipeCategory(guiHelper, MekanismBlock.PURIFICATION_CHAMBER, ProgressBar.RED));
        addRecipeCategory(registry, MekanismBlock.OSMIUM_COMPRESSOR, new ItemStackGasToItemStackRecipeCategory(guiHelper, MekanismBlock.OSMIUM_COMPRESSOR, ProgressBar.RED));
        addRecipeCategory(registry, MekanismBlock.CHEMICAL_INJECTION_CHAMBER, new ItemStackGasToItemStackRecipeCategory(guiHelper, MekanismBlock.CHEMICAL_INJECTION_CHAMBER, ProgressBar.YELLOW));

        addRecipeCategory(registry, MekanismBlock.PRECISION_SAWMILL, new SawmillRecipeCategory(guiHelper, MekanismBlock.PRECISION_SAWMILL, ProgressBar.PURPLE));

        addRecipeCategory(registry, MekanismBlock.ENRICHMENT_CHAMBER, new ItemStackToItemStackRecipeCategory(guiHelper, MekanismBlock.ENRICHMENT_CHAMBER, ProgressBar.BLUE));
        addRecipeCategory(registry, MekanismBlock.CRUSHER, new ItemStackToItemStackRecipeCategory(guiHelper, MekanismBlock.CRUSHER, ProgressBar.CRUSH));
        addRecipeCategory(registry, MekanismBlock.ENERGIZED_SMELTER, new ItemStackToItemStackRecipeCategory(guiHelper, MekanismBlock.ENERGIZED_SMELTER, ProgressBar.BLUE));

        //There is no config option to disable the thermal evaporation plant
        addRecipeCategory(registry, MekanismBlock.THERMAL_EVAPORATION_CONTROLLER, new FluidToFluidRecipeCategory(guiHelper));
    }

    private void addRecipeCategory(IRecipeCategoryRegistration registry, MekanismBlock mekanismBlock, BaseRecipeCategory category) {
        if (mekanismBlock.isEnabled()) {
            registry.addRecipeCategories(category);
        }
    }

    //TODO: Reimplement the blacklist?
    /*@Override
    public void register(IModRegistry registry) {
        registry.addAdvancedGuiHandlers(new GuiElementHandler());

        //Blacklist
        IIngredientBlacklist ingredientBlacklist = registry.getJeiHelpers().getIngredientBlacklist();
        //TODO: Why do these still show up in JEI (Is it due to an error with category types?)
        ingredientBlacklist.addIngredientToBlacklist(MekanismItem.ITEM_PROXY.getItemStack());
        ingredientBlacklist.addIngredientToBlacklist(MekanismBlock.BOUNDING_BLOCK.getItemStack());
    }*/

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registry) {
        GuiHandlerRegistryHelper.register(registry, MekanismBlock.ENRICHMENT_CHAMBER, GuiEnrichmentChamber.class, 79, 40, 24, 7);
        GuiHandlerRegistryHelper.register(registry, MekanismBlock.CRUSHER, GuiEnrichmentChamber.class, 79, 40, 24, 7);
        GuiHandlerRegistryHelper.register(registry, MekanismBlock.COMBINER, GuiEnrichmentChamber.class, 79, 40, 24, 7);
        GuiHandlerRegistryHelper.register(registry, MekanismBlock.PURIFICATION_CHAMBER, GuiEnrichmentChamber.class, 79, 40, 24, 7);
        GuiHandlerRegistryHelper.register(registry, MekanismBlock.OSMIUM_COMPRESSOR, GuiEnrichmentChamber.class, 79, 40, 24, 7);
        GuiHandlerRegistryHelper.register(registry, MekanismBlock.CHEMICAL_INJECTION_CHAMBER, GuiEnrichmentChamber.class, 79, 40, 24, 7);
        GuiHandlerRegistryHelper.register(registry, MekanismBlock.PRECISION_SAWMILL, GuiEnrichmentChamber.class, 79, 40, 24, 7);
        GuiHandlerRegistryHelper.register(registry, MekanismBlock.METALLURGIC_INFUSER, GuiMetallurgicInfuser.class, 72, 47, 32, 8);
        GuiHandlerRegistryHelper.register(registry, MekanismBlock.CHEMICAL_CRYSTALLIZER, GuiChemicalCrystallizer.class, 53, 62, 48, 8);
        GuiHandlerRegistryHelper.register(registry, MekanismBlock.CHEMICAL_DISSOLUTION_CHAMBER, GuiChemicalDissolutionChamber.class, 64, 40, 48, 8);
        GuiHandlerRegistryHelper.register(registry, MekanismBlock.CHEMICAL_INFUSER, GuiChemicalInfuser.class, 47, 39, 28, 8);
        GuiHandlerRegistryHelper.register(registry, MekanismBlock.CHEMICAL_INFUSER, GuiChemicalInfuser.class, 101, 39, 28, 8);
        GuiHandlerRegistryHelper.register(registry, MekanismBlock.CHEMICAL_OXIDIZER, GuiChemicalOxidizer.class, 64, 40, 48, 8);
        GuiHandlerRegistryHelper.register(registry, MekanismBlock.CHEMICAL_WASHER, GuiChemicalWasher.class, 61, 39, 55, 8);
        GuiHandlerRegistryHelper.register(registry, MekanismBlock.SOLAR_NEUTRON_ACTIVATOR, GuiSolarNeutronActivator.class, 64, 39, 48, 8);
        GuiHandlerRegistryHelper.register(registry, MekanismBlock.ELECTROLYTIC_SEPARATOR, GuiElectrolyticSeparator.class, 80, 30, 16, 6);
        GuiHandlerRegistryHelper.register(registry, MekanismBlock.THERMAL_EVAPORATION_CONTROLLER, GuiThermalEvaporationController.class, 49, 20, 78, 38);
        GuiHandlerRegistryHelper.register(registry, MekanismBlock.PRESSURIZED_REACTION_CHAMBER, GuiPRC.class, 75, 37, 36, 10);
        GuiHandlerRegistryHelper.registerCondensentrator(registry);
        GuiHandlerRegistryHelper.registerSmelter(registry);

        registry.addGuiContainerHandler(GuiMekanism.class, new GuiElementHandler());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        //Register the recipes and their catalysts if enabled
        RecipeRegistryHelper.register(registry, MekanismBlock.ENRICHMENT_CHAMBER, Recipe.ENRICHMENT_CHAMBER);
        RecipeRegistryHelper.register(registry, MekanismBlock.CRUSHER, Recipe.CRUSHER);
        RecipeRegistryHelper.register(registry, MekanismBlock.COMBINER, Recipe.COMBINER);
        RecipeRegistryHelper.register(registry, MekanismBlock.PURIFICATION_CHAMBER, Recipe.PURIFICATION_CHAMBER);
        RecipeRegistryHelper.register(registry, MekanismBlock.OSMIUM_COMPRESSOR, Recipe.OSMIUM_COMPRESSOR);
        RecipeRegistryHelper.register(registry, MekanismBlock.CHEMICAL_INJECTION_CHAMBER, Recipe.CHEMICAL_INJECTION_CHAMBER);
        RecipeRegistryHelper.register(registry, MekanismBlock.PRECISION_SAWMILL, Recipe.PRECISION_SAWMILL);
        RecipeRegistryHelper.register(registry, MekanismBlock.METALLURGIC_INFUSER, Recipe.METALLURGIC_INFUSER);
        RecipeRegistryHelper.register(registry, MekanismBlock.CHEMICAL_CRYSTALLIZER, Recipe.CHEMICAL_CRYSTALLIZER);
        RecipeRegistryHelper.register(registry, MekanismBlock.CHEMICAL_DISSOLUTION_CHAMBER, Recipe.CHEMICAL_DISSOLUTION_CHAMBER);
        RecipeRegistryHelper.register(registry, MekanismBlock.CHEMICAL_INFUSER, Recipe.CHEMICAL_INFUSER);
        RecipeRegistryHelper.register(registry, MekanismBlock.CHEMICAL_OXIDIZER, Recipe.CHEMICAL_OXIDIZER);
        RecipeRegistryHelper.register(registry, MekanismBlock.CHEMICAL_WASHER, Recipe.CHEMICAL_WASHER);
        RecipeRegistryHelper.register(registry, MekanismBlock.SOLAR_NEUTRON_ACTIVATOR, Recipe.SOLAR_NEUTRON_ACTIVATOR);
        RecipeRegistryHelper.register(registry, MekanismBlock.ELECTROLYTIC_SEPARATOR, Recipe.ELECTROLYTIC_SEPARATOR);
        RecipeRegistryHelper.register(registry, MekanismBlock.THERMAL_EVAPORATION_CONTROLLER, Recipe.THERMAL_EVAPORATION_PLANT);
        RecipeRegistryHelper.register(registry, MekanismBlock.PRESSURIZED_REACTION_CHAMBER, Recipe.PRESSURIZED_REACTION_CHAMBER);
        RecipeRegistryHelper.registerCondensentrator(registry);
        RecipeRegistryHelper.registerSmelter(registry);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
        CatalystRegistryHelper.register(registry, MekanismBlock.ENRICHMENT_CHAMBER);
        CatalystRegistryHelper.register(registry, MekanismBlock.CRUSHER);
        CatalystRegistryHelper.register(registry, MekanismBlock.COMBINER);
        CatalystRegistryHelper.register(registry, MekanismBlock.PURIFICATION_CHAMBER);
        CatalystRegistryHelper.register(registry, MekanismBlock.OSMIUM_COMPRESSOR);
        CatalystRegistryHelper.register(registry, MekanismBlock.CHEMICAL_INJECTION_CHAMBER);
        CatalystRegistryHelper.register(registry, MekanismBlock.PRECISION_SAWMILL);
        CatalystRegistryHelper.register(registry, MekanismBlock.METALLURGIC_INFUSER);
        CatalystRegistryHelper.register(registry, MekanismBlock.CHEMICAL_CRYSTALLIZER);
        CatalystRegistryHelper.register(registry, MekanismBlock.CHEMICAL_DISSOLUTION_CHAMBER);
        CatalystRegistryHelper.register(registry, MekanismBlock.CHEMICAL_INFUSER);
        CatalystRegistryHelper.register(registry, MekanismBlock.CHEMICAL_CRYSTALLIZER);
        CatalystRegistryHelper.register(registry, MekanismBlock.CHEMICAL_OXIDIZER);
        CatalystRegistryHelper.register(registry, MekanismBlock.CHEMICAL_WASHER);
        CatalystRegistryHelper.register(registry, MekanismBlock.SOLAR_NEUTRON_ACTIVATOR);
        CatalystRegistryHelper.register(registry, MekanismBlock.ELECTROLYTIC_SEPARATOR);
        CatalystRegistryHelper.register(registry, MekanismBlock.THERMAL_EVAPORATION_CONTROLLER);
        CatalystRegistryHelper.register(registry, MekanismBlock.PRESSURIZED_REACTION_CHAMBER);
        CatalystRegistryHelper.registerCondensentrator(registry);
        CatalystRegistryHelper.registerSmelter(registry);
        CatalystRegistryHelper.register(registry, MekanismBlock.FORMULAIC_ASSEMBLICATOR, VanillaRecipeCategoryUid.CRAFTING);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registry) {
        registry.addRecipeTransferHandler(InventoryRobitContainer.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36);
        if (MekanismBlock.FORMULAIC_ASSEMBLICATOR.isEnabled()) {
            registry.addRecipeTransferHandler(FormulaicAssemblicatorContainer.class, VanillaRecipeCategoryUid.CRAFTING, 20, 9, 35, 36);
        }
    }
}