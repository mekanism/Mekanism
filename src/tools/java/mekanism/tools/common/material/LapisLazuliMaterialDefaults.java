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
        return 3;
    }

    @Override
    public float getSwordAtkSpeed() {
        return -2.4F;
    }

    @Override
    public float getShovelDamage() {
        return 1.5F;
    }

    @Override
    public float getShovelAtkSpeed() {
        return -3.0F;
    }

    @Override
    public float getAxeDamage() {
        return 6;
    }

    @Override
    public float getAxeAtkSpeed() {
        return -3.1F;
    }

    @Override
    public int getPickaxeDamage() {
        return 1;
    }

    @Override
    public float getPickaxeAtkSpeed() {
        return -2.8F;
    }

    @Override
    public float getHoeAtkSpeed() {
        return getAttackDamage() - 3.0F;
    }

    @Override
    public float getPaxelDamage() {
        return 6;
    }

    @Override
    public float getPaxelAtkSpeed() {
        return -2.4F;
    }

    @Override
    public int getPaxelHarvestLevel() {
        return 2;
    }

    @Override
    public int getMaxUses() {
        //TODO: Used to be 250 for paxel
        return 200;
    }

    @Override
    public float getEfficiency() {
        //TODO: Used to be 6 for paxel
        return 5;
    }

    @Override
    public float getAttackDamage() {
        return 2;
    }

    @Override
    public int getHarvestLevel() {
        return 2;
    }

    @Override
    public int getEnchantability() {
        //TODO: Used to be 10 for paxel
        return 8;
    }

    @Override
    public float getToughness() {
        return 0;
    }

    @Override
    public int getDurability(@Nonnull EquipmentSlotType slotType) {
        switch (slotType) {
            case FEET:
                return 169;
            case LEGS:
                return 195;
            case CHEST:
                return 208;
            case HEAD:
                return 143;
        }
        return 0;
    }

    @Override
    public int getDamageReductionAmount(@Nonnull EquipmentSlotType slotType) {
        switch (slotType) {
            case FEET:
                return 2;
            case LEGS:
                return 6;
            case CHEST:
                return 5;
            case HEAD:
                return 2;
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