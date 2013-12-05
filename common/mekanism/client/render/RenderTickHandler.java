package mekanism.client.render;

import java.util.EnumSet;
import java.util.Random;

import mekanism.api.Object3D;
import mekanism.common.Mekanism;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTickHandler implements ITickHandler
{
	public Random rand = new Random();
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) 
	{
		float partialTick = (Float)tickData[0];
		Minecraft mc = FMLClientHandler.instance().getClient();
		
		if(mc.thePlayer != null && mc.theWorld != null)
		{
			EntityPlayer player = mc.thePlayer;
			World world = mc.thePlayer.worldObj;
			
			FontRenderer font = mc.fontRenderer;
			
			MovingObjectPosition pos = player.rayTrace(40.0D, 1.0F);
			
			if(pos != null)
			{
				int x = MathHelper.floor_double(pos.blockX);
				int y = MathHelper.floor_double(pos.blockY);
				int z = MathHelper.floor_double(pos.blockZ);
				
				Object3D obj = new Object3D(x, y, z);
				
				if(Mekanism.debug && mc.currentScreen == null && !mc.gameSettings.showDebugInfo)
				{
					String tileDisplay = "";
					
					if(obj.getTileEntity(world) != null)
					{
						if(obj.getTileEntity(world).getClass() != null)
						{
							tileDisplay = obj.getTileEntity(world).getClass().getSimpleName();
						}
					}
				
					font.drawStringWithShadow("Block ID: " + obj.getBlockId(world), 1, 1, 0x404040);
					font.drawStringWithShadow("Metadata: " + obj.getMetadata(world), 1, 10, 0x404040);
					font.drawStringWithShadow("TileEntity: " + tileDisplay, 1, 19, 0x404040);
					font.drawStringWithShadow("Side: " + pos.sideHit, 1, 28, 0x404040);
				}
			}
			
			for(EntityPlayer p : Mekanism.jetpackOn)
			{
				if(p.getDistance(player.posX, player.posY, player.posZ) > 40)
				{
					continue;
				}
				
				float random = (rand.nextFloat()-0.5F)*0.1F;
				
				Vector3 vLeft = new Vector3();
				vLeft.z -= 0.54;
				vLeft.x -= 0.43;
				vLeft.rotate(p.renderYawOffset);
				vLeft.y -= 0.55;
				
				Vector3 vRight = new Vector3();
				vRight.z -= 0.54;
				vRight.x += 0.43;
				vRight.rotate(p.renderYawOffset);
				vRight.y -= 0.55;
				
				Vector3 rLeft = vLeft.clone().scale(random);
				Vector3 rRight = vRight.clone().scale(random);
				
				Vector3 mLeft = Vector3.translate(vLeft.clone().scale(0.2), new Vector3(p.motionX, p.motionY, p.motionZ));
				Vector3 mRight = Vector3.translate(vRight.clone().scale(0.2), new Vector3(p.motionX, p.motionY, p.motionZ));
				
				mLeft.translate(rLeft);
				mRight.translate(rRight);
				
				Vector3 v = new Vector3(p).translate(vLeft);
				world.spawnParticle("flame", v.x, v.y, v.z, mLeft.x, mLeft.y, mLeft.z);
				world.spawnParticle("smoke", v.x, v.y, v.z, mLeft.x, mLeft.y, mLeft.z);
				
				v = new Vector3(p).translate(vRight);
				world.spawnParticle("flame", v.x, v.y, v.z, mRight.x, mRight.y, mRight.z);
				world.spawnParticle("smoke", v.x, v.y, v.z, mRight.x, mRight.y, mRight.z);
			}
		}
	}

	@Override
	public EnumSet<TickType> ticks() 
	{
		return EnumSet.of(TickType.RENDER);
	}

	@Override
	public String getLabel() 
	{
		return "MekanismRender";
	}
}