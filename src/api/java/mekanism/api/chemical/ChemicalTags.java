package mekanism.api.chemical;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.tags.ITagManager;

public class ChemicalTags<CHEMICAL extends Chemical<CHEMICAL>> {

    public static final ChemicalTags<Gas> GAS = new ChemicalTags<>(MekanismAPI::gasRegistryName, MekanismAPI::gasRegistry);
    public static final ChemicalTags<InfuseType> INFUSE_TYPE = new ChemicalTags<>(MekanismAPI::infuseTypeRegistryName, MekanismAPI::infuseTypeRegistry);
    public static final ChemicalTags<Pigment> PIGMENT = new ChemicalTags<>(MekanismAPI::pigmentRegistryName, MekanismAPI::pigmentRegistry);
    public static final ChemicalTags<Slurry> SLURRY = new ChemicalTags<>(MekanismAPI::slurryRegistryName, MekanismAPI::slurryRegistry);

    private final Supplier<IForgeRegistry<CHEMICAL>> registrySupplier;
    private final Supplier<ResourceKey<? extends Registry<CHEMICAL>>> registryKeySupplier;

    private ChemicalTags(Supplier<ResourceKey<? extends Registry<CHEMICAL>>> registryKeySupplier, Supplier<IForgeRegistry<CHEMICAL>> registrySupplier) {
        this.registrySupplier = registrySupplier;
        this.registryKeySupplier = registryKeySupplier;
    }

    /**
     * Helper to create a chemical tag.
     *
     * @param name Tag name.
     *
     * @return Tag reference.
     *
     * @apiNote For statically initializing optional tags, {@link net.minecraftforge.registries.DeferredRegister#createOptionalTagKey(String, Set)} must be used instead.
     */
    public TagKey<CHEMICAL> tag(ResourceLocation name) {
        return getManager().map(manager -> manager.createTagKey(name))
              .orElseGet(() -> TagKey.create(registryKeySupplier.get(), name));
    }

    /**
     * Gets the tag manager for this type of tag if it is after the registry has been created.
     */
    public Optional<ITagManager<CHEMICAL>> getManager() {
        IForgeRegistry<CHEMICAL> registry = registrySupplier.get();
        if (registry == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(registry.tags());
    }
}