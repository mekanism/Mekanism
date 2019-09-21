package mekanism.api.infuse;

import mekanism.api.MekanismAPI;
import net.minecraft.util.ResourceLocation;

//TODO: Override things so people cannot modify the empty infuse type
public class EmptyInfuseType extends InfuseType {

    public EmptyInfuseType() {
        super(new ResourceLocation(MekanismAPI.API_VERSION, "empty_infuse_type"), -1);
    }
}