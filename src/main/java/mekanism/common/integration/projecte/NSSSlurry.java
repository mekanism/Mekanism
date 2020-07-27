/*
package mekanism.common.integration.projecte;

import java.util.function.Function;
import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.api.nss.NSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;

*//**
 * Implementation of {@link NormalizedSimpleStack} and {@link NSSTag} for representing {@link Slurry}.
 *//*
public final class NSSSlurry extends AbstractNSSTag<Slurry> {

    private NSSSlurry(@Nonnull ResourceLocation resourceLocation, boolean isTag) {
        super(resourceLocation, isTag);
    }

    *//**
 * Helper method to create an {@link NSSSlurry} representing a slurry type from a {@link SlurryStack}
 *//*
    @Nonnull
    public static NSSSlurry createSlurry(@Nonnull SlurryStack stack) {
        //Don't bother checking if it is empty as getType returns EMPTY which will then fail anyways for being empty
        return createSlurry(stack.getType());
    }

    *//**
 * Helper method to create an {@link NSSSlurry} representing a slurry type from a {@link Slurry}
 *//*
    @Nonnull
    public static NSSSlurry createSlurry(@Nonnull Slurry slurry) {
        if (slurry.isEmptyType()) {
            throw new IllegalArgumentException("Can't make NSSSlurry with an empty slurry");
        }
        //This should never be null or it would have crashed on being registered
        return createSlurry(slurry.getRegistryName());
    }

    *//**
 * Helper method to create an {@link NSSSlurry} representing a slurry type from a {@link ResourceLocation}
 *//*
    @Nonnull
    public static NSSSlurry createSlurry(@Nonnull ResourceLocation slurryID) {
        return new NSSSlurry(slurryID, false);
    }

    *//**
 * Helper method to create an {@link NSSSlurry} representing a tag from a {@link ResourceLocation}
 *//*
    @Nonnull
    public static NSSSlurry createTag(@Nonnull ResourceLocation tagId) {
        return new NSSSlurry(tagId, true);
    }

    *//**
 * Helper method to create an {@link NSSSlurry} representing a tag from a {@link Tag<Slurry>}
 *//*
    @Nonnull
    public static NSSSlurry createTag(@Nonnull ITag<Slurry> tag) {
        return createTag(ChemicalTags.SLURRY.lookupTag(tag));
    }

    @Override
    protected boolean isInstance(AbstractNSSTag o) {
        return o instanceof NSSSlurry;
    }

    @Nonnull
    @Override
    public String getJsonPrefix() {
        return "SLURRY|";
    }

    @Nonnull
    @Override
    public String getType() {
        return "Slurry";
    }

    @Nonnull
    @Override
    protected TagCollection<Slurry> getTagCollection() {
        return ChemicalTags.SLURRY.getCollection();
    }

    @Override
    protected Function<Slurry, NormalizedSimpleStack> createNew() {
        return NSSSlurry::createSlurry;
    }
}*/