package mekanism.common.integration.crafttweaker.commands;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.impl.commands.CTCommands.CommandImpl;
import com.mojang.brigadier.context.CommandContext;
import java.util.Arrays;
import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.common.integration.crafttweaker.helpers.RecipeInfoHelper;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.machines.ChemicalInfuserRecipe;
import mekanism.common.recipe.machines.CombinerRecipe;
import mekanism.common.recipe.machines.CrusherRecipe;
import mekanism.common.recipe.machines.CrystallizerRecipe;
import mekanism.common.recipe.machines.DissolutionRecipe;
import mekanism.common.recipe.machines.EnrichmentRecipe;
import mekanism.common.recipe.machines.InjectionRecipe;
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
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.ITextComponent;

public class MekRecipesCommand extends CommandImpl {

    private static final List<String> subCommands = Arrays.asList("crystallizer", "dissolution", "chemicalInfuser", "injection", "oxidizer", "washer", "combiner", "crusher",
          "separator", "smelter", "enrichment", "metallurgicInfuser", "compressor", "sawmill", "prc", "purification", "solarneutronactivator", "thermalevaporation");

    public MekRecipesCommand() {
        super("mekrecipes", "Outputs a list of all registered Mekanism Machine recipes to the crafttweaker.log for the given machine type.",
              MekRecipesCommand::executeCommand);
    }

    private static int executeCommand(CommandContext<CommandSource> context) {
        //TODO: Rework this command unless support for sub commands gets added to CrT again
        /*if (args.length == 0 || !subCommands.contains(args[0])) {
            sender.sendMessage(SpecialMessagesChat.getNormalMessage("Recipe Type required, pick one of the following: " + Arrays.toString(subCommands.toArray())));
            return;
        }*/
        String subCommand = context.getInput();//args[0];
        CraftTweakerAPI.logInfo(subCommand + ":");
        Recipe type;
        switch (subCommand) {
            case "crystallizer":
                type = Recipe.CHEMICAL_CRYSTALLIZER;
                for (CrystallizerRecipe recipe : Recipe.CHEMICAL_CRYSTALLIZER.get().values()) {
                    CraftTweakerAPI.logInfo(String.format("mods.mekanism.chemical.crystallizer.addRecipe(%s, %s)",
                          RecipeInfoHelper.getGasName(recipe.getInput().ingredient),
                          RecipeInfoHelper.getItemName(recipe.getOutput().output)
                    ));
                }
                break;
            case "dissolution":
                type = Recipe.CHEMICAL_DISSOLUTION_CHAMBER;
                for (DissolutionRecipe recipe : Recipe.CHEMICAL_DISSOLUTION_CHAMBER.get().values()) {
                    CraftTweakerAPI.logInfo(String.format("mods.mekanism.chemical.dissolution.addRecipe(%s, %s)",
                          RecipeInfoHelper.getItemName(recipe.getInput().ingredient),
                          RecipeInfoHelper.getGasName(recipe.getOutput().output)
                    ));
                }
                break;
            case "chemicalInfuser":
                type = Recipe.CHEMICAL_INFUSER;
                for (ChemicalInfuserRecipe recipe : Recipe.CHEMICAL_INFUSER.get().values()) {
                    CraftTweakerAPI.logInfo(String.format("mods.mekanism.chemical.infuser.addRecipe(%s, %s, %s)",
                          RecipeInfoHelper.getGasName(recipe.getInput().leftGas),
                          RecipeInfoHelper.getGasName(recipe.getInput().rightGas),
                          RecipeInfoHelper.getGasName(recipe.getOutput().output)
                    ));
                }
                break;
            case "injection":
                type = Recipe.CHEMICAL_INJECTION_CHAMBER;
                for (InjectionRecipe recipe : Recipe.CHEMICAL_INJECTION_CHAMBER.get().values()) {
                    CraftTweakerAPI.logInfo(String.format("mods.mekanism.chemical.injection.addRecipe(%s, %s, %s)",
                          RecipeInfoHelper.getItemName(recipe.getInput().itemStack),
                          RecipeInfoHelper.getGasName(recipe.getInput().gasType),
                          RecipeInfoHelper.getItemName(recipe.getOutput().output)
                    ));
                }
                break;
            case "oxidizer":
                type = Recipe.CHEMICAL_OXIDIZER;
                for (OxidationRecipe recipe : Recipe.CHEMICAL_OXIDIZER.get().values()) {
                    CraftTweakerAPI.logInfo(String.format("mods.mekanism.chemical.oxidizer.addRecipe(%s, %s)",
                          RecipeInfoHelper.getItemName(recipe.getInput().ingredient),
                          RecipeInfoHelper.getGasName(recipe.getOutput().output)
                    ));
                }
                break;
            case "washer":
                type = Recipe.CHEMICAL_WASHER;
                for (WasherRecipe recipe : Recipe.CHEMICAL_WASHER.get().values()) {
                    CraftTweakerAPI.logInfo(String.format("mods.mekanism.chemical.washer.addRecipe(%s, %s)",
                          RecipeInfoHelper.getGasName(recipe.getInput().ingredient),
                          RecipeInfoHelper.getGasName(recipe.getOutput().output)
                    ));
                }
                break;
            case "combiner":
                type = Recipe.COMBINER;
                for (CombinerRecipe recipe : Recipe.COMBINER.get().values()) {
                    CraftTweakerAPI.logInfo(String.format("mods.mekanism.combiner.addRecipe(%s, %s, %s)",
                          RecipeInfoHelper.getItemName(recipe.getInput().itemStack),
                          RecipeInfoHelper.getItemName(recipe.getInput().extraStack),
                          RecipeInfoHelper.getItemName(recipe.getOutput().output)
                    ));
                }
                break;
            case "crusher":
                type = Recipe.CRUSHER;
                for (CrusherRecipe recipe : Recipe.CRUSHER.get().values()) {
                    CraftTweakerAPI.logInfo(String.format("mods.mekanism.crusher.addRecipe(%s, %s)",
                          RecipeInfoHelper.getItemName(recipe.getInput().ingredient),
                          RecipeInfoHelper.getItemName(recipe.getOutput().output)
                    ));
                }
                break;
            case "separator":
                type = Recipe.ELECTROLYTIC_SEPARATOR;
                for (SeparatorRecipe recipe : Recipe.ELECTROLYTIC_SEPARATOR.get().values()) {
                    CraftTweakerAPI.logInfo(String.format("mods.mekanism.separator.addRecipe(%s, %s, %s, %s)",
                          RecipeInfoHelper.getFluidName(recipe.getInput().ingredient),
                          recipe.energyUsage,
                          RecipeInfoHelper.getGasName(recipe.getOutput().leftGas),
                          RecipeInfoHelper.getGasName(recipe.getOutput().rightGas)
                    ));
                }
                break;
            case "smelter":
                type = Recipe.ENERGIZED_SMELTER;
                for (SmeltingRecipe recipe : Recipe.ENERGIZED_SMELTER.get().values()) {
                    CraftTweakerAPI.logInfo(String.format("mods.mekanism.smelter.addRecipe(%s, %s)",
                          RecipeInfoHelper.getItemName(recipe.getInput().ingredient),
                          RecipeInfoHelper.getItemName(recipe.getOutput().output)
                    ));
                }
                break;
            case "enrichment":
                type = Recipe.ENRICHMENT_CHAMBER;
                for (EnrichmentRecipe recipe : Recipe.ENRICHMENT_CHAMBER.get().values()) {
                    CraftTweakerAPI.logInfo(String.format("mods.mekanism.enrichment.addRecipe(%s, %s)",
                          RecipeInfoHelper.getItemName(recipe.getInput().ingredient),
                          RecipeInfoHelper.getItemName(recipe.getOutput().output)
                    ));
                }
                break;
            case "metallurgicInfuser":
                type = Recipe.METALLURGIC_INFUSER;
                for (MetallurgicInfuserRecipe recipe : Recipe.METALLURGIC_INFUSER.get().values()) {
                    CraftTweakerAPI.logInfo(String.format("mods.mekanism.infuser.addRecipe(%s, %s, %s, %s)",
                          recipe.getInput().infuse.getType(),
                          recipe.getInput().infuse.getAmount(),
                          RecipeInfoHelper.getItemName(recipe.getInput().inputStack),
                          RecipeInfoHelper.getItemName(recipe.getOutput().output)
                    ));
                }
                break;
            case "compressor":
                type = Recipe.OSMIUM_COMPRESSOR;
                for (OsmiumCompressorRecipe recipe : Recipe.OSMIUM_COMPRESSOR.get().values()) {
                    CraftTweakerAPI.logInfo(String.format("mods.mekanism.compressor.addRecipe(%s, %s, %s)",
                          RecipeInfoHelper.getItemName(recipe.getInput().itemStack),
                          RecipeInfoHelper.getGasName(recipe.getInput().gasType),
                          RecipeInfoHelper.getItemName(recipe.getOutput().output)
                    ));
                }
                break;
            case "sawmill":
                type = Recipe.PRECISION_SAWMILL;
                for (SawmillRecipe recipe : Recipe.PRECISION_SAWMILL.get().values()) {
                    CraftTweakerAPI.logInfo(String.format("mods.mekanism.sawmill.addRecipe(%s, %s, %s, %s)",
                          RecipeInfoHelper.getItemName(recipe.getInput().ingredient),
                          RecipeInfoHelper.getItemName(recipe.getOutput().primaryOutput),
                          RecipeInfoHelper.getItemName(recipe.getOutput().secondaryOutput),
                          recipe.getOutput().secondaryChance
                    ));
                }
                break;
            case "prc":
                type = Recipe.PRESSURIZED_REACTION_CHAMBER;
                for (PressurizedRecipe recipe : Recipe.PRESSURIZED_REACTION_CHAMBER.get().values()) {
                    CraftTweakerAPI.logInfo(String.format("mods.mekanism.reaction.addRecipe(%s, %s, %s, %s, %s, %s, %s)",
                          RecipeInfoHelper.getItemName(recipe.getInput().getSolid()),
                          RecipeInfoHelper.getFluidName(recipe.getInput().getFluid()),
                          RecipeInfoHelper.getGasName(recipe.getInput().getGas()),
                          RecipeInfoHelper.getItemName(recipe.getOutput().getItemOutput()),
                          RecipeInfoHelper.getGasName(recipe.getOutput().getGasOutput()),
                          recipe.extraEnergy,
                          recipe.ticks
                    ));
                }
                break;
            case "purification":
                type = Recipe.PURIFICATION_CHAMBER;
                for (PurificationRecipe recipe : Recipe.PURIFICATION_CHAMBER.get().values()) {
                    CraftTweakerAPI.logInfo(String.format("mods.mekanism.purification.addRecipe(%s, %s, %s)",
                          RecipeInfoHelper.getItemName(recipe.getInput().itemStack),
                          RecipeInfoHelper.getGasName(recipe.getInput().gasType),
                          RecipeInfoHelper.getItemName(recipe.getOutput().output)
                    ));
                }
                break;
            case "solarneutronactivator":
                type = Recipe.SOLAR_NEUTRON_ACTIVATOR;
                for (SolarNeutronRecipe recipe : Recipe.SOLAR_NEUTRON_ACTIVATOR.get().values()) {
                    CraftTweakerAPI.logInfo(String.format("mods.mekanism.solarneutronactivator.addRecipe(%s, %s)",
                          RecipeInfoHelper.getGasName(recipe.getInput().ingredient),
                          RecipeInfoHelper.getGasName(recipe.getOutput().output)
                    ));
                }
                break;
            case "thermalevaporation":
                type = Recipe.THERMAL_EVAPORATION_PLANT;
                for (ThermalEvaporationRecipe recipe : Recipe.THERMAL_EVAPORATION_PLANT.get().values()) {
                    CraftTweakerAPI.logInfo(String.format("mods.mekanism.thermalevaporation.addRecipe(%s, %s)",
                          RecipeInfoHelper.getFluidName(recipe.getInput().ingredient),
                          RecipeInfoHelper.getFluidName(recipe.getOutput().output)
                    ));
                }
                break;
            default:
                ITextComponent message = TextComponentUtil.build(EnumColor.RED,
                      "Recipe Type required, pick one of the following: " + Arrays.toString(subCommands.toArray()));
                context.getSource().sendFeedback(message, true);
                CraftTweakerAPI.logInfo(message.getFormattedText());
                return 1;
        }
        ITextComponent message = TextComponentUtil.build(EnumColor.BRIGHT_GREEN,
              "List of " + type.get().size() + " " + subCommand + " recipes generated! Check the crafttweaker.log file!");
        context.getSource().sendFeedback(message, true);
        CraftTweakerAPI.logInfo(message.getFormattedText());
        return 0;
    }
}