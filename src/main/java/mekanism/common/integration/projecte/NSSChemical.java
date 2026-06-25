package mekanism.common.integration.projecte;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismRegistries;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.chemical.ChemicalStack;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.TypedInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

/// Implementation of [NormalizedSimpleStack] and [moze_intel.projecte.api.nss.NSSTag] for representing [Chemical]s.
public final class NSSChemical extends AbstractNSSTag<Chemical> {

    public static final MapCodec<NSSChemical> CODEC = createCodec(MekanismAPI.CHEMICAL_REGISTRY, false, NSSChemical::new);

    private NSSChemical(Identifier resourceLocation, boolean isTag) {
        super(resourceLocation, isTag);
    }

    /// Helper method to create an [NSSChemical] representing a chemical from a [ChemicalStack]
    public static NSSChemical createChemical(TypedInstance<Chemical> stack) {
        //Don't bother checking if it is empty as getType returns EMPTY which will then fail anyway for being empty
        return createChemical(stack.typeHolder());
    }

    /// Helper method to create an [NSSChemical] representing a chemical from a [Holder].
    public static NSSChemical createChemical(Holder<Chemical> chemical) {
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
        return createChemical(key.identifier());
    }

    /// Helper method to create an [NSSChemical] representing a chemical from a [Identifier]
    public static NSSChemical createChemical(Identifier chemicalId) {
        if (chemicalId.equals(ChemicalIds.EMPTY.identifier())) {
            throw new IllegalArgumentException("Can't make NSSChemical with an empty chemical");
        }
        return new NSSChemical(chemicalId, false);
    }

    /// Helper method to create an [NSSChemical] representing a tag from a [Identifier]
    public static NSSChemical createTag(Identifier tagId) {
        return new NSSChemical(tagId, true);
    }

    /// Helper method to create an [NSSChemical] representing a tag from a [TagKey]<[Chemical]>
    public static NSSChemical createTag(TagKey<Chemical> tag) {
        return createTag(tag.location());
    }

    @Override
    protected Registry<Chemical> getRegistry() {
        return MekanismRegistries.CHEMICAL;
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