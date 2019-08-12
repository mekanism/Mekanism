//TODO: IC2
/*package mekanism.common.integration.ic2;

import ic2.api.item.IElectricItemManager;
import mekanism.api.energy.IEnergizedItem;
import net.minecraft.entity.LivingEntity;
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
            double energyToStore = Math.min(Math.min(IC2Integration.fromEU(amount), energizedItem.getMaxEnergy(itemStack) * 0.01), energyNeeded);
            if (!simulate) {
                energizedItem.setEnergy(itemStack, energizedItem.getEnergy(itemStack) + energyToStore);
            }
            return IC2Integration.toEU(energyToStore);
        }
        return 0;
    }

    @Override
    public double discharge(ItemStack itemStack, double amount, int tier, boolean ignoreTransferLimit, boolean external,
          boolean simulate) {
        if (energizedItem.canSend(itemStack)) {
            double energyWanted = IC2Integration.fromEU(amount);
            double energyToGive = Math.min(Math.min(energyWanted, energizedItem.getMaxEnergy(itemStack) * 0.01), energizedItem.getEnergy(itemStack));
            if (!simulate) {
                energizedItem.setEnergy(itemStack, energizedItem.getEnergy(itemStack) - energyToGive);
            }
            return IC2Integration.toEU(energyToGive);
        }
        return 0;
    }

    @Override
    public boolean canUse(ItemStack itemStack, double amount) {
        return energizedItem.getEnergy(itemStack) >= IC2Integration.fromEU(amount);
    }

    @Override
    public double getCharge(ItemStack itemStack) {
        return IC2Integration.toEU(energizedItem.getEnergy(itemStack));
    }

    @Override
    public boolean use(ItemStack itemStack, double amount, LivingEntity entity) {
        return false;
    }

    @Override
    public void chargeFromArmor(ItemStack itemStack, LivingEntity entity) {
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
}*/