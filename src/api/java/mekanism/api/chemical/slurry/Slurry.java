package mekanism.api.chemical.slurry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.providers.ISlurryProvider;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Represents a slurry chemical subtype
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Slurry extends Chemical<Slurry> implements ISlurryProvider {

    @Nullable
    private final TagKey<Item> oreTag;

    public Slurry(SlurryBuilder builder) {
        super(builder, ChemicalTags.SLURRY);
        this.oreTag = builder.getOreTag();
    }

    public static Slurry readFromNBT(@Nullable CompoundTag nbtTags) {
        return ChemicalUtils.readChemicalFromNBT(nbtTags, MekanismAPI.EMPTY_SLURRY, NBTConstants.SLURRY_NAME, Slurry::getFromRegistry);
    }

    public static Slurry getFromRegistry(@Nullable ResourceLocation name) {
        return ChemicalUtils.readChemicalFromRegistry(name, MekanismAPI.EMPTY_SLURRY, MekanismAPI.slurryRegistry());
    }

    @Override
    public String toString() {
        return "[Slurry: " + getRegistryName() + "]";
    }

    @Override
    public CompoundTag write(CompoundTag nbtTags) {
        nbtTags.putString(NBTConstants.SLURRY_NAME, getRegistryName().toString());
        return nbtTags;
    }

    @Override
    public final boolean isEmptyType() {
        return this == MekanismAPI.EMPTY_SLURRY;
    }

    @Override
    protected String getDefaultTranslationKey() {
        return Util.makeDescriptionId("slurry", getRegistryName());
    }

    /**
     * Gets the item tag representing the ore for this slurry.
     *
     * @return The tag for the item the slurry goes with. May be null.
     */
    @Nullable
    public TagKey<Item> getOreTag() {
        return oreTag;
    }
}