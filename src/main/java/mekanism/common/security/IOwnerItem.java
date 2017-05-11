package mekanism.common.security;

import com.mojang.authlib.GameProfile;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.UUID;

public interface IOwnerItem
{
	default UUID getOwner(ItemStack stack)
	{
		if (ItemDataUtils.hasID(stack, "ownerUUID"))
		{
			return ItemDataUtils.getUUID(stack, "ownerUUID");
		}
		//TODO Remove in next version, currently needed for transition to UUIDs
		else if (ItemDataUtils.hasData(stack, "owner"))
		{
			String owner = ItemDataUtils.getString(stack, "owner");
			GameProfile gameProfile = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache().getGameProfileForUsername(owner);
			if (gameProfile != null)
			{
				return gameProfile.getId();
			}
		}

		return null;
	}

	default void setOwner(ItemStack stack, UUID owner)
	{
		if(owner == null) {
			ItemDataUtils.removeData(stack, "ownerUUID");
			return;
		}

		ItemDataUtils.setUUID(stack, "ownerUUID", owner);
	}

	boolean hasOwner(ItemStack stack);
}
