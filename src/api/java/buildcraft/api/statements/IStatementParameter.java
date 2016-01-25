/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.statements;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IStatementParameter {

    /** Every parameter needs a unique tag, it should be in the format of "&lt;modid&gt;:&lt;name&gt;".
     *
     * @return the unique id */
    String getUniqueTag();

    @SideOnly(Side.CLIENT)
    TextureAtlasSprite getIcon();

    ItemStack getItemStack();

    /** Return the parameter description in the UI */
    String getDescription();

    void onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse);

    void readFromNBT(NBTTagCompound compound);

    void writeToNBT(NBTTagCompound compound);

    /** This returns the parameter after a left rotation. Used in particular in blueprints orientation. */
    IStatementParameter rotateLeft();
}
