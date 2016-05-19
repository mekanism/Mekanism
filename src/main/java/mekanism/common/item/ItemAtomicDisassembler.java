package mekanism.common.item;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.MekanismConfig.general;
import mekanism.api.util.ListUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;

public class ItemAtomicDisassembler extends ItemEnergized
{
	public double HOE_USAGE = 10 * general.DISASSEMBLER_USAGE;

	public ItemAtomicDisassembler()
	{
		super(1000000);
	}

	@Override
	public boolean canHarvestBlock(Block block, ItemStack stack)
	{
		return block != Blocks.bedrock;
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag)
	{
		super.addInformation(itemstack, entityplayer, list, flag);

		list.add(LangUtils.localize("tooltip.mode") + ": " + EnumColor.INDIGO + getModeName(itemstack));
		list.add(LangUtils.localize("tooltip.efficiency") + ": " + EnumColor.INDIGO + getEfficiency(itemstack));
	}

	@Override
	public boolean hitEntity(ItemStack itemstack, EntityLivingBase hitEntity, EntityLivingBase player)
	{
		if(getEnergy(itemstack) > 0)
		{
			hitEntity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer)player), 20);
			setEnergy(itemstack, getEnergy(itemstack) - 2000);
		}
		else {
			hitEntity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer)player), 4);
		}

		return false;
	}

	@Override
	public float getDigSpeed(ItemStack itemstack, IBlockState state)
	{
		return getEnergy(itemstack) != 0 ? getEfficiency(itemstack) : 1F;
	}

	@Override
	public boolean onBlockDestroyed(ItemStack itemstack, World world, Block block, BlockPos pos, EntityLivingBase entityliving)
	{
		if(block.getBlockHardness(world, pos) != 0.0D)
		{
			setEnergy(itemstack, getEnergy(itemstack) - (general.DISASSEMBLER_USAGE*getEfficiency(itemstack)));
		}
		else {
			setEnergy(itemstack, getEnergy(itemstack) - (general.DISASSEMBLER_USAGE*(getEfficiency(itemstack))/2));
		}

		return true;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player)
	{
		super.onBlockStartBreak(itemstack, pos, player);

		if(!player.worldObj.isRemote)
		{
			IBlockState state = player.worldObj.getBlockState(pos);
			Block block = state.getBlock();
			int meta = block.getMetaFromState(state);

			if(block == Blocks.lit_redstone_ore)
			{
				block = Blocks.redstone_ore;
			}

			ItemStack stack = new ItemStack(block, 1, meta);
			Coord4D orig = new Coord4D(pos, player.worldObj);

			List<String> names = MekanismUtils.getOreDictName(stack);

			boolean isOre = false;

			for(String s : names)
			{
				if(s.startsWith("ore") || s.equals("logWood"))
				{
					isOre = true;
				}
			}

			if(getMode(itemstack) == 3 && isOre && !player.capabilities.isCreativeMode)
			{
				Set<Coord4D> found = new Finder(player.worldObj, stack, new Coord4D(pos, player.worldObj)).calc();

				for(Coord4D coord : found)
				{
					if(coord.equals(orig) || getEnergy(itemstack) < (general.DISASSEMBLER_USAGE*getEfficiency(itemstack)))
					{
						continue;
					}

					Block block2 = coord.getBlock(player.worldObj);

					block2.onBlockDestroyedByPlayer(player.worldObj, coord.getPos(), state);
					player.worldObj.playAuxSFXAtEntity(null, 2001, coord.getPos(), meta << 12);
					player.worldObj.setBlockToAir(coord.getPos());
					block2.breakBlock(player.worldObj, coord.getPos(), state);
					block2.dropBlockAsItem(player.worldObj, coord.getPos(), state, 0);

					setEnergy(itemstack, getEnergy(itemstack) - (general.DISASSEMBLER_USAGE*getEfficiency(itemstack)));
				}
			}
		}

		return false;
	}

	@Override
	public boolean isFull3D()
	{
		return true;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		if(!world.isRemote && entityplayer.isSneaking())
		{
			toggleMode(itemstack);
			entityplayer.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + LangUtils.localize("tooltip.modeToggle") + " " + EnumColor.INDIGO + getModeName(itemstack) + EnumColor.AQUA + " (" + getEfficiency(itemstack) + ")"));
		}

		return itemstack;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if(!player.isSneaking())
		{
			if(!useHoe(stack, player, world, pos, side))
			{
				if(world.getBlockState(pos).getBlock() != Blocks.farmland)
				{
					return false;
				}
			}

			switch(getEfficiency(stack))
			{
				case 20:
					for(int x1 = -1; x1 <= +1; x1++)
					{
						for(int z1 = -1; z1 <= +1; z1++)
						{
							useHoe(stack, player, world, pos.add(x1, 0, z1), side);
						}
					}

					break;
				case 128:
					for(int x1 = -2; x1 <= +2; x1++)
					{
						for(int z1 = -2; z1 <= +2; z1++)
						{
							useHoe(stack, player, world, pos.add(x1, 0, z1), side);
						}
					}

					break;
			}

			return true;
		}

		return false;
	}

	private boolean useHoe(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side)
	{
		if(!player.canPlayerEdit(pos, side, stack) || (!player.capabilities.isCreativeMode && getEnergy(stack) < HOE_USAGE))
		{
			return false;
		}
		else {
			UseHoeEvent event = new UseHoeEvent(player, stack, world, pos);

			if(MinecraftForge.EVENT_BUS.post(event))
			{
				return false;
			}

			if(event.getResult() == Result.ALLOW)
			{
				setEnergy(stack, getEnergy(stack)-HOE_USAGE);
				return true;
			}

			Block block1 = world.getBlockState(pos).getBlock();
			boolean air = block1.isAir(world, pos.up());

			if(side != EnumFacing.DOWN && air && (block1 == Blocks.grass || block1 == Blocks.dirt))
			{
				Block farm = Blocks.farmland;
				world.playSoundEffect(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, farm.stepSound.getStepSound(), (farm.stepSound.getVolume() + 1.0F) / 2.0F, farm.stepSound.getFrequency() * 0.8F);

				if(world.isRemote)
				{
					return true;
				}
				else {
					world.setBlockState(pos, farm.getDefaultState());
					
					if(!player.capabilities.isCreativeMode)
					{
						setEnergy(stack, getEnergy(stack)-HOE_USAGE);
					}
					
					return true;
				}
			}
			else {
				return false;
			}
		}
	}

	public int getEfficiency(ItemStack itemStack)
	{
		switch(getMode(itemStack))
		{
			case 0:
				return 20;
			case 1:
				return 8;
			case 2:
				return 128;
			case 3:
				return 20;
			case 4:
				return 0;
		}

		return 0;
	}

	public int getMode(ItemStack itemStack)
	{
		return ItemDataUtils.getInt(itemStack, "mode");
	}

	public String getModeName(ItemStack itemStack)
	{
		int mode = getMode(itemStack);

		switch(mode)
		{
			case 0:
				return LangUtils.localize("tooltip.disassembler.normal");
			case 1:
				return LangUtils.localize("tooltip.disassembler.slow");
			case 2:
				return LangUtils.localize("tooltip.disassembler.fast");
			case 3:
				return LangUtils.localize("tooltip.disassembler.vein");
			case 4:
				return LangUtils.localize("tooltip.disassembler.off");
		}

		return null;
	}

	public void toggleMode(ItemStack itemStack)
	{
		ItemDataUtils.setInt(itemStack, "mode", getMode(itemStack) < 4 ? getMode(itemStack)+1 : 0);
	}

	@Override
	public boolean canSend(ItemStack itemStack)
	{
		return false;
	}

	public static class Finder
	{
		public World world;

		public ItemStack stack;

		public Coord4D location;

		public Set<Coord4D> found = new HashSet<Coord4D>();

		public static Map<Block, List<Block>> ignoreBlocks = new HashMap<Block, List<Block>>();

		public Finder(World w, ItemStack s, Coord4D loc)
		{
			world = w;
			stack = s;
			location = loc;
		}

		public void loop(Coord4D pointer)
		{
			if(found.contains(pointer) || found.size() > 128)
			{
				return;
			}

			found.add(pointer);

			for(EnumFacing side : EnumFacing.VALUES)
			{
				Coord4D coord = pointer.offset(side);

				if(coord.exists(world) && checkID(coord.getBlock(world)) && (coord.getBlockMeta(world) == stack.getItemDamage() || (MekanismUtils.getOreDictName(stack).contains("logWood") && coord.getBlockMeta(world) % 4 == stack.getItemDamage() % 4)))
				{
					loop(coord);
				}
			}
		}

		public Set<Coord4D> calc()
		{
			loop(location);

			return found;
		}

		public boolean checkID(Block b)
		{
			Block origBlock = location.getBlock(world);
			return (ignoreBlocks.get(origBlock) == null && b == origBlock) || (ignoreBlocks.get(origBlock) != null && ignoreBlocks.get(origBlock).contains(b));
		}

		static {
			ignoreBlocks.put(Blocks.redstone_ore, ListUtils.asList(Blocks.redstone_ore, Blocks.lit_redstone_ore));
			ignoreBlocks.put(Blocks.lit_redstone_ore, ListUtils.asList(Blocks.redstone_ore, Blocks.lit_redstone_ore));
		}
	}
}
