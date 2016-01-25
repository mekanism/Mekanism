package buildcraft.api.core;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IIconProvider {
    public TextureAtlasSprite getIcon(int iconIndex);

    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap iconRegister);
}
