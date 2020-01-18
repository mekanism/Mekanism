package mekanism.common.item.gear;

import java.util.concurrent.Callable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.client.render.ModelCustomArmor;
import mekanism.client.render.armor.CustomArmor;
import mekanism.client.render.item.gear.RenderGasMask;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemGasMask extends ArmorItem implements ISpecialGear {

    public static final GasMaskMaterial GAS_MASK_MATERIAL = new GasMaskMaterial();

    public ItemGasMask(Properties properties) {
        super(GAS_MASK_MATERIAL, EquipmentSlotType.HEAD, properties.setTEISR(() -> getTEISR()).setNoRepair());
    }

    @OnlyIn(Dist.CLIENT)
    private static Callable<ItemStackTileEntityRenderer> getTEISR() {
        //NOTE: This extra method is needed to avoid classloading issues on servers
        return RenderGasMask::new;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
        return "mekanism:render/null_armor.png";
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public CustomArmor getGearModel() {
        ModelCustomArmor model = ModelCustomArmor.INSTANCE;
        model.modelType = ModelCustomArmor.ArmorModel.GASMASK;
        return model;
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    protected static class GasMaskMaterial implements IArmorMaterial {

        @Override
        public int getDurability(EquipmentSlotType slotType) {
            return 0;
        }

        @Override
        public int getDamageReductionAmount(EquipmentSlotType slotType) {
            return 0;
        }

        @Override
        public int getEnchantability() {
            return 0;
        }

        @Override
        public SoundEvent getSoundEvent() {
            return SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
        }

        @Override
        public Ingredient getRepairMaterial() {
            return Ingredient.EMPTY;
        }

        @Override
        public String getName() {
            return "gas_mask";
        }

        @Override
        public float getToughness() {
            return 0;
        }
    }
}