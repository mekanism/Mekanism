package mekanism.client.render.ctm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.Attributes;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.apache.commons.lang3.tuple.Pair;

public class CTMRegistry 
{
    private static List<TextureSpriteCallback> textures = new ArrayList<TextureSpriteCallback>();

	private IBakedModel baseModel;
	public static ResourceLocation baseResource = new ResourceLocation("mekanism:block/ctm_block");
	
	public static List<Pair<String, String>> ctmTypes = new ArrayList<Pair<String, String>>();
	
	public static Map<String, TextureCTM> textureCache = new HashMap<String, TextureCTM>();
	
    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) 
    {
        for(TextureSpriteCallback callback : textures) 
        {
            callback.stitch(event.getMap());
        }
    }
    
    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) throws Exception 
    {
        IModel model = ModelLoaderRegistry.getModel(baseResource);
        baseModel = model.bake(new TRSRTransformation(ModelRotation.X0_Y0), Attributes.DEFAULT_BAKED_FORMAT, r -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(r.toString()));
        
        for(Pair<String, String> pair : ctmTypes) 
        {
        	ModelCTM chiselModel = new ModelCTM(baseModel, pair.getRight());
        	chiselModel.load();
            event.getModelRegistry().putObject(new ModelResourceLocation(pair.getLeft() + ":" + pair.getRight()), new CTMModelFactory(chiselModel));
        }
    }
    
    public static void registerCTMs(String domain, String... ctms)
    {
    	for(String s : ctms)
    	{
    		ctmTypes.add(Pair.of(domain, s));
    		textureCache.put(s, createTexture(domain, s));
    	}
    }

    public static void register(TextureSpriteCallback callback) 
    {
        textures.add(callback);
    }

    public static TextureCTM createTexture(String domain, String name)
    {
    	TextureSpriteCallback[] callbacks = new TextureSpriteCallback[CTM.REQUIRED_TEXTURES];
    	
    	callbacks[0] = new TextureSpriteCallback(new ResourceLocation(domain + ":blocks/ctm/" + name));
    	callbacks[1] = new TextureSpriteCallback(new ResourceLocation(domain + ":blocks/ctm/" + name + "-ctm"));
    
    	register(callbacks[0]);
    	register(callbacks[1]);
    	
    	return new TextureCTM(callbacks);
    }
}
