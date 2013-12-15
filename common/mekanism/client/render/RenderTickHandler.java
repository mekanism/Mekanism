package mekanism.client.render;

import java.util.EnumSet;
import java.util.Random;

import mekanism.api.EnumColor;
import mekanism.api.Object3D;
import mekanism.common.Mekanism;
import mekanism.common.item.ItemJetpack;
import mekanism.common.item.ItemScubaTank;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EntityFlameFX;
import net.minecraft.client.particle.EntitySmokeFX;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTickHandler implements ITickHandler
{
	public Random rand = new Random();
	public Minecraft mc = Minecraft.getMinecraft();
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) 
	{
		float partialTick = (Float)tickData[0];
		
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
			
			if(player != null && !(mc.currentScreen instanceof GuiChat) && player.getCurrentItemOrArmor(3) != null)
			{
				ItemStack stack = player.getCurrentItemOrArmor(3);
				
				ScaledResolution scaledresolution = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
				int x = scaledresolution.getScaledWidth();
				int y = scaledresolution.getScaledHeight();
				
				if(stack.getItem() instanceof ItemJetpack)
				{
					ItemJetpack jetpack = (ItemJetpack)stack.getItem();
					
					font.drawStringWithShadow("Mode: " + jetpack.getMode(stack).getName(), 1, y - 20, 0x404040);
					font.drawStringWithShadow("Hydrogen: " + jetpack.getStored(stack), 1, y - 11, 0x404040);
				}
				else if(stack.getItem() instanceof ItemScubaTank)
				{
					ItemScubaTank scubaTank = (ItemScubaTank)stack.getItem();
					String state = (scubaTank.getFlowing(stack) ? EnumColor.DARK_GREEN + "On" : EnumColor.DARK_RED + "Off");
					
					font.drawStringWithShadow("Mode: " + state, 1, y - 20, 0x404040);
					font.drawStringWithShadow("Oxygen: " + scubaTank.getStored(stack), 1, y - 11, 0x404040);
				}
			}
			
			for(String s : Mekanism.jetpackOn)
			{
				EntityPlayer p = mc.theWorld.getPlayerEntityByName(s);
				
				if(p == null)
				{
					continue;
				}
				
				Vector3 playerPos = new Vector3(p);
				
				if(p != mc.thePlayer)
				{
					playerPos.translate(new Vector3(0, 1.7, 0));
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
				
				Vector3 vCenter = new Vector3();
				vCenter.z -= 0.30;
				vCenter.x = (rand.nextFloat()-0.5F)*0.4F;
				vCenter.rotate(p.renderYawOffset);
				vCenter.y -= 0.86;
				
				Vector3 rLeft = vLeft.clone().scale(random);
				Vector3 rRight = vRight.clone().scale(random);
				
				Vector3 mLeft = Vector3.translate(vLeft.clone().scale(0.2), new Vector3(p.motionX, p.motionY, p.motionZ));
				Vector3 mRight = Vector3.translate(vRight.clone().scale(0.2), new Vector3(p.motionX, p.motionY, p.motionZ));
				Vector3 mCenter = Vector3.translate(vCenter.clone().scale(0.2), new Vector3(p.motionX, p.motionY, p.motionZ));
				
				mLeft.translate(rLeft);
				mRight.translate(rRight);
				
				Vector3 v = new Vector3(playerPos).translate(vLeft);
				spawnAndSetParticle("flame", world, v.x, v.y, v.z, mLeft.x, mLeft.y, mLeft.z);
				spawnAndSetParticle("smoke", world, v.x, v.y, v.z, mLeft.x, mLeft.y, mLeft.z);
				
				v = new Vector3(playerPos).translate(vRight);
				spawnAndSetParticle("flame", world, v.x, v.y, v.z, mRight.x, mRight.y, mRight.z);
				spawnAndSetParticle("smoke", world, v.x, v.y, v.z, mRight.x, mRight.y, mRight.z);
				
				v = new Vector3(playerPos).translate(vCenter);
				spawnAndSetParticle("flame", world, v.x, v.y, v.z, mCenter.x, mCenter.y, mCenter.z);
				spawnAndSetParticle("smoke", world, v.x, v.y, v.z, mCenter.x, mCenter.y, mCenter.z);
			}
		}
	}
	
	public void spawnAndSetParticle(String s, World world, double x, double y, double z, double velX, double velY, double velZ)
	{
		EntityFX fx = null;
		
		if(s.equals("flame"))
		{
			fx = new EntityFlameFX(world, x, y, z, velX, velY, velZ);
		}
		else if(s.equals("smoke"))
		{
			fx = new EntitySmokeFX(world, x, y, z, velX, velY, velZ);
		}
        
		mc.effectRenderer.addEffect(fx);
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