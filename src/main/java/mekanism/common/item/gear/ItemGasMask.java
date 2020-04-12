package mekanism.common.item.gear;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.client.render.armor.CustomArmor;
import mekanism.client.render.armor.GasMaskArmor;
import mekanism.client.render.item.ISTERProvider;
import mekanism.common.Mekanism;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemGasMask extends ArmorItem implements ISpecialGear {

    private static final GasMaskMaterial GAS_MASK_MATERIAL = new GasMaskMaterial();

    public ItemGasMask(Properties properties) {
        super(GAS_MASK_MATERIAL, EquipmentSlotType.HEAD, properties.setNoRepair().setISTER(ISTERProvider::gasMask));
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
        return "mekanism:render/null_armor.png";
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public CustomArmor getGearModel() {
        return GasMaskArmor.GAS_MASK;
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    protected static class GasMaskMaterial extends BaseSpecialArmorMaterial {

        @Override
        public int getDamageReductionAmount(EquipmentSlotType slotType) {
            return 0;
        }

        @Override
        public String getName() {
            return Mekanism.MODID + ":gas_mask";
        }

        @Override
        public float getToughness() {
            return 0;
        }
    }
}