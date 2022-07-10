package mekanism.common.item.gear;

import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.Mekanism;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStack.TooltipPart;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class ItemScubaMask extends ItemSpecialArmor {

    private static final ScubaMaskMaterial SCUBA_MASK_MATERIAL = new ScubaMaskMaterial();

    public ItemScubaMask(Properties properties) {
        super(SCUBA_MASK_MATERIAL, EquipmentSlot.HEAD, properties.rarity(Rarity.RARE).setNoRepair());
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(RenderPropertiesProvider.scubaMask());
    }

    @Override
    public int getDefaultTooltipHideFlags(@NotNull ItemStack stack) {
        return super.getDefaultTooltipHideFlags(stack) | TooltipPart.MODIFIERS.getMask();
    }

    @NothingNullByDefault
    protected static class ScubaMaskMaterial extends BaseSpecialArmorMaterial {

        @Override
        public String getName() {
            return Mekanism.MODID + ":scuba_mask";
        }
    }
}