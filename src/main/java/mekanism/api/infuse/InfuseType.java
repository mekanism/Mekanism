package mekanism.api.infuse;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

/**
 * The types of infuse currently available in Mekanism.
 * @author AidanBrady
 *
 */
public final class InfuseType
{
	/** The name of this infusion. */
	public String name;

	/** This infuse GUI's icon */
	public ResourceLocation iconResource;

	/** The texture representing this infuse type. */
	public TextureAtlasSprite sprite;

	/** The unlocalized name of this type. */
	public String unlocalizedName;

	public InfuseType(String s, ResourceLocation res)
	{
		name = s;
		iconResource = res;
	}
	
	public void setIcon(TextureAtlasSprite tex)
	{
		sprite = tex;
	}

	public InfuseType setUnlocalizedName(String name)
	{
		unlocalizedName = "infuse." + name;

		return this;
	}

	public String getLocalizedName()
	{
		return I18n.translateToLocal(unlocalizedName);
	}
}
