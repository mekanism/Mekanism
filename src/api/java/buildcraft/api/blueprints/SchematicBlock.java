/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.blueprints;

import java.util.ArrayList;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockLiquid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.BlockFluidBase;

public class SchematicBlock extends SchematicBlockBase {

	public Block block = null;
	public int meta = 0;
	public BuildingPermission defaultPermission = BuildingPermission.ALL;

	/**
	 * This field contains requirements for a given block when stored in the
	 * blueprint. Modders can either rely on this list or compute their own int
	 * Schematic.
	 */
	public ItemStack [] storedRequirements = new ItemStack [0];

	private boolean doNotUse = false;
	
	@Override
	public void getRequirementsForPlacement(IBuilderContext context, LinkedList<ItemStack> requirements) {
		if (block != null) {
			if (storedRequirements.length != 0) {
				for (ItemStack s : storedRequirements) {
					requirements.add(s);
				}
			} else {
				requirements.add(new ItemStack(block, 1, meta));
			}
		}
	}

	@Override
	public boolean isAlreadyBuilt(IBuilderContext context, int x, int y, int z) {
		return block == context.world().getBlock(x, y, z) && meta == context.world().getBlockMetadata(x, y, z);
	}

	@Override
	public void placeInWorld(IBuilderContext context, int x, int y, int z, LinkedList<ItemStack> stacks) {
		super.placeInWorld(context, x, y, z, stacks);

		this.setBlockInWorld(context, x, y, z);
	}

	@Override
	public void storeRequirements(IBuilderContext context, int x, int y, int z) {
		super.storeRequirements(context, x, y, z);

		if (block != null) {
			ArrayList<ItemStack> req = block.getDrops(context.world(), x,
					y, z, context.world().getBlockMetadata(x, y, z), 0);

			if (req != null) {
				storedRequirements = new ItemStack [req.size()];
				req.toArray(storedRequirements);
			}
		}
	}

	@Override
	public void writeSchematicToNBT(NBTTagCompound nbt, MappingRegistry registry) {
		super.writeSchematicToNBT(nbt, registry);

		writeBlockToNBT(nbt, registry);
		writeRequirementsToNBT(nbt, registry);
	}

	@Override
	public void readSchematicFromNBT(NBTTagCompound nbt,	MappingRegistry registry) {
		super.readSchematicFromNBT(nbt, registry);

		readBlockFromNBT(nbt, registry);
		if (!doNotUse()) {
			readRequirementsFromNBT(nbt, registry);
		}
	}

	@Override
	public BuildingStage getBuildStage () {
		if (block instanceof BlockFalling) {
			return BuildingStage.SUPPORTED;
		} else if (block instanceof BlockFluidBase || block instanceof BlockLiquid) {
			return BuildingStage.EXPANDING;
		} else if (block.isOpaqueCube()) {
			return BuildingStage.STANDALONE;
		} else {
			return BuildingStage.SUPPORTED;
		}
	}

	@Override
	public BuildingPermission getBuildingPermission() {
		return defaultPermission;
	}
	
	// Utility functions
	protected void setBlockInWorld(IBuilderContext context, int x, int y, int z) {
		// Meta needs to be specified twice, depending on the block behavior
		context.world().setBlock(x, y, z, block, meta, 3);
		context.world().setBlockMetadataWithNotify(x, y, z, meta, 3);
	}
	
	public boolean doNotUse() {
		return doNotUse;
	}
	
	protected void readBlockFromNBT(NBTTagCompound nbt, MappingRegistry registry) {
		try {
			block = registry.getBlockForId(nbt.getInteger("blockId"));
			meta = nbt.getInteger("blockMeta");
		} catch (MappingNotFoundException e) {
			doNotUse = true;
		}
	}
	
	protected void readRequirementsFromNBT(NBTTagCompound nbt, MappingRegistry registry) {
		if (nbt.hasKey("rq")) {
			NBTTagList rq = nbt.getTagList("rq", Constants.NBT.TAG_COMPOUND);

			ArrayList<ItemStack> rqs = new ArrayList<ItemStack>();
			int idTEST = 0;
			for (int i = 0; i < rq.tagCount(); ++i) {
				try {
					NBTTagCompound sub = rq.getCompoundTagAt(i);
					idTEST = sub.getInteger("id");
					if (sub.getInteger("id") >= 0) {
						registry.stackToWorld(sub);
						rqs.add(ItemStack.loadItemStackFromNBT(sub));
					} else {
						defaultPermission = BuildingPermission.CREATIVE_ONLY;
					}
				} catch (MappingNotFoundException e) {
					defaultPermission = BuildingPermission.CREATIVE_ONLY;
				} catch (Throwable t) {
					t.printStackTrace();
					defaultPermission = BuildingPermission.CREATIVE_ONLY;
				}
			}

			storedRequirements = rqs.toArray(new ItemStack[rqs.size()]);
		} else {
			storedRequirements = new ItemStack[0];
		}
	}
	
	protected void writeBlockToNBT(NBTTagCompound nbt, MappingRegistry registry) {
		nbt.setInteger("blockId", registry.getIdForBlock(block));
		nbt.setInteger("blockMeta", meta);
	}
	
	protected void writeRequirementsToNBT(NBTTagCompound nbt, MappingRegistry registry) {
		if (storedRequirements.length > 0) {
			NBTTagList rq = new NBTTagList();

			for (ItemStack stack : storedRequirements) {
				NBTTagCompound sub = new NBTTagCompound();
				stack.writeToNBT(sub);
				registry.stackToRegistry(sub);
				rq.appendTag(sub);
			}

			nbt.setTag("rq", rq);
		}
	}
}
