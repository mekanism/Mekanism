package mekanism.common.tile.base;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.IMekWrench;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.base.ITileComponent;
import mekanism.common.base.ITileNetwork;
import mekanism.common.block.interfaces.IBlockDisableable;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.frequency.IFrequencyHandler;
import mekanism.common.integration.wrenches.Wrenches;
import mekanism.common.network.PacketDataRequest.DataRequestMessage;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.security.ISecurityTile;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

//TODO: Is the IWrenchable needed, seems unused
//@Interface(iface = "ic2.api.tile.IWrenchable", modid = MekanismHooks.IC2_MOD_ID)
public abstract class TileEntityMekanism extends TileEntity implements ITileNetwork, IFrequencyHandler, ITickable {

    /**
     * The players currently using this block.
     */
    public Set<EntityPlayer> playersUsing = new HashSet<>();

    /**
     * A timer used to send packets to clients.
     */
    public int ticker;

    public boolean redstone = false;
    public boolean redstoneLastTick = false;

    public boolean doAutoSync = true;

    private List<ITileComponent> components = new ArrayList<>();

    public void addComponent(ITileComponent component) {
        components.add(component);
    }

    public List<ITileComponent> getComponents() {
        return components;
    }

    public WrenchResult tryWrench(IBlockState state, EntityPlayer player, EnumHand hand, Supplier<RayTraceResult> rayTraceSupplier) {
        ItemStack stack = player.getHeldItem(hand);
        if (!stack.isEmpty()) {
            IMekWrench wrenchHandler = Wrenches.getHandler(stack);
            if (wrenchHandler != null) {
                RayTraceResult raytrace = rayTraceSupplier.get();
                if (wrenchHandler.canUseWrench(player, hand, stack, raytrace)) {
                    if (!SecurityUtils.canAccess(player, this)) {
                        SecurityUtils.displayNoAccess(player);
                        return WrenchResult.NO_SECURITY;
                    }
                    wrenchHandler.wrenchUsed(player, hand, stack, raytrace);
                    if (player.isSneaking()) {
                        MekanismUtils.dismantleBlock(getBlockType(), state, world, pos);
                        return WrenchResult.DISMANTLED;
                    }
                    return WrenchResult.SUCCESS;
                }
            }
        }
        return WrenchResult.PASS;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (world.isRemote) {
            Mekanism.packetHandler.sendToServer(new DataRequestMessage(Coord4D.get(this)));
        }
    }

    @Override
    public void update() {
        if (!world.isRemote && MekanismConfig.current().general.destroyDisabledBlocks.val()) {
            Block block = getBlockType();
            if (block instanceof IBlockDisableable && !((IBlockDisableable) block).isEnabled()) {
                //TODO: Better way of doing name?
                Mekanism.logger.info("Destroying machine of type '" + block.getClass().getSimpleName() + "' at coords " + Coord4D.get(this) + " as according to config.");
                world.setBlockToAir(getPos());
                return;
            }
        }

        for (ITileComponent component : components) {
            component.tick();
        }

        onUpdate();
        if (!world.isRemote) {
            if (doAutoSync && playersUsing.size() > 0) {
                TileEntityMessage updateMessage = new TileEntityMessage(this);
                for (EntityPlayer player : playersUsing) {
                    Mekanism.packetHandler.sendTo(updateMessage, (EntityPlayerMP) player);
                }
            }
        }
        ticker++;
        redstoneLastTick = redstone;
    }

    @Override
    public void updateContainingBlockInfo() {
        super.updateContainingBlockInfo();
        onAdded();
    }

    public void open(EntityPlayer player) {
        playersUsing.add(player);
    }

    public void close(EntityPlayer player) {
        playersUsing.remove(player);
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            redstone = dataStream.readBoolean();
            for (ITileComponent component : components) {
                component.read(dataStream);
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        data.add(redstone);
        for (ITileComponent component : components) {
            component.write(data);
        }
        return data;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        for (ITileComponent component : components) {
            component.invalidate();
        }
    }

    @Override
    public void validate() {
        super.validate();
        if (world.isRemote) {
            Mekanism.packetHandler.sendToServer(new DataRequestMessage(Coord4D.get(this)));
        }
    }

    /**
     * Update call for machines. Use instead of updateEntity -- it's called every tick.
     */
    public abstract void onUpdate();

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);
        redstone = nbtTags.getBoolean("redstone");
        for (ITileComponent component : components) {
            component.read(nbtTags);
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);
        nbtTags.setBoolean("redstone", redstone);
        for (ITileComponent component : components) {
            component.write(nbtTags);
        }
        return nbtTags;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return capability == Capabilities.TILE_NETWORK_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (capability == Capabilities.TILE_NETWORK_CAPABILITY) {
            return Capabilities.TILE_NETWORK_CAPABILITY.cast(this);
        }
        return super.getCapability(capability, facing);
    }

    public boolean isPowered() {
        return redstone;
    }

    public boolean wasPowered() {
        return redstoneLastTick;
    }

    public void onPowerChange() {
    }

    public void onNeighborChange(Block block) {
        if (!world.isRemote) {
            updatePower();
        }
    }

    private void updatePower() {
        boolean power = world.isBlockPowered(getPos());
        if (redstone != power) {
            redstone = power;
            Mekanism.packetHandler.sendUpdatePacket(this);
            onPowerChange();
        }
    }

    /**
     * Called when block is placed in world
     */
    public void onAdded() {
        updatePower();
    }

    @Override
    public Frequency getFrequency(FrequencyManager manager) {
        //TODO: I don't think this is needed, only thing that uses this method is querying the quantum entangloporter
        if (manager == Mekanism.securityFrequencies && this instanceof ISecurityTile) {
            return ((ISecurityTile) this).getSecurity().getFrequency();
        }
        return null;
    }

    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag() {
        // Forge writes only x/y/z/id info to a new NBT Tag Compound. This is fine, we have a custom network system
        // to send other data so we don't use this one (yet).
        return super.getUpdateTag();
    }

    @Override
    public void handleUpdateTag(@Nonnull NBTTagCompound tag) {
        // The super implementation of handleUpdateTag is to call this readFromNBT. But, the given TagCompound
        // only has x/y/z/id data, so our readFromNBT will set a bunch of default values which are wrong.
        // So simply call the super's readFromNBT, to let Forge do whatever it wants, but don't treat this like
        // a full NBT object, don't pass it to our custom read methods.
        super.readFromNBT(tag);
    }
}