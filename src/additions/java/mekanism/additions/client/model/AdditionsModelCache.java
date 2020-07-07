package mekanism.additions.client.model;

import mekanism.additions.common.MekanismAdditions;
import mekanism.client.model.BaseModelCache;

public class AdditionsModelCache extends BaseModelCache {

    public static final AdditionsModelCache INSTANCE = new AdditionsModelCache();

    public final JSONModelData BALLOON = registerJSON(MekanismAdditions.rl("item/balloon"));
    public final JSONModelData BALLOON_FREE = registerJSON(MekanismAdditions.rl("item/balloon_free"));
}
