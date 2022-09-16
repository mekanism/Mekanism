package mekanism.additions.client.model;

import mekanism.additions.common.MekanismAdditions;
import mekanism.client.model.BaseModelCache;

public class AdditionsModelCache extends BaseModelCache {

    public static final AdditionsModelCache INSTANCE = new AdditionsModelCache();

    public final JSONModelData BALLOON = registerJSON("item/balloon_latched");
    public final JSONModelData BALLOON_FREE = registerJSON("item/balloon_free");

    private AdditionsModelCache() {
        super(MekanismAdditions.MODID);
    }
}
