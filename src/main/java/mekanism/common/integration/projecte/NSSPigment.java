/*package mekanism.common.integration.projecte;

import java.util.function.Function;
import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.api.nss.NSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;

*//**
 * Implementation of {@link NormalizedSimpleStack} and {@link NSSTag} for representing {@link Pigment}s.
 *//*
public final class NSSPigment extends AbstractNSSTag<Pigment> {

    private NSSPigment(@Nonnull ResourceLocation resourceLocation, boolean isTag) {
        super(resourceLocation, isTag);
    }

    *//**
 * Helper method to create an {@link NSSPigment} representing a pigment type from a {@link PigmentStack}
 *//*
    @Nonnull
    public static NSSPigment createPigment(@Nonnull PigmentStack stack) {
        //Don't bother checking if it is empty as getType returns EMPTY which will then fail anyways for being empty
        return createPigment(stack.getType());
    }

    *//**
 * Helper method to create an {@link NSSPigment} representing a pigment type from a {@link Pigment}
 *//*
    @Nonnull
    public static NSSPigment createPigment(@Nonnull Pigment pigment) {
        if (pigment.isEmptyType()) {
            throw new IllegalArgumentException("Can't make NSSPigment with an empty pigment");
        }
        //This should never be null or it would have crashed on being registered
        return createPigment(pigment.getRegistryName());
    }

    *//**
 * Helper method to create an {@link NSSPigment} representing a pigment type from a {@link ResourceLocation}
 *//*
    @Nonnull
    public static NSSPigment createPigment(@Nonnull ResourceLocation pigmentID) {
        return new NSSPigment(pigmentID, false);
    }

    *//**
 * Helper method to create an {@link NSSPigment} representing a tag from a {@link ResourceLocation}
 *//*
    @Nonnull
    public static NSSPigment createTag(@Nonnull ResourceLocation tagId) {
        return new NSSPigment(tagId, true);
    }

    *//**
 * Helper method to create an {@link NSSPigment} representing a tag from a {@link Tag<Pigment>}
 *//*
    @Nonnull
    public static NSSPigment createTag(@Nonnull ITag<Pigment> tag) {
        return createTag(ChemicalTags.PIGMENT.lookupTag(tag));
    }

    @Override
    protected boolean isInstance(AbstractNSSTag o) {
        return o instanceof NSSPigment;
    }

    @Nonnull
    @Override
    public String getJsonPrefix() {
        return "PIGMENT|";
    }

    @Nonnull
    @Override
    public String getType() {
        return "Pigment";
    }

    @Nonnull
    @Override
    protected TagCollection<Pigment> getTagCollection() {
        return ChemicalTags.PIGMENT.getCollection();
    }

    @Override
    protected Function<Pigment, NormalizedSimpleStack> createNew() {
        return NSSPigment::createPigment;
    }
}*/