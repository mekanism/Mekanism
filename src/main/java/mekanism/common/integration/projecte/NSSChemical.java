package mekanism.common.integration.projecte;

import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalTags;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public abstract class NSSChemical<CHEMICAL extends Chemical<CHEMICAL>> extends AbstractNSSTag<CHEMICAL> {

    protected NSSChemical(@Nonnull ResourceLocation resourceLocation, boolean isTag) {
        super(resourceLocation, isTag);
    }

    @Nonnull
    @Override
    protected final Optional<Named<CHEMICAL>> getTag() {
        return Optional.empty();
    }

    @Nonnull
    protected abstract ChemicalTags<CHEMICAL> tags();

    @Override
    public void forEachElement(Consumer<NormalizedSimpleStack> consumer) {
        if (representsTag()) {
            tags().getManager().ifPresent(manager -> {
                TagKey<CHEMICAL> key = manager.createTagKey(getResourceLocation());
                if (manager.isKnownTagName(key)) {
                    manager.getTag(key).stream().map(createNew()).forEach(consumer);
                }
            });
        }
    }
}