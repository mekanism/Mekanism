package mekanism.common.item.gear;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.Mekanism;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStack.TooltipPart;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemScubaMask extends ItemSpecialArmor {

    private static final ScubaMaskMaterial SCUBA_MASK_MATERIAL = new ScubaMaskMaterial();

    public ItemScubaMask(Properties properties) {
        super(SCUBA_MASK_MATERIAL, EquipmentSlot.HEAD, properties.rarity(Rarity.RARE).setNoRepair());
    }

    @Override
    public void initializeClient(@Nonnull Consumer<IItemRenderProperties> consumer) {
        consumer.accept(RenderPropertiesProvider.scubaMask());
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        stack.hideTooltipPart(TooltipPart.MODIFIERS);
        return super.initCapabilities(stack, nbt);
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