package mekanism.generators.common.tile.turbine;

import mekanism.api.SerializationConstants;
import mekanism.common.tile.prefab.TileEntityInternalMultiblock;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityTurbineRotor extends TileEntityInternalMultiblock implements Clearable {

    // Blades on this rotor
    public int blades = 0;

    // Position of this rotor, relative to bottom
    private int position = -1;
    //Rough radius of blades
    private int radius = -1;

    // Rendering helpers
    public float rotationLower;
    public float rotationUpper;

    public TileEntityTurbineRotor(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.TURBINE_ROTOR, pos, state);
    }

    @Override
    public void onNeighborChange(Block block, BlockPos neighborPos) {
        super.onNeighborChange(block, neighborPos);
        if (!isRemote()) {
            updateRotors();
        }
    }

    public void updateRotors() {
        // In order to render properly, each rotor has to know its position, relative to other contiguous rotors
        // along the Y axis. When a neighbor changes, rescan the rotors and figure out everyone's position
        // N.B. must be in bottom->top order.

        // Find the bottom-most rotor and start scan from there
        TileEntityTurbineRotor rotor = getRotor(getBlockPos().below());
        if (rotor == null) {
            // This is the bottom-most rotor, so start scan up
            scanRotors(0);
        } else {
            rotor.updateRotors();
        }
    }

    private void scanRotors(int index) {
        if (index != position) {
            // Our position has changed, update and generate an update packet for client
            position = index;
            updateRadius();
            if (blades > 0) {
                //Only send an update packet to the client if we actually have some blades installed
                // otherwise we don't bother updating the client on what position we are at as they do not
                // actually need it for rendering, and may not even have the tile placed yet
                sendUpdatePacket();
            }
        }

        // Pass the scan along to next rotor up, along with their new index
        TileEntityTurbineRotor rotor = getRotor(getBlockPos().above());
        if (rotor != null) {
            rotor.scanRotors(index + 1);
        }
    }

    public boolean addBlade(boolean checkBelow) {
        if (checkBelow) {
            //If we want to check rotors that are below (aka we aren't being called by them)
            // and if the rotor beneath has less than two blades, add to it
            TileEntityTurbineRotor previous = getRotor(getBlockPos().below());
            if (previous != null && previous.blades < 2) {
                return previous.addBlade(true);
            }
        }
        if (blades < 2) {
            // Add the blades to this rotor
            blades++;
            if (position == -1) {
                //If we haven't gotten a position assigned yet (single rotor height) then rescan it to set things to the correct values
                // This will also handle sending the update to the client
                scanRotors(0);
            } else {
                if (hasFormedMultiblock() && getMultiblock() instanceof TurbineMultiblockData multiblock) {
                    //If for some reason someone is modifying the number of blades on a running turbine (which is unsupported),
                    // update the turbine's blade count as best we can
                    multiblock.blades++;
                }
                // Update client state
                sendUpdatePacket();
            }
            return true;
        }

        // This rotor and the rotor below are full up; pass the call
        // on up to the next rotor in stack
        TileEntityTurbineRotor next = getRotor(getBlockPos().above());
        return next != null && next.addBlade(false);
    }

    public boolean removeBlade() {
        // If the rotor above has any blades, remove them first
        TileEntityTurbineRotor next = getRotor(getBlockPos().above());
        if (next != null && next.blades > 0) {
            return next.removeBlade();
        } else if (blades > 0) {
            // Remove blades from this rotor
            blades--;
            if (hasFormedMultiblock() && getMultiblock() instanceof TurbineMultiblockData multiblock) {
                //If for some reason someone is modifying the number of blades on a running turbine (which is unsupported),
                // update the turbine's blade count as best we can
                multiblock.blades--;
            }

            // Update client state
            sendUpdatePacket();
            return true;
        }

        // This rotor and the rotor above are empty; pass the call
        // on up to the next rotor in stack
        next = getRotor(getBlockPos().below());
        return next != null && next.removeBlade();
    }

    @Override
    public void clearContent() {
        blades = 0;
    }

    public int getHousedBlades() {
        return blades;
    }

    public int getPosition() {
        return position;
    }

    private void updateRadius() {
        radius = 1 + position / 4;
    }

    public int getRadius() {
        return radius;
    }

    @Nullable
    private TileEntityTurbineRotor getRotor(BlockPos pos) {
        return WorldUtils.getTileEntity(TileEntityTurbineRotor.class, getLevel(), pos);
    }

    @Override
    public void blockRemoved() {
        super.blockRemoved();
        if (!isRemote()) {
            int amount = getHousedBlades();
            if (amount > 0) {
                Block.popResource(level, worldPosition, GeneratorsItems.TURBINE_BLADE.asStack(amount));
            }
        }
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        blades = nbt.getInt(SerializationConstants.BLADES);
        position = nbt.getInt(SerializationConstants.POSITION);
        updateRadius();
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbtTags, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(nbtTags, provider);
        nbtTags.putInt(SerializationConstants.BLADES, getHousedBlades());
        nbtTags.putInt(SerializationConstants.POSITION, getPosition());
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider) {
        CompoundTag updateTag = super.getReducedUpdateTag(provider);
        updateTag.putInt(SerializationConstants.BLADES, blades);
        updateTag.putInt(SerializationConstants.POSITION, position);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        int prevBlades = blades;
        int prevPosition = position;
        NBTUtils.setIntIfPresent(tag, SerializationConstants.BLADES, value -> blades = value);
        NBTUtils.setIntIfPresent(tag, SerializationConstants.POSITION, value -> {
            position = value;
            updateRadius();
        });
        if (prevBlades != blades || prevPosition != prevBlades) {
            rotationLower = 0;
            rotationUpper = 0;
        }
    }
}