package mekanism.api.chemical.gas;

import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;

public final class EmptyGas extends Gas {

    public EmptyGas() {
        super(GasBuilder.builder().hidden());
        setRegistryName(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "empty_gas"));
    }

    @Override
    public boolean isIn(@Nonnull ITag<Gas> tags) {
        //Empty gas is in no tags
        return false;
    }

    @Nonnull
    @Override
    public Set<ResourceLocation> getTags() {
        return Collections.emptySet();
    }
}