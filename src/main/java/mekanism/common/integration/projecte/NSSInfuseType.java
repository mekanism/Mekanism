package mekanism.common.integration.projecte;

import java.util.function.Function;
import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.providers.IInfuseTypeProvider;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

/**
 * Implementation of {@link NormalizedSimpleStack} and {@link moze_intel.projecte.api.nss.NSSTag} for representing {@link InfuseType}s.
 */
public final class NSSInfuseType extends AbstractNSSTag<InfuseType> {

    private NSSInfuseType(@Nonnull ResourceLocation resourceLocation, boolean isTag) {
        super(resourceLocation, isTag);
    }

    /**
     * Helper method to create an {@link NSSInfuseType} representing an infuse type from a {@link InfusionStack}
     */
    @Nonnull
    public static NSSInfuseType createInfuseType(@Nonnull InfusionStack stack) {
        //Don't bother checking if it is empty as getType returns EMPTY which will then fail anyways for being empty
        return createInfuseType(stack.getType());
    }

    /**
     * Helper method to create an {@link NSSInfuseType} representing an infuse type from an {@link IInfuseTypeProvider}
     */
    @Nonnull
    public static NSSInfuseType createInfuseType(@Nonnull IInfuseTypeProvider infuseTypeProvider) {
        return createInfuseType(infuseTypeProvider.getChemical());
    }

    /**
     * Helper method to create an {@link NSSInfuseType} representing an infuse type from a {@link InfuseType}
     */
    @Nonnull
    public static NSSInfuseType createInfuseType(@Nonnull InfuseType infuseType) {
        if (infuseType.isEmptyType()) {
            throw new IllegalArgumentException("Can't make NSSInfuseType with an empty infuse type");
        }
        //This should never be null or it would have crashed on being registered
        return createInfuseType(infuseType.getRegistryName());
    }

    /**
     * Helper method to create an {@link NSSInfuseType} representing an infuse type from a {@link ResourceLocation}
     */
    @Nonnull
    public static NSSInfuseType createInfuseType(@Nonnull ResourceLocation infuseTypeID) {
        return new NSSInfuseType(infuseTypeID, false);
    }

    /**
     * Helper method to create an {@link NSSInfuseType} representing a tag from a {@link ResourceLocation}
     */
    @Nonnull
    public static NSSInfuseType createTag(@Nonnull ResourceLocation tagId) {
        return new NSSInfuseType(tagId, true);
    }

    /**
     * Helper method to create an {@link NSSInfuseType} representing a tag from a {@link Tag<InfuseType>}
     */
    @Nonnull
    public static NSSInfuseType createTag(@Nonnull ITag<InfuseType> tag) {
        return createTag(ChemicalTags.INFUSE_TYPE.lookupTag(tag));
    }

    @Override
    protected boolean isInstance(AbstractNSSTag o) {
        return o instanceof NSSInfuseType;
    }

    @Nonnull
    @Override
    public String getJsonPrefix() {
        return "INFUSE_TYPE|";
    }

    @Nonnull
    @Override
    public String getType() {
        return "Infuse Type";
    }

    @Nonnull
    @Override
    protected ITagCollection<InfuseType> getTagCollection() {
        return ChemicalTags.INFUSE_TYPE.getCollection();
    }

    @Override
    protected Function<InfuseType, NormalizedSimpleStack> createNew() {
        return NSSInfuseType::createInfuseType;
    }
}