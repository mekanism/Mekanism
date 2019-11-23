package mekanism.api.infuse;

import java.util.Collections;
import java.util.Set;
import mekanism.api.MekanismAPI;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public final class EmptyInfuseType extends InfuseType {

    public EmptyInfuseType() {
        super(-1);
        setRegistryName(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "empty_infuse_type"));
    }

    @Override
    public void setTint(int tint) {
        //NO-OP
    }

    @Override
    public boolean isIn(Tag<InfuseType> tags) {
        //Empty infuse type is in no tags
        return false;
    }

    @Override
    public Set<ResourceLocation> getTags() {
        return Collections.emptySet();
    }
}