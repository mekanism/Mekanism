package mekanism.api;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import mekanism.api.util.BlockInfo;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;

public class MekanismAPI
{
	public static final String MODID = "mekanism";

	//Add a BlockInfo value here if you don't want a certain block to be picked up by cardboard boxes
	private static Set<BlockInfo> cardboardBoxIgnore = new HashSet<>();

	//ignore all mod blocks
	private static Set<String> cardboardBoxModIgnore = new HashSet<>();

	private static MekanismRecipeHelper helper = null;
	
	/** Mekanism debug mode */
	public static boolean debug = false;

	public static boolean isBlockCompatible(Block block, int meta)
	{
		if (cardboardBoxModIgnore.contains(Objects.requireNonNull(block.getRegistryName()).getResourceDomain())){
			return false;
		}

		for(BlockInfo i : cardboardBoxIgnore)
		{
			if(i.block == block && (i.meta == OreDictionary.WILDCARD_VALUE || i.meta == meta))
			{
				return false;
			}
		}

		return true;
	}

	public static void addBoxBlacklist(Block block, int meta)
	{
		if (block == null)
			return;//allow lazy adding via registry get
		cardboardBoxIgnore.add(new BlockInfo(block, meta));
	}

	public static void removeBoxBlacklist(Block block, int meta)
	{
		cardboardBoxIgnore.remove(new BlockInfo(block, meta));
	}

	public static Set<BlockInfo> getBoxIgnore()
	{
		return cardboardBoxIgnore;
	}

	public static void addBoxBlacklistMod(@Nonnull String modid){
		cardboardBoxModIgnore.add(modid);
	}

	public static void removeBoxBlacklistMod(@Nonnull String modid){
		cardboardBoxModIgnore.remove(modid);
	}

	public static Set<String> getBoxModIgnore(){
		return cardboardBoxModIgnore;
	}

	public static class BoxBlacklistEvent extends Event {
		public void blacklist(Block block, int meta){
			addBoxBlacklist(block, meta);
		}
		public void blacklist(Block block){
			addBoxBlacklist(block, OreDictionary.WILDCARD_VALUE);
		}
		public void blacklistMod(String modid){
			addBoxBlacklistMod(modid);
		}
	}

	/**
	 * Get the instance of the recipe helper to directly add recipes.
	 *
	 * Do NOT copy/repackage this method into your package, nor use the class directly as it may change.
	 *
	 * @return {@link MekanismRecipeHelper} The handler.
	 */
	public static MekanismRecipeHelper recipeHelper(){
		if (helper == null){
			try {
				helper = (MekanismRecipeHelper)Class.forName("mekanism.common.recipe.APIHandler").newInstance();
			} catch (ClassNotFoundException|InstantiationException|IllegalAccessException e) {
				LogManager.getLogger("MekanismAPI").error("Could not find API Handler", e);
			}
		}
		return helper;
	}
}
