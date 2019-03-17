package mekanism.common.integration.crafttweaker.commands;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.mc1120.commands.CraftTweakerCommand;
import crafttweaker.mc1120.commands.SpecialMessagesChat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
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
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

public class MekRecipesCommand extends CraftTweakerCommand {

    private final List<String> subCommands;

    public MekRecipesCommand() {
        super("mekrecipes");
        subCommands = Stream.of("crystallizer", "dissolution", "chemicalInfuser", "injection", "oxidizer", "washer",
              "combiner", "crusher", "separator", "smelter", "enrichment", "metallurgicInfuser", "compressor",
              "sawmill", "prc", "purification", "solarneutronactivator", "thermalevaporation")
              .collect(Collectors.toList());
    }

    @Override
    protected void init() {
        setDescription(SpecialMessagesChat.getClickableCommandText(
              TextFormatting.DARK_GREEN + "/ct " + subCommandName + "<type>", "/ct " + subCommandName, true),
              SpecialMessagesChat.getNormalMessage(TextFormatting.DARK_AQUA +
                    "Outputs a list of all registered commands for to the crafttweaker.log"));
    }

    @Override
    public void executeCommand(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length == 0 || !subCommands.contains(args[0])) {
            sender.sendMessage(SpecialMessagesChat.getNormalMessage(
                  "Recipe Type required, pick one of the following: " + Arrays.toString(subCommands.toArray())));
            return;
        }
        String subCommand = args[0];
        CraftTweakerAPI.logCommand(subCommand + ":");
        Collection values = Collections.emptyList();
        switch (subCommand) {
            case "crystallizer":
                values = Recipe.CHEMICAL_CRYSTALLIZER.get().values();
                for (Object value : values) {
                    if (value instanceof CrystallizerRecipe) {
                        CrystallizerRecipe recipe = ((CrystallizerRecipe) value);
                        CraftTweakerAPI
                              .logCommand(String.format("mods.mekanism.chemical.crystallizer.addRecipe(%s, %s)",
                                    RecipeInfoHelper.getGasName(recipe.getInput().ingredient),
                                    RecipeInfoHelper.getItemName(recipe.getOutput().output)
                              ));
                    }
                }
                break;
            case "dissolution":
                values = Recipe.CHEMICAL_DISSOLUTION_CHAMBER.get().values();
                for (Object value : values) {
                    if (value instanceof DissolutionRecipe) {
                        DissolutionRecipe recipe = (DissolutionRecipe) value;
                        CraftTweakerAPI.logCommand(String.format("mods.mekanism.chemical.dissolution.addRecipe(%s, %s)",
                              RecipeInfoHelper.getItemName(recipe.getInput().ingredient),
                              RecipeInfoHelper.getGasName(recipe.getOutput().output)
                        ));
                    }
                }
                break;
            case "chemicalInfuser":
                values = Recipe.METALLURGIC_INFUSER.get().values();
                for (Object value : values) {
                    if (value instanceof ChemicalInfuserRecipe) {
                        ChemicalInfuserRecipe recipe = (ChemicalInfuserRecipe) value;
                        CraftTweakerAPI.logCommand(String.format("mods.mekanism.chemical.infuser.addRecipe(%s, %s, %s)",
                              RecipeInfoHelper.getGasName(recipe.getInput().leftGas),
                              RecipeInfoHelper.getGasName(recipe.getInput().rightGas),
                              RecipeInfoHelper.getGasName(recipe.getOutput().output)
                        ));
                    }
                }
                break;
            case "injection":
                values = Recipe.CHEMICAL_INJECTION_CHAMBER.get().values();
                for (Object value : values) {
                    if (value instanceof InjectionRecipe) {
                        InjectionRecipe recipe = (InjectionRecipe) value;
                        CraftTweakerAPI
                              .logCommand(String.format("mods.mekanism.chemical.injection.addRecipe(%s, %s, %s)",
                                    RecipeInfoHelper.getItemName(recipe.getInput().itemStack),
                                    RecipeInfoHelper.getGasName(recipe.getInput().gasType),
                                    RecipeInfoHelper.getItemName(recipe.getOutput().output)
                              ));
                    }
                }
                break;
            case "oxidizer":
                values = Recipe.CHEMICAL_OXIDIZER.get().values();
                for (Object value : values) {
                    if (value instanceof OxidationRecipe) {
                        OxidationRecipe recipe = (OxidationRecipe) value;
                        CraftTweakerAPI.logCommand(String.format("mods.mekanism.chemical.oxidizer.addRecipe(%s, %s)",
                              RecipeInfoHelper.getItemName(recipe.getInput().ingredient),
                              RecipeInfoHelper.getGasName(recipe.getOutput().output)
                        ));
                    }
                }
                break;
            case "washer":
                values = Recipe.CHEMICAL_WASHER.get().values();
                for (Object value : values) {
                    if (value instanceof WasherRecipe) {
                        WasherRecipe recipe = (WasherRecipe) value;
                        CraftTweakerAPI.logCommand(String.format("mods.mekanism.chemical.washer.addRecipe(%s, %s)",
                              RecipeInfoHelper.getGasName(recipe.getInput().ingredient),
                              RecipeInfoHelper.getGasName(recipe.getOutput().output)
                        ));
                    }
                }
                break;
            case "combiner":
                values = Recipe.COMBINER.get().values();
                for (Object value : values) {
                    if (value instanceof CombinerRecipe) {
                        CombinerRecipe recipe = (CombinerRecipe) value;
                        CraftTweakerAPI.logCommand(String.format("mods.mekanism.combiner.addRecipe(%s, %s, %s)",
                              RecipeInfoHelper.getItemName(recipe.getInput().itemStack),
                              RecipeInfoHelper.getItemName(recipe.getInput().extraStack),
                              RecipeInfoHelper.getItemName(recipe.getOutput().output)
                        ));
                    }
                }
                break;
            case "crusher":
                values = Recipe.CRUSHER.get().values();
                for (Object value : values) {
                    if (value instanceof CrusherRecipe) {
                        CrusherRecipe recipe = (CrusherRecipe) value;
                        CraftTweakerAPI.logCommand(String.format("mods.mekanism.crusher.addRecipe(%s, %s)",
                              RecipeInfoHelper.getItemName(recipe.getInput().ingredient),
                              RecipeInfoHelper.getItemName(recipe.getOutput().output)
                        ));
                    }
                }
                break;
            case "separator":
                values = Recipe.ELECTROLYTIC_SEPARATOR.get().values();
                for (Object value : values) {
                    if (value instanceof SeparatorRecipe) {
                        SeparatorRecipe recipe = (SeparatorRecipe) value;
                        CraftTweakerAPI.logCommand(String.format("mods.mekanism.separator.addRecipe(%s, %s, %s, %s)",
                              RecipeInfoHelper.getFluidName(recipe.getInput().ingredient),
                              recipe.energyUsage,
                              RecipeInfoHelper.getGasName(recipe.getOutput().leftGas),
                              RecipeInfoHelper.getGasName(recipe.getOutput().rightGas)
                        ));
                    }
                }
                break;
            case "smelter":
                values = Recipe.ENERGIZED_SMELTER.get().values();
                for (Object value : values) {
                    if (value instanceof SmeltingRecipe) {
                        SmeltingRecipe recipe = (SmeltingRecipe) value;
                        CraftTweakerAPI.logCommand(String.format("mods.mekanism.smelter.addRecipe(%s, %s)",
                              RecipeInfoHelper.getItemName(recipe.getInput().ingredient),
                              RecipeInfoHelper.getItemName(recipe.getOutput().output)
                        ));
                    }
                }
                break;
            case "enrichment":
                values = Recipe.ENRICHMENT_CHAMBER.get().values();
                for (Object value : values) {
                    if (value instanceof EnrichmentRecipe) {
                        EnrichmentRecipe recipe = (EnrichmentRecipe) value;
                        CraftTweakerAPI.logCommand(String.format("mods.mekanism.enrichment.addRecipe(%s, %s)",
                              RecipeInfoHelper.getItemName(recipe.getInput().ingredient),
                              RecipeInfoHelper.getItemName(recipe.getOutput().output)
                        ));
                    }
                }
                break;
            case "metallurgicInfuser":
                values = Recipe.METALLURGIC_INFUSER.get().values();
                for (Object value : values) {
                    if (value instanceof MetallurgicInfuserRecipe) {
                        MetallurgicInfuserRecipe recipe = (MetallurgicInfuserRecipe) value;
                        CraftTweakerAPI.logCommand(String.format("mods.mekanism.infuser.addRecipe(%s, %s, %s, %s)",
                              recipe.getInput().infuse.type,
                              recipe.getInput().infuse.amount,
                              RecipeInfoHelper.getItemName(recipe.getInput().inputStack),
                              RecipeInfoHelper.getItemName(recipe.getOutput().output)
                        ));
                    }
                }
                break;
            case "compressor":
                values = Recipe.OSMIUM_COMPRESSOR.get().values();
                for (Object value : values) {
                    if (value instanceof OsmiumCompressorRecipe) {
                        OsmiumCompressorRecipe recipe = (OsmiumCompressorRecipe) value;
                        CraftTweakerAPI.logCommand(String.format("mods.mekanism.compressor.addRecipe(%s, %s, %s)",
                              RecipeInfoHelper.getItemName(recipe.getInput().itemStack),
                              RecipeInfoHelper.getGasName(recipe.getInput().gasType),
                              RecipeInfoHelper.getItemName(recipe.getOutput().output)
                        ));
                    }
                }
                break;
            case "sawmill":
                values = Recipe.PRECISION_SAWMILL.get().values();
                for (Object value : values) {
                    if (value instanceof SawmillRecipe) {
                        SawmillRecipe recipe = (SawmillRecipe) value;
                        CraftTweakerAPI.logCommand(String.format("mods.mekanism.sawmill.addRecipe(%s, %s, %s, %s)",
                              RecipeInfoHelper.getItemName(recipe.getInput().ingredient),
                              RecipeInfoHelper.getItemName(recipe.getOutput().primaryOutput),
                              RecipeInfoHelper.getItemName(recipe.getOutput().secondaryOutput),
                              recipe.getOutput().secondaryChance
                        ));
                    }
                }
                break;
            case "prc":
                values = Recipe.PRESSURIZED_REACTION_CHAMBER.get().values();
                for (Object value : values) {
                    if (value instanceof PressurizedRecipe) {
                        PressurizedRecipe recipe = (PressurizedRecipe) value;
                        CraftTweakerAPI
                              .logCommand(String.format("mods.mekanism.reaction.addRecipe(%s, %s, %s, %s, %s, %s, %s)",
                                    RecipeInfoHelper.getItemName(recipe.getInput().getSolid()),
                                    RecipeInfoHelper.getFluidName(recipe.getInput().getFluid()),
                                    RecipeInfoHelper.getGasName(recipe.getInput().getGas()),
                                    RecipeInfoHelper.getItemName(recipe.getOutput().getItemOutput()),
                                    RecipeInfoHelper.getGasName(recipe.getOutput().getGasOutput()),
                                    recipe.extraEnergy,
                                    recipe.ticks
                              ));
                    }
                }
                break;
            case "purification":
                values = Recipe.PURIFICATION_CHAMBER.get().values();
                for (Object value : values) {
                    if (value instanceof PurificationRecipe) {
                        PurificationRecipe recipe = (PurificationRecipe) value;
                        CraftTweakerAPI.logCommand(String.format("mods.mekanism.purification.addRecipe(%s, %s, %s)",
                              RecipeInfoHelper.getItemName(recipe.getInput().itemStack),
                              RecipeInfoHelper.getGasName(recipe.getInput().gasType),
                              RecipeInfoHelper.getItemName(recipe.getOutput().output)
                        ));
                    }
                }
                break;
            case "solarneutronactivator":
                values = Recipe.SOLAR_NEUTRON_ACTIVATOR.get().values();
                for (Object value : values) {
                    if (value instanceof SolarNeutronRecipe) {
                        SolarNeutronRecipe recipe = (SolarNeutronRecipe) value;
                        CraftTweakerAPI
                              .logCommand(String.format("mods.mekanism.solarneutronactivator.addRecipe(%s, %s)",
                                    RecipeInfoHelper.getGasName(recipe.getInput().ingredient),
                                    RecipeInfoHelper.getGasName(recipe.getOutput().output)
                              ));
                    }
                }
                break;
            case "thermalevaporation":
                values = Recipe.THERMAL_EVAPORATION_PLANT.get().values();
                for (Object value : values) {
                    if (value instanceof ThermalEvaporationRecipe) {
                        ThermalEvaporationRecipe recipe = (ThermalEvaporationRecipe) value;
                        CraftTweakerAPI.logCommand(String.format("mods.mekanism.thermalevaporation.addRecipe(%s, %s)",
                              RecipeInfoHelper.getFluidName(recipe.getInput().ingredient),
                              RecipeInfoHelper.getFluidName(recipe.getOutput().output)
                        ));
                    }
                }
                break;
        }

        sender.sendMessage(SpecialMessagesChat
              .getLinkToCraftTweakerLog("List of " + values.size() + " " + subCommand + " recipes generated;", sender));
    }

    @Override
    public List<String> getSubSubCommand(MinecraftServer server, ICommandSender sender, String[] args,
          @Nullable BlockPos targetPos) {
        if (args.length > 1) {
            return Collections.emptyList();
        }
        String start = args[0].toLowerCase();
        return subCommands.stream().filter(command -> command.toLowerCase().startsWith(start))
              .collect(Collectors.toList());
    }
}