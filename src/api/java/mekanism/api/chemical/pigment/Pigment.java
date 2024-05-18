package mekanism.api.chemical.pigment;

import com.mojang.serialization.Codec;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.providers.IPigmentProvider;
import net.minecraft.Util;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Represents a pigment chemical subtype
 */
@NothingNullByDefault
public class Pigment extends Chemical<Pigment> implements IPigmentProvider {

    /**
     * A codec which can (de)encode pigments.
     *
     * @since 10.6.0
     */
    public static final Codec<Pigment> CODEC = MekanismAPI.PIGMENT_REGISTRY.byNameCodec();
    /**
     * A stream codec which can be used to encode and decode pigments over the network.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, Pigment> STREAM_CODEC = ByteBufCodecs.registry(MekanismAPI.PIGMENT_REGISTRY_NAME);

    public Pigment(PigmentBuilder builder) {
        super(builder);
    }

    @Override
    public String toString() {
        return "[Pigment: " + getRegistryName() + "]";
    }

    @Override
    public final boolean isEmptyType() {
        return this == MekanismAPI.EMPTY_PIGMENT;
    }

    @Override
    protected final DefaultedRegistry<Pigment> getRegistry() {
        return MekanismAPI.PIGMENT_REGISTRY;
    }

    @Override
    protected String getDefaultTranslationKey() {
        return Util.makeDescriptionId("pigment", getRegistryName());
    }
}