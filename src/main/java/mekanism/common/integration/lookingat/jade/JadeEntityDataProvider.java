package mekanism.common.integration.lookingat.jade;

import mekanism.common.integration.lookingat.HwylaLookingAtHelper;
import mekanism.common.integration.lookingat.LookingAtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import snownee.jade.api.IServerDataProvider;

public class JadeEntityDataProvider implements IServerDataProvider<Entity> {

    static final JadeEntityDataProvider INSTANCE = new JadeEntityDataProvider();

    @Override
    public ResourceLocation getUid() {
        return JadeConstants.ENTITY_DATA;
    }

    @Override
    public void appendServerData(CompoundTag data, ServerPlayer player, Level world, Entity entity, boolean showDetails) {
        HwylaLookingAtHelper helper = new HwylaLookingAtHelper();
        LookingAtUtils.addInfo(helper, entity);
        //Add our data if we have any
        helper.finalizeData(data);
    }
}