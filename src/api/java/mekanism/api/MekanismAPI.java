package mekanism.api;

import javax.annotation.Nonnull;
import mekanism.api.gas.EmptyGas;
import mekanism.api.gas.Gas;
import mekanism.api.infuse.EmptyInfuseType;
import mekanism.api.infuse.InfuseType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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
}