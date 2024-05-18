package mekanism.api.chemical.gas;

import com.mojang.serialization.Codec;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.providers.IGasProvider;
import net.minecraft.Util;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Gas - a class used to set specific properties of gases when used or seen in-game.
 *
 * @author aidancbrady
 */
@NothingNullByDefault
public class Gas extends Chemical<Gas> implements IGasProvider {

    /**
     * A codec which can (de)encode gases.
     *
     * @since 10.6.0
     */
    public static final Codec<Gas> CODEC = MekanismAPI.GAS_REGISTRY.byNameCodec();
    /**
     * A stream codec which can be used to encode and decode gases over the network.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, Gas> STREAM_CODEC = ByteBufCodecs.registry(MekanismAPI.GAS_REGISTRY_NAME);

    public Gas(GasBuilder builder) {
        super(builder);
    }

    @Override
    public String toString() {
        return "[Gas: " + getRegistryName() + "]";
    }

    @Override
    public final boolean isEmptyType() {
        return this == MekanismAPI.EMPTY_GAS;
    }

    @Override
    protected final DefaultedRegistry<Gas> getRegistry() {
        return MekanismAPI.GAS_REGISTRY;
    }

    @Override
    protected String getDefaultTranslationKey() {
        return Util.makeDescriptionId("gas", getRegistryName());
    }
}