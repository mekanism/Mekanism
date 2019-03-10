package mekanism.api;

import java.util.HashSet;
import java.util.Set;
import mekanism.api.util.BlockInfo;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;

public class MekanismAPI {

    /**
     * Mekanism debug mode
     */
    public static boolean debug = false;
    //Add a BlockInfo value here if you don't want a certain block to be picked up by cardboard boxes
    private static Set<BlockInfo> cardboardBoxIgnore = new HashSet<>();
    private static MekanismRecipeHelper helper = null;

    public static boolean isBlockCompatible(Block block, int meta) {
        for (BlockInfo i : cardboardBoxIgnore) {
            if (i.block == block && (i.meta == OreDictionary.WILDCARD_VALUE || i.meta == meta)) {
                return false;
            }
        }

        return true;
    }

    public static void addBoxBlacklist(Block block, int meta) {
        cardboardBoxIgnore.add(new BlockInfo(block, meta));
    }

    public static void removeBoxBlacklist(Block block, int meta) {
        cardboardBoxIgnore.remove(new BlockInfo(block, meta));
    }

    public static Set<BlockInfo> getBoxIgnore() {
        return cardboardBoxIgnore;
    }

    /**
     * Get the instance of the recipe helper to directly add recipes.
     *
     * Do NOT copy/repackage this method into your package, nor use the class directly as it may change.
     *
     * @return {@link MekanismRecipeHelper} The handler.
     */
    public static MekanismRecipeHelper recipeHelper() {
        if (helper == null) {
            try {
                helper = (MekanismRecipeHelper) Class.forName("mekanism.common.recipe.APIHandler").newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                LogManager.getLogger("MekanismAPI").error("Could not find API Handler", e);
            }
        }
        return helper;
    }

    public static class BoxBlacklistEvent extends Event {

    }
}
