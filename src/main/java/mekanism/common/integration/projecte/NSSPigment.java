package mekanism.common.integration.projecte;

import com.mojang.datafixers.util.Either;
import java.util.Optional;
import java.util.function.Function;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.providers.IPigmentProvider;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.tags.ITag;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link NormalizedSimpleStack} and {@link moze_intel.projecte.api.nss.NSSTag} for representing {@link Pigment}s.
 */
public final class NSSPigment extends AbstractNSSTag<Pigment> {

    private NSSPigment(@NotNull ResourceLocation resourceLocation, boolean isTag) {
        super(resourceLocation, isTag);
    }

    /**
     * Helper method to create an {@link NSSPigment} representing a pigment type from a {@link PigmentStack}
     */
    @NotNull
    public static NSSPigment createPigment(@NotNull PigmentStack stack) {
        //Don't bother checking if it is empty as getType returns EMPTY which will then fail anyway for being empty
        return createPigment(stack.getType());
    }

    /**
     * Helper method to create an {@link NSSPigment} representing a pigment type from a {@link IPigmentProvider}
     */
    @NotNull
    public static NSSPigment createPigment(@NotNull IPigmentProvider pigmentProvider) {
        return createPigment(pigmentProvider.getChemical());
    }

    /**
     * Helper method to create an {@link NSSPigment} representing a pigment type from a {@link Pigment}
     */
    @NotNull
    public static NSSPigment createPigment(@NotNull Pigment pigment) {
        if (pigment.isEmptyType()) {
            throw new IllegalArgumentException("Can't make NSSPigment with an empty pigment");
        }
        //This should never be null, or it would have crashed on being registered
        return createPigment(pigment.getRegistryName());
    }

    /**
     * Helper method to create an {@link NSSPigment} representing a pigment type from a {@link ResourceLocation}
     */
    @NotNull
    public static NSSPigment createPigment(@NotNull ResourceLocation pigmentID) {
        return new NSSPigment(pigmentID, false);
    }

    /**
     * Helper method to create an {@link NSSPigment} representing a tag from a {@link ResourceLocation}
     */
    @NotNull
    public static NSSPigment createTag(@NotNull ResourceLocation tagId) {
        return new NSSPigment(tagId, true);
    }

    /**
     * Helper method to create an {@link NSSPigment} representing a tag from a {@link TagKey<Pigment>}
     */
    @NotNull
    public static NSSPigment createTag(@NotNull TagKey<Pigment> tag) {
        return createTag(tag.location());
    }

    @Override
    protected boolean isInstance(AbstractNSSTag o) {
        return o instanceof NSSPigment;
    }

    @NotNull
    @Override
    public String getJsonPrefix() {
        return "PIGMENT|";
    }

    @NotNull
    @Override
    public String getType() {
        return "Pigment";
    }

    @NotNull
    @Override
    protected Optional<Either<Named<Pigment>, ITag<Pigment>>> getTag() {
        return getTag(MekanismAPI.pigmentRegistry());
    }

    @Override
    protected Function<Pigment, NormalizedSimpleStack> createNew() {
        return NSSPigment::createPigment;
    }
}