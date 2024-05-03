package mekanism.common.integration.lookingat.jade;

import mekanism.common.integration.lookingat.LookingAtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public class JadeDataProvider implements IServerDataProvider<BlockAccessor> {

    static final JadeDataProvider INSTANCE = new JadeDataProvider();

    @Override
    public ResourceLocation getUid() {
        return JadeConstants.BLOCK_DATA;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor blockAccessor) {
        JadeLookingAtHelper helper = new JadeLookingAtHelper(blockAccessor.getLevel().registryAccess());
        LookingAtUtils.addInfoOrRedirect(helper, blockAccessor.getLevel(), blockAccessor.getPosition(), blockAccessor.getBlockState(), blockAccessor.getBlockEntity(), true, true);
        //Add our data if we have any
        helper.finalizeData(data);
    }
}