/** Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.blueprints;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

public class SchematicTile extends SchematicBlock {

    /** This tree contains additional data to be stored in the blueprint. By default, it will be initialized from
     * Schematic.readFromWord with the standard readNBT function of the corresponding tile (if any) and will be loaded
     * from BptBlock.writeToWorld using the standard writeNBT function. */
    public NBTTagCompound tileNBT = new NBTTagCompound();

    @Override
    public void idsToBlueprint(MappingRegistry registry) {}

    @Override
    public void idsToWorld(MappingRegistry registry) {
        try {
            registry.scanAndTranslateStacksToWorld(tileNBT);
        } catch (MappingNotFoundException e) {
            tileNBT = new NBTTagCompound();
        }
    }

    public void onNBTLoaded() {

    }

    /** Places the block in the world, at the location specified in the slot. */
    @Override
    public void placeInWorld(IBuilderContext context, BlockPos pos, NonNullList<ItemStack> stacks) {
        super.placeInWorld(context, pos, stacks);

        if (state.getBlock().hasTileEntity(state)) {
            tileNBT.setInteger("x", pos.getX());
            tileNBT.setInteger("y", pos.getY());
            tileNBT.setInteger("z", pos.getZ());
            TileEntity tile = TileEntity.create(context.world(), tileNBT);
            if (tile != null) {
                tile.setWorld(context.world());
                context.world().setTileEntity(pos, tile);
            }
        }
    }

    @Override
    public void initializeFromObjectAt(IBuilderContext context, BlockPos pos) {
        super.initializeFromObjectAt(context, pos);

        if (state.getBlock().hasTileEntity(state)) {
            TileEntity tile = context.world().getTileEntity(pos);

            if (tile != null) {
                tile.writeToNBT(tileNBT);
            }

            tileNBT = tileNBT.copy();
            onNBTLoaded();
        }
    }

    @Override
    public void storeRequirements(IBuilderContext context, BlockPos pos) {
        super.storeRequirements(context, pos);

        if (state.getBlock().hasTileEntity(state)) {
            TileEntity tile = context.world().getTileEntity(pos);

            if (tile instanceof IInventory) {
                IInventory inv = (IInventory) tile;

                for (int i = 0; i < inv.getSizeInventory(); ++i) {
                    ItemStack stack = inv.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        storedRequirements.add(stack);
                    }
                }
            }
        }
    }

    @Override
    public void writeSchematicToNBT(NBTTagCompound nbt, MappingRegistry registry) {
        super.writeSchematicToNBT(nbt, registry);

        nbt.setTag("blockCpt", tileNBT);
    }

    @Override
    public void readSchematicFromNBT(NBTTagCompound nbt, MappingRegistry registry) {
        super.readSchematicFromNBT(nbt, registry);

        tileNBT = nbt.getCompoundTag("blockCpt");
        onNBTLoaded();
    }

    @Override
    public int buildTime() {
        return 5;
    }
}
