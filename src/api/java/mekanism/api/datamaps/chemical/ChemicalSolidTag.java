package mekanism.api.datamaps.chemical;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * A {@link MekanismAPI#CHEMICAL_REGISTRY chemical} data map that allows defining a solid tag for a chemical.
 *
 * @param solidRepresentation the item tag that represents the ore or block that goes with a chemical.
 *
 * @since 10.7.11
 */
public record ChemicalSolidTag(TagKey<Item> solidRepresentation) {//TODO - 1.22: Do we want to just define this in the recipe

    /**
     * The ID of the data map.
     *
     * @see mekanism.api.datamaps.IMekanismDataMapTypes#chemicalSolidTag()
     */
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_solid_tag");

    private static final Codec<TagKey<Item>> TAG_CODEC = TagKey.codec(Registries.ITEM);
    /**
     * Compressed codec for serializing and deserializing chemical solid tags for use over the network.
     */
    public static final Codec<ChemicalSolidTag> SOLID_TAG_CODEC = TagKey.codec(Registries.ITEM).xmap(ChemicalSolidTag::new, ChemicalSolidTag::solidRepresentation);
    /**
     * Codec for serializing and deserializing chemical solid tags.
     */
    public static final Codec<ChemicalSolidTag> CODEC = Codec.withAlternative(RecordCodecBuilder.create(in -> in.group(
          TAG_CODEC.fieldOf(SerializationConstants.REPRESENTATION).forGetter(ChemicalSolidTag::solidRepresentation)
    ).apply(in, ChemicalSolidTag::new)), SOLID_TAG_CODEC);

    /**
     * Looks up the contents of the solid representation tag.
     */
    public Optional<HolderSet.Named<Item>> lookupTag() {
        return BuiltInRegistries.ITEM.getTag(solidRepresentation);
    }
}
