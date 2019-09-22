package mekanism.api;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.gas.EmptyGas;
import mekanism.api.gas.Gas;
import mekanism.api.infuse.EmptyInfuseType;
import mekanism.api.infuse.InfuseType;
import mekanism.api.providers.IBlockProvider;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//TODO: Refactor what packages various things are in
// Also move more things from main mekanism package to here, for example tier information and stuff
@Mod.EventBusSubscriber(modid = MekanismAPI.MEKANISM_MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MekanismAPI {

    //TODO: Add back support for the other mods API as needed, ideally would be through gradle

    /**
     * The version of the api classes - may not always match the mod's version
     */
    public static final String API_VERSION = "9.8.1";
    public static final String MEKANISM_MODID = "mekanism";

    public static Logger logger = LogManager.getLogger(MEKANISM_MODID + "_api");

    //TODO: Override these? And make it so that they return empty if there is no instance??
    public static IForgeRegistry<Gas> GAS_REGISTRY;
    public static IForgeRegistry<InfuseType> INFUSE_TYPE_REGISTRY;

    /**
     * Mekanism debug mode
     */
    public static boolean debug = false;
    //Add a BlockInfo value here if you don't want a certain block to be picked up by cardboard boxes
    private static Set<Block> cardboardBoxIgnore = new HashSet<>();
    //Ignore all mod blocks
    private static Set<String> cardboardBoxModIgnore = new HashSet<>();
    private static MekanismRecipeHelper helper = null;

    //TODO: Make a new empty gas
    @Nonnull
    public static final Gas EMPTY_GAS = new EmptyGas();
    @Nonnull
    public static final InfuseType EMPTY_INFUSE_TYPE = new EmptyInfuseType();

    @SubscribeEvent
    public static void buildRegistry(RegistryEvent.NewRegistry event) {
        GAS_REGISTRY = new RegistryBuilder<Gas>().setName(new ResourceLocation(MEKANISM_MODID, "gas")).setType(Gas.class).create();
        INFUSE_TYPE_REGISTRY = new RegistryBuilder<InfuseType>().setName(new ResourceLocation(MEKANISM_MODID, "infuse_type")).setType(InfuseType.class).create();
    }

    @SubscribeEvent
    public static void registerGases(RegistryEvent.Register<Gas> event) {
        //Register EMPTY Gas
        event.getRegistry().register(EMPTY_GAS);
    }

    @SubscribeEvent
    public static void registerInfuseTypes(RegistryEvent.Register<InfuseType> event) {
        //Register EMPTY InfuseType
        event.getRegistry().register(EMPTY_INFUSE_TYPE);
    }

    public static boolean isBlockCompatible(@Nonnull Block block) {
        if (cardboardBoxModIgnore.contains(Objects.requireNonNull(block.getRegistryName()).getNamespace())) {
            return false;
        }
        return cardboardBoxIgnore.stream().noneMatch(i -> i == block);
    }

    public static void addBoxBlacklist(@Nonnull IBlockProvider blockProvider) {
        addBoxBlacklist(blockProvider.getBlock());
    }

    public static void addBoxBlacklist(@Nullable Block block) {
        //Allow block to be null but don't do anything if it is
        if (block != null) {
            cardboardBoxIgnore.add(block);
        }
    }

    public static void removeBoxBlacklist(@Nonnull IBlockProvider blockProvider) {
        removeBoxBlacklist(blockProvider.getBlock());
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

        public void blacklist(@Nonnull IBlockProvider blockProvider) {
            addBoxBlacklist(blockProvider);
        }

        public void blacklist(@Nullable Block block) {
            addBoxBlacklist(block);
        }

        public void blacklistMod(@Nonnull String modid) {
            addBoxBlacklistMod(modid);
        }

        public void removeBlacklist(@Nonnull IBlockProvider blockProvider) {
            removeBoxBlacklist(blockProvider);
        }

        public void removeBlacklist(@Nonnull Block block) {
            removeBoxBlacklist(block);
        }

        public void removeModBlacklist(@Nonnull String modid) {
            removeBoxBlacklistMod(modid);
        }
    }
}