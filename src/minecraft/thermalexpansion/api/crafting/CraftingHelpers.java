
package thermalexpansion.api.crafting;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import thermalexpansion.api.item.ItemRegistry;

/**
 * This class adds some basic and fool-proof recipe handlers that can help you out. They will add
 * simple recipes that follow the TE defaults - they are not the only way of adding recipes. You can
 * also use the more specific recipe functions defined in the Manager interfaces if necessary. Once
 * again, only call these during @PostInit or things may not go so rosy.
 */
public class CraftingHelpers {

    private static ItemStack sawdust = ItemRegistry.getItem("sawdust", 1);
    private static ItemStack slag = ItemRegistry.getItem("slag", 1);
    private static ItemStack slagRich = ItemRegistry.getItem("slagRich", 1);
    private static ItemStack fluxSand = new ItemStack(Block.sand);

    /**
     * Ore x1 to Dust x2 conversion. 400 MJ. Will return false if recipe already exists.
     */
    public static boolean addPulverizerOreToDustRecipe(ItemStack inputOre, ItemStack outputDust) {

        ItemStack ore = inputOre.copy();
        ore.stackSize = 1;

        ItemStack primaryDust = outputDust.copy();
        primaryDust.stackSize = 2;

        return CraftingManagers.pulverizerManager.addRecipe(400, ore, primaryDust, false);
    }

    /**
     * Ore x1 to Dust x2 conversion, 10% chance of Secondary x1 being generated. 400 MJ. Will return
     * false if recipe already exists.
     */
    public static boolean addPulverizerOreToDustRecipe(ItemStack inputOre, ItemStack outputDust, ItemStack outputSecondary) {

        ItemStack ore = inputOre.copy();
        ore.stackSize = 1;

        ItemStack primaryDust = outputDust.copy();
        primaryDust.stackSize = 2;

        ItemStack secondary = outputSecondary.copy();
        secondary.stackSize = 1;

        return CraftingManagers.pulverizerManager.addRecipe(400, ore, primaryDust, secondary, 10, false);
    }

    /**
     * Log x1 to Plank x6 conversion, 100% chance of Sawdust. 80 MJ. Will return false if recipe
     * already exists.
     */
    public static boolean addSawmillLogToPlankRecipe(ItemStack inputLog, ItemStack outputPlanks) {

        ItemStack log = inputLog.copy();
        log.stackSize = 1;

        ItemStack planks = outputPlanks.copy();
        planks.stackSize = 6;

        return CraftingManagers.sawmillManager.addRecipe(80, log, planks, sawdust, false);
    }

    /**
     * Dust x2, Sand x1 to Ingot x2, 25% chance of Slag. 80 MJ. Will return false if recipe already
     * exists.
     */
    public static boolean addSmelterDustToIngotsRecipe(ItemStack inputDust, ItemStack outputIngots) {

        ItemStack dust = inputDust.copy();
        dust.stackSize = 2;

        ItemStack ingots = outputIngots.copy();
        ingots.stackSize = 2;

        return CraftingManagers.smelterManager.addRecipe(80, dust, fluxSand, ingots, slag, 25, false);
    }

    /**
     * Ore x1, Sand x1 to Ingot x2, 5% chance of Rich Slag. 320 MJ. Also, Ore x1, Rich Slag x1 to
     * Ingot x3, 75% chance of Slag. 400 MJ. Will return false if recipe already exists.
     */
    public static boolean addSmelterOreToIngotsRecipe(ItemStack inputOre, ItemStack outputIngots) {

        ItemStack ore = inputOre.copy();
        ore.stackSize = 1;

        ItemStack ingots2 = outputIngots.copy();
        ingots2.stackSize = 2;

        ItemStack ingots3 = outputIngots.copy();
        ingots3.stackSize = 3;

        if (!CraftingManagers.smelterManager.addRecipe(320, ore, fluxSand, ingots2, slagRich, 5, false)) {
            return false;
        }
        if (!CraftingManagers.smelterManager.addRecipe(400, ore, slagRich, ingots3, slag, 75, false)) {
            return false;
        }
        return true;
    }
}
