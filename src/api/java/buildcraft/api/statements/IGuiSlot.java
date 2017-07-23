package buildcraft.api.statements;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IGuiSlot {
    /** Every parameter needs a unique tag, it should be in the format of "&lt;modid&gt;:&lt;name&gt;".
     *
     * @return the unique id */
    String getUniqueTag();

    /** Return the parameter description in the UI */
    String getDescription();

    /** @return A sprite to show in a GUI, or null if this should not render a sprite. */
    @SideOnly(Side.CLIENT)
    TextureAtlasSprite getGuiSprite();
}
