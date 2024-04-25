package mekanism.common.integration.projecte;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.providers.ISlurryProvider;
import moze_intel.projecte.api.codec.NSSCodecHolder;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link NormalizedSimpleStack} and {@link moze_intel.projecte.api.nss.NSSTag} for representing {@link Slurry}.
 */
public final class NSSSlurry extends AbstractNSSTag<Slurry> {

    private static final boolean ALLOW_DEFAULT = false;

    /**
     * Codec for encoding NSSSlurries to and from strings.
     */
    public static final Codec<NSSSlurry> LEGACY_CODEC = createLegacyCodec(MekanismAPI.SLURRY_REGISTRY, ALLOW_DEFAULT, "SLURRY|", NSSSlurry::new);

    public static final MapCodec<NSSSlurry> EXPLICIT_MAP_CODEC = createExplicitCodec(MekanismAPI.SLURRY_REGISTRY, ALLOW_DEFAULT, NSSSlurry::new);
    public static final Codec<NSSSlurry> EXPLICIT_CODEC = EXPLICIT_MAP_CODEC.codec();

    public static final NSSCodecHolder<NSSSlurry> CODECS = new NSSCodecHolder<>("SLURRY", LEGACY_CODEC, EXPLICIT_CODEC);

    private NSSSlurry(@NotNull ResourceLocation resourceLocation, boolean isTag) {
        super(resourceLocation, isTag);
    }

    /**
     * Helper method to create an {@link NSSSlurry} representing a slurry type from a {@link SlurryStack}
     */
    @NotNull
    public static NSSSlurry createSlurry(@NotNull SlurryStack stack) {
        //Don't bother checking if it is empty as getType returns EMPTY which will then fail anyway for being empty
        return createSlurry(stack.getChemical());
    }

    /**
     * Helper method to create an {@link NSSSlurry} representing a slurry type from a {@link ISlurryProvider}
     */
    @NotNull
    public static NSSSlurry createSlurry(@NotNull ISlurryProvider slurryProvider) {
        return createSlurry(slurryProvider.getChemical());
    }

    /**
     * Helper method to create an {@link NSSSlurry} representing a slurry type from a {@link Slurry}
     */
    @NotNull
    public static NSSSlurry createSlurry(@NotNull Slurry slurry) {
        if (slurry.isEmptyType()) {
            throw new IllegalArgumentException("Can't make NSSSlurry with an empty slurry");
        }
        //This should never be null, or it would have crashed on being registered
        return createSlurry(slurry.getRegistryName());
    }

    /**
     * Helper method to create an {@link NSSSlurry} representing a slurry type from a {@link ResourceLocation}
     */
    @NotNull
    public static NSSSlurry createSlurry(@NotNull ResourceLocation slurryID) {
        return new NSSSlurry(slurryID, false);
    }

    /**
     * Helper method to create an {@link NSSSlurry} representing a tag from a {@link ResourceLocation}
     */
    @NotNull
    public static NSSSlurry createTag(@NotNull ResourceLocation tagId) {
        return new NSSSlurry(tagId, true);
    }

    /**
     * Helper method to create an {@link NSSSlurry} representing a tag from a {@link TagKey<Slurry>}
     */
    @NotNull
    public static NSSSlurry createTag(@NotNull TagKey<Slurry> tag) {
        return createTag(tag.location());
    }

    @NotNull
    @Override
    protected Registry<Slurry> getRegistry() {
        return MekanismAPI.SLURRY_REGISTRY;
    }

    @Override
    protected NSSSlurry createNew(Slurry slurry) {
        return createSlurry(slurry);
    }

    @Override
    public NSSCodecHolder<NSSSlurry> codecs() {
        return CODECS;
    }
}