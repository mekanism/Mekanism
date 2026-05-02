package mekanism.client.model.robit;

import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.client.resources.model.ResolvedModel;

@NothingNullByDefault
public class RobitModelDataBakedModel {
    private final ResolvedModel original;

    public RobitModelDataBakedModel(ResolvedModel original) {
        this.original = original;
    }

    public ResolvedModel getModel() {
        return original;
    }
}