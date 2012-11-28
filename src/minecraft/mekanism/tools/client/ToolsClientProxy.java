package mekanism.tools.client;

import java.io.IOException;

import net.minecraftforge.client.MinecraftForgeClient;

import cpw.mods.fml.client.registry.RenderingRegistry;

import mekanism.tools.common.ToolsCommonProxy;
import mekanism.tools.common.EntityKnife;

public class ToolsClientProxy extends ToolsCommonProxy
{
	@Override
	public void registerRenderInformation()
	{
		//Preload block/item textures
		MinecraftForgeClient.preloadTexture("/resources/mekanism/textures/tools/items.png");
		
		//Register entity rendering handlers
		RenderingRegistry.registerEntityRenderingHandler(EntityKnife.class, new RenderKnife());
		
		System.out.println("[MekanismTools] Render registrations complete.");
	}
}
