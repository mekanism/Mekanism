package mekanism.common.capabilities.basic;

import mekanism.api.IConfigCardAccess;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.basic.DefaultStorageHelper.NullStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class DefaultConfigCardAccess implements IConfigCardAccess {

    public static void register() {
        CapabilityManager.INSTANCE.register(IConfigCardAccess.class, new NullStorage<>(), DefaultConfigCardAccess::new);
    }

    @Override
    public String getConfigCardName() {
        return MekanismLang.NONE.getTranslationKey();
    }

    @Override
    public CompoundNBT getConfigurationData(PlayerEntity player) {
        return new CompoundNBT();
    }

    @Override
    public void setConfigurationData(PlayerEntity player, CompoundNBT data) {
    }

    @Override
    public TileEntityType<?> getConfigurationDataType() {
        return null;
    }

    @Override
    public void configurationDataSet() {
    }
}