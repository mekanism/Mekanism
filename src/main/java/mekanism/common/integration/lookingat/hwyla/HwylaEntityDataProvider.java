package mekanism.common.integration.lookingat.hwyla;

import mcp.mobius.waila.api.IServerDataProvider;
import mekanism.common.integration.lookingat.LookingAtUtils;
import mekanism.common.integration.lookingat.hwyla.HwylaDataProvider.HwylaLookingAtHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class HwylaEntityDataProvider implements IServerDataProvider<Entity> {

    static final HwylaEntityDataProvider INSTANCE = new HwylaEntityDataProvider();

    @Override
    public void appendServerData(CompoundTag data, ServerPlayer player, Level world, Entity entity, boolean showDetails) {
        HwylaLookingAtHelper helper = new HwylaLookingAtHelper();
        LookingAtUtils.addInfo(helper, entity);
        //Add our data if we have any
        helper.finalizeData(data);
    }
}