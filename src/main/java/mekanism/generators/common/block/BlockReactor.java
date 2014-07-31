package mekanism.generators.common.block;

import java.util.List;
import java.util.Random;

import mekanism.common.ItemAttacher;
import mekanism.common.Mekanism;
import mekanism.common.tile.TileEntityElectricBlock;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.GeneratorsBlocks;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import mekanism.generators.common.tile.reactor.TileEntityReactorFrame;
import mekanism.generators.common.tile.reactor.TileEntityReactorGlass;
import mekanism.generators.common.tile.reactor.TileEntityReactorLaserFocusMatrix;
import mekanism.generators.common.tile.reactor.TileEntityReactorNeutronCapture;
import mekanism.generators.common.tile.reactor.TileEntityReactorPort;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import buildcraft.api.tools.IToolWrench;
import cpw.mods.fml.common.ModAPIManager;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockReactor extends BlockContainer
{
	public IIcon[][] icons = new IIcon[16][16];

	public BlockReactor()
	{
		super(Material.iron);
		setHardness(3.5F);
		setResistance(8F);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register)
	{
		if(this == GeneratorsBlocks.Reactor)
		{
			icons[0][0] = register.registerIcon("mekanism:ReactorControllerOff");
			icons[0][1] = register.registerIcon("mekanism:ReactorControllerOn");
			icons[0][2] = register.registerIcon("mekanism:ReactorFrame");
			icons[1][0] = register.registerIcon("mekanism:ReactorFrame");
			icons[2][0] = register.registerIcon("mekanism:ReactorNeutronCapture");
			icons[3][0] = register.registerIcon("mekanism:ReactorPort");
		}
		else if(this == GeneratorsBlocks.ReactorGlass)
		{
			icons[0][0] = register.registerIcon("mekanism:ReactorGlass");
			icons[1][0] = register.registerIcon("mekanism:ReactorLaserFocus");
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)
	{
		if(this == GeneratorsBlocks.Reactor)
		{
			if(meta == 0)
			{
				return icons[0][side == 6 ? 0 : 2];
			}
			else {
				return icons[meta][0];
			}
		}
		else if(this == GeneratorsBlocks.ReactorGlass)
		{
			return icons[meta][0];
		}

		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side)
	{
		int metadata = world.getBlockMetadata(x, y, z);

		if(this == GeneratorsBlocks.Reactor)
		{
			if(metadata == 0)
			{
				if(side == 1)
				{
					return MekanismUtils.isActive(world, x, y, z) ? icons[0][1] : icons[0][0];
				}
				else {
					return icons[0][2];
				}
			}
			else {
				return icons[metadata][0];
			}
		}
		else if(this == GeneratorsBlocks.ReactorGlass)
		{
			return icons[metadata][0];
		}

		return null;
	}

	@Override
	public int damageDropped(int i)
	{
		return i;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int facing, float playerX, float playerY, float playerZ)
	{
		if(ItemAttacher.canAttach(entityplayer.getCurrentEquippedItem()))
		{
			return false;
		}

		if(world.isRemote)
		{
			return true;
		}

		TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getTileEntity(x, y, z);
		int metadata = world.getBlockMetadata(x, y, z);

		if(entityplayer.getCurrentEquippedItem() != null)
		{
			if(ModAPIManager.INSTANCE.hasAPI("BuildCraftAPI|tools") && entityplayer.getCurrentEquippedItem().getItem() instanceof IToolWrench && !entityplayer.getCurrentEquippedItem().getUnlocalizedName().contains("omniwrench"))
			{
				if(entityplayer.isSneaking())
				{
					dismantleBlock(world, x, y, z, false);
					return true;
				}

				((IToolWrench)entityplayer.getCurrentEquippedItem().getItem()).wrenchUsed(entityplayer, x, y, z);

				int change = 0;

				switch(tileEntity.facing)
				{
					case 3:
						change = 5;
						break;
					case 5:
						change = 2;
						break;
					case 2:
						change = 4;
						break;
					case 4:
						change = 3;
						break;
				}

				tileEntity.setFacing((short)change);
				world.notifyBlocksOfNeighborChange(x, y, z, this);
				return true;
			}
		}

		if(tileEntity instanceof TileEntityReactorController)
		{
			if(!entityplayer.isSneaking())
			{
				entityplayer.openGui(MekanismGenerators.instance, ReactorBlockType.get(this, metadata).guiId, world, x, y, z);
			}
			else {
				((TileEntityReactorController)tileEntity).formMultiblock();
			}
			
			return true;
		}

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativetabs, List list)
	{
		for(ReactorBlockType type : ReactorBlockType.values())
		{
			if(type.typeBlock == this)
			{
				list.add(new ItemStack(item, 1, type.meta));
			}
		}
	}

	@Override
	public int quantityDropped(Random random)
	{
		return 0;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		ReactorBlockType type = ReactorBlockType.get(this, metadata);

		if(type != null)
		{
			return type.create();
		}

		return null;
	}

	@Override
	public Item getItemDropped(int i, Random random, int j)
	{
		return null;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}
	
	@Override
	public int getRenderBlockPass()
	{
		return this == GeneratorsBlocks.Reactor ? 0 : 1;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	/*This method is not used, metadata manipulation is required to create a Tile Entity.*/
	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return null;
	}

	public static enum ReactorBlockType
	{
		CONTROLLER(GeneratorsBlocks.Reactor, 0, "ReactorController", 10, TileEntityReactorController.class),
		FRAME(GeneratorsBlocks.Reactor, 1, "ReactorFrame", -1, TileEntityReactorFrame.class),
		NEUTRON_CAPTURE(GeneratorsBlocks.Reactor, 2, "ReactorNeutronCapturePlate", 13, TileEntityReactorNeutronCapture.class),
		PORT(GeneratorsBlocks.Reactor, 3, "ReactorInOutPort", -1, TileEntityReactorPort.class),
		GLASS(GeneratorsBlocks.ReactorGlass, 0, "ReactorGlass", -1, TileEntityReactorGlass.class),
		LASER_FOCUS_MATRIX(GeneratorsBlocks.ReactorGlass, 1, "ReactorLaserFocusMatrix", -1, TileEntityReactorLaserFocusMatrix.class);

		public Block typeBlock;
		public int meta;
		public String name;
		public int guiId;
		public Class<? extends TileEntity> tileEntityClass;

		private ReactorBlockType(Block b, int i, String s, int j, Class<? extends TileEntityElectricBlock> tileClass)
		{
			typeBlock = b;
			meta = i;
			name = s;
			guiId = j;
			tileEntityClass = tileClass;
		}

		public static ReactorBlockType get(Block block, int meta)
		{
			for(ReactorBlockType type : values())
			{
				if(type.typeBlock == block && type.meta == meta)
				{
					return type;
				}
			}
			
			return null;
		}
		
		public static ReactorBlockType get(ItemStack stack)
		{
			return get(Block.getBlockFromItem(stack.getItem()), stack.getItemDamage());
		}

		public TileEntity create()
		{
			try {
				return tileEntityClass.newInstance();
			} catch(Exception e) {
				Mekanism.logger.error("Unable to indirectly create tile entity.");
				e.printStackTrace();
				return null;
			}
		}

		public String getDescription()
		{
			return MekanismUtils.localize("tooltip." + name);
		}

		public ItemStack getStack()
		{
			return new ItemStack(typeBlock, 1, meta);
		}

		@Override
		public String toString()
		{
			return Integer.toString(meta);
		}
	}

	public ItemStack dismantleBlock(World world, int x, int y, int z, boolean returnBlock)
	{
		ItemStack itemStack = getPickBlock(null, world, x, y, z);

		world.setBlockToAir(x, y, z);

		if(!returnBlock)
		{
			float motion = 0.7F;
			double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

			EntityItem entityItem = new EntityItem(world, x + motionX, y + motionY, z + motionZ, itemStack);

			world.spawnEntityInWorld(entityItem);
		}

		return itemStack;
	}
}
