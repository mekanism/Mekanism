package mekanism.common.integration.projecte;

import com.mojang.datafixers.util.Either;
import java.util.Optional;
import java.util.function.Function;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.providers.ISlurryProvider;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.tags.ITag;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link NormalizedSimpleStack} and {@link moze_intel.projecte.api.nss.NSSTag} for representing {@link Slurry}.
 */
public final class NSSSlurry extends AbstractNSSTag<Slurry> {

    private NSSSlurry(@NotNull ResourceLocation resourceLocation, boolean isTag) {
        super(resourceLocation, isTag);
    }

    /**
     * Helper method to create an {@link NSSSlurry} representing a slurry type from a {@link SlurryStack}
     */
    @NotNull
    public static NSSSlurry createSlurry(@NotNull SlurryStack stack) {
        //Don't bother checking if it is empty as getType returns EMPTY which will then fail anyway for being empty
        return createSlurry(stack.getType());
    }

    /**
     * Helper method to create an {@link NSSSlurry} representing a slurry type from a {@link ISlurryProvider}
     */
    @NotNull
    public static NSSSlurry createSlurry(@NotNull ISlurryProvider slurryProvider) {
        return createSlurry(slurryProvider.getChemical());
    }

    /**
     * Helper method to create an {@link NSSSlurry} representing a slurry type from a {@link Slurry}
     */
    @NotNull
    public static NSSSlurry createSlurry(@NotNull Slurry slurry) {
        if (slurry.isEmptyType()) {
            throw new IllegalArgumentException("Can't make NSSSlurry with an empty slurry");
        }
        //This should never be null, or it would have crashed on being registered
        return createSlurry(slurry.getRegistryName());
    }

    /**
     * Helper method to create an {@link NSSSlurry} representing a slurry type from a {@link ResourceLocation}
     */
    @NotNull
    public static NSSSlurry createSlurry(@NotNull ResourceLocation slurryID) {
        return new NSSSlurry(slurryID, false);
    }

    /**
     * Helper method to create an {@link NSSSlurry} representing a tag from a {@link ResourceLocation}
     */
    @NotNull
    public static NSSSlurry createTag(@NotNull ResourceLocation tagId) {
        return new NSSSlurry(tagId, true);
    }

    /**
     * Helper method to create an {@link NSSSlurry} representing a tag from a {@link TagKey<Slurry>}
     */
    @NotNull
    public static NSSSlurry createTag(@NotNull TagKey<Slurry> tag) {
        return createTag(tag.location());
    }

    @Override
    protected boolean isInstance(AbstractNSSTag o) {
        return o instanceof NSSSlurry;
    }

    @NotNull
    @Override
    public String getJsonPrefix() {
        return "SLURRY|";
    }

    @NotNull
    @Override
    public String getType() {
        return "Slurry";
    }

    @NotNull
    @Override
    protected Optional<Either<Named<Slurry>, ITag<Slurry>>> getTag() {
        return getTag(MekanismAPI.slurryRegistry());
    }

    @Override
    protected Function<Slurry, NormalizedSimpleStack> createNew() {
        return NSSSlurry::createSlurry;
    }
}