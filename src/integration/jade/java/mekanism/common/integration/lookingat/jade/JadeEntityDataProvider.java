package mekanism.common.integration.lookingat.jade;

import mekanism.common.integration.lookingat.LookingAtConstants;
import mekanism.common.integration.lookingat.LookingAtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;

public class JadeEntityDataProvider implements IServerDataProvider<EntityAccessor> {

    static final JadeEntityDataProvider INSTANCE = new JadeEntityDataProvider();

    @Override
    public Identifier getUid() {
        return LookingAtConstants.Jade.ENTITY_DATA;
    }

    @Override
    public void appendServerData(CompoundTag data, EntityAccessor entityAccessor) {
        JadeLookingAtHelper helper = new JadeLookingAtHelper();
        LookingAtUtils.addInfo(helper, entityAccessor.getEntity());
        //Add our data if we have any
        helper.finalizeData(data, entityAccessor);
    }
}