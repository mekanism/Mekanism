package mekanism.induction.client;

import mekanism.induction.client.gui.GuiBattery;
import mekanism.induction.client.gui.GuiMultimeter;
import mekanism.induction.client.render.BlockRenderingHandler;
import mekanism.induction.client.render.RenderBattery;
import mekanism.induction.client.render.RenderEMContractor;
import mekanism.induction.client.render.RenderMultimeter;
import mekanism.induction.client.render.RenderTesla;
import mekanism.induction.common.InductionCommonProxy;
import mekanism.induction.common.tileentity.TileEntityBattery;
import mekanism.induction.common.tileentity.TileEntityEMContractor;
import mekanism.induction.common.tileentity.TileEntityMultimeter;
import mekanism.induction.common.tileentity.TileEntityTesla;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class InductionClientProxy extends InductionCommonProxy
{
	public static int INDUCTION_RENDER_ID = RenderingRegistry.getNextAvailableRenderId();
	
	@Override
	public void registerRenderers()
	{
		RenderingRegistry.registerBlockHandler(BlockRenderingHandler.INSTANCE);

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTesla.class, new RenderTesla());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMultimeter.class, new RenderMultimeter());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEMContractor.class, new RenderEMContractor());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBattery.class, new RenderBattery());
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if(tileEntity instanceof TileEntityMultimeter)
		{
			return new GuiMultimeter(player.inventory, ((TileEntityMultimeter) tileEntity));
		}
		else if(tileEntity instanceof TileEntityBattery)
		{
			return new GuiBattery(player.inventory, ((TileEntityBattery) tileEntity));
		}

		return null;
	}

	@Override
	public boolean isFancy()
	{
		return FMLClientHandler.instance().getClient().gameSettings.fancyGraphics;
	}

	@Override
	public void renderElectricShock(World world, Vector3 start, Vector3 target, float r, float g, float b, boolean split)
	{
		if(world.isRemote)
		{
			FMLClientHandler.instance().getClient().effectRenderer.addEffect(new FXElectricBolt(world, start, target, split).setColor(r, g, b));
		}
	}
}
