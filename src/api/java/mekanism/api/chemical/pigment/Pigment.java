package mekanism.api.chemical.pigment;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.providers.IPigmentProvider;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/**
 * Represents a pigment chemical subtype
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Pigment extends Chemical<Pigment> implements IPigmentProvider {

    public Pigment(PigmentBuilder builder) {
        super(builder, ChemicalTags.PIGMENT);
    }

    public static Pigment readFromNBT(@Nullable CompoundTag nbtTags) {
        return ChemicalUtils.readChemicalFromNBT(nbtTags, MekanismAPI.EMPTY_PIGMENT, NBTConstants.PIGMENT_NAME, Pigment::getFromRegistry);
    }

    public static Pigment getFromRegistry(@Nullable ResourceLocation name) {
        return ChemicalUtils.readChemicalFromRegistry(name, MekanismAPI.EMPTY_PIGMENT, MekanismAPI.pigmentRegistry());
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
    protected String getDefaultTranslationKey() {
        return Util.makeDescriptionId("pigment", getRegistryName());
    }
}