package mekanism.api.chemical.infuse;

import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;

public final class EmptyInfuseType extends InfuseType {

    public EmptyInfuseType() {
        super(InfuseTypeBuilder.builder().hidden());
    }

    @Override
    public boolean isIn(@Nonnull ITag<InfuseType> tags) {
        //Empty infuse type is in no tags
        return false;
    }

    @Nonnull
    @Override
    public Set<ResourceLocation> getTags() {
        return Collections.emptySet();
    }
}