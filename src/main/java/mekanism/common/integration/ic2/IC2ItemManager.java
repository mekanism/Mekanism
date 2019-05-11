package mekanism.common.integration.ic2;

import ic2.api.item.IElectricItemManager;
import mekanism.api.energy.IEnergizedItem;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class IC2ItemManager implements IElectricItemManager {

    public IEnergizedItem energizedItem;

    public static IC2ItemManager getManager(IEnergizedItem item) {
        IC2ItemManager manager = new IC2ItemManager();
        manager.energizedItem = item;
        return manager;
    }

    @Override
    public double charge(ItemStack itemStack, double amount, int tier, boolean ignoreTransferLimit, boolean simulate) {
        if (energizedItem.canReceive(itemStack)) {
            double energyNeeded = energizedItem.getMaxEnergy(itemStack) - energizedItem.getEnergy(itemStack);
            double energyToStore = Math.min(Math.min(amount * MekanismConfig.current().general.FROM_IC2.val(), energizedItem.getMaxEnergy(itemStack) * 0.01), energyNeeded);
            if (!simulate) {
                energizedItem.setEnergy(itemStack, energizedItem.getEnergy(itemStack) + energyToStore);
            }
            return MekanismUtils.clampToInt(energyToStore * MekanismConfig.current().general.TO_IC2.val());
        }
        return 0;
    }

    @Override
    public double discharge(ItemStack itemStack, double amount, int tier, boolean ignoreTransferLimit, boolean external,
          boolean simulate) {
        if (energizedItem.canSend(itemStack)) {
            double energyWanted = amount * MekanismConfig.current().general.FROM_IC2.val();
            double energyToGive = Math.min(Math.min(energyWanted, energizedItem.getMaxEnergy(itemStack) * 0.01), energizedItem.getEnergy(itemStack));
            if (!simulate) {
                energizedItem.setEnergy(itemStack, energizedItem.getEnergy(itemStack) - energyToGive);
            }
            return MekanismUtils.clampToInt(energyToGive * MekanismConfig.current().general.TO_IC2.val());
        }
        return 0;
    }

    @Override
    public boolean canUse(ItemStack itemStack, double amount) {
        return energizedItem.getEnergy(itemStack) >= amount * MekanismConfig.current().general.FROM_IC2.val();
    }

    @Override
    public double getCharge(ItemStack itemStack) {
        return MekanismUtils.clampToInt(energizedItem.getEnergy(itemStack) * MekanismConfig.current().general.TO_IC2.val());
    }

    @Override
    public boolean use(ItemStack itemStack, double amount, EntityLivingBase entity) {
        return false;
    }

    @Override
    public void chargeFromArmor(ItemStack itemStack, EntityLivingBase entity) {
    }

    @Override
    public String getToolTip(ItemStack itemStack) {
        return null;
    }

    @Override
    public double getMaxCharge(ItemStack stack) {
        return 0;
    }

    @Override
    public int getTier(ItemStack stack) {
        return 4;
    }
}