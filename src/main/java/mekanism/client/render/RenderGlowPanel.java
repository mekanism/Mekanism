package mekanism.client.render;

import java.util.Map;

import mekanism.api.EnumColor;
import mekanism.common.multipart.PartGlowPanel;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

import codechicken.lib.colour.Colour;
import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.lighting.LightModel;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.ColourMultiplier;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.render.TextureUtils.IIconSelfRegister;
import codechicken.lib.render.uv.IconTransformation;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;

public class RenderGlowPanel implements IIconSelfRegister
{
	public static RenderGlowPanel INSTANCE;

	public static CCModel[] frameModels;
	public static CCModel[] lightModels;

	public static IIcon icon;

	public static RenderGlowPanel getInstance()
	{
		return INSTANCE;
	}

	public static void init()
	{
		INSTANCE = new RenderGlowPanel();
		TextureUtils.addIconRegistrar(INSTANCE);

		Map<String, CCModel> models = CCModel.parseObjModels(MekanismUtils.getResource(ResourceType.MODEL, "glow_panel.obj"), 7, null);

		frameModels = new CCModel[6];
		frameModels[0] = models.get("frame").backfacedCopy().apply(Vector3.center.translation()).shrinkUVs(0.0005);
		CCModel.generateSidedModels(frameModels, 0, Vector3.center);

		lightModels = new CCModel[6];
		lightModels[0] = models.get("light").backfacedCopy().apply(Vector3.center.translation()).shrinkUVs(0.0005);
		CCModel.generateSidedModels(lightModels, 0, Vector3.center);

		for(CCModel c : frameModels)
		{
			c.computeLighting(LightModel.standardLightModel);
		}
	}

	public void renderStatic(PartGlowPanel panel)
	{
		CCRenderState.reset();
		CCRenderState.setBrightness(panel.world(), panel.x(), panel.y(), panel.z());

		Colour colour = new ColourRGBA(panel.colour.getColor(0), panel.colour.getColor(1), panel.colour.getColor(2), 1);
		int side = panel.side.ordinal();
		
		frameModels[side].render(new Translation(panel.x(), panel.y(), panel.z()), new IconTransformation(icon));
		lightModels[side].render(new Translation(panel.x(), panel.y(), panel.z()), new IconTransformation(icon), new ColourMultiplier(colour.rgba()));
	}

	public void renderItem(int metadata)
	{
		TextureUtils.bindAtlas(0);
		CCRenderState.reset();
		CCRenderState.startDrawing();
		CCRenderState.hasColour = true;
		EnumColor c = EnumColor.DYES[metadata];

		Colour colour = new ColourRGBA(c.getColor(0), c.getColor(1), c.getColor(2), 1);
		Colour white = new ColourRGBA(1.0, 1.0, 1.0, 1.0);
		
		for(int i = 4; i < 5; i++)
		{
			frameModels[i].render(new IconTransformation(icon), new ColourMultiplier(white.rgba()));
			lightModels[i].render(new IconTransformation(icon), new ColourMultiplier(colour.rgba()));
		}
		
		CCRenderState.draw();
	}

	@Override
	public void registerIcons(IIconRegister register)
	{
		icon = register.registerIcon("mekanism:models/GlowPanel");
	}

	@Override
	public int atlasIndex()
	{
		return 0;
	}
}
