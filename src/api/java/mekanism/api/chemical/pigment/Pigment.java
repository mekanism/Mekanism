package mekanism.api.chemical.pigment;

import com.mojang.serialization.Codec;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.providers.IPigmentProvider;
import net.minecraft.Util;
import net.minecraft.core.Registry;

/**
 * Represents a pigment chemical subtype
 */
@NothingNullByDefault
public class Pigment extends Chemical<Pigment> implements IPigmentProvider {

    public static final Codec<Pigment> CODEC = MekanismAPI.PIGMENT_REGISTRY.byNameCodec();

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
    protected final Registry<Pigment> getRegistry() {
        return MekanismAPI.PIGMENT_REGISTRY;
    }

    @Override
    protected String getDefaultTranslationKey() {
        return Util.makeDescriptionId("pigment", getRegistryName());
    }
}