package mekanism.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;

public class KeySync
{
	public static int ASCEND = 0;
	public static int DESCEND = 1;

	public Map<EntityPlayer, KeySet> keys = new HashMap<EntityPlayer, KeySet>();

	public static class KeySet
	{
		public Set<Integer> keysActive = new HashSet<Integer>();

		public KeySet(int key)
		{
			keysActive.add(key);
		}
	}

	public KeySet getPlayerKeys(EntityPlayer player)
	{
		return keys.get(player);
	}

	public void add(EntityPlayer player, int key)
	{
		if(!keys.containsKey(player))
		{
			keys.put(player, new KeySet(key));
			return;
		}

		keys.get(player).keysActive.add(key);
	}

	public void remove(EntityPlayer player, int key)
	{
		if(!keys.containsKey(player))
		{
			return;
		}

		keys.get(player).keysActive.remove(key);
	}

	public boolean has(EntityPlayer player, int key)
	{
		if(!keys.containsKey(player))
		{
			return false;
		}

		return keys.get(player).keysActive.contains(key);
	}

	public void update(EntityPlayer player, int key, boolean add)
	{
		if(add)
		{
			add(player, key);
		}
		else {
			remove(player, key);
		}
	}
}
