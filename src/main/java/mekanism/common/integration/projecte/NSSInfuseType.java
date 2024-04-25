package mekanism.common.integration.projecte;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.providers.IInfuseTypeProvider;
import moze_intel.projecte.api.codec.NSSCodecHolder;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link NormalizedSimpleStack} and {@link moze_intel.projecte.api.nss.NSSTag} for representing {@link InfuseType}s.
 */
public final class NSSInfuseType extends AbstractNSSTag<InfuseType> {

    private static final boolean ALLOW_DEFAULT = false;

    /**
     * Codec for encoding NSSInfuseTypes to and from strings.
     */
    public static final Codec<NSSInfuseType> LEGACY_CODEC = createLegacyCodec(MekanismAPI.INFUSE_TYPE_REGISTRY, ALLOW_DEFAULT, "INFUSE_TYPE|", NSSInfuseType::new);

    public static final MapCodec<NSSInfuseType> EXPLICIT_MAP_CODEC = createExplicitCodec(MekanismAPI.INFUSE_TYPE_REGISTRY, ALLOW_DEFAULT, NSSInfuseType::new);
    public static final Codec<NSSInfuseType> EXPLICIT_CODEC = EXPLICIT_MAP_CODEC.codec();

    public static final NSSCodecHolder<NSSInfuseType> CODECS = new NSSCodecHolder<>("INFUSE_TYPE", LEGACY_CODEC, EXPLICIT_CODEC);

    private NSSInfuseType(@NotNull ResourceLocation resourceLocation, boolean isTag) {
        super(resourceLocation, isTag);
    }

    /**
     * Helper method to create an {@link NSSInfuseType} representing an infuse type from a {@link InfusionStack}
     */
    @NotNull
    public static NSSInfuseType createInfuseType(@NotNull InfusionStack stack) {
        //Don't bother checking if it is empty as getType returns EMPTY which will then fail anyways for being empty
        return createInfuseType(stack.getChemical());
    }

    /**
     * Helper method to create an {@link NSSInfuseType} representing an infuse type from an {@link IInfuseTypeProvider}
     */
    @NotNull
    public static NSSInfuseType createInfuseType(@NotNull IInfuseTypeProvider infuseTypeProvider) {
        return createInfuseType(infuseTypeProvider.getChemical());
    }

    /**
     * Helper method to create an {@link NSSInfuseType} representing an infuse type from a {@link InfuseType}
     */
    @NotNull
    public static NSSInfuseType createInfuseType(@NotNull InfuseType infuseType) {
        if (infuseType.isEmptyType()) {
            throw new IllegalArgumentException("Can't make NSSInfuseType with an empty infuse type");
        }
        //This should never be null, or it would have crashed on being registered
        return createInfuseType(infuseType.getRegistryName());
    }

    /**
     * Helper method to create an {@link NSSInfuseType} representing an infuse type from a {@link ResourceLocation}
     */
    @NotNull
    public static NSSInfuseType createInfuseType(@NotNull ResourceLocation infuseTypeID) {
        return new NSSInfuseType(infuseTypeID, false);
    }

    /**
     * Helper method to create an {@link NSSInfuseType} representing a tag from a {@link ResourceLocation}
     */
    @NotNull
    public static NSSInfuseType createTag(@NotNull ResourceLocation tagId) {
        return new NSSInfuseType(tagId, true);
    }

    /**
     * Helper method to create an {@link NSSInfuseType} representing a tag from a {@link TagKey<InfuseType>}
     */
    @NotNull
    public static NSSInfuseType createTag(@NotNull TagKey<InfuseType> tag) {
        return createTag(tag.location());
    }

    @NotNull
    @Override
    protected Registry<InfuseType> getRegistry() {
        return MekanismAPI.INFUSE_TYPE_REGISTRY;
    }

    @Override
    protected NSSInfuseType createNew(InfuseType infuseType) {
        return createInfuseType(infuseType);
    }

    @Override
    public NSSCodecHolder<NSSInfuseType> codecs() {
        return CODECS;
    }
}