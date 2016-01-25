/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.statements;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IStatement {
    /** Every statement needs a unique tag, it should be in the format of "&lt;modid&gt;:&lt;name&gt;.
     *
     * @return the unique id */
    String getUniqueTag();

    /** Return the maximum number of parameter this statement can have, 0 if none. */
    int maxParameters();

    /** Return the minimum number of parameter this statement can have, 0 if none. */
    int minParameters();

    /** Return the statement description in the UI */
    String getDescription();

    /** Create parameters for the statement. */
    IStatementParameter createParameter(int index);

    /** This returns the statement after a left rotation. Used in particular in blueprints orientation. */
    IStatement rotateLeft();

    /** This should return the icon for this statement, suitable for rending directly into a GUI. This should refer to a
     * texture on the map block texture map. */
    @SideOnly(Side.CLIENT)
    TextureAtlasSprite getGuiSprite();
}
