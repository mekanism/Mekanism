/** Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.blueprints;

import java.util.*;

import com.google.gson.GsonBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.BlockFluidBase;

import buildcraft.api.core.BCLog;

public class SchematicBlock extends SchematicBlockBase {
    public IBlockState state = null;
    public BuildingPermission defaultPermission = BuildingPermission.ALL;

    /** This field contains requirements for a given block when stored in the blueprint. Modders can either rely on this
     * list or compute their own int Schematic. */
    public ItemStack[] storedRequirements = new ItemStack[0];

    private boolean doNotUse = false;

    @Override
    public void getRequirementsForPlacement(IBuilderContext context, List<ItemStack> requirements) {
        if (state != null) {
            if (storedRequirements.length != 0) {
                Collections.addAll(requirements, storedRequirements);
            } else {
                requirements.add(getItemStack(state));
            }
        }
    }

    @Override
    public boolean isAlreadyBuilt(IBuilderContext context, BlockPos pos) {
        IBlockState placed = context.world().getBlockState(pos);
        if (state == placed) return true;
        if (state.getBlock() != placed.getBlock()) return false;
        // This fixes bugs with blocks like stairs that return extra properties that were not visible from the meta.
        if (state.getBlock().getMetaFromState(state) == placed.getBlock().getMetaFromState(placed)) return true;
        return false;
    }

    @Override
    public void placeInWorld(IBuilderContext context, BlockPos pos, List<ItemStack> stacks) {
        super.placeInWorld(context, pos, stacks);

        this.setBlockInWorld(context, pos);
    }

    @Override
    public void storeRequirements(IBuilderContext context, BlockPos pos) {
        super.storeRequirements(context, pos);

        if (state != null) {
            List<ItemStack> req = state.getBlock().getDrops(context.world(), pos, state, 0);

            if (req != null) {
                storedRequirements = new ItemStack[req.size()];
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
    public void readSchematicFromNBT(NBTTagCompound nbt, MappingRegistry registry) {
        super.readSchematicFromNBT(nbt, registry);

        readBlockFromNBT(nbt, registry);
        if (!doNotUse()) {
            readRequirementsFromNBT(nbt, registry);
        }
    }

    /** Get a list of relative block coordinates which have to be built before this block can be placed. */
    public Set<BlockPos> getPrerequisiteBlocks(IBuilderContext context) {
        Set<BlockPos> indexes = new HashSet<BlockPos>();
        if (state.getBlock() instanceof BlockFalling) {
            indexes.add(new BlockPos(0, -1, 0));
        }
        return indexes;
    }

    @Override
    public BuildingStage getBuildStage() {
        if (state.getBlock() instanceof BlockFluidBase || state.getBlock() instanceof BlockLiquid) {
            return BuildingStage.EXPANDING;
        } else {
            return BuildingStage.STANDALONE;
        }
    }

    @Override
    public BuildingPermission getBuildingPermission() {
        return defaultPermission;
    }

    // Utility functions
    protected void setBlockInWorld(IBuilderContext context, BlockPos pos) {
        context.world().setBlockState(pos, state, 3);
    }

    public boolean doNotUse() {
        return doNotUse;
    }

    protected void readBlockFromNBT(NBTTagCompound nbt, MappingRegistry registry) {
        try {
            Block block = registry.getBlockForId(nbt.getInteger("blockId"));
            state = block.getStateFromMeta(nbt.getInteger("blockMeta"));
        } catch (MappingNotFoundException e) {
            BCLog.logger.info(e);
            doNotUse = true;
        }
    }

    protected void readRequirementsFromNBT(NBTTagCompound nbt, MappingRegistry registry) {
        if (nbt.hasKey("rq")) {
            NBTTagList rq = nbt.getTagList("rq", Constants.NBT.TAG_COMPOUND);

            ArrayList<ItemStack> rqs = new ArrayList<ItemStack>();
            for (int i = 0; i < rq.tagCount(); ++i) {
                try {
                    NBTTagCompound sub = rq.getCompoundTagAt(i);
                    registry.stackToWorld(sub);
                    rqs.add(ItemStack.loadItemStackFromNBT(sub));
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
        nbt.setInteger("blockId", registry.getIdForBlock(state.getBlock()));
        nbt.setInteger("blockMeta", state.getBlock().getMetaFromState(state));
    }

    protected void writeRequirementsToNBT(NBTTagCompound nbt, MappingRegistry registry) {
        if (storedRequirements.length > 0) {
            NBTTagList rq = new NBTTagList();

            for (ItemStack stack : storedRequirements) {
                if (stack == null || stack.getItem() == null) throw new IllegalStateException("Found a null requirement! " + getClass());
                NBTTagCompound sub = new NBTTagCompound();
                stack.writeToNBT(sub);
                rq.appendTag(sub);
            }

            nbt.setTag("rq", rq);
        }
    }

    protected ItemStack getItemStack(IBlockState state, int quantity) {
        return new ItemStack(state.getBlock(), quantity, state.getBlock().damageDropped(state));
    }

    protected ItemStack getItemStack(IBlockState state) {
        return getItemStack(state, 1);
    }

    // Pretty much all blocks (that rotate) rotate this way now
    @Override
    public void rotateLeft(IBuilderContext context) {
        IProperty<EnumFacing> facingProp = getFacingProp();
        if (facingProp != null) {
            EnumFacing face = state.getValue(facingProp);
            if (face.getAxis() == Axis.Y) return;
            state = state.withProperty(facingProp, face.rotateY());
        }
    }

    protected IProperty<EnumFacing> getFacingProp() {
        Collection<IProperty> props = state.getPropertyNames();
        for (IProperty prop : props) {
            if ("facing".equals(prop.getName()) && state.getValue(prop) instanceof EnumFacing) {
                return prop;
            }
        }
        return null;
    }
}
