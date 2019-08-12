package mekanism.api;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;

public class MekanismAPI {

    //TODO: Add back support for the other mods API as needed, ideally would be through gradle

    /**
     * The version of the api classes - may not always match the mod's version
     */
    public static final String API_VERSION = "9.8.1";

    /**
     * Mekanism debug mode
     */
    public static boolean debug = false;
    //Add a BlockInfo value here if you don't want a certain block to be picked up by cardboard boxes
    private static Set<Block> cardboardBoxIgnore = new HashSet<>();
    //Ignore all mod blocks
    private static Set<String> cardboardBoxModIgnore = new HashSet<>();
    private static MekanismRecipeHelper helper = null;

    public static boolean isBlockCompatible(@Nonnull Block block) {
        if (cardboardBoxModIgnore.contains(Objects.requireNonNull(block.getRegistryName()).getNamespace())) {
            return false;
        }
        return cardboardBoxIgnore.stream().noneMatch(i -> i == block);
    }

    public static void addBoxBlacklist(@Nullable Block block) {
        //Allow block to be null but don't do anything if it is
        if (block != null) {
            cardboardBoxIgnore.add(block);
        }
    }

    public static void removeBoxBlacklist(@Nonnull Block block) {
        cardboardBoxIgnore.remove(block);
    }

    public static Set<Block> getBoxIgnore() {
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

    public static void addBoxBlacklistMod(@Nonnull String modid) {
        cardboardBoxModIgnore.add(modid);
    }

    public static void removeBoxBlacklistMod(@Nonnull String modid) {
        cardboardBoxModIgnore.remove(modid);
    }

    public static Set<String> getBoxModIgnore() {
        return cardboardBoxModIgnore;
    }

    public static class BoxBlacklistEvent extends Event {

        public void blacklist(@Nonnull ResourceLocation blockLocation) {
            blacklist(ForgeRegistries.BLOCKS.getValue(blockLocation));
        }

        public void blacklist(@Nullable Block block) {
            addBoxBlacklist(block);
        }

        public void blacklistMod(@Nonnull String modid) {
            addBoxBlacklistMod(modid);
        }

        public void removeBlacklist(@Nonnull Block block) {
            removeBoxBlacklist(block);
        }

        public void removeModBlacklist(@Nonnull String modid) {
            removeBoxBlacklistMod(modid);
        }
    }
}