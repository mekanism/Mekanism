package mekanism.api.chemical.infuse;

import com.mojang.serialization.Codec;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.providers.IInfuseTypeProvider;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class InfuseType extends Chemical<InfuseType> implements IInfuseTypeProvider {

    public static final Codec<InfuseType> CODEC = MekanismAPI.INFUSE_TYPE_REGISTRY.byNameCodec();

    public InfuseType(InfuseTypeBuilder builder) {
        super(builder);
    }

    public static InfuseType readFromNBT(@Nullable CompoundTag nbtTags) {
        return ChemicalUtils.readChemicalFromNBT(nbtTags, MekanismAPI.EMPTY_INFUSE_TYPE, NBTConstants.INFUSE_TYPE_NAME, InfuseType::getFromRegistry);
    }

    public static InfuseType getFromRegistry(@Nullable ResourceLocation name) {
        return ChemicalUtils.readChemicalFromRegistry(name, MekanismAPI.EMPTY_INFUSE_TYPE, MekanismAPI.INFUSE_TYPE_REGISTRY);
    }

    @Override
    public String toString() {
        return "[InfuseType: " + getRegistryName() + "]";
    }

    @Override
    public CompoundTag write(CompoundTag nbtTags) {
        nbtTags.putString(NBTConstants.INFUSE_TYPE_NAME, getRegistryName().toString());
        return nbtTags;
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