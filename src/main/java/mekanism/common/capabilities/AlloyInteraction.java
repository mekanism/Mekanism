package mekanism.common.capabilities;

import mekanism.api.IAlloyInteraction;
import mekanism.common.capabilities.StorageHelper.NullStorage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class AlloyInteraction implements IAlloyInteraction
{
	@Override
	public void onAlloyInteraction(EntityPlayer player, int tierOrdinal) {}

	public static void register()
	{
        CapabilityManager.INSTANCE.register(IAlloyInteraction.class, new NullStorage<>(), AlloyInteraction.class);
	}
}
