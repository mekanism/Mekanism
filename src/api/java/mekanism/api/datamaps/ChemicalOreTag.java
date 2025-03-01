package mekanism.api.datamaps;

import com.mojang.serialization.Codec;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * A {@link MekanismAPI#CHEMICAL_REGISTRY chemical} data map that allows defining what ore tag a given chemical has
 *
 * @param oreTag the item tag that represents the ore that goes with a chemical.
 *
 * @since 10.7.11
 */
public record ChemicalOreTag(TagKey<Item> oreTag) {

    /**
     * The ID of the data map.
     *
     * @see net.neoforged.neoforge.registries.RegistryManager#getDataMap(ResourceKey, ResourceLocation)
     */
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_ore_tag");

    public static final Codec<ChemicalOreTag> CODEC = TagKey.codec(Registries.ITEM).xmap(ChemicalOreTag::new, ChemicalOreTag::oreTag);

    public Optional<HolderSet.Named<Item>> lookupTag() {
        return BuiltInRegistries.ITEM.getTag(oreTag);
    }
}
