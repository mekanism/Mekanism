package mekanism.client.render.block;

import mekanism.client.ClientProxy;
import mekanism.client.model.ModelTransmitter;
import mekanism.client.model.ModelTransmitter.Size;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TransmitterRenderingHandler implements ISimpleBlockRenderingHandler
{
	public ModelTransmitter smallTransmitter = new ModelTransmitter(Size.SMALL);
	public ModelTransmitter largeTransmitter = new ModelTransmitter(Size.LARGE);
	
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) 
	{
		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
		GL11.glRotatef(180F, 0.0F, -1.0F, 0.0F);
    	GL11.glTranslated(0.0F, -1.0F, 0.0F);
    	GL11.glDisable(GL11.GL_CULL_FACE);
    	
    	switch(metadata)
    	{
    		case 0:
    			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "PressurizedTube.png"));
    			break;
    		case 1:
    			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "UniversalCable.png"));
    			break;
    		case 2:
    			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "MechanicalPipe.png"));
    			break;
    		case 3:
    			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "LogisticalTransporter.png"));
    			break;
    		case 4:
    			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "RestrictiveTransporter.png"));
    			break;
    		case 5:
    			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "DiversionTransporter.png"));
    			break;
    	}
    	
    	if(metadata != 3 && metadata != 4 && metadata != 5)
    	{
	    	smallTransmitter.renderSide(ForgeDirection.UP, true);
	    	smallTransmitter.renderSide(ForgeDirection.DOWN, true);
	    	smallTransmitter.renderCenter(new boolean[]{true, true, false, false, false, false});
    	}
    	else {
        	largeTransmitter.renderSide(ForgeDirection.UP, true);
	    	largeTransmitter.renderSide(ForgeDirection.DOWN, true);
	    	largeTransmitter.renderCenter(new boolean[]{true, true, false, false, false, false});
    	}
    	
    	GL11.glEnable(GL11.GL_CULL_FACE);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		return false;
	}

	@Override
	public boolean shouldRender3DInInventory() 
	{
		return true;
	}

	@Override
	public int getRenderId() 
	{
		return ClientProxy.TRANSMITTER_RENDER_ID;
	}
}
