package mekanism.api.chemical.pigment;

import com.mojang.serialization.Codec;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.providers.IPigmentProvider;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a pigment chemical subtype
 */
@NothingNullByDefault
public class Pigment extends Chemical<Pigment> implements IPigmentProvider {

    public static final Codec<Pigment> CODEC = MekanismAPI.PIGMENT_REGISTRY.byNameCodec();

    public Pigment(PigmentBuilder builder) {
        super(builder);
    }

    public static Pigment readFromNBT(@Nullable CompoundTag nbtTags) {
        return ChemicalUtils.readChemicalFromNBT(nbtTags, MekanismAPI.EMPTY_PIGMENT, NBTConstants.PIGMENT_NAME, Pigment::getFromRegistry);
    }

    public static Pigment getFromRegistry(@Nullable ResourceLocation name) {
        return ChemicalUtils.readChemicalFromRegistry(name, MekanismAPI.EMPTY_PIGMENT, MekanismAPI.PIGMENT_REGISTRY);
    }

    @Override
    public String toString() {
        return "[Pigment: " + getRegistryName() + "]";
    }

    @Override
    public CompoundTag write(CompoundTag nbtTags) {
        nbtTags.putString(NBTConstants.PIGMENT_NAME, getRegistryName().toString());
        return nbtTags;
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