package mekanism.generators.common.block;

import java.util.Arrays;
import java.util.List;

import mekanism.client.render.ctm.CTMBlockRenderContext;
import mekanism.client.render.ctm.CTMData;
import mekanism.client.render.ctm.ICTMBlock;
import mekanism.common.Mekanism;
import mekanism.common.block.states.BlockStateBasic;
import mekanism.common.tile.TileEntityBasicBlock;
import mekanism.common.tile.TileEntityElectricBlock;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.GeneratorsBlocks;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.block.states.BlockStateReactor;
import mekanism.generators.common.block.states.BlockStateReactor.ReactorBlock;
import mekanism.generators.common.block.states.BlockStateReactor.ReactorBlockType;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import buildcraft.api.tools.IToolWrench;
import codechicken.lib.render.TextureUtils.IIconRegister;

public abstract class BlockReactor extends BlockContainer implements ICTMBlock
{
	public CTMData[][] ctms = new CTMData[16][2];

	public BlockReactor()
	{
		super(Material.iron);
		setHardness(3.5F);
		setResistance(8F);
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	public static BlockReactor getReactorBlock(ReactorBlock block)
	{
		return new BlockReactor()
		{
			@Override
			public ReactorBlock getReactorBlock()
			{
				return block;
			}
		};
	}

	public abstract ReactorBlock getReactorBlock();
	
	@SideOnly(Side.CLIENT)
    @Override
    public IBlockState getExtendedState(IBlockState stateIn, IBlockAccess w, BlockPos pos) 
	{
        if(stateIn.getBlock() == null || stateIn.getBlock().getMaterial() == Material.air) 
        {
            return stateIn;
        }
        
        IExtendedBlockState state = (IExtendedBlockState)stateIn;
        CTMBlockRenderContext ctx = new CTMBlockRenderContext(w, pos);

        return state.withProperty(BlockStateBasic.ctmProperty, ctx);
    }

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register)
	{
		if(this == GeneratorsBlocks.Reactor)
		{
			ctms[0][0] = new CTMData("ctm/ReactorFrame", this, Arrays.asList(0, 1, 2, 3, 4)).addSideOverride(EnumFacing.UP, "ctm/ReactorControllerOff").registerIcons(register);
			ctms[0][1] = new CTMData("ctm/ReactorFrame", this, Arrays.asList(0, 1, 2, 3, 4)).addSideOverride(EnumFacing.UP, "ctm/ReactorControllerOn").registerIcons(register);
			ctms[1][0] = new CTMData("ctm/ReactorFrame", this, Arrays.asList(0, 1, 2, 3, 4)).registerIcons(register);
			ctms[2][0] = new CTMData("ctm/ReactorNeutronCapture", this, Arrays.asList(0, 1, 2, 3, 4)).registerIcons(register);
			ctms[3][0] = new CTMData("ctm/ReactorPort", this, Arrays.asList(0, 1, 2, 3, 4)).registerIcons(register);
			ctms[4][0] = new CTMData("ctm/ReactorLogicAdapter", this, Arrays.asList(0, 1, 2, 3, 4)).registerIcons(register);

			icons[0][0] = ctms[0][0].sideOverrides[1].icon;
			icons[0][1] = ctms[0][1].sideOverrides[1].icon;
			icons[0][2] = ctms[0][0].mainTextureData.icon;
			icons[1][0] = ctms[1][0].mainTextureData.icon;
			icons[2][0] = ctms[2][0].mainTextureData.icon;
			icons[3][0] = ctms[3][0].mainTextureData.icon;
			icons[4][0] = ctms[4][0].mainTextureData.icon;
		}
		else if(this == GeneratorsBlocks.ReactorGlass)
		{
			ctms[0][0] = new CTMData("ctm/ReactorGlass", this, Arrays.asList(0, 1)).registerIcons(register);
			ctms[1][0] = new CTMData("ctm/ReactorLaserFocus", this, Arrays.asList(1, 0)).registerIcons(register);

			icons[0][0] = ctms[0][0].mainTextureData.icon;
			icons[1][0] = ctms[1][0].mainTextureData.icon;
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
				return icons[0][side == 1 ? 0 : 2];
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
	public int damageDropped(IBlockState state)
	{
		return getMetaFromState(state);
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock)
	{
		if(!world.isRemote)
		{
			TileEntity tileEntity = world.getTileEntity(pos);

			if(tileEntity instanceof TileEntityBasicBlock)
			{
				((TileEntityBasicBlock)tileEntity).onNeighborChange(neighborBlock);
			}
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumFacing facing, float playerX, float playerY, float playerZ)
	{
		if(world.isRemote)
		{
			return true;
		}

		TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getTileEntity(pos);
		int metadata = state.getBlock().getMetaFromState(state);

		if(entityplayer.getCurrentEquippedItem() != null)
		{
			if(MekanismUtils.isBCWrench(entityplayer.getCurrentEquippedItem().getItem()) && !entityplayer.getCurrentEquippedItem().getUnlocalizedName().contains("omniwrench"))
			{
				if(entityplayer.isSneaking())
				{
					dismantleBlock(world, pos, false);
					return true;
				}

				((IToolWrench)entityplayer.getCurrentEquippedItem().getItem()).wrenchUsed(entityplayer, pos);

				return true;
			}
		}

		if(tileEntity instanceof TileEntityReactorController)
		{
			if(!entityplayer.isSneaking())
			{
				entityplayer.openGui(MekanismGenerators.instance, ReactorBlockType.get(this, metadata).guiId, world, pos.getX(), pos.getY(), pos.getZ());
				return true;
			}
		}
		
		if(tileEntity instanceof TileEntityReactorLogicAdapter)
		{
			if(!entityplayer.isSneaking())
			{
				entityplayer.openGui(MekanismGenerators.instance, BlockStateReactor.ReactorBlockType.get(this, metadata).guiId, world, pos.getX(), pos.getY(), pos.getZ());
				return true;
			}
		}

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativetabs, List list)
	{
		for(BlockStateReactor.ReactorBlockType type : BlockStateReactor.ReactorBlockType.values())
		{
			if(type.blockType == getReactorBlock() && type.isValidReactorBlock())
			{
				list.add(new ItemStack(item, 1, type.meta));
			}
		}
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		int metadata = getMetaFromState(state);
		
		if(ReactorBlockType.get(getReactorBlock(), metadata) == null)
		{
			return null;
		}

		return ReactorBlockType.get(getReactorBlock(), metadata).create();
	}
	
	@Override
	public EnumWorldBlockLayer getBlockLayer()
	{
		return this == GeneratorsBlocks.Reactor ? EnumWorldBlockLayer.CUTOUT : EnumWorldBlockLayer.TRANSLUCENT;
	}

	@Override
	public int getRenderType()
	{
		return 3;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
	
	@Override
	public boolean isFullCube()
	{
		return false;
	}

	/*This method is not used, metadata manipulation is required to create a Tile Entity.*/
	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return null;
	}

	@Override
	public CTMData getCTMData(IBlockAccess world, int x, int y, int z, int meta)
	{
		if(ctms[meta][1] != null && MekanismUtils.isActive(world, x, y, z))
		{
			return ctms[meta][1];
		}

		return ctms[meta][0];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		BlockPos offsetPos = pos.offset(side.getOpposite());
		
		if(this == GeneratorsBlocks.ReactorGlass)
		{
			IBlockState state = world.getBlockState(offsetPos);
			int metadata = state.getBlock().getMetaFromState(state);
			
			switch(metadata)
			{
				case 0:
				case 1:
					return ctms[metadata][0].shouldRenderSide(world, pos, side);
				default:
					return super.shouldSideBeRendered(world, pos, side);
			}
		}
		else {
			return super.shouldSideBeRendered(world, pos, side);
		}
	}
	
	@Override
	public int getWeakPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side)
    {
		TileEntity tile = world.getTileEntity(pos);
		
		if(tile instanceof TileEntityReactorLogicAdapter)
		{
			return ((TileEntityReactorLogicAdapter)tile).checkMode() ? 15 : 0;
		}
		
        return 0;
    }
	
	@Override
	public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		IBlockState state = world.getBlockState(pos);
		ReactorBlockType type = ReactorBlockType.get(getReactorBlock(), state.getBlock().getMetaFromState(state));

		switch(type)
		{
			case REACTOR_FRAME:
			case REACTOR_PORT:
			case REACTOR_LOGIC_ADAPTER:
				return true;
			default:
				return false;
		}
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		IBlockState state = world.getBlockState(pos);
		ReactorBlockType type = BlockStateReactor.ReactorBlockType.get(this, state.getBlock().getMetaFromState(state));

		switch(type)
		{
			case REACTOR_LOGIC_ADAPTER:
				return true;
			default:
				return false;
		}
	}
	
	@Override
	public PropertyEnum<ReactorBlockType> getTypeProperty()
	{
		return getReactorBlock().getProperty();
	}

	public ItemStack dismantleBlock(World world, BlockPos pos, boolean returnBlock)
	{
		IBlockState state = world.getBlockState(pos);
		ItemStack itemStack = new ItemStack(this, 1, state.getBlock().getMetaFromState(state));

		world.setBlockToAir(pos);

		if(!returnBlock)
		{
			float motion = 0.7F;
			double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

			EntityItem entityItem = new EntityItem(world, pos.getX() + motionX, pos.getY() + motionY, pos.getZ() + motionZ, itemStack);

			world.spawnEntityInWorld(entityItem);
		}

		return itemStack;
	}
}
