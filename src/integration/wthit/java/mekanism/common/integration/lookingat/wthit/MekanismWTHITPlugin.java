package mekanism.common.integration.lookingat.wthit;

import mcp.mobius.waila.api.ICommonRegistrar;
import mcp.mobius.waila.api.IWailaCommonPlugin;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityRobit;
import mekanism.common.integration.lookingat.LookingAtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;

@SuppressWarnings("unused")
public class MekanismWTHITPlugin implements IWailaCommonPlugin {

    static final Identifier MEK_DATA = Mekanism.rl("wthit_data");

    @Override
    public void register(ICommonRegistrar registration) {
        registration.blockData(WTHITDataProvider.INSTANCE, BlockEntity.class);
        registration.entityData(WTHITEntityDataProvider.INSTANCE, EntityRobit.class);
        registration.localConfig(LookingAtUtils.ENERGY, true);
        registration.localConfig(LookingAtUtils.FLUID, true);
        registration.localConfig(LookingAtUtils.CHEMICAL, true);
        registration.dataType(WTHITLookingAtHelper.TYPE, WTHITLookingAtHelper.STREAM_CODEC);
    }
}