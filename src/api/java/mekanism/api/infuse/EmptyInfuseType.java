package mekanism.api.infuse;

import java.util.Collections;
import java.util.Set;
import mekanism.api.MekanismAPI;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

//TODO: Override things so people cannot modify the empty infuse type
public class EmptyInfuseType extends InfuseType {

    public EmptyInfuseType() {
        super(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "empty_infuse_type"), -1);
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