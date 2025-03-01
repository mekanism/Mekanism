package mekanism.api.datamaps.chemical;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * A {@link MekanismAPI#CHEMICAL_REGISTRY chemical} data map that allows defining an ore tag for a chemical.
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

    private static final Codec<TagKey<Item>> TAG_CODEC = TagKey.codec(Registries.ITEM);
    public static final Codec<ChemicalOreTag> ORE_TAG_CODEC = TagKey.codec(Registries.ITEM).xmap(ChemicalOreTag::new, ChemicalOreTag::oreTag);
    public static final Codec<ChemicalOreTag> CODEC = Codec.withAlternative(RecordCodecBuilder.create(in -> in.group(
          TAG_CODEC.fieldOf(SerializationConstants.ORE_TYPE).forGetter(ChemicalOreTag::oreTag)
    ).apply(in, ChemicalOreTag::new)), ORE_TAG_CODEC);

    public Optional<HolderSet.Named<Item>> lookupTag() {
        return BuiltInRegistries.ITEM.getTag(oreTag);
    }
}
