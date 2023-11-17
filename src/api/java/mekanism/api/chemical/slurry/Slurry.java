package mekanism.api.chemical.slurry;

import com.mojang.serialization.Codec;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.providers.ISlurryProvider;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a slurry chemical subtype
 */
@NothingNullByDefault
public class Slurry extends Chemical<Slurry> implements ISlurryProvider {

    public static final Codec<Slurry> CODEC = MekanismAPI.SLURRY_REGISTRY.byNameCodec();

    @Nullable
    private final TagKey<Item> oreTag;

    public Slurry(SlurryBuilder builder) {
        super(builder);
        this.oreTag = builder.getOreTag();
    }

    public static Slurry readFromNBT(@Nullable CompoundTag nbtTags) {
        return ChemicalUtils.readChemicalFromNBT(nbtTags, MekanismAPI.EMPTY_SLURRY, NBTConstants.SLURRY_NAME, Slurry::getFromRegistry);
    }

    public static Slurry getFromRegistry(@Nullable ResourceLocation name) {
        return ChemicalUtils.readChemicalFromRegistry(name, MekanismAPI.EMPTY_SLURRY, MekanismAPI.SLURRY_REGISTRY);
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
    protected final Registry<Slurry> getRegistry() {
        return MekanismAPI.SLURRY_REGISTRY;
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