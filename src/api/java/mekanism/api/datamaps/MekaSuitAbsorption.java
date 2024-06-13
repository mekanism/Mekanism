package mekanism.api.datamaps;

import com.mojang.serialization.Codec;
import mekanism.api.MekanismAPI;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * A {@link net.minecraft.core.registries.Registries#DAMAGE_TYPE damage type} data map that allows
 * changing how much damage of a given type the meka suit should absorb.
 *
 * @param absorption how much damage will be absorbed
 * @since 10.5.0
 */
public record MekaSuitAbsorption(float absorption) {
    /**
     * The ID of the data map.
     * @see net.neoforged.neoforge.registries.RegistryManager#getDataMap(ResourceKey, ResourceLocation)
     */
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "mekasuit_absorption");

    public static final Codec<MekaSuitAbsorption> CODEC = Codec.floatRange(0, 1)
            .xmap(MekaSuitAbsorption::new, MekaSuitAbsorption::absorption);
}
