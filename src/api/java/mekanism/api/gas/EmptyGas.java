package mekanism.api.gas;

import java.util.Collections;
import java.util.Set;
import mekanism.api.MekanismAPI;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public final class EmptyGas extends Gas {

    public EmptyGas() {
        super(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "empty_gas"), -1);
        super.setVisible(false);
    }

    @Override
    public void setVisible(boolean v) {
        //NO-OP
    }

    @Override
    public void setTint(int tint) {
        //NO-OP
    }

    @Override
    public boolean isIn(Tag<Gas> tags) {
        //Empty gas is in no tags
        return false;
    }

    @Override
    public Set<ResourceLocation> getTags() {
        return Collections.emptySet();
    }
}