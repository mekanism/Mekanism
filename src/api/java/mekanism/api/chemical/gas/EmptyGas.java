package mekanism.api.chemical.gas;

import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;

public final class EmptyGas extends Gas {

    public EmptyGas() {
        super(GasBuilder.builder().hidden());
    }

    @Override
    public boolean isIn(@Nonnull Tag<Gas> tags) {
        //Empty gas is in no tags
        return false;
    }

    @Nonnull
    @Override
    public Set<ResourceLocation> getTags() {
        return Collections.emptySet();
    }
}