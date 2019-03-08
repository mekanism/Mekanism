package mekanism.common.util;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;

/**
 * Created by Thiakil on 8/11/2017.
 */
public class TextComponentGroup extends TextComponentString
{

	public TextComponentGroup(){
		super("");
	}

	public TextComponentGroup(TextFormatting color)
	{
		super("");
		getStyle().setColor(color);
	}

	@Nonnull
	@Override
	public String getUnformattedComponentText()
	{
		return "";
	}

	@Nonnull
	@Override
	public TextComponentGroup createCopy()
	{
		TextComponentGroup textcomponentstring = new TextComponentGroup();
		textcomponentstring.setStyle(this.getStyle().createShallowCopy());

		for (ITextComponent itextcomponent : this.getSiblings())
		{
			textcomponentstring.appendSibling(itextcomponent.createCopy());
		}

		return textcomponentstring;
	}

	public TextComponentGroup string(String s){
		this.appendSibling(new TextComponentString(s));
		return this;
	}

	public TextComponentGroup string(String s, TextFormatting color){
		ITextComponent t = new TextComponentString(s);
		t.getStyle().setColor(color);
		this.appendSibling(t);
		return this;
	}

	public TextComponentGroup translation(String key){
		this.appendSibling(new TextComponentTranslation(key));
		return this;
	}

	public TextComponentGroup translation(String key, TextFormatting color){
		ITextComponent t = new TextComponentTranslation(key);
		t.getStyle().setColor(color);
		this.appendSibling(t);
		return this;
	}
}
