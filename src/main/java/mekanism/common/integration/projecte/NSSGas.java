package mekanism.common.integration.projecte;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.providers.IGasProvider;
import moze_intel.projecte.api.codec.NSSCodecHolder;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link NormalizedSimpleStack} and {@link moze_intel.projecte.api.nss.NSSTag} for representing {@link Gas}s.
 */
public final class NSSGas extends AbstractNSSTag<Gas> {

    private static final boolean ALLOW_DEFAULT = false;

    /**
     * Codec for encoding NSSGases to and from strings.
     */
    public static final Codec<NSSGas> LEGACY_CODEC = createLegacyCodec(MekanismAPI.GAS_REGISTRY, ALLOW_DEFAULT, "GAS|", NSSGas::new);

    public static final MapCodec<NSSGas> EXPLICIT_MAP_CODEC = createExplicitCodec(MekanismAPI.GAS_REGISTRY, ALLOW_DEFAULT, NSSGas::new);
    public static final Codec<NSSGas> EXPLICIT_CODEC = EXPLICIT_MAP_CODEC.codec();

    public static final NSSCodecHolder<NSSGas> CODECS = new NSSCodecHolder<>("GAS", LEGACY_CODEC, EXPLICIT_CODEC);

    private NSSGas(@NotNull ResourceLocation resourceLocation, boolean isTag) {
        super(resourceLocation, isTag);
    }

    /**
     * Helper method to create an {@link NSSGas} representing a gas from a {@link GasStack}
     */
    @NotNull
    public static NSSGas createGas(@NotNull GasStack stack) {
        //Don't bother checking if it is empty as getType returns EMPTY which will then fail anyway for being empty
        return createGas(stack.getChemical());
    }

    /**
     * Helper method to create an {@link NSSGas} representing a gas from an {@link IGasProvider}
     */
    @NotNull
    public static NSSGas createGas(@NotNull IGasProvider gasProvider) {
        return createGas(gasProvider.getChemical());
    }

    /**
     * Helper method to create an {@link NSSGas} representing a gas from a {@link Gas}
     */
    @NotNull
    public static NSSGas createGas(@NotNull Gas gas) {
        if (gas.isEmptyType()) {
            throw new IllegalArgumentException("Can't make NSSGas with an empty gas");
        }
        //This should never be null, or it would have crashed on being registered
        return createGas(gas.getRegistryName());
    }

    /**
     * Helper method to create an {@link NSSGas} representing a gas from a {@link ResourceLocation}
     */
    @NotNull
    public static NSSGas createGas(@NotNull ResourceLocation gasID) {
        return new NSSGas(gasID, false);
    }

    /**
     * Helper method to create an {@link NSSGas} representing a tag from a {@link ResourceLocation}
     */
    @NotNull
    public static NSSGas createTag(@NotNull ResourceLocation tagId) {
        return new NSSGas(tagId, true);
    }

    /**
     * Helper method to create an {@link NSSGas} representing a tag from a {@link TagKey<Gas>}
     */
    @NotNull
    public static NSSGas createTag(@NotNull TagKey<Gas> tag) {
        return createTag(tag.location());
    }

    @NotNull
    @Override
    protected Registry<Gas> getRegistry() {
        return MekanismAPI.GAS_REGISTRY;
    }

    @Override
    protected NSSGas createNew(Gas gas) {
        return createGas(gas);
    }

    @Override
    public NSSCodecHolder<NSSGas> codecs() {
        return CODECS;
    }
}