package mekanism.api.infuse;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

/**
 * The types of infuse currently available in Mekanism.
 * @author AidanBrady
 *
 */
public final class InfuseType
{
	/** The name of this infusion */
	public String name;

	/** This infuse GUI's icon */
	public ResourceLocation icon;

	public TextureAtlasSprite sprite;
	
	/** The location of this infuse GUI's icon */
	public String textureLocation;

	/** The unlocalized name of this type. */
	public String unlocalizedName;

	public InfuseType(String s, String tex)
	{
		name = s;
		textureLocation = tex;
	}
	
	public void setIcon(ResourceLocation i)
	{
		icon = i;
	}

	public InfuseType setUnlocalizedName(String name)
	{
		unlocalizedName = "infuse." + name;

		return this;
	}

	public String getLocalizedName()
	{
		return StatCollector.translateToLocal(unlocalizedName);
	}
}
