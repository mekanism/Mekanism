package mekanism.api.chemical.infuse;

import com.mojang.serialization.Codec;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.providers.IInfuseTypeProvider;
import net.minecraft.Util;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

@NothingNullByDefault
public class InfuseType extends Chemical<InfuseType> implements IInfuseTypeProvider {

    /**
     * A codec which can (de)encode infuse types.
     *
     * @since 10.6.0
     */
    public static final Codec<InfuseType> CODEC = MekanismAPI.INFUSE_TYPE_REGISTRY.byNameCodec();
    /**
     * A stream codec which can be used to encode and decode infuse types over the network.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, InfuseType> STREAM_CODEC = ByteBufCodecs.registry(MekanismAPI.INFUSE_TYPE_REGISTRY_NAME);

    public InfuseType(InfuseTypeBuilder builder) {
        super(builder);
    }

    @Override
    public String toString() {
        return "[InfuseType: " + getRegistryName() + "]";
    }

    @Override
    public final boolean isEmptyType() {
        return this == MekanismAPI.EMPTY_INFUSE_TYPE;
    }

    @Override
    protected final DefaultedRegistry<InfuseType> getRegistry() {
        return MekanismAPI.INFUSE_TYPE_REGISTRY;
    }

    @Override
    protected String getDefaultTranslationKey() {
        return Util.makeDescriptionId("infuse_type", getRegistryName());
    }
}