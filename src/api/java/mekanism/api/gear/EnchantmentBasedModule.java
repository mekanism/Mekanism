package mekanism.api.gear;

import java.util.LinkedHashMap;
import java.util.Map;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * Abstract implementation to make creating custom modules that provide a specific enchantment when installed easier, while also properly "hiding" the fact that there is
 * an enchantment applied. This does not provide any easy way to make the enchantment use energy or other resources, and is probably only useful for enchantments that
 * have a lot of hardcoded checks so reproducing functionality would be extremely hard if even possible.
 *
 * Instances of this should be returned via the {@link ModuleData}.
 */
@NothingNullByDefault
public abstract class EnchantmentBasedModule<MODULE extends EnchantmentBasedModule<MODULE>> implements ICustomModule<MODULE> {

    /**
     * Gets the enchantment that this module provides when enabled.
     *
     * @return The enchantment that this module provides.
     */
    public abstract Enchantment getEnchantment();

    @Override
    public void onAdded(IModule<MODULE> module, boolean first) {
        if (module.isEnabled()) {
            if (first) {
                enchant(module, getEnchantment());
            } else {
                Map<Enchantment, Integer> enchantments = getEnchantments(module);
                enchantments.put(getEnchantment(), module.getInstalledCount());
                setEnchantments(module, enchantments);
            }
        }
    }

    @Override
    public void onRemoved(IModule<MODULE> module, boolean last) {
        if (module.isEnabled()) {
            Map<Enchantment, Integer> enchantments = getEnchantments(module);
            if (last) {
                enchantments.remove(getEnchantment());
            } else {
                enchantments.put(getEnchantment(), module.getInstalledCount());
            }
            setEnchantments(module, enchantments);
        }
    }

    @Override
    public void onEnabledStateChange(IModule<MODULE> module) {
        if (module.isEnabled()) {
            //Was disabled and now is enabled, add enchantment
            enchant(module, getEnchantment());
        } else {
            //Was enabled and is now disabled, remove the enchantment
            Map<Enchantment, Integer> enchantments = getEnchantments(module);
            enchantments.remove(getEnchantment());
            setEnchantments(module, enchantments);
        }
    }

    private void enchant(IModule<MODULE> module, Enchantment enchantment) {
        CompoundTag dataMap = getOrCreateDataTag(module);
        ListTag enchantments;
        if (dataMap.contains(NBTConstants.ENCHANTMENTS, Tag.TAG_LIST)) {
            enchantments = dataMap.getList(NBTConstants.ENCHANTMENTS, Tag.TAG_COMPOUND);
        } else {
            dataMap.put(NBTConstants.ENCHANTMENTS, enchantments = new ListTag());
        }
        enchantments.add(EnchantmentHelper.storeEnchantment(EnchantmentHelper.getEnchantmentId(enchantment), (byte) module.getInstalledCount()));
    }

    private Map<Enchantment, Integer> getEnchantments(IModule<MODULE> module) {
        CompoundTag tag = module.getContainer().getTag();
        if (tag != null && tag.contains(NBTConstants.MEK_DATA, Tag.TAG_COMPOUND)) {
            CompoundTag mekData = tag.getCompound(NBTConstants.MEK_DATA);
            ListTag enchantmentTag = mekData.getList(NBTConstants.ENCHANTMENTS, Tag.TAG_COMPOUND);
            return EnchantmentHelper.deserializeEnchantments(enchantmentTag);
        }
        return new LinkedHashMap<>();
    }

    private CompoundTag getOrCreateDataTag(IModule<MODULE> module) {
        CompoundTag tag = module.getContainer().getOrCreateTag();
        if (tag.contains(NBTConstants.MEK_DATA, Tag.TAG_COMPOUND)) {
            return tag.getCompound(NBTConstants.MEK_DATA);
        }
        CompoundTag dataMap = new CompoundTag();
        tag.put(NBTConstants.MEK_DATA, dataMap);
        return dataMap;
    }

    private void setEnchantments(IModule<MODULE> module, Map<Enchantment, Integer> enchantments) {
        ListTag enchantmentTag = new ListTag();
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (enchantment != null) {
                enchantmentTag.add(EnchantmentHelper.storeEnchantment(EnchantmentHelper.getEnchantmentId(enchantment), entry.getValue()));
            }
        }
        CompoundTag dataMap = getOrCreateDataTag(module);
        if (enchantments.isEmpty()) {
            dataMap.remove(NBTConstants.ENCHANTMENTS);
            if (dataMap.isEmpty()) {
                module.getContainer().removeTagKey(NBTConstants.MEK_DATA);
            }
        } else {
            dataMap.put(NBTConstants.ENCHANTMENTS, enchantmentTag);
        }
    }
}