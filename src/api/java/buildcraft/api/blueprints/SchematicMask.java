/** Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.blueprints;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import buildcraft.api.core.BuildCraftAPI;

public class SchematicMask extends SchematicBlockBase {
    /** True of the block should be a solid block (stone, grass or a furnace) and false if it shouldn't (flowers, air or
     * vines) */
    public boolean isConcrete = true;

    public SchematicMask() {

    }

    public SchematicMask(boolean isConcrete) {
        this.isConcrete = isConcrete;
    }

    @Override
    public void placeInWorld(IBuilderContext context, BlockPos pos, List<ItemStack> stacks) {
        if (isConcrete) {
            if (stacks.size() == 0 || !BuildCraftAPI.isSoftBlock(context.world(), pos)) {
                return;
            } else {
                ItemStack stack = stacks.get(0);
                EntityPlayer player = BuildCraftAPI.fakePlayerProvider.getBuildCraftPlayer((WorldServer) context.world()).get();

                // force the block to be air block, in case it's just a soft
                // block which replacement is not straightforward
                context.world().setBlockToAir(pos);

                // Find nearest solid surface to place on
                EnumFacing solidFace = null;
                for (EnumFacing face : EnumFacing.values()) {
                    BlockPos offset = pos.offset(face);
                    if (!BuildCraftAPI.isSoftBlock(context.world(), offset)) {
                        solidFace = face;
                        break;
                    }
                }
                ItemBlock itemBlock = (ItemBlock) stack.getItem();
                IBlockState state = itemBlock.block.onBlockPlaced(context.world(), pos, solidFace, 0, 0, 0, stack.getMetadata(), player);
                itemBlock.placeBlockAt(stack, player, context.world(), pos, solidFace, 0, 0, 0, state);

            }
        } else {
            context.world().setBlockToAir(pos);
        }
    }

    @Override
    public boolean isAlreadyBuilt(IBuilderContext context, BlockPos pos) {
        if (isConcrete) {
            return !BuildCraftAPI.getWorldProperty("replaceable").get(context.world(), pos);
        } else {
            return BuildCraftAPI.getWorldProperty("replaceable").get(context.world(), pos);
        }
    }

    @Override
    public void writeSchematicToNBT(NBTTagCompound nbt, MappingRegistry registry) {
        nbt.setBoolean("isConcrete", isConcrete);
    }

    @Override
    public void readSchematicFromNBT(NBTTagCompound nbt, MappingRegistry registry) {
        isConcrete = nbt.getBoolean("isConcrete");
    }
}
