package mekanism.api;

import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.EmptyGas;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.EmptyInfuseType;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.EmptyPigment;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.EmptySlurry;
import mekanism.api.chemical.slurry.Slurry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MekanismAPI {

    private MekanismAPI() {
    }

    /**
     * The version of the api classes - may not always match the mod's version
     */
    public static final String API_VERSION = "10.0.21";
    public static final String MEKANISM_MODID = "mekanism";
    /**
     * Mekanism debug mode
     */
    public static boolean debug = false;

    public static final Logger logger = LogManager.getLogger(MEKANISM_MODID + "_api");

    private static IForgeRegistry<Gas> GAS_REGISTRY;
    private static IForgeRegistry<InfuseType> INFUSE_TYPE_REGISTRY;
    private static IForgeRegistry<Pigment> PIGMENT_REGISTRY;
    private static IForgeRegistry<Slurry> SLURRY_REGISTRY;

    //Note: None of the empty variants support registry replacement
    @Nonnull
    public static final Gas EMPTY_GAS = new EmptyGas();
    @Nonnull
    public static final InfuseType EMPTY_INFUSE_TYPE = new EmptyInfuseType();
    @Nonnull
    public static final Pigment EMPTY_PIGMENT = new EmptyPigment();
    @Nonnull
    public static final Slurry EMPTY_SLURRY = new EmptySlurry();

    /**
     * Gets the Forge Registry for {@link Gas}.
     *
     * @apiNote If registering via {@link net.minecraftforge.registries.DeferredRegister<Gas>} instead of {@link net.minecraftforge.event.RegistryEvent.Register<Gas>}
     * make sure to use {@link net.minecraftforge.registries.DeferredRegister#create(Class, String)} rather than passing the result of this method to the other create
     * method, as this method <strong>CAN</strong> return {@code null} if called before the {@link net.minecraftforge.event.RegistryEvent.NewRegistry} events have been
     * fired. This method is marked as {@link Nonnull} just because except for when this is being called super early it is never {@code null}.
     */
    @Nonnull
    public static IForgeRegistry<Gas> gasRegistry() {
        if (GAS_REGISTRY == null) {
            GAS_REGISTRY = RegistryManager.ACTIVE.getRegistry(Gas.class);
        }
        return GAS_REGISTRY;
    }

    /**
     * Gets the Forge Registry for {@link InfuseType}.
     *
     * @apiNote If registering via {@link net.minecraftforge.registries.DeferredRegister<InfuseType>} instead of {@link
     * net.minecraftforge.event.RegistryEvent.Register<InfuseType>} make sure to use {@link net.minecraftforge.registries.DeferredRegister#create(Class, String)} rather
     * than passing the result of this method to the other create method, as this method <strong>CAN</strong> return {@code null} if called before the {@link
     * net.minecraftforge.event.RegistryEvent.NewRegistry} events have been fired. This method is marked as {@link Nonnull} just because except for when this is being
     * called super early it is never {@code null}.
     */
    @Nonnull
    public static IForgeRegistry<InfuseType> infuseTypeRegistry() {
        if (INFUSE_TYPE_REGISTRY == null) {
            INFUSE_TYPE_REGISTRY = RegistryManager.ACTIVE.getRegistry(InfuseType.class);
        }
        return INFUSE_TYPE_REGISTRY;
    }

    /**
     * Gets the Forge Registry for {@link Pigment}.
     *
     * @apiNote If registering via {@link net.minecraftforge.registries.DeferredRegister<Pigment>} instead of {@link
     * net.minecraftforge.event.RegistryEvent.Register<Pigment>} make sure to use {@link net.minecraftforge.registries.DeferredRegister#create(Class, String)} rather than
     * passing the result of this method to the other create method, as this method <strong>CAN</strong> return {@code null} if called before the {@link
     * net.minecraftforge.event.RegistryEvent.NewRegistry} events have been fired. This method is marked as {@link Nonnull} just because except for when this is being
     * called super early it is never {@code null}.
     */
    @Nonnull
    public static IForgeRegistry<Pigment> pigmentRegistry() {
        if (PIGMENT_REGISTRY == null) {
            PIGMENT_REGISTRY = RegistryManager.ACTIVE.getRegistry(Pigment.class);
        }
        return PIGMENT_REGISTRY;
    }

    /**
     * Gets the Forge Registry for {@link Slurry}.
     *
     * @apiNote If registering via {@link net.minecraftforge.registries.DeferredRegister<Slurry>} instead of {@link net.minecraftforge.event.RegistryEvent.Register<Slurry>}
     * make sure to use {@link net.minecraftforge.registries.DeferredRegister#create(Class, String)} rather than passing the result of this method to the other create
     * method, as this method <strong>CAN</strong> return {@code null} if called before the {@link net.minecraftforge.event.RegistryEvent.NewRegistry} events have been
     * fired. This method is marked as {@link Nonnull} just because except for when this is being called super early it is never {@code null}.
     */
    @Nonnull
    public static IForgeRegistry<Slurry> slurryRegistry() {
        if (SLURRY_REGISTRY == null) {
            SLURRY_REGISTRY = RegistryManager.ACTIVE.getRegistry(Slurry.class);
        }
        return SLURRY_REGISTRY;
    }
}