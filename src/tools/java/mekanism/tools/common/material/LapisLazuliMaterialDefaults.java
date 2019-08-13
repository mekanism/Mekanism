package mekanism.tools.common.material;

import javax.annotation.Nonnull;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LapisLazuliMaterialDefaults implements IMekanismMaterial {

    @Override
    public int getSwordDamage() {
        return 0;
    }

    @Override
    public float getSwordAtkSpeed() {
        return 0;
    }

    @Override
    public int getShovelDamage() {
        return 0;
    }

    @Override
    public float getShovelAtkSpeed() {
        return 0;
    }

    @Override
    public int getAxeDamage() {
        return 0;
    }

    @Override
    public float getAxeAtkSpeed() {
        return 0;
    }

    @Override
    public int getPickaxeDamage() {
        return 0;
    }

    @Override
    public float getPickaxeAtkSpeed() {
        return 0;
    }

    @Override
    public float getHoeAtkSpeed() {
        return 0;
    }

    @Override
    public int getPaxelHarvestLevel() {
        return 0;
    }

    @Override
    public int getPaxelDamage() {
        return 0;
    }

    @Override
    public float getPaxelAtkSpeed() {
        return 0;
    }

    @Override
    public int getMaxUses() {
        return 0;
    }

    @Override
    public float getEfficiency() {
        return 0;
    }

    @Override
    public float getAttackDamage() {
        return 0;
    }

    @Override
    public int getHarvestLevel() {
        return 0;
    }

    @Override
    public int getEnchantability() {
        return 0;
    }

    @Override
    public float getToughness() {
        return 0;
    }

    @Override
    public int getDurability(@Nonnull EquipmentSlotType slotType) {
        switch (slotType) {
            case FEET:
                return 0;
            case LEGS:
                return 0;
            case CHEST:
                return 0;
            case HEAD:
                return 0;
        }
        return 0;
    }

    @Override
    public int getDamageReductionAmount(@Nonnull EquipmentSlotType slotType) {
        switch (slotType) {
            case FEET:
                return 0;
            case LEGS:
                return 0;
            case CHEST:
                return 0;
            case HEAD:
                return 0;
        }
        return 0;
    }

    @Nonnull
    @Override
    public String getRegistryPrefix() {
        return "lapis_lazuli";
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() {
        return SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND;
    }

    @Nonnull
    @Override
    public Ingredient getRepairMaterial() {
        return Ingredient.fromItems(Items.LAPIS_LAZULI);
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public String getName() {
        return getRegistryPrefix();
    }
}