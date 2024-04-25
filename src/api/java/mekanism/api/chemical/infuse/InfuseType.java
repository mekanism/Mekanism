package mekanism.api.chemical.infuse;

import com.mojang.serialization.Codec;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.providers.IInfuseTypeProvider;
import net.minecraft.Util;
import net.minecraft.core.Registry;

@NothingNullByDefault
public class InfuseType extends Chemical<InfuseType> implements IInfuseTypeProvider {

    public static final Codec<InfuseType> CODEC = MekanismAPI.INFUSE_TYPE_REGISTRY.byNameCodec();

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
    protected final Registry<InfuseType> getRegistry() {
        return MekanismAPI.INFUSE_TYPE_REGISTRY;
    }

    @Override
    protected String getDefaultTranslationKey() {
        return Util.makeDescriptionId("infuse_type", getRegistryName());
    }
}