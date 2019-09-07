package mekanism.common.integration.crafttweaker.commands;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.mc1120.commands.CraftTweakerCommand;
import crafttweaker.mc1120.commands.SpecialMessagesChat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.IMekanismRecipe;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.common.integration.crafttweaker.helpers.RecipeInfoHelper;
import mekanism.common.recipe.RecipeHandler.Recipe;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

public class MekRecipesCommand extends CraftTweakerCommand {

    private final List<String> subCommands;

    public MekRecipesCommand() {
        super("mekrecipes");
        subCommands = Stream.of("crystallizer", "dissolution", "chemicalInfuser", "injection", "oxidizer", "washer", "combiner", "crusher", "separator", "smelter",
              "enrichment", "metallurgicInfuser", "compressor", "sawmill", "prc", "purification", "solarneutronactivator", "thermalevaporation").collect(Collectors.toList());
    }

    @Override
    protected void init() {
        setDescription(SpecialMessagesChat.getClickableCommandText(TextFormatting.DARK_GREEN + "/ct " + subCommandName + "<type>", "/ct " + subCommandName, true),
              SpecialMessagesChat.getNormalMessage(TextFormatting.DARK_AQUA + "Outputs a list of all registered Mekanism Machine recipes to the crafttweaker.log for the given machine type."));
    }

    @Override
    public void executeCommand(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length == 0 || !subCommands.contains(args[0])) {
            sender.sendMessage(SpecialMessagesChat.getNormalMessage("Recipe Type required, pick one of the following: " + Arrays.toString(subCommands.toArray())));
            return;
        }
        String subCommand = args[0];
        CraftTweakerAPI.logCommand(subCommand + ":");
        Recipe<? extends IMekanismRecipe> type;
        switch (subCommand) {
            case "crystallizer":
                type = Recipe.CHEMICAL_CRYSTALLIZER;
                for (ChemicalCrystallizerRecipe recipe : Recipe.CHEMICAL_CRYSTALLIZER.get()) {
                    CraftTweakerAPI.logCommand(String.format("mods.mekanism.chemical.crystallizer.addRecipe(%s, %s)",
                          RecipeInfoHelper.getGasName(recipe.getInput().ingredient),
                          RecipeInfoHelper.getItemName(recipe.getOutput().output)
                    ));
                }
                break;
            case "dissolution":
                type = Recipe.CHEMICAL_DISSOLUTION_CHAMBER;
                for (ItemStackToGasRecipe recipe : Recipe.CHEMICAL_DISSOLUTION_CHAMBER.get()) {
                    CraftTweakerAPI.logCommand(String.format("mods.mekanism.chemical.dissolution.addRecipe(%s, %s)",
                          RecipeInfoHelper.getItemName(recipe.getInput().ingredient),
                          RecipeInfoHelper.getGasName(recipe.getOutputDefinition())
                    ));
                }
                break;
            case "chemicalInfuser":
                type = Recipe.CHEMICAL_INFUSER;
                for (ChemicalInfuserRecipe recipe : Recipe.CHEMICAL_INFUSER.get()) {
                    CraftTweakerAPI.logCommand(String.format("mods.mekanism.chemical.infuser.addRecipe(%s, %s, %s)",
                          RecipeInfoHelper.getGasName(recipe.getLeftInput().leftGas),
                          RecipeInfoHelper.getGasName(recipe.getRightInput().rightGas),
                          RecipeInfoHelper.getGasName(recipe.getOutput().output)
                    ));
                }
                break;
            case "injection":
                type = Recipe.CHEMICAL_INJECTION_CHAMBER;
                for (ItemStackGasToItemStackRecipe recipe : Recipe.CHEMICAL_INJECTION_CHAMBER.get()) {
                    CraftTweakerAPI.logCommand(String.format("mods.mekanism.chemical.injection.addRecipe(%s, %s, %s)",
                          RecipeInfoHelper.getItemName(recipe.getInput().itemStack),
                          RecipeInfoHelper.getGasName(recipe.getInput().gasType),
                          RecipeInfoHelper.getItemName(recipe.getOutput().output)
                    ));
                }
                break;
            case "oxidizer":
                type = Recipe.CHEMICAL_OXIDIZER;
                for (ItemStackToGasRecipe recipe : Recipe.CHEMICAL_OXIDIZER.get()) {
                    CraftTweakerAPI.logCommand(String.format("mods.mekanism.chemical.oxidizer.addRecipe(%s, %s)",
                          RecipeInfoHelper.getItemName(recipe.getInput().ingredient),
                          RecipeInfoHelper.getGasName(recipe.getOutput().output)
                    ));
                }
                break;
            case "washer":
                type = Recipe.CHEMICAL_WASHER;
                for (GasToGasRecipe recipe : Recipe.CHEMICAL_WASHER.get()) {
                    CraftTweakerAPI.logCommand(String.format("mods.mekanism.chemical.washer.addRecipe(%s, %s)",
                          RecipeInfoHelper.getGasName(recipe.getInput().ingredient),
                          RecipeInfoHelper.getGasName(recipe.getOutput().output)
                    ));
                }
                break;
            case "combiner":
                type = Recipe.COMBINER;
                for (CombinerRecipe recipe : Recipe.COMBINER.get()) {
                    CraftTweakerAPI.logCommand(String.format("mods.mekanism.combiner.addRecipe(%s, %s, %s)",
                          RecipeInfoHelper.getItemName(recipe.getInput().itemStack),
                          RecipeInfoHelper.getItemName(recipe.getInput().extraStack),
                          RecipeInfoHelper.getItemName(recipe.getOutput().output)
                    ));
                }
                break;
            case "crusher":
                type = Recipe.CRUSHER;
                for (ItemStackToItemStackRecipe recipe : Recipe.CRUSHER.get()) {
                    CraftTweakerAPI.logCommand(String.format("mods.mekanism.crusher.addRecipe(%s, %s)",
                          RecipeInfoHelper.getItemName(recipe.getInput().ingredient),
                          RecipeInfoHelper.getItemName(recipe.getOutput().output)
                    ));
                }
                break;
            case "separator":
                type = Recipe.ELECTROLYTIC_SEPARATOR;
                for (ElectrolysisRecipe recipe : Recipe.ELECTROLYTIC_SEPARATOR.get()) {
                    CraftTweakerAPI.logCommand(String.format("mods.mekanism.separator.addRecipe(%s, %s, %s, %s)",
                          RecipeInfoHelper.getFluidName(recipe.getInput().ingredient),
                          recipe.getEnergyUsage(),
                          RecipeInfoHelper.getGasName(recipe.getOutput().leftGas),
                          RecipeInfoHelper.getGasName(recipe.getOutput().rightGas)
                    ));
                }
                break;
            case "smelter":
                type = Recipe.ENERGIZED_SMELTER;
                for (ItemStackToItemStackRecipe recipe : Recipe.ENERGIZED_SMELTER.get()) {
                    CraftTweakerAPI.logCommand(String.format("mods.mekanism.smelter.addRecipe(%s, %s)",
                          RecipeInfoHelper.getItemName(recipe.getInput().ingredient),
                          RecipeInfoHelper.getItemName(recipe.getOutput().output)
                    ));
                }
                break;
            case "enrichment":
                type = Recipe.ENRICHMENT_CHAMBER;
                for (ItemStackToItemStackRecipe recipe : Recipe.ENRICHMENT_CHAMBER.get()) {
                    CraftTweakerAPI.logCommand(String.format("mods.mekanism.enrichment.addRecipe(%s, %s)",
                          RecipeInfoHelper.getItemName(recipe.getInput().ingredient),
                          RecipeInfoHelper.getItemName(recipe.getOutput().output)
                    ));
                }
                break;
            case "metallurgicInfuser":
                type = Recipe.METALLURGIC_INFUSER;
                for (MetallurgicInfuserRecipe recipe : Recipe.METALLURGIC_INFUSER.get()) {
                    CraftTweakerAPI.logCommand(String.format("mods.mekanism.infuser.addRecipe(%s, %s, %s, %s)",
                          recipe.getInput().infuse.getType(),
                          recipe.getInput().infuse.getAmount(),
                          RecipeInfoHelper.getItemName(recipe.getInput().inputStack),
                          RecipeInfoHelper.getItemName(recipe.getOutput().output)
                    ));
                }
                break;
            case "compressor":
                type = Recipe.OSMIUM_COMPRESSOR;
                for (ItemStackGasToItemStackRecipe recipe : Recipe.OSMIUM_COMPRESSOR.get()) {
                    CraftTweakerAPI.logCommand(String.format("mods.mekanism.compressor.addRecipe(%s, %s, %s)",
                          RecipeInfoHelper.getItemName(recipe.getInput().itemStack),
                          RecipeInfoHelper.getGasName(recipe.getInput().gasType),
                          RecipeInfoHelper.getItemName(recipe.getOutput().output)
                    ));
                }
                break;
            case "sawmill":
                type = Recipe.PRECISION_SAWMILL;
                for (SawmillRecipe recipe : Recipe.PRECISION_SAWMILL.get()) {
                    CraftTweakerAPI.logCommand(String.format("mods.mekanism.sawmill.addRecipe(%s, %s, %s, %s)",
                          RecipeInfoHelper.getItemName(recipe.getInput().ingredient),
                          RecipeInfoHelper.getItemName(recipe.getOutput().primaryOutput),
                          RecipeInfoHelper.getItemName(recipe.getOutput().secondaryOutput),
                          recipe.getOutput().secondaryChance
                    ));
                }
                break;
            case "prc":
                type = Recipe.PRESSURIZED_REACTION_CHAMBER;
                for (PressurizedReactionRecipe recipe : Recipe.PRESSURIZED_REACTION_CHAMBER.get()) {
                    CraftTweakerAPI.logCommand(String.format("mods.mekanism.reaction.addRecipe(%s, %s, %s, %s, %s, %s, %s)",
                          RecipeInfoHelper.getItemName(recipe.getInput().getSolid()),
                          RecipeInfoHelper.getFluidName(recipe.getInput().getFluid()),
                          RecipeInfoHelper.getGasName(recipe.getInput().getGas()),
                          RecipeInfoHelper.getItemName(recipe.getOutput().getItemOutput()),
                          RecipeInfoHelper.getGasName(recipe.getOutput().getGasOutput()),
                          recipe.getEnergyRequired(),
                          recipe.getDuration()
                    ));
                }
                break;
            case "purification":
                type = Recipe.PURIFICATION_CHAMBER;
                for (ItemStackGasToItemStackRecipe recipe : Recipe.PURIFICATION_CHAMBER.get()) {
                    CraftTweakerAPI.logCommand(String.format("mods.mekanism.purification.addRecipe(%s, %s, %s)",
                          RecipeInfoHelper.getItemName(recipe.getInput().itemStack),
                          RecipeInfoHelper.getGasName(recipe.getInput().gasType),
                          RecipeInfoHelper.getItemName(recipe.getOutput().output)
                    ));
                }
                break;
            case "solarneutronactivator":
                type = Recipe.SOLAR_NEUTRON_ACTIVATOR;
                for (GasToGasRecipe recipe : Recipe.SOLAR_NEUTRON_ACTIVATOR.get()) {
                    CraftTweakerAPI.logCommand(String.format("mods.mekanism.solarneutronactivator.addRecipe(%s, %s)",
                          RecipeInfoHelper.getGasName(recipe.getInput().ingredient),
                          RecipeInfoHelper.getGasName(recipe.getOutput().output)
                    ));
                }
                break;
            case "thermalevaporation":
                type = Recipe.THERMAL_EVAPORATION_PLANT;
                for (FluidToFluidRecipe recipe : Recipe.THERMAL_EVAPORATION_PLANT.get()) {
                    CraftTweakerAPI.logCommand(String.format("mods.mekanism.thermalevaporation.addRecipe(%s, %s)",
                          RecipeInfoHelper.getFluidName(recipe.getInput().ingredient),
                          RecipeInfoHelper.getFluidName(recipe.getOutput().output)
                    ));
                }
                break;
            default:
                sender.sendMessage(SpecialMessagesChat.getNormalMessage("Recipe Type required, pick one of the following: " + Arrays.toString(subCommands.toArray())));
                return;
        }

        sender.sendMessage(SpecialMessagesChat.getLinkToCraftTweakerLog("List of " + type.get().size() + " " + subCommand + " recipes generated;", sender));
    }

    @Override
    public List<String> getSubSubCommand(MinecraftServer server, ICommandSender sender, String[] args,
          @Nullable BlockPos targetPos) {
        if (args.length > 1) {
            return Collections.emptyList();
        }
        String start = args[0].toLowerCase();
        return subCommands.stream().filter(command -> command.toLowerCase().startsWith(start)).collect(Collectors.toList());
    }
}