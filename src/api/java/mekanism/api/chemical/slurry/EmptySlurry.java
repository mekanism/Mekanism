package mekanism.api.chemical.slurry;

import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;

public final class EmptySlurry extends Slurry {

    public EmptySlurry() {
        super(SlurryBuilder.clean().hidden());
        setRegistryName(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "empty_slurry"));
    }

    @Override
    public boolean isIn(@Nonnull ITag<Slurry> tags) {
        //Empty slurry is in no tags
        return false;
    }

    @Nonnull
    @Override
    public Set<ResourceLocation> getTags() {
        return Collections.emptySet();
    }
}