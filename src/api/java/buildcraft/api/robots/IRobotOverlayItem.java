package buildcraft.api.robots;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IRobotOverlayItem {
    boolean isValidRobotOverlay(ItemStack stack);

    @SideOnly(Side.CLIENT)
    void renderRobotOverlay(ItemStack stack, TextureManager textureManager);
}
