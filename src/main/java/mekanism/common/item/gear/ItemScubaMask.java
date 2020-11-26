package mekanism.common.item.gear;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.client.render.armor.CustomArmor;
import mekanism.client.render.armor.ScubaMaskArmor;
import mekanism.client.render.item.ISTERProvider;
import mekanism.common.Mekanism;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemScubaMask extends ItemSpecialArmor {

    private static final ScubaMaskMaterial SCUBA_MASK_MATERIAL = new ScubaMaskMaterial();

    public ItemScubaMask(Properties properties) {
        super(SCUBA_MASK_MATERIAL, EquipmentSlotType.HEAD, properties.rarity(Rarity.RARE).setNoRepair().setISTER(ISTERProvider::scubaMask));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        if (stack.getTag() == null) {
            stack.setTag(new CompoundNBT());
        }
        stack.getTag().putInt("HideFlags", 2);
        return super.initCapabilities(stack, nbt);
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public CustomArmor getGearModel() {
        return ScubaMaskArmor.SCUBA_MASK;
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    protected static class ScubaMaskMaterial extends BaseSpecialArmorMaterial {

        @Override
        public String getName() {
            return Mekanism.MODID + ":scuba_mask";
        }
    }
}