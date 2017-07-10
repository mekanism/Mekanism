package buildcraft.api.transport.pluggable;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;

public abstract class PipePluggable {
    public final PluggableDefinition definition;
    public final IPipeHolder holder;
    public final EnumFacing side;

    public PipePluggable(PluggableDefinition definition, IPipeHolder holder, EnumFacing side) {
        this.definition = definition;
        this.holder = holder;
        this.side = side;
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();
        return nbt;
    }

    /** Writes the payload that will be passed into
     * {@link PluggableDefinition#loadFromBuffer(IPipeHolder, EnumFacing, PacketBuffer)} on the client. (This is called
     * on the server and sent to the client). Note that this will be called *instead* of write and read payload. */
    public void writeCreationPayload(PacketBuffer buffer) {

    }

    public void writePayload(PacketBuffer buffer, Side side) {

    }

    public void readPayload(PacketBuffer buffer, Side side, MessageContext ctx) throws IOException {

    }

    public final void scheduleNetworkUpdate() {
        holder.scheduleNetworkUpdate(PipeMessageReceiver.PLUGGABLES[side.ordinal()]);
    }

    public void onTick() {}

    /** @return A bounding box that will be used for collisions and raytracing. */
    public abstract AxisAlignedBB getBoundingBox();

    /** @return True if the pipe cannot connect outwards (it is blocked), or False if this does not block the pipe. */
    public boolean isBlocking() {
        return false;
    }

    /** Gets the value of a specified capability key, or null if the given capability is not supported at the call time.
     * This is effectively {@link ICapabilityProvider}, but where
     * {@link ICapabilityProvider#hasCapability(Capability, EnumFacing)} will return true when this returns a non-null
     * value. */
    public <T> T getCapability(@Nonnull Capability<T> cap) {
        return null;
    }

    /** Gets the {@link Capability} that is accessible from the pipe that this is attached to.
     * 
     * @param cap
     * @return */
    public <T> T getInternalCapability(@Nonnull Capability<T> cap) {
        return null;
    }

    /** Called whenever this pluggable is removed from the pipe. */
    public void onRemove() {}

    /** @param toDrop A list containing all the items to drop (so you should add your items to this list) * */
    public void getDrops(NonNullList<ItemStack> toDrop) {
        ItemStack stack = getPickStack();
        if (!stack.isEmpty()) {
            toDrop.add(stack);
        }
    }

    /** Called whenever this pluggable is picked by the player (similar to Block.getPickBlock)
     * 
     * @return The stack that should be picked, or ItemStack.EMPTY if no stack can be picked from this pluggable. */
    public ItemStack getPickStack() {
        return ItemStack.EMPTY;
    }

    public boolean onPluggableActivate(EntityPlayer player, RayTraceResult trace, float hitX, float hitY, float hitZ) {
        return false;
    }

    @Nullable
    public PluggableModelKey getModelRenderKey(BlockRenderLayer layer) {
        return null;
    }

    /** Called if the {@link IPluggableStaticBaker} returns quads with tint indexes set to
     * <code>data * 6 + key.side.ordinal()</code>. <code>"data"</code> is passed in here as <code>"tintIndex"</code>.
     * 
     * @return The tint index to render the quad with, or -1 for default. */
    @SideOnly(Side.CLIENT)
    public int getBlockColor(int tintIndex) {
        return -1;
    }

    /** PipePluggable version of
     * {@link Block#canBeConnectedTo(net.minecraft.world.IBlockAccess, net.minecraft.util.math.BlockPos, EnumFacing)}. */
    public boolean canBeConnected() {
        return false;
    }

    /** PipePluggable version of
     * {@link Block#isBlockSolid(net.minecraft.world.IBlockAccess, net.minecraft.util.math.BlockPos, EnumFacing)} */
    public boolean isSideSolid() {
        return false;
    }

    /** PipePluggable version of {@link Block#getExplosionResistance(World, BlockPos, Entity, Explosion)} */
    public float getExplosionResistance(@Nullable Entity exploder, Explosion explosion) {
        return 0;
    }

    public boolean canConnectToRedstone(@Nullable EnumFacing to) {
        return false;
    }
}
