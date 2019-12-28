package mekanism.generators.common.tile.turbine;

import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.multiblock.TileEntityInternalMultiblock;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class TileEntityTurbineRotor extends TileEntityInternalMultiblock {

    // Blades on this rotor
    public int blades = 0;

    // Position of this rotor, relative to bottom
    private int position = -1;

    // Rendering helpers
    public float rotationLower;
    public float rotationUpper;

    public TileEntityTurbineRotor() {
        super(GeneratorsBlocks.TURBINE_ROTOR);
    }

    @Override
    public void onNeighborChange(Block block) {
        if (!isRemote()) {
            updateRotors();
        }
    }

    public void updateRotors() {
        // In order to render properly, each rotor has to know its position, relative to other contiguous rotors
        // along the Z axis. When a neighbor changes, rescan the rotors and figure out everyone's position
        // N.B. must be in bottom->top order.

        // Find the bottom-most rotor and start scan from there
        TileEntityTurbineRotor rotor = nextRotor(getPos().down());
        if (rotor != null) {
            rotor.updateRotors();
        } else {
            // This is the bottom-most rotor, so start scan up
            scanRotors(0);
        }
    }

    private void scanRotors(int index) {
        if (index != position) {
            // Our position has changed, update and generate an update packet for client
            position = index;
            sendUpdatePacket();
        }

        // Pass the scan along to next rotor up, along with their new index
        TileEntityTurbineRotor rotor = nextRotor(getPos().up());
        if (rotor != null) {
            rotor.scanRotors(index + 1);
        }
    }


    public boolean addBlade() {
        // If the the rotor beneath has less than two blades, add to it
        TileEntityTurbineRotor next = nextRotor(getPos().down());
        if (next != null && next.blades < 2) {
            return next.addBlade();
        } else if (blades < 2) {
            // Add the blades to this rotor
            blades++;
            // Update client state
            sendUpdatePacket();
            return true;
        }

        // This rotor and the rotor below are full up; pass the call
        // on up to the next rotor in stack
        next = nextRotor(getPos().up());
        if (next != null) {
            return next.addBlade();
        }
        return false;
    }

    public boolean removeBlade() {
        // If the the rotor above has any blades, remove them first
        TileEntityTurbineRotor next = nextRotor(getPos().up());
        if (next != null && next.blades > 0) {
            return next.removeBlade();
        } else if (blades > 0) {
            // Remove blades from this rotor
            blades--;

            // Update client state
            sendUpdatePacket();
            return true;
        }

        // This rotor and the rotor above are empty; pass the call
        // on up to the next rotor in stack
        next = nextRotor(getPos().down());
        if (next != null) {
            return next.removeBlade();
        }
        return false;
    }


    public int getHousedBlades() {
        return blades;
    }

    public int getPosition() {
        return position;
    }

    private TileEntityTurbineRotor nextRotor(BlockPos pos) {
        return MekanismUtils.getTileEntity(TileEntityTurbineRotor.class, getWorld(), pos);
    }

    private void sendUpdatePacket() {
        Mekanism.packetHandler.sendUpdatePacket(this);
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (isRemote()) {
            int prevBlades = blades;
            int prevPosition = position;
            blades = dataStream.readInt();
            position = dataStream.readInt();

            if (prevBlades != blades || prevPosition != prevBlades) {
                rotationLower = 0;
                rotationUpper = 0;
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(blades);
        data.add(position);
        return data;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        blades = nbtTags.getInt("blades");
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt("blades", getHousedBlades());
        return nbtTags;
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public void onUpdate() {
    }

    @Override
    public void setMultiblock(String id) {
        // Override the multiblock setter so that we can be sure to relay the ID down to the client; otherwise,
        // the rendering won't work properly
        super.setMultiblock(id);
        sendUpdatePacket();
    }
}