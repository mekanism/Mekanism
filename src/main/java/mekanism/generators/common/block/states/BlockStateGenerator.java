package mekanism.generators.common.block.states;

import java.util.ArrayList;
import java.util.List;

import mekanism.common.Mekanism;
import mekanism.common.base.IBlockType;
import mekanism.common.block.states.BlockStateFacing;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.config.MekanismConfig.generators;
import mekanism.common.util.LangUtils;
import mekanism.generators.common.GeneratorsBlocks;
import mekanism.generators.common.block.BlockGenerator;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import mekanism.generators.common.tile.turbine.TileEntityElectromagneticCoil;
import mekanism.generators.common.tile.turbine.TileEntityRotationalComplex;
import mekanism.generators.common.tile.turbine.TileEntitySaturatingCondenser;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import mekanism.generators.common.tile.turbine.TileEntityTurbineValve;
import mekanism.generators.common.tile.turbine.TileEntityTurbineVent;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class BlockStateGenerator extends ExtendedBlockState
{
	public static final PropertyBool activeProperty = PropertyBool.create("active");

	public BlockStateGenerator(BlockGenerator block, PropertyEnum<?> typeProperty)
	{
		super(block, new IProperty[] {BlockStateFacing.facingProperty, typeProperty, activeProperty}, new IUnlistedProperty[] {});
	}

	public enum GeneratorBlock
	{
		GENERATOR_BLOCK_1;

		PropertyEnum<GeneratorType> generatorTypeProperty;

		public PropertyEnum<GeneratorType> getProperty()
		{
			if(generatorTypeProperty == null)
			{
				generatorTypeProperty = PropertyEnum.create("type", GeneratorType.class, new GeneratorBlockPredicate(this));
			}

			return generatorTypeProperty;
		}

		public Block getBlock()
		{
			switch(this)
			{
				case GENERATOR_BLOCK_1:
					return GeneratorsBlocks.Generator;
				default:
					return null;
			}
		}
	}
	
	public static class GeneratorBlockPredicate implements Predicate<GeneratorType>
	{
		public GeneratorBlock GeneratorBlock;

		public GeneratorBlockPredicate(GeneratorBlock type)
		{
			GeneratorBlock = type;
		}

		@Override
		public boolean apply(GeneratorType input)
		{
			return input.blockType == GeneratorBlock;
		}
	}
	
	public enum GeneratorType implements IStringSerializable, IBlockType
	{
		HEAT_GENERATOR(GeneratorBlock.GENERATOR_BLOCK_1, 0, "HeatGenerator", 0, 160000, TileEntityHeatGenerator.class, true, Plane.HORIZONTAL, false),
		SOLAR_GENERATOR(GeneratorBlock.GENERATOR_BLOCK_1, 1, "SolarGenerator", 1, 96000, TileEntitySolarGenerator.class, true, Plane.HORIZONTAL, false),
		GAS_GENERATOR(GeneratorBlock.GENERATOR_BLOCK_1, 3, "GasGenerator", 3, general.FROM_H2*100, TileEntityGasGenerator.class, true, Plane.HORIZONTAL, false),
		BIO_GENERATOR(GeneratorBlock.GENERATOR_BLOCK_1, 4, "BioGenerator", 4, 160000, TileEntityBioGenerator.class, true, Plane.HORIZONTAL, false),
		ADVANCED_SOLAR_GENERATOR(GeneratorBlock.GENERATOR_BLOCK_1, 5, "AdvancedSolarGenerator", 1, 200000, TileEntityAdvancedSolarGenerator.class, true, Plane.HORIZONTAL, false),
		WIND_GENERATOR(GeneratorBlock.GENERATOR_BLOCK_1, 6, "WindGenerator", 5, 200000, TileEntityWindGenerator.class, true, Plane.HORIZONTAL, false),
		TURBINE_ROTOR(GeneratorBlock.GENERATOR_BLOCK_1, 7, "TurbineRotor", -1, -1, TileEntityTurbineRotor.class, false, Predicates.alwaysFalse(), false),
		ROTATIONAL_COMPLEX(GeneratorBlock.GENERATOR_BLOCK_1, 8, "RotationalComplex", -1, -1, TileEntityRotationalComplex.class, false, Predicates.alwaysFalse(), false),
		ELECTROMAGNETIC_COIL(GeneratorBlock.GENERATOR_BLOCK_1, 9, "ElectromagneticCoil", -1, -1, TileEntityElectromagneticCoil.class, false, Predicates.alwaysFalse(), false),
		TURBINE_CASING(GeneratorBlock.GENERATOR_BLOCK_1, 10, "TurbineCasing", -1, -1, TileEntityTurbineCasing.class, false, Predicates.alwaysFalse(), false),
		TURBINE_VALVE(GeneratorBlock.GENERATOR_BLOCK_1, 11, "TurbineValve", -1, -1, TileEntityTurbineValve.class, false, Predicates.alwaysFalse(), false),
		TURBINE_VENT(GeneratorBlock.GENERATOR_BLOCK_1, 12, "TurbineVent", -1, -1, TileEntityTurbineVent.class, false, Predicates.alwaysFalse(), false),
		SATURATING_CONDENSER(GeneratorBlock.GENERATOR_BLOCK_1, 13, "SaturatingCondenser", -1, -1, TileEntitySaturatingCondenser.class, false, Predicates.alwaysFalse(), false);
	
		public GeneratorBlock blockType;
		public int meta;
		public String blockName;
		public int guiId;
		public double maxEnergy;
		public Class<? extends TileEntity> tileEntityClass;
		public boolean hasModel;
		public Predicate<EnumFacing> facingPredicate;
		public boolean activable;
	
		GeneratorType(GeneratorBlock block, int i, String s, int j, double k, Class<? extends TileEntity> tileClass, boolean model, Predicate<EnumFacing> predicate, boolean hasActiveTexture)
		{
			blockType = block;
			meta = i;
			blockName = s;
			guiId = j;
			maxEnergy = k;
			tileEntityClass = tileClass;
			hasModel = model;
			facingPredicate = predicate;
			activable = hasActiveTexture;
		}
		
		public static List<GeneratorType> getGeneratorsForConfig()
		{
			List<GeneratorType> ret = new ArrayList<>();

			for(GeneratorType type : GeneratorType.values())
			{
				if(type.ordinal() <= 5)
				{
					ret.add(type);
				}
			}

			return ret;
		}
		
		@Override
		public String getBlockName()
		{
			return blockName;
		}

		@Override
		public boolean isEnabled()
		{
			if(meta > WIND_GENERATOR.meta)
			{
				return true;
			}
			
			return generators.generatorsManager.isEnabled(blockName);
		}

		public static GeneratorType get(IBlockState state)
		{
			if(state.getBlock() instanceof BlockGenerator)
			{
				return state.getValue(((BlockGenerator)state.getBlock()).getTypeProperty());
			}
			
			return null;
		}
	
		public static GeneratorType get(Block block, int meta)
		{
			if(block instanceof BlockGenerator)
			{
				return get(((BlockGenerator)block).getGeneratorBlock(), meta);
			}

			return null;
		}

		public static GeneratorType get(GeneratorBlock block, int meta)
		{
			for(GeneratorType type : values())
			{
				if(type.meta == meta && type.blockType == block)
				{
					return type;
				}
			}

			return null;
		}
		
		public static GeneratorType get(ItemStack stack)
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
			return name().toLowerCase();
		}
		
		public String getDescription()
		{
			return LangUtils.localize("tooltip." + blockName);
		}
		
		public ItemStack getStack()
		{
			return new ItemStack(GeneratorsBlocks.Generator, 1, meta);
		}
		
		public boolean canRotateTo(EnumFacing side)
		{
			return facingPredicate.apply(side);
		}

		public boolean hasRotations()
		{
			return !facingPredicate.equals(Predicates.alwaysFalse());
		}

		public boolean hasActiveTexture()
		{
			return activable;
		}
	}
	
	public static class GeneratorBlockStateMapper extends StateMapperBase
	{
		@Override
		protected ModelResourceLocation getModelResourceLocation(IBlockState state)
		{
			BlockGenerator block = (BlockGenerator)state.getBlock();
			GeneratorType type = state.getValue(block.getTypeProperty());
			StringBuilder builder = new StringBuilder();
			String nameOverride = null;
			
			if(type.hasActiveTexture())
			{
				builder.append(activeProperty.getName());
				builder.append("=");
				builder.append(state.getValue(activeProperty));
			}
			
			if(type.hasRotations())
			{
				EnumFacing facing = state.getValue(BlockStateFacing.facingProperty);
				
				if(!type.canRotateTo(facing))
				{
					facing = EnumFacing.NORTH;
				}
				
				if(builder.length() > 0)
				{
					builder.append(",");
				}
				
				builder.append(BlockStateFacing.facingProperty.getName());
				builder.append("=");
				builder.append(facing.getName());
			}

			if(builder.length() == 0)
			{
				builder.append("normal");
			}

			ResourceLocation baseLocation = new ResourceLocation("mekanismgenerators", nameOverride != null ? nameOverride : type.getName());
			
			return new ModelResourceLocation(baseLocation, builder.toString());
		}
	}
}
