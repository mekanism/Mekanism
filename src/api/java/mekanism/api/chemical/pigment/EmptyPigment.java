package mekanism.api.chemical.pigment;

import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;

public final class EmptyPigment extends Pigment {

    public EmptyPigment() {
        super(PigmentBuilder.builder().hidden());
    }

    @Override
    public boolean isIn(@Nonnull ITag<Pigment> tags) {
        //Empty pigment is in no tags
        return false;
    }

    @Nonnull
    @Override
    public Set<ResourceLocation> getTags() {
        return Collections.emptySet();
    }
}