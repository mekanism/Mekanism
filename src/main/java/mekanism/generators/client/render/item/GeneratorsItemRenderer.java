package mekanism.generators.client.render.item;

import mekanism.generators.client.GeneratorsClientProxy;
import mekanism.generators.common.item.ItemBlockGenerator;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.SideOnly;
import codechicken.lib.render.IItemRenderer;

@SideOnly(Side.CLIENT)
public class GeneratorsItemRenderer implements IItemRenderer
{
	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return true;
	}
	
	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		RenderBlocks renderBlocks = (RenderBlocks)data[0];

		if(type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON)
		{
			GlStateManager.translate(0.5F, 0.5F, 0.5F);
		}
		
		if(item.getItem() instanceof ItemBlockGenerator)
		{
			RenderingRegistry.instance().renderInventoryBlock((RenderBlocks)data[0], Block.getBlockFromItem(item.getItem()), item.getItemDamage(), GeneratorsClientProxy.GENERATOR_RENDER_ID);
		}
	}
}
