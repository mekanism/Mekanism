package mekanism.common.integration.lookingat.wthit;

import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;
import mcp.mobius.waila.api.IServerDataProvider;
import mekanism.common.integration.lookingat.HwylaLookingAtHelper;
import mekanism.common.integration.lookingat.LookingAtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public class WTHITEntityDataProvider implements IServerDataProvider<Entity> {

    static final WTHITEntityDataProvider INSTANCE = new WTHITEntityDataProvider();

    @Override
    public void appendServerData(CompoundTag data, IServerAccessor<Entity> serverAccessor, IPluginConfig config) {
        HwylaLookingAtHelper helper = new HwylaLookingAtHelper();
        LookingAtUtils.addInfo(helper, serverAccessor.getTarget());
        //Add our data if we have any
        helper.finalizeData(data);
    }
}