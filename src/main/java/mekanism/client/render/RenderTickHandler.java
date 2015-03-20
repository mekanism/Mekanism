package mekanism.client.render;

import java.util.List;
import java.util.Random;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Pos3D;
import mekanism.client.render.particle.EntityJetpackFlameFX;
import mekanism.client.render.particle.EntityJetpackSmokeFX;
import mekanism.common.Mekanism;
import mekanism.common.item.ItemFlamethrower;
import mekanism.common.item.ItemJetpack;
import mekanism.common.item.ItemScubaTank;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTickHandler
{
	public Random rand = new Random();
	public Minecraft mc = Minecraft.getMinecraft();

	@SubscribeEvent
	public void tickEnd(RenderTickEvent event)
	{
		if(event.phase == Phase.END)
		{
			if(mc.thePlayer != null && mc.theWorld != null && !mc.isGamePaused())
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
	
					Coord4D obj = new Coord4D(x, y, z, world.provider.dimensionId);
	
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
	
						font.drawStringWithShadow("Block: " + obj.getBlock(world).getUnlocalizedName(), 1, 1, 0x404040);
						font.drawStringWithShadow("Metadata: " + obj.getMetadata(world), 1, 10, 0x404040);
						font.drawStringWithShadow("Location: " + MekanismUtils.getCoordDisplay(obj), 1, 19, 0x404040);
						font.drawStringWithShadow("TileEntity: " + tileDisplay, 1, 28, 0x404040);
						font.drawStringWithShadow("Side: " + pos.sideHit, 1, 37, 0x404040);
					}
				}
	
				if(player != null && mc.currentScreen == null && player.getEquipmentInSlot(3) != null)
				{
					ItemStack stack = player.getEquipmentInSlot(3);
	
					ScaledResolution scaledresolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
	
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

				synchronized(Mekanism.jetpackOn)
				{
					for(String s : Mekanism.jetpackOn)
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

						float random = (rand.nextFloat() - 0.5F) * 0.1F;

						Pos3D vLeft = new Pos3D();
						vLeft.xPos -= 0.43;
						vLeft.yPos -= 0.55;
						vLeft.zPos -= 0.54;
						vLeft.rotateYaw(p.renderYawOffset);

						Pos3D vRight = new Pos3D();
						vRight.xPos += 0.43;
						vRight.yPos -= 0.55;
						vRight.zPos -= 0.54;
						vRight.rotateYaw(p.renderYawOffset);

						Pos3D vCenter = new Pos3D();
						vCenter.xPos = (rand.nextFloat() - 0.5F) * 0.4F;
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
				
				if(world.getWorldTime() % 4 == 0)
				{
					for(EntityPlayer p : (List<EntityPlayer>)world.playerEntities)
					{
						if(!Mekanism.flamethrowerActive.contains(p.getCommandSenderName()) && !p.isSwingInProgress && p.getCurrentEquippedItem() != null && p.getCurrentEquippedItem().getItem() instanceof ItemFlamethrower)
						{
							if(((ItemFlamethrower)p.getCurrentEquippedItem().getItem()).getGas(p.getCurrentEquippedItem()) != null)
							{
								Pos3D playerPos = new Pos3D(p);
								Pos3D flameVec = new Pos3D();
								
								if(p.isSneaking())
								{
									flameVec.yPos -= 0.35F;
									flameVec.zPos -= 0.15F;
								}
								
								Pos3D flameMotion = new Pos3D(p.motionX, p.onGround ? 0 : p.motionY, p.motionZ);
								
								if(player == p && mc.gameSettings.thirdPersonView == 0)
								{
									flameVec = new Pos3D(0.8, 0.8, 0.8);
									
									flameVec.multiply(new Pos3D(p.getLook(90)));
									flameVec.rotateYaw(15);
								}
								else {
									flameVec.xPos -= 0.45F;
									
									if(player == p)
									{
										flameVec.yPos -= 0.5F;
									}
									else {
										flameVec.yPos += 1F;
									}
									
									flameVec.zPos += 1.05F;
									
									flameVec.rotateYaw(p.renderYawOffset);
								}
								
								Pos3D mergedVec = playerPos.clone().translate(flameVec);
								
								spawnAndSetParticle("flame", world, mergedVec.xPos, mergedVec.yPos, mergedVec.zPos, flameMotion.xPos, flameMotion.yPos, flameMotion.zPos);
							}
						}
					}
				}
			}
		}
	}

	public void spawnAndSetParticle(String s, World world, double x, double y, double z, double velX, double velY, double velZ)
	{
		EntityFX fx = null;

		if(s.equals("flame"))
		{
			fx = new EntityJetpackFlameFX(world, x, y, z, velX, velY, velZ);
		}
		else if(s.equals("smoke"))
		{
			fx = new EntityJetpackSmokeFX(world, x, y, z, velX, velY, velZ);
		}

		mc.effectRenderer.addEffect(fx);
	}
}
