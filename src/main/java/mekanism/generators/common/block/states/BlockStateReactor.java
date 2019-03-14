package mekanism.generators.common.block.states;

import mekanism.common.Mekanism;
import mekanism.common.tile.prefab.TileEntityElectricBlock;
import mekanism.common.util.LangUtils;
import mekanism.generators.common.GeneratorsBlocks;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.block.BlockReactor;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import mekanism.generators.common.tile.reactor.TileEntityReactorFrame;
import mekanism.generators.common.tile.reactor.TileEntityReactorGlass;
import mekanism.generators.common.tile.reactor.TileEntityReactorLaserFocusMatrix;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter;
import mekanism.generators.common.tile.reactor.TileEntityReactorPort;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import com.google.common.base.Predicate;

import java.util.Locale;

public class BlockStateReactor extends ExtendedBlockState
{
	public static final PropertyBool activeProperty = PropertyBool.create("active");
	
	public BlockStateReactor(BlockReactor block, PropertyEnum<ReactorBlockType> typeProperty)
	{
		super(block, new IProperty[] {typeProperty, activeProperty}, new IUnlistedProperty[] {});
	}
	
	public enum ReactorBlock
	{
		REACTOR_BLOCK,
		REACTOR_GLASS;

		private PropertyEnum<ReactorBlockType> predicatedProperty;

		public PropertyEnum<ReactorBlockType> getProperty()
		{
			if(predicatedProperty == null)
			{
				predicatedProperty = PropertyEnum.create("type", ReactorBlockType.class, new ReactorBlockPredicate(this));
			}
			
			return predicatedProperty;
		}

		public Block getBlock()
		{
			switch(this)
			{
				case REACTOR_BLOCK:
					return GeneratorsBlocks.Reactor;
				case REACTOR_GLASS:
					return GeneratorsBlocks.ReactorGlass;
				default:
					return null;
			}
		}
	}
	
	public static class ReactorBlockPredicate implements Predicate<ReactorBlockType>
	{
		public ReactorBlock basicBlock;

		public ReactorBlockPredicate(ReactorBlock type)
		{
			basicBlock = type;
		}

		@Override
		public boolean apply(ReactorBlockType input)
		{
			return input.blockType == basicBlock;
		}
	}
	
	public enum ReactorBlockType implements IStringSerializable
	{
		REACTOR_CONTROLLER(ReactorBlock.REACTOR_BLOCK, 0, "ReactorController", 10, TileEntityReactorController.class, true),
		REACTOR_FRAME(ReactorBlock.REACTOR_BLOCK, 1, "ReactorFrame", -1, TileEntityReactorFrame.class, false),
		REACTOR_PORT(ReactorBlock.REACTOR_BLOCK, 2, "ReactorPort", -1, TileEntityReactorPort.class, true),
		REACTOR_LOGIC_ADAPTER(ReactorBlock.REACTOR_BLOCK, 3, "ReactorLogicAdapter", 15, TileEntityReactorLogicAdapter.class, false),
		REACTOR_GLASS(ReactorBlock.REACTOR_GLASS, 0, "ReactorGlass", -1, TileEntityReactorGlass.class, false),
		LASER_FOCUS_MATRIX(ReactorBlock.REACTOR_GLASS, 1, "ReactorLaserFocusMatrix", -1, TileEntityReactorLaserFocusMatrix.class, false);
	
		public ReactorBlock blockType;
		public int meta;
		public String name;
		public int guiId;
		public Class<? extends TileEntity> tileEntityClass;
		public boolean activable;
	
		ReactorBlockType(ReactorBlock b, int i, String s, int j, Class<? extends TileEntityElectricBlock> tileClass, boolean activeState)
		{
			blockType = b;
			meta = i;
			name = s;
			guiId = j;
			tileEntityClass = tileClass;
			activable = activeState;
		}
		
		public static ReactorBlockType get(Block block, int meta)
		{
			if(block instanceof BlockReactor)
			{
				return get(((BlockReactor)block).getReactorBlock(), meta);
			}

			return null;
		}

		public static ReactorBlockType get(ReactorBlock block, int meta)
		{
			for(ReactorBlockType type : values())
			{
				if(type.meta == meta && type.blockType == block)
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
		
		@Override
		public String getName()
		{
			return name().toLowerCase(Locale.ROOT);
		}
	
		public String getDescription()
		{
			return LangUtils.localize("tooltip." + name);
		}
	
		public ItemStack getStack(int amount)
		{
			return new ItemStack(blockType.getBlock(), amount, meta);
		}

		public boolean hasActiveTexture()
		{
			return activable;
		}
	}
	
	public static class ReactorBlockStateMapper extends StateMapperBase
	{
		@Override
		protected ModelResourceLocation getModelResourceLocation(IBlockState state)
		{
			BlockReactor block = (BlockReactor)state.getBlock();
			ReactorBlockType type = state.getValue(block.getTypeProperty());
			StringBuilder builder = new StringBuilder();
			String nameOverride = null;

			if(type.hasActiveTexture())
			{
				builder.append(activeProperty.getName());
				builder.append("=");
				builder.append(state.getValue(activeProperty));
			}

			if(builder.length() == 0)
			{
				builder.append("normal");
			}

			ResourceLocation baseLocation = new ResourceLocation(MekanismGenerators.MODID, nameOverride != null ? nameOverride : type.getName());
			
			return new ModelResourceLocation(baseLocation, builder.toString());
		}
	}
}
