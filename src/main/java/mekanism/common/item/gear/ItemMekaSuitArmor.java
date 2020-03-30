package mekanism.common.item.gear;

import javax.annotation.Nonnull;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.item.RateLimitEnergyHandler;
import mekanism.common.capabilities.radiation.item.RadiationShieldingHandler;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Modules;
import mekanism.common.util.StorageUtils;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public abstract class ItemMekaSuitArmor extends ArmorItem implements IModuleContainerItem, ISpecialGear {

    private FloatingLong maxEnergy;

    protected ItemMekaSuitArmor(IArmorMaterial material, EquipmentSlotType slot, Properties properties, FloatingLong maxEnergy) {
        super(material, slot, properties.setNoRepair().maxStackSize(1));
        this.maxEnergy = maxEnergy;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return StorageUtils.getDurabilityForDisplay(stack);
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);
        if (isInGroup(group)) {
            items.add(StorageUtils.getFilledEnergyVariant(new ItemStack(this), maxEnergy));
        }
    }

    protected abstract double getRadiationShielding();

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        //Note: We interact with this capability using "manual" as the automation type, to ensure we can properly bypass the energy limit for extracting
        // Internal is used by the "null" side, which is what will get used for most items
        return new ItemCapabilityWrapper(stack, RateLimitEnergyHandler.create(() -> maxEnergy, BasicEnergyContainer.notExternal, BasicEnergyContainer.alwaysTrue),
            RadiationShieldingHandler.create(item -> isModuleEnabled(item, Modules.RADIATION_SHIELDING_UNIT) ? getRadiationShielding() : 0));
    }
}
