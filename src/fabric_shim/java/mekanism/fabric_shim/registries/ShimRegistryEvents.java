package mekanism.fabric_shim.registries;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;

/**
 * Drives the shim registration lifecycle from the Fabric mod initializer:
 * posts {@link NewRegistryEvent}, adds collected custom registries to the root registry, then posts
 * one {@link RegisterEvent} per registry in NeoForge's registration order.
 *
 * <p>Must run inside {@code onInitialize()}: Fabric permits {@code Registry.register} during mod
 * initialization and freezes registries afterwards.
 */
public final class ShimRegistryEvents {

    private ShimRegistryEvents() {
    }

    public static void fire(IEventBus modBus) {
        NewRegistryEvent newRegistries = new NewRegistryEvent();
        modBus.post(newRegistries);
        newRegistries.fill();
        for (Registry<?> registry : registrationOrder()) {
            modBus.post(new RegisterEvent(registry.key(), registry));
        }
    }

    /**
     * NeoForge fires RegisterEvent in a fixed order (its GameData.getRegistrationOrder):
     * attributes, data component types and armor materials first (Item/MobEffect construction
     * depends on them), then the remaining registries in root-registry insertion order — which for
     * vanilla is registration order, and also covers custom registries added by the fill step above.
     */
    private static List<Registry<?>> registrationOrder() {
        Set<ResourceLocation> seen = new LinkedHashSet<>();
        List<Registry<?>> ordered = new ArrayList<>();
        for (ResourceLocation early : List.of(Registries.ATTRIBUTE.location(), Registries.DATA_COMPONENT_TYPE.location(),
              Registries.ARMOR_MATERIAL.location())) {
            Registry<?> registry = BuiltInRegistries.REGISTRY.get(early);
            if (registry != null && seen.add(early)) {
                ordered.add(registry);
            }
        }
        for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
            if (seen.add(registry.key().location())) {
                ordered.add(registry);
            }
        }
        return ordered;
    }
}
