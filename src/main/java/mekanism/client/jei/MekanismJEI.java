package mekanism.client.jei;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.client.gui.GuiChemicalCrystallizer;
import mekanism.client.gui.GuiChemicalDissolutionChamber;
import mekanism.client.gui.GuiChemicalInfuser;
import mekanism.client.gui.GuiChemicalInjectionChamber;
import mekanism.client.gui.GuiChemicalOxidizer;
import mekanism.client.gui.GuiChemicalWasher;
import mekanism.client.gui.GuiCombiner;
import mekanism.client.gui.GuiCrusher;
import mekanism.client.gui.GuiElectrolyticSeparator;
import mekanism.client.gui.GuiEnergizedSmelter;
import mekanism.client.gui.GuiEnrichmentChamber;
import mekanism.client.gui.GuiMetallurgicInfuser;
import mekanism.client.gui.GuiOsmiumCompressor;
import mekanism.client.gui.GuiPRC;
import mekanism.client.gui.GuiPrecisionSawmill;
import mekanism.client.gui.GuiPurificationChamber;
import mekanism.client.gui.GuiRotaryCondensentrator;
import mekanism.client.gui.GuiSolarNeutronActivator;
import mekanism.client.gui.GuiThermalEvaporationController;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.jei.gas.GasStackRenderer;
import mekanism.client.jei.machine.AdvancedMachineRecipeCategory;
import mekanism.client.jei.machine.ChanceMachineRecipeCategory;
import mekanism.client.jei.machine.DoubleMachineRecipeCategory;
import mekanism.client.jei.machine.MachineRecipeCategory;
import mekanism.client.jei.machine.advanced.ChemicalInjectionChamberRecipeWrapper;
import mekanism.client.jei.machine.advanced.CombinerRecipeWrapper;
import mekanism.client.jei.machine.advanced.OsmiumCompressorRecipeWrapper;
import mekanism.client.jei.machine.advanced.PurificationChamberRecipeWrapper;
import mekanism.client.jei.machine.basic.CrusherRecipeWrapper;
import mekanism.client.jei.machine.basic.EnrichmentRecipeWrapper;
import mekanism.client.jei.machine.basic.SmeltingRecipeWrapper;
import mekanism.client.jei.machine.chance.PrecisionSawmillRecipeWrapper;
import mekanism.client.jei.machine.chemical.ChemicalCrystallizerRecipeCategory;
import mekanism.client.jei.machine.chemical.ChemicalCrystallizerRecipeWrapper;
import mekanism.client.jei.machine.chemical.ChemicalDissolutionChamberRecipeCategory;
import mekanism.client.jei.machine.chemical.ChemicalDissolutionChamberRecipeWrapper;
import mekanism.client.jei.machine.chemical.ChemicalInfuserRecipeCategory;
import mekanism.client.jei.machine.chemical.ChemicalInfuserRecipeWrapper;
import mekanism.client.jei.machine.chemical.ChemicalOxidizerRecipeCategory;
import mekanism.client.jei.machine.chemical.ChemicalOxidizerRecipeWrapper;
import mekanism.client.jei.machine.chemical.ChemicalWasherRecipeCategory;
import mekanism.client.jei.machine.chemical.ChemicalWasherRecipeWrapper;
import mekanism.client.jei.machine.other.ElectrolyticSeparatorRecipeCategory;
import mekanism.client.jei.machine.other.ElectrolyticSeparatorRecipeWrapper;
import mekanism.client.jei.machine.other.MetallurgicInfuserRecipeCategory;
import mekanism.client.jei.machine.other.MetallurgicInfuserRecipeWrapper;
import mekanism.client.jei.machine.other.PRCRecipeCategory;
import mekanism.client.jei.machine.other.PRCRecipeWrapper;
import mekanism.client.jei.machine.other.RotaryCondensentratorRecipeCategory;
import mekanism.client.jei.machine.other.RotaryCondensentratorRecipeWrapper;
import mekanism.client.jei.machine.other.SolarNeutronRecipeCategory;
import mekanism.client.jei.machine.other.SolarNeutronRecipeWrapper;
import mekanism.client.jei.machine.other.ThermalEvaporationRecipeCategory;
import mekanism.client.jei.machine.other.ThermalEvaporationRecipeWrapper;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.Tier;
import mekanism.common.base.IFactory;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.base.ITierItem;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.integration.crafttweaker.handlers.EnergizedSmelter;
import mekanism.common.inventory.container.ContainerFormulaicAssemblicator;
import mekanism.common.inventory.container.ContainerRobitInventory;
import mekanism.common.item.ItemBlockEnergyCube;
import mekanism.common.item.ItemBlockGasTank;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.common.recipe.machines.BasicMachineRecipe;
import mekanism.common.recipe.machines.ChanceMachineRecipe;
import mekanism.common.recipe.machines.ChemicalInfuserRecipe;
import mekanism.common.recipe.machines.CombinerRecipe;
import mekanism.common.recipe.machines.CrusherRecipe;
import mekanism.common.recipe.machines.CrystallizerRecipe;
import mekanism.common.recipe.machines.DissolutionRecipe;
import mekanism.common.recipe.machines.DoubleMachineRecipe;
import mekanism.common.recipe.machines.EnrichmentRecipe;
import mekanism.common.recipe.machines.InjectionRecipe;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.machines.MetallurgicInfuserRecipe;
import mekanism.common.recipe.machines.OsmiumCompressorRecipe;
import mekanism.common.recipe.machines.OxidationRecipe;
import mekanism.common.recipe.machines.PressurizedRecipe;
import mekanism.common.recipe.machines.PurificationRecipe;
import mekanism.common.recipe.machines.SawmillRecipe;
import mekanism.common.recipe.machines.SeparatorRecipe;
import mekanism.common.recipe.machines.SmeltingRecipe;
import mekanism.common.recipe.machines.SolarNeutronRecipe;
import mekanism.common.recipe.machines.ThermalEvaporationRecipe;
import mekanism.common.recipe.machines.WasherRecipe;
import mekanism.common.util.MekanismUtils;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.ISubtypeRegistry.ISubtypeInterpreter;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.lang3.tuple.Pair;

@JEIPlugin
public class MekanismJEI implements IModPlugin {

    public static final IIngredientType<GasStack> GAS_INGREDIENT_TYPE = () -> GasStack.class;
    public static final ISubtypeInterpreter NBT_INTERPRETER = itemStack ->
    {
        String ret = Integer.toString(itemStack.getMetadata());

        if (itemStack.getItem() instanceof ITierItem) {
            ret += ":" + ((ITierItem) itemStack.getItem()).getBaseTier(itemStack).getSimpleName();
        }

        if (itemStack.getItem() instanceof IFactory) {
            ret += ":" + RecipeType.values()[((IFactory) itemStack.getItem()).getRecipeType(itemStack)].getName();
        }

        if (itemStack.getItem() instanceof ItemBlockGasTank) {
            GasStack gasStack = ((ItemBlockGasTank) itemStack.getItem()).getGas(itemStack);
            if (gasStack != null) {
                ret += ":" + gasStack.getGas().getName();
            }
        }

        if (itemStack.getItem() instanceof ItemBlockEnergyCube) {
            ret += ":" + (((ItemBlockEnergyCube) itemStack.getItem()).getEnergy(itemStack) > 0 ? "filled" : "empty");
        }

        return ret.isEmpty() ? ISubtypeInterpreter.NONE : ret.toLowerCase(Locale.ROOT);
    };
    private static final GasStackRenderer GAS_RENDERER = new GasStackRenderer();
    private static final boolean CRAFTTWEAKER_LOADED = Loader.isModLoaded("crafttweaker");

    @Override
    public void registerItemSubtypes(ISubtypeRegistry registry) {
        registry.registerSubtypeInterpreter(Item.getItemFromBlock(MekanismBlocks.EnergyCube), NBT_INTERPRETER);
        registry.registerSubtypeInterpreter(Item.getItemFromBlock(MekanismBlocks.MachineBlock), NBT_INTERPRETER);
        registry.registerSubtypeInterpreter(Item.getItemFromBlock(MekanismBlocks.MachineBlock2), NBT_INTERPRETER);
        registry.registerSubtypeInterpreter(Item.getItemFromBlock(MekanismBlocks.MachineBlock3), NBT_INTERPRETER);
        registry.registerSubtypeInterpreter(Item.getItemFromBlock(MekanismBlocks.BasicBlock), NBT_INTERPRETER);
        registry.registerSubtypeInterpreter(Item.getItemFromBlock(MekanismBlocks.BasicBlock2), NBT_INTERPRETER);
        registry.registerSubtypeInterpreter(Item.getItemFromBlock(MekanismBlocks.GasTank), NBT_INTERPRETER);
        registry.registerSubtypeInterpreter(Item.getItemFromBlock(MekanismBlocks.CardboardBox), NBT_INTERPRETER);
        registry.registerSubtypeInterpreter(Item.getItemFromBlock(MekanismBlocks.Transmitter), NBT_INTERPRETER);
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {
        List<GasStack> list = GasRegistry.getRegisteredGasses().stream().filter(Gas::isVisible)
              .map(g -> new GasStack(g, Fluid.BUCKET_VOLUME)).collect(Collectors.toList());
        registry.register(GAS_INGREDIENT_TYPE, list, new GasStackHelper(), GAS_RENDERER);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {

        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();

        List<Pair<MachineType, Supplier<IRecipeCategory>>> categories = new ArrayList<>();

        categories
              .add(Pair.of(MachineType.CHEMICAL_CRYSTALLIZER, () -> new ChemicalCrystallizerRecipeCategory(guiHelper)));
        categories.add(Pair.of(MachineType.CHEMICAL_DISSOLUTION_CHAMBER,
              () -> new ChemicalDissolutionChamberRecipeCategory(guiHelper)));
        categories.add(Pair.of(MachineType.CHEMICAL_INFUSER, () -> new ChemicalInfuserRecipeCategory(guiHelper)));
        categories.add(Pair.of(MachineType.CHEMICAL_OXIDIZER, () -> new ChemicalOxidizerRecipeCategory(guiHelper)));
        categories.add(Pair.of(MachineType.CHEMICAL_WASHER, () -> new ChemicalWasherRecipeCategory(guiHelper)));
        categories.add(Pair
              .of(MachineType.ELECTROLYTIC_SEPARATOR, () -> new ElectrolyticSeparatorRecipeCategory(guiHelper)));
        categories.add(Pair.of(MachineType.METALLURGIC_INFUSER, () -> new MetallurgicInfuserRecipeCategory(guiHelper)));
        categories.add(Pair.of(MachineType.PRESSURIZED_REACTION_CHAMBER, () -> new PRCRecipeCategory(guiHelper)));
        categories.add(Pair
              .of(MachineType.ROTARY_CONDENSENTRATOR, () -> new RotaryCondensentratorRecipeCategory(guiHelper, true)));
        categories.add(Pair
              .of(MachineType.ROTARY_CONDENSENTRATOR, () -> new RotaryCondensentratorRecipeCategory(guiHelper, false)));

        categories.add(Pair.of(MachineType.SOLAR_NEUTRON_ACTIVATOR, () -> new SolarNeutronRecipeCategory(guiHelper)));
        categories.add(Pair.of(null, () -> new ThermalEvaporationRecipeCategory(guiHelper)));

        categories.add(Pair.of(MachineType.COMBINER,
              () -> new DoubleMachineRecipeCategory(guiHelper, Recipe.COMBINER.jeiRecipeUid,
                    "tile.MachineBlock.Combiner.name", ProgressBar.STONE)));

        categories.add(Pair.of(MachineType.PURIFICATION_CHAMBER,
              () -> new AdvancedMachineRecipeCategory(guiHelper, Recipe.PURIFICATION_CHAMBER.jeiRecipeUid,
                    "tile.MachineBlock.PurificationChamber.name", ProgressBar.RED)));
        categories.add(Pair.of(MachineType.OSMIUM_COMPRESSOR,
              () -> new AdvancedMachineRecipeCategory(guiHelper, Recipe.OSMIUM_COMPRESSOR.jeiRecipeUid,
                    "tile.MachineBlock.OsmiumCompressor.name", ProgressBar.RED)));
        categories.add(Pair.of(MachineType.CHEMICAL_INJECTION_CHAMBER,
              () -> new AdvancedMachineRecipeCategory(guiHelper, Recipe.CHEMICAL_INJECTION_CHAMBER.jeiRecipeUid,
                    "nei.chemicalInjectionChamber", ProgressBar.YELLOW)));

        categories.add(Pair.of(MachineType.PRECISION_SAWMILL,
              () -> new ChanceMachineRecipeCategory(guiHelper, Recipe.PRECISION_SAWMILL.jeiRecipeUid,
                    "tile.MachineBlock2.PrecisionSawmill.name", ProgressBar.PURPLE)));

        categories.add(Pair.of(MachineType.ENRICHMENT_CHAMBER,
              () -> new MachineRecipeCategory(guiHelper, Recipe.ENRICHMENT_CHAMBER.jeiRecipeUid,
                    "tile.MachineBlock.EnrichmentChamber.name", ProgressBar.BLUE)));
        categories.add(Pair.of(MachineType.CRUSHER,
              () -> new MachineRecipeCategory(guiHelper, Recipe.CRUSHER.jeiRecipeUid, "tile.MachineBlock.Crusher.name",
                    ProgressBar.CRUSH)));
        categories.add(Pair.of(MachineType.ENERGIZED_SMELTER,
              () -> new MachineRecipeCategory(guiHelper, Recipe.ENERGIZED_SMELTER.jeiRecipeUid,
                    "tile.MachineBlock.EnergizedSmelter.name", ProgressBar.BLUE)));

        registry.addRecipeCategories(categories.stream().filter(p -> p.getLeft() == null || p.getLeft().isEnabled())
              .map(it -> it.getRight().get()).toArray(IRecipeCategory[]::new));
    }

    @Override
    public void register(IModRegistry registry) {
        registry.addAdvancedGuiHandlers(new GuiElementHandler());

        registry.getJeiHelpers().getIngredientBlacklist()
              .addIngredientToBlacklist(new ItemStack(MekanismItems.ItemProxy));
        registry.getJeiHelpers().getIngredientBlacklist()
              .addIngredientToBlacklist(new ItemStack(MekanismBlocks.BoundingBlock));

        if (MachineType.ENRICHMENT_CHAMBER.isEnabled()) {
            registry.handleRecipes(EnrichmentRecipe.class, EnrichmentRecipeWrapper::new,
                  Recipe.ENRICHMENT_CHAMBER.jeiRecipeUid);
            addRecipes(registry, Recipe.ENRICHMENT_CHAMBER, BasicMachineRecipe.class, EnrichmentRecipeWrapper.class,
                  Recipe.ENRICHMENT_CHAMBER.jeiRecipeUid);
            registry.addRecipeClickArea(GuiEnrichmentChamber.class, 79, 40, 24, 7,
                  Recipe.ENRICHMENT_CHAMBER.jeiRecipeUid);
        }

        if (MachineType.CRUSHER.isEnabled()) {
            registry.handleRecipes(CrusherRecipe.class, CrusherRecipeWrapper::new, Recipe.CRUSHER.jeiRecipeUid);
            addRecipes(registry, Recipe.CRUSHER, BasicMachineRecipe.class, CrusherRecipeWrapper.class,
                  Recipe.CRUSHER.jeiRecipeUid);
            registry.addRecipeClickArea(GuiCrusher.class, 79, 40, 24, 7, Recipe.CRUSHER.jeiRecipeUid);
        }

        if (MachineType.COMBINER.isEnabled()) {
            registry.handleRecipes(CombinerRecipe.class, CombinerRecipeWrapper::new, Recipe.COMBINER.jeiRecipeUid);
            addRecipes(registry, Recipe.COMBINER, DoubleMachineRecipe.class, CombinerRecipeWrapper.class,
                  Recipe.COMBINER.jeiRecipeUid);
            registry.addRecipeClickArea(GuiCombiner.class, 79, 40, 24, 7, Recipe.COMBINER.jeiRecipeUid);
        }

        if (MachineType.PURIFICATION_CHAMBER.isEnabled()) {
            registry.handleRecipes(PurificationRecipe.class, PurificationChamberRecipeWrapper::new,
                  Recipe.PURIFICATION_CHAMBER.jeiRecipeUid);
            addRecipes(registry, Recipe.PURIFICATION_CHAMBER, AdvancedMachineRecipe.class,
                  PurificationChamberRecipeWrapper.class, Recipe.PURIFICATION_CHAMBER.jeiRecipeUid);
            registry.addRecipeClickArea(GuiPurificationChamber.class, 79, 40, 24, 7,
                  Recipe.PURIFICATION_CHAMBER.jeiRecipeUid);
        }

        if (MachineType.OSMIUM_COMPRESSOR.isEnabled()) {
            registry.handleRecipes(OsmiumCompressorRecipe.class, OsmiumCompressorRecipeWrapper::new,
                  Recipe.OSMIUM_COMPRESSOR.jeiRecipeUid);
            addRecipes(registry, Recipe.OSMIUM_COMPRESSOR, AdvancedMachineRecipe.class,
                  OsmiumCompressorRecipeWrapper.class, Recipe.OSMIUM_COMPRESSOR.jeiRecipeUid);
            registry
                  .addRecipeClickArea(GuiOsmiumCompressor.class, 79, 40, 24, 7, Recipe.OSMIUM_COMPRESSOR.jeiRecipeUid);
        }

        if (MachineType.CHEMICAL_INJECTION_CHAMBER.isEnabled()) {
            registry.handleRecipes(InjectionRecipe.class, ChemicalInjectionChamberRecipeWrapper::new,
                  Recipe.CHEMICAL_INJECTION_CHAMBER.jeiRecipeUid);
            addRecipes(registry, Recipe.CHEMICAL_INJECTION_CHAMBER, AdvancedMachineRecipe.class,
                  ChemicalInjectionChamberRecipeWrapper.class, Recipe.CHEMICAL_INJECTION_CHAMBER.jeiRecipeUid);
            registry.addRecipeClickArea(GuiChemicalInjectionChamber.class, 79, 40, 24, 7,
                  Recipe.CHEMICAL_INJECTION_CHAMBER.jeiRecipeUid);
        }

        if (MachineType.PRECISION_SAWMILL.isEnabled()) {
            registry.handleRecipes(SawmillRecipe.class, PrecisionSawmillRecipeWrapper::new,
                  Recipe.PRECISION_SAWMILL.jeiRecipeUid);
            addRecipes(registry, Recipe.PRECISION_SAWMILL, ChanceMachineRecipe.class,
                  PrecisionSawmillRecipeWrapper.class, Recipe.PRECISION_SAWMILL.jeiRecipeUid);
            registry
                  .addRecipeClickArea(GuiPrecisionSawmill.class, 79, 40, 24, 7, Recipe.PRECISION_SAWMILL.jeiRecipeUid);
        }

        if (MachineType.METALLURGIC_INFUSER.isEnabled()) {
            registry.handleRecipes(MetallurgicInfuserRecipe.class, MetallurgicInfuserRecipeWrapper::new,
                  Recipe.METALLURGIC_INFUSER.jeiRecipeUid);
            addRecipes(registry, Recipe.METALLURGIC_INFUSER, MetallurgicInfuserRecipe.class,
                  MetallurgicInfuserRecipeWrapper.class, Recipe.METALLURGIC_INFUSER.jeiRecipeUid);
            registry.addRecipeClickArea(GuiMetallurgicInfuser.class, 72, 47, 32, 8,
                  Recipe.METALLURGIC_INFUSER.jeiRecipeUid);
        }

        if (MachineType.CHEMICAL_CRYSTALLIZER.isEnabled()) {
            registry.handleRecipes(CrystallizerRecipe.class, ChemicalCrystallizerRecipeWrapper::new,
                  Recipe.CHEMICAL_CRYSTALLIZER.jeiRecipeUid);
            addRecipes(registry, Recipe.CHEMICAL_CRYSTALLIZER, CrystallizerRecipe.class,
                  ChemicalCrystallizerRecipeWrapper.class, Recipe.CHEMICAL_CRYSTALLIZER.jeiRecipeUid);
            registry.addRecipeClickArea(GuiChemicalCrystallizer.class, 53, 62, 48, 8,
                  Recipe.CHEMICAL_CRYSTALLIZER.jeiRecipeUid);
        }

        if (MachineType.CHEMICAL_DISSOLUTION_CHAMBER.isEnabled()) {
            registry.handleRecipes(DissolutionRecipe.class, ChemicalDissolutionChamberRecipeWrapper::new,
                  Recipe.CHEMICAL_DISSOLUTION_CHAMBER.jeiRecipeUid);
            addRecipes(registry, Recipe.CHEMICAL_DISSOLUTION_CHAMBER, DissolutionRecipe.class,
                  ChemicalDissolutionChamberRecipeWrapper.class, Recipe.CHEMICAL_DISSOLUTION_CHAMBER.jeiRecipeUid);
            registry.addRecipeClickArea(GuiChemicalDissolutionChamber.class, 64, 40, 48, 8,
                  Recipe.CHEMICAL_DISSOLUTION_CHAMBER.jeiRecipeUid);
        }

        if (MachineType.CHEMICAL_INFUSER.isEnabled()) {
            registry.handleRecipes(ChemicalInfuserRecipe.class, ChemicalInfuserRecipeWrapper::new,
                  Recipe.CHEMICAL_INFUSER.jeiRecipeUid);
            addRecipes(registry, Recipe.CHEMICAL_INFUSER, ChemicalInfuserRecipe.class,
                  ChemicalInfuserRecipeWrapper.class, Recipe.CHEMICAL_INFUSER.jeiRecipeUid);
            registry.addRecipeClickArea(GuiChemicalInfuser.class, 47, 39, 28, 8, Recipe.CHEMICAL_INFUSER.jeiRecipeUid);
            registry.addRecipeClickArea(GuiChemicalInfuser.class, 101, 39, 28, 8, Recipe.CHEMICAL_INFUSER.jeiRecipeUid);
        }

        if (MachineType.CHEMICAL_OXIDIZER.isEnabled()) {
            registry.handleRecipes(OxidationRecipe.class, ChemicalOxidizerRecipeWrapper::new,
                  Recipe.CHEMICAL_OXIDIZER.jeiRecipeUid);
            addRecipes(registry, Recipe.CHEMICAL_OXIDIZER, OxidationRecipe.class, ChemicalOxidizerRecipeWrapper.class,
                  Recipe.CHEMICAL_OXIDIZER.jeiRecipeUid);
            registry
                  .addRecipeClickArea(GuiChemicalOxidizer.class, 64, 40, 48, 8, Recipe.CHEMICAL_OXIDIZER.jeiRecipeUid);
        }

        if (MachineType.CHEMICAL_WASHER.isEnabled()) {
            registry.handleRecipes(WasherRecipe.class, ChemicalWasherRecipeWrapper::new,
                  Recipe.CHEMICAL_WASHER.jeiRecipeUid);
            addRecipes(registry, Recipe.CHEMICAL_WASHER, WasherRecipe.class, ChemicalWasherRecipeWrapper.class,
                  Recipe.CHEMICAL_WASHER.jeiRecipeUid);
            registry.addRecipeClickArea(GuiChemicalWasher.class, 61, 39, 55, 8, Recipe.CHEMICAL_WASHER.jeiRecipeUid);
        }

        if (MachineType.SOLAR_NEUTRON_ACTIVATOR.isEnabled()) {
            registry.handleRecipes(SolarNeutronRecipe.class, SolarNeutronRecipeWrapper::new,
                  Recipe.SOLAR_NEUTRON_ACTIVATOR.jeiRecipeUid);
            addRecipes(registry, Recipe.SOLAR_NEUTRON_ACTIVATOR, SolarNeutronRecipe.class,
                  SolarNeutronRecipeWrapper.class, Recipe.SOLAR_NEUTRON_ACTIVATOR.jeiRecipeUid);
            registry.addRecipeClickArea(GuiSolarNeutronActivator.class, 64, 39, 48, 8,
                  Recipe.SOLAR_NEUTRON_ACTIVATOR.jeiRecipeUid);
        }

        if (MachineType.ELECTROLYTIC_SEPARATOR.isEnabled()) {
            registry.handleRecipes(SeparatorRecipe.class, ElectrolyticSeparatorRecipeWrapper::new,
                  Recipe.ELECTROLYTIC_SEPARATOR.jeiRecipeUid);
            addRecipes(registry, Recipe.ELECTROLYTIC_SEPARATOR, SeparatorRecipe.class,
                  ElectrolyticSeparatorRecipeWrapper.class, Recipe.ELECTROLYTIC_SEPARATOR.jeiRecipeUid);
            registry.addRecipeClickArea(GuiElectrolyticSeparator.class, 80, 30, 16, 6,
                  Recipe.ELECTROLYTIC_SEPARATOR.jeiRecipeUid);
        }

        registry.handleRecipes(ThermalEvaporationRecipe.class, ThermalEvaporationRecipeWrapper::new,
              Recipe.THERMAL_EVAPORATION_PLANT.jeiRecipeUid);
        addRecipes(registry, Recipe.THERMAL_EVAPORATION_PLANT, ThermalEvaporationRecipe.class,
              ThermalEvaporationRecipeWrapper.class, Recipe.THERMAL_EVAPORATION_PLANT.jeiRecipeUid);
        registry.addRecipeClickArea(GuiThermalEvaporationController.class, 49, 20, 78, 38,
              Recipe.THERMAL_EVAPORATION_PLANT.jeiRecipeUid);

        if (MachineType.PRESSURIZED_REACTION_CHAMBER.isEnabled()) {
            registry.handleRecipes(PressurizedRecipe.class, PRCRecipeWrapper::new,
                  Recipe.PRESSURIZED_REACTION_CHAMBER.jeiRecipeUid);
            addRecipes(registry, Recipe.PRESSURIZED_REACTION_CHAMBER, PressurizedRecipe.class, PRCRecipeWrapper.class,
                  Recipe.PRESSURIZED_REACTION_CHAMBER.jeiRecipeUid);
            registry.addRecipeClickArea(GuiPRC.class, 75, 37, 36, 10, Recipe.PRESSURIZED_REACTION_CHAMBER.jeiRecipeUid);
        }

        if (MachineType.ROTARY_CONDENSENTRATOR.isEnabled()) {
            List<RotaryCondensentratorRecipeWrapper> condensentratorRecipes = new ArrayList<>();
            List<RotaryCondensentratorRecipeWrapper> decondensentratorRecipes = new ArrayList<>();

            for (Gas gas : GasRegistry.getRegisteredGasses()) {
                if (gas.hasFluid()) {
                    condensentratorRecipes.add(new RotaryCondensentratorRecipeWrapper(gas.getFluid(), gas, true));
                    decondensentratorRecipes.add(new RotaryCondensentratorRecipeWrapper(gas.getFluid(), gas, false));
                }
            }
            registry.addRecipes(condensentratorRecipes, RotaryCondensentratorRecipeCategory.UID_COND);
            registry.addRecipes(decondensentratorRecipes, RotaryCondensentratorRecipeCategory.UID_DECOND);
            registry.addRecipeClickArea(GuiRotaryCondensentrator.class, 64, 39, 48, 8,
                  RotaryCondensentratorRecipeCategory.UID_COND, RotaryCondensentratorRecipeCategory.UID_DECOND);
        }

        if (MachineType.ENERGIZED_SMELTER.isEnabled()) {
            registry.handleRecipes(SmeltingRecipe.class, SmeltingRecipeWrapper::new,
                  Recipe.ENERGIZED_SMELTER.jeiRecipeUid);

            if (CRAFTTWEAKER_LOADED && EnergizedSmelter.hasRemovedRecipe()) // Removed / Removed + Added
            {
                // Add all recipes
                addRecipes(registry, Recipe.ENERGIZED_SMELTER, BasicMachineRecipe.class, SmeltingRecipeWrapper.class,
                      Recipe.ENERGIZED_SMELTER.jeiRecipeUid);
            } else if (CRAFTTWEAKER_LOADED && EnergizedSmelter.hasAddedRecipe()) // Added but not removed
            {
                // Only add added recipes
                Map<ItemStackInput, SmeltingRecipe> smeltingRecipes = Recipe.ENERGIZED_SMELTER.get();
                Collection<SmeltingRecipe> recipes = smeltingRecipes.entrySet().stream()
                      .filter(entry -> !FurnaceRecipes.instance().getSmeltingList().keySet()
                            .contains(entry.getKey().ingredient))
                      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).values();

                addRecipes(registry, recipes, BasicMachineRecipe.class, SmeltingRecipeWrapper.class,
                      Recipe.ENERGIZED_SMELTER.jeiRecipeUid);
            }
            // else - Only use furnace list, so no extra registration.
        }

        if (MachineType.ENERGIZED_SMELTER.isEnabled()) {
            // Energized smelter
            if (CRAFTTWEAKER_LOADED && EnergizedSmelter.hasRemovedRecipe()) {
                registry.addRecipeClickArea(GuiEnergizedSmelter.class, 79, 40, 24, 7,
                      Recipe.ENERGIZED_SMELTER.jeiRecipeUid);
            } else if (CRAFTTWEAKER_LOADED && EnergizedSmelter.hasAddedRecipe()) {
                registry.addRecipeClickArea(GuiEnergizedSmelter.class, 79, 40, 24, 7, VanillaRecipeCategoryUid.SMELTING,
                      Recipe.ENERGIZED_SMELTER.jeiRecipeUid);

                registerVanillaSmeltingRecipeCatalyst(registry);
            } else {
                registry
                      .addRecipeClickArea(GuiEnergizedSmelter.class, 79, 40, 24, 7, VanillaRecipeCategoryUid.SMELTING);

                registerVanillaSmeltingRecipeCatalyst(registry);
            }
        }

        registerRecipeItem(registry, MachineType.ENRICHMENT_CHAMBER, Recipe.ENRICHMENT_CHAMBER);
        registerRecipeItem(registry, MachineType.CRUSHER, Recipe.CRUSHER);
        registerRecipeItem(registry, MachineType.ENERGIZED_SMELTER, Recipe.ENERGIZED_SMELTER);
        registerRecipeItem(registry, MachineType.COMBINER, Recipe.COMBINER);
        registerRecipeItem(registry, MachineType.PURIFICATION_CHAMBER, Recipe.PURIFICATION_CHAMBER);
        registerRecipeItem(registry, MachineType.OSMIUM_COMPRESSOR, Recipe.OSMIUM_COMPRESSOR);
        registerRecipeItem(registry, MachineType.CHEMICAL_INJECTION_CHAMBER, Recipe.CHEMICAL_INJECTION_CHAMBER);
        registerRecipeItem(registry, MachineType.PRECISION_SAWMILL, Recipe.PRECISION_SAWMILL);
        registerRecipeItem(registry, MachineType.METALLURGIC_INFUSER, Recipe.METALLURGIC_INFUSER);
        registerRecipeItem(registry, MachineType.CHEMICAL_CRYSTALLIZER, Recipe.CHEMICAL_CRYSTALLIZER);
        registerRecipeItem(registry, MachineType.CHEMICAL_DISSOLUTION_CHAMBER, Recipe.CHEMICAL_DISSOLUTION_CHAMBER);
        registerRecipeItem(registry, MachineType.CHEMICAL_INFUSER, Recipe.CHEMICAL_INFUSER);
        registerRecipeItem(registry, MachineType.CHEMICAL_OXIDIZER, Recipe.CHEMICAL_OXIDIZER);
        registerRecipeItem(registry, MachineType.CHEMICAL_WASHER, Recipe.CHEMICAL_WASHER);
        registerRecipeItem(registry, MachineType.SOLAR_NEUTRON_ACTIVATOR, Recipe.SOLAR_NEUTRON_ACTIVATOR);
        registerRecipeItem(registry, MachineType.ELECTROLYTIC_SEPARATOR, Recipe.ELECTROLYTIC_SEPARATOR);
        registerRecipeItem(registry, MachineType.PRESSURIZED_REACTION_CHAMBER, Recipe.PRESSURIZED_REACTION_CHAMBER);
        registry.addRecipeCatalyst(MachineType.ROTARY_CONDENSENTRATOR.getStack(),
              RotaryCondensentratorRecipeCategory.UID_COND, RotaryCondensentratorRecipeCategory.UID_DECOND);

        registry.addRecipeCatalyst(BasicBlockType.THERMAL_EVAPORATION_CONTROLLER.getStack(1),
              Recipe.THERMAL_EVAPORATION_PLANT.jeiRecipeUid);
        registry.addRecipeCatalyst(MachineType.FORMULAIC_ASSEMBLICATOR.getStack(), VanillaRecipeCategoryUid.CRAFTING);

        registry.getRecipeTransferRegistry()
              .addRecipeTransferHandler(ContainerFormulaicAssemblicator.class, VanillaRecipeCategoryUid.CRAFTING, 20, 9,
                    35, 36);
        registry.getRecipeTransferRegistry()
              .addRecipeTransferHandler(ContainerRobitInventory.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36);
    }

    private void registerRecipeItem(IModRegistry registry, MachineType type, Recipe<?, ?, ?> recipeType) {
        if (!type.isEnabled()) {
            return;
        }
        registry.addRecipeCatalyst(type.getStack(), recipeType.jeiRecipeUid);
        RecipeType factoryType = null;
        for (RecipeType t : RecipeType.values()) {
            if (t.getType() == type) {
                factoryType = t;
                break;
            }
        }
        if (factoryType != null) {
            for (Tier.FactoryTier tier : Tier.FactoryTier.values()) {
                if (tier.machineType.isEnabled()) {
                    registry.addRecipeCatalyst(MekanismUtils.getFactory(tier, factoryType), recipeType.jeiRecipeUid);
                }
            }
        }
    }

    private void addRecipes(IModRegistry registry, Recipe type, Class<?> recipe,
          Class<? extends IRecipeWrapper> wrapper, String recipeCategoryUid) {
        addRecipes(registry, type.get().values(), recipe, wrapper, recipeCategoryUid);
    }

    private void addRecipes(IModRegistry registry, Collection recipeList, Class<?> recipe,
          Class<? extends IRecipeWrapper> wrapper, String recipeCategoryUid) {
        List<IRecipeWrapper> recipes = new ArrayList<>();

        //add all recipes with wrapper to the list
        for (Object obj : recipeList) {
            if (obj instanceof MachineRecipe) {
                try {
                    recipes.add(wrapper.getConstructor(recipe).newInstance(obj));
                } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    Mekanism.logger.fatal("Error registering JEI recipe: " + recipe.getName(), e);
                }
            }
        }

        registry.addRecipes(recipes, recipeCategoryUid);
    }

    private void registerVanillaSmeltingRecipeCatalyst(IModRegistry registry) {
        registry.addRecipeCatalyst(MachineType.ENERGIZED_SMELTER.getStack(), VanillaRecipeCategoryUid.SMELTING);
        for (Tier.FactoryTier tier : Tier.FactoryTier.values()) {
            if (tier.machineType.isEnabled()) {
                registry.addRecipeCatalyst(MekanismUtils.getFactory(tier, RecipeType.SMELTING),
                      VanillaRecipeCategoryUid.SMELTING);
            }
        }
    }
}
