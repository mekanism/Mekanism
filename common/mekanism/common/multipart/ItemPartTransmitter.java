package mekanism.common.multipart;

import java.util.List;

import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.common.Mekanism;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.TMultiPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class ItemPartTransmitter extends JItemMultiPart
{

	public ItemPartTransmitter(int id)
	{
		super(id);
		setHasSubtypes(true);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public TMultiPart newPart(ItemStack arg0, EntityPlayer arg1, World arg2, BlockCoord arg3, int arg4, Vector3 arg5)
	{
		return PartTransmitter.getPartType(TransmissionType.values()[this.getDamage(arg0)]);
	}
	
	@Override
	public int getMetadata(int damage)
	{
		return damage;
	}

    @Override
    public void getSubItems(int itemID, CreativeTabs tab, List listToAddTo) {
        for (TransmissionType type : TransmissionType.values()) {
            listToAddTo.add(new ItemStack(itemID, 1, type.ordinal()));
        }
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public int getSpriteNumber()
    {
    	return 0;
    }
    
    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
    	return getUnlocalizedName()+"."+TransmissionType.values()[stack.getItemDamage()].name().toLowerCase();
    }
}
