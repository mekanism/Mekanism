package mekanism.api.chemical.gas;

import com.mojang.serialization.Codec;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.providers.IGasProvider;
import net.minecraft.Util;
import net.minecraft.core.Registry;

/**
 * Gas - a class used to set specific properties of gases when used or seen in-game.
 *
 * @author aidancbrady
 */
@NothingNullByDefault
public class Gas extends Chemical<Gas> implements IGasProvider {

    public static final Codec<Gas> CODEC = MekanismAPI.GAS_REGISTRY.byNameCodec();

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
    protected final Registry<Gas> getRegistry() {
        return MekanismAPI.GAS_REGISTRY;
    }

    @Override
    protected String getDefaultTranslationKey() {
        return Util.makeDescriptionId("gas", getRegistryName());
    }
}