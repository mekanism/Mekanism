package mekanism.client.render;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Pos3D;
import mekanism.common.Mekanism;
import mekanism.common.item.ItemJetpack;
import mekanism.common.item.ItemScubaTank;
import net.minecraft.client.Minecraft;
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
				
				Coord4D obj = new Coord4D(x, y, z);
				
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
			
			Set<String> copy = (Set)((HashSet)Mekanism.jetpackOn).clone();
			
			for(String s : copy)
			{
				EntityPlayer p = mc.theWorld.getPlayerEntityByName(s);
				
				if(p == null)
				{
					continue;
				}
				
				Pos3D playerPos = new Pos3D(p);
				
				if(p != mc.thePlayer)
				{
					playerPos.translate(0, 1.7, 0);
				}
				
				float random = (rand.nextFloat()-0.5F)*0.1F;
				
				Pos3D vLeft = new Pos3D();
				vLeft.zPos -= 0.54;
				vLeft.xPos -= 0.43;
				vLeft.rotateYaw(p.renderYawOffset);
				vLeft.yPos -= 0.55;
				
				Pos3D vRight = new Pos3D();
				vRight.xPos += 0.43;
				vRight.yPos -= 0.55;
				vRight.zPos -= 0.54;
				vRight.rotateYaw(p.renderYawOffset);
				
				Pos3D vCenter = new Pos3D();
				vCenter.xPos = (rand.nextFloat()-0.5F)*0.4F;
				vCenter.yPos -= 0.86;
				vCenter.zPos -= 0.30;
				vCenter.rotateYaw(p.renderYawOffset);
				
				Pos3D rLeft = vLeft.clone().scale(random);
				Pos3D rRight = vRight.clone().scale(random);
				
				Pos3D mLeft = vLeft.clone().scale(0.2).translate(new Pos3D(p.motionX, p.motionY, p.motionZ));
				Pos3D mRight = vRight.clone().scale(0.2).translate(new Pos3D(p.motionX, p.motionY, p.motionZ));
				Pos3D mCenter = vCenter.clone().scale(0.2).translate(new Pos3D(p.motionX, p.motionY, p.motionZ));
				
				mLeft.translate(rLeft);
				mRight.translate(rRight);
				
				Pos3D v = playerPos.clone().translate(vLeft);
				spawnAndSetParticle("flame", world, v.xPos, v.yPos, v.zPos, mLeft.xPos, mLeft.yPos, mLeft.zPos);
				spawnAndSetParticle("smoke", world, v.xPos, v.yPos, v.zPos, mLeft.xPos, mLeft.yPos, mLeft.zPos);
				
				v = playerPos.clone().translate(vRight);
				spawnAndSetParticle("flame", world, v.xPos, v.yPos, v.zPos, mRight.xPos, mRight.yPos, mRight.zPos);
				spawnAndSetParticle("smoke", world, v.xPos, v.yPos, v.zPos, mRight.xPos, mRight.yPos, mRight.zPos);
				
				v = playerPos.clone().translate(vCenter);
				spawnAndSetParticle("flame", world, v.xPos, v.yPos, v.zPos, mCenter.xPos, mCenter.yPos, mCenter.zPos);
				spawnAndSetParticle("smoke", world, v.xPos, v.yPos, v.zPos, mCenter.xPos, mCenter.yPos, mCenter.zPos);
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