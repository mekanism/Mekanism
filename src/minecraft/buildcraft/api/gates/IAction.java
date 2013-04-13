package buildcraft.api.gates;

import buildcraft.api.core.IIconProvider;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IAction {

	int getId();

    int getIconIndex();
    
    @SideOnly(Side.CLIENT)
    IIconProvider getIconProvider();
    
	boolean hasParameter();

	String getDescription();

}
