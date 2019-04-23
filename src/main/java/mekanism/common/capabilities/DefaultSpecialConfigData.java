package mekanism.common.capabilities;

import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.common.capabilities.DefaultStorageHelper.NullStorage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class DefaultSpecialConfigData extends DefaultConfigCardAccess implements ISpecialConfigData {

    public static void register() {
        CapabilityManager.INSTANCE
              .register(ISpecialConfigData.class, new NullStorage<>(), DefaultSpecialConfigData::new);
    }

    @Override
    public NBTTagCompound getConfigurationData(NBTTagCompound nbtTags) {
        return null;
    }

    @Override
    public void setConfigurationData(NBTTagCompound nbtTags) {

    }

    @Override
    public String getDataType() {
        return null;
    }
}
