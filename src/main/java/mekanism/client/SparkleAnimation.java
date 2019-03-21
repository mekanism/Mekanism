package mekanism.client;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.common.MekanismSounds;
import mekanism.common.config.MekanismConfig;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SparkleAnimation
{
	public TileEntity pointer;

	public Random random = new Random();

	public Set<Coord4D> iteratedNodes = new HashSet<>();
	
	public INodeChecker nodeChecker;

	public SparkleAnimation(TileEntity tileEntity, INodeChecker checker)
	{
		pointer = tileEntity;
		nodeChecker = checker;
	}

	public void run()
	{
		try {
			if(MekanismConfig.current().general.dynamicTankEasterEgg.val())
			{
				pointer.getWorld().playSound(null, pointer.getPos().getX(), pointer.getPos().getY(), pointer.getPos().getZ(), MekanismSounds.CJ_EASTER_EGG, SoundCategory.BLOCKS, 1F, 1F);
			}

			loop(pointer);
		} catch(Exception e) {}
		
		try {
			new Thread(() ->
			{
                World world = pointer.getWorld();

				int count = MekanismConfig.current().client.multiblockSparkleIntensity.val();

                for(Coord4D coord : iteratedNodes)
                {
                    for(int i = 0; i < count; i++)
                    {
                        world.spawnParticle(EnumParticleTypes.REDSTONE, coord.x + random.nextDouble(), coord.y + -.01, coord.z + random.nextDouble(), 0, 0, 0);
                        world.spawnParticle(EnumParticleTypes.REDSTONE, coord.x + random.nextDouble(), coord.y + 1.01, coord.z + random.nextDouble(), 0, 0, 0);
                        world.spawnParticle(EnumParticleTypes.REDSTONE, coord.x + random.nextDouble(), coord.y + random.nextDouble(), coord.z + -.01, 0, 0, 0);
                        world.spawnParticle(EnumParticleTypes.REDSTONE, coord.x + random.nextDouble(), coord.y + random.nextDouble(), coord.z + 1.01, 0, 0, 0);
                        world.spawnParticle(EnumParticleTypes.REDSTONE, coord.x + -.01, coord.y + random.nextDouble(), coord.z + random.nextDouble(), 0, 0, 0);
                        world.spawnParticle(EnumParticleTypes.REDSTONE, coord.x + 1.01, coord.y + random.nextDouble(), coord.z + random.nextDouble(), 0, 0, 0);
                    }
                }
            }).start();
		} catch(Exception e) {}
	}

	public void loop(TileEntity tileEntity)
	{
		iteratedNodes.add(Coord4D.get(tileEntity));

		for(EnumFacing side : EnumFacing.VALUES)
		{
			Coord4D coord = Coord4D.get(tileEntity).offset(side);
			
			if(coord.exists(pointer.getWorld()))
			{
				TileEntity tile = coord.getTileEntity(pointer.getWorld());
	
				if(tile != null && isNode(tile) && !iteratedNodes.contains(coord))
				{
					loop(tile);
				}
			}
		}
	}
	
	public boolean isNode(TileEntity tile)
	{
		return nodeChecker.isNode(tile);
	}
	
	public interface INodeChecker
	{
		boolean isNode(TileEntity tile);
	}
}
