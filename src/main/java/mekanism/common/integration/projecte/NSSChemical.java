package mekanism.common.integration.projecte;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link NormalizedSimpleStack} and {@link moze_intel.projecte.api.nss.NSSTag} for representing {@link Chemical}s.
 */
public final class NSSChemical extends AbstractNSSTag<Chemical> {

    public static final MapCodec<NSSChemical> CODEC = createCodec(MekanismAPI.CHEMICAL_REGISTRY, false, NSSChemical::new);

    private NSSChemical(@NotNull ResourceLocation resourceLocation, boolean isTag) {
        super(resourceLocation, isTag);
    }

    /**
     * Helper method to create an {@link NSSChemical} representing a chemical from a {@link ChemicalStack}
     */
    @NotNull
    public static NSSChemical createChemical(@NotNull ChemicalStack stack) {
        //Don't bother checking if it is empty as getType returns EMPTY which will then fail anyway for being empty
        return createChemical(stack.getChemicalHolder());
    }

    /**
     * Helper method to create an {@link NSSChemical} representing a chemical from a {@link Holder}.
     */
    @NotNull
    public static NSSChemical createChemical(@NotNull Holder<Chemical> chemical) {
        ResourceKey<Chemical> key = chemical.getKey();
        if (key == null) {
            if (!chemical.isBound()) {
                throw new IllegalArgumentException("Can't make an NSSChemical with an unbound direct holder");
            }
            Optional<ResourceKey<Chemical>> registryKey = MekanismAPI.CHEMICAL_REGISTRY.getResourceKey(chemical.value());
            if (registryKey.isEmpty()) {
                throw new IllegalArgumentException("Can't make an NSSChemical with an unregistered chemical");
            }
            key = registryKey.get();
        }
        //This should never be null, or it would have crashed on being registered
        return createChemical(key.location());
    }

    /**
     * Helper method to create an {@link NSSChemical} representing a chemical from a {@link ResourceLocation}
     */
    @NotNull
    public static NSSChemical createChemical(@NotNull ResourceLocation chemicalId) {
        if (chemicalId.equals(MekanismAPI.CHEMICAL_REGISTRY.getDefaultKey())) {
            throw new IllegalArgumentException("Can't make NSSChemical with an empty chemical");
        }
        return new NSSChemical(chemicalId, false);
    }

    /**
     * Helper method to create an {@link NSSChemical} representing a tag from a {@link ResourceLocation}
     */
    @NotNull
    public static NSSChemical createTag(@NotNull ResourceLocation tagId) {
        return new NSSChemical(tagId, true);
    }

    /**
     * Helper method to create an {@link NSSChemical} representing a tag from a {@link TagKey}&lt;{@link Chemical}&gt;
     */
    @NotNull
    public static NSSChemical createTag(@NotNull TagKey<Chemical> tag) {
        return createTag(tag.location());
    }

    @NotNull
    @Override
    protected Registry<Chemical> getRegistry() {
        return MekanismAPI.CHEMICAL_REGISTRY;
    }

    @Override
    protected NormalizedSimpleStack createNew(Holder<Chemical> chemical) {
        return createChemical(chemical);
    }

    @Override
    public MapCodec<? extends NormalizedSimpleStack> codec() {
        return CODEC;
    }
}