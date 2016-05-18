package mekanism.client.render.obj;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;

public class MekanismOBJLoader extends OBJLoader
{
	private final Map<ResourceLocation, MekanismOBJModel> modelCache = new HashMap<ResourceLocation, MekanismOBJModel>();
	
	@Override
	public boolean accepts(ResourceLocation modelLocation)
	{
		return modelLocation.getResourcePath().endsWith(".obj.mek");
	}
	
	@Override
	public IModel loadModel(ResourceLocation loc) throws IOException
	{
		if(!modelCache.containsKey(loc))
		{
			IModel model = super.loadModel(loc);
			
			if(model instanceof OBJModel)
			{
				MekanismOBJModel mekModel = new MekanismOBJModel(((OBJModel)model).getMatLib(), loc);
				modelCache.put(loc, mekModel);
			}
		}
		
		MekanismOBJModel mekModel = modelCache.get(loc);
		
		if(mekModel == null)
		{
			return ModelLoaderRegistry.getMissingModel();
		}
		
		return mekModel;
	}
}
