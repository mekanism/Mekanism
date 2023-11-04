package mekanism.api.chemical.infuse;

import com.mojang.serialization.Codec;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.providers.IInfuseTypeProvider;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class InfuseType extends Chemical<InfuseType> implements IInfuseTypeProvider {

    public static final Codec<InfuseType> CODEC = ExtraCodecs.lazyInitializedCodec(() -> MekanismAPI.infuseTypeRegistry().getCodec());

    public InfuseType(InfuseTypeBuilder builder) {
        super(builder, ChemicalTags.INFUSE_TYPE);
    }

    public static InfuseType readFromNBT(@Nullable CompoundTag nbtTags) {
        return ChemicalUtils.readChemicalFromNBT(nbtTags, MekanismAPI.EMPTY_INFUSE_TYPE, NBTConstants.INFUSE_TYPE_NAME, InfuseType::getFromRegistry);
    }

    public static InfuseType getFromRegistry(@Nullable ResourceLocation name) {
        return ChemicalUtils.readChemicalFromRegistry(name, MekanismAPI.EMPTY_INFUSE_TYPE, MekanismAPI.infuseTypeRegistry());
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
    @SuppressWarnings("ConstantConditions")
    public final ResourceLocation getRegistryName() {
        //May be null if called before the object is registered
        IForgeRegistry<InfuseType> registry = MekanismAPI.infuseTypeRegistry();
        return registry == null ? null : registry.getKey(this);
    }

    @Override
    protected String getDefaultTranslationKey() {
        return Util.makeDescriptionId("infuse_type", getRegistryName());
    }
}