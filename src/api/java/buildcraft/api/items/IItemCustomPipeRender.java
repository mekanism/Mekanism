package buildcraft.api.items;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IItemCustomPipeRender {
    float getPipeRenderScale(ItemStack stack);

    /** @return False to use the default renderer, true otherwise. */
    @SideOnly(Side.CLIENT)
    boolean renderItemInPipe(ItemStack stack, double x, double y, double z);
}
