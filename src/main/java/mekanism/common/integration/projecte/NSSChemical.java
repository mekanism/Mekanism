package mekanism.common.integration.projecte;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IChemicalProvider;
import moze_intel.projecte.api.codec.NSSCodecHolder;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link NormalizedSimpleStack} and {@link moze_intel.projecte.api.nss.NSSTag} for representing {@link Chemical}s.
 */
public final class NSSChemical extends AbstractNSSTag<Chemical> {

    private static final boolean ALLOW_DEFAULT = false;

    /**
     * Codec for encoding NSSGases to and from strings.
     */
    public static final Codec<NSSChemical> LEGACY_CODEC = createLegacyCodec(MekanismAPI.CHEMICAL_REGISTRY, ALLOW_DEFAULT, "CHEMICAL|", NSSChemical::new);

    public static final MapCodec<NSSChemical> EXPLICIT_MAP_CODEC = createExplicitCodec(MekanismAPI.CHEMICAL_REGISTRY, ALLOW_DEFAULT, NSSChemical::new);
    public static final Codec<NSSChemical> EXPLICIT_CODEC = EXPLICIT_MAP_CODEC.codec();

    public static final NSSCodecHolder<NSSChemical> CODECS = new NSSCodecHolder<>("CHEMICAL", LEGACY_CODEC, EXPLICIT_CODEC);

    private NSSChemical(@NotNull ResourceLocation resourceLocation, boolean isTag) {
        super(resourceLocation, isTag);
    }

    /**
     * Helper method to create an {@link NSSChemical} representing a gas from a {@link ChemicalStack}
     */
    @NotNull
    public static NSSChemical createChemical(@NotNull ChemicalStack stack) {
        //Don't bother checking if it is empty as getType returns EMPTY which will then fail anyway for being empty
        return createChemical(stack.getChemical());
    }

    /**
     * Helper method to create an {@link NSSChemical} representing a gas from an {@link IChemicalProvider}
     */
    @NotNull
    public static NSSChemical createChemical(@NotNull IChemicalProvider chemicalProvider) {
        return createChemical(chemicalProvider.getChemical());
    }

    /**
     * Helper method to create an {@link NSSChemical} representing a chemical from a {@link Chemical}
     */
    @NotNull
    public static NSSChemical createChemical(@NotNull Chemical chemical) {
        if (chemical.isEmptyType()) {
            throw new IllegalArgumentException("Can't make NSSChemical with an empty chemical");
        }
        //This should never be null, or it would have crashed on being registered
        return createChemical(chemical.getRegistryName());
    }

    /**
     * Helper method to create an {@link NSSChemical} representing a gas from a {@link ResourceLocation}
     */
    @NotNull
    public static NSSChemical createChemical(@NotNull ResourceLocation gasID) {
        return new NSSChemical(gasID, false);
    }

    /**
     * Helper method to create an {@link NSSChemical} representing a tag from a {@link ResourceLocation}
     */
    @NotNull
    public static NSSChemical createTag(@NotNull ResourceLocation tagId) {
        return new NSSChemical(tagId, true);
    }

    /**
     * Helper method to create an {@link NSSChemical} representing a tag from a {@link TagKey< Chemical >}
     */
    @NotNull
    public static NSSChemical createTag(@NotNull TagKey<Chemical> tag) {
        return createTag(tag.location());
    }

    @NotNull
    @Override
    protected Registry<Chemical> getRegistry() {
        return MekanismAPI.CHEMICAL_REGISTRY;
    }

    @Override
    protected NSSChemical createNew(Chemical gas) {
        return createChemical(gas);
    }

    @Override
    public NSSCodecHolder<NSSChemical> codecs() {
        return CODECS;
    }
}