/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.statements;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IStatementParameter {
	
	/**
	 * Every parameter needs a unique tag, it should be in the format of
	 * "<modid>:<name>".
	 *
	 * @return the unique id
	 */
	String getUniqueTag();
	
	@SideOnly(Side.CLIENT)
	IIcon getIcon();

	ItemStack getItemStack();

	/**
	 * Something that is initially unintuitive: you HAVE to
	 * keep your icons as static variables, due to the fact
	 * that every IStatementParameter is instantiated upon creation,
	 * in opposition to IStatements which are singletons (due to the
	 * fact that they, unlike Parameters, store no additional data)
	 */
	@SideOnly(Side.CLIENT)
	void registerIcons(IIconRegister iconRegister);
	
	/**
	 * Return the parameter description in the UI
	 */
	String getDescription();

	void onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse);

	void readFromNBT(NBTTagCompound compound);

	void writeToNBT(NBTTagCompound compound);

	/**
	 * This returns the parameter after a left rotation. Used in particular in
	 * blueprints orientation.
	 */
	IStatementParameter rotateLeft();
}
