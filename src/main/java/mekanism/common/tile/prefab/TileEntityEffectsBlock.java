package mekanism.common.tile.prefab;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.base.TileNetworkList;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityEffectsBlock extends TileEntityElectricBlock implements IActiveState {

    private SoundEvent soundEvent;

    @SideOnly(Side.CLIENT)
    private ISound activeSound;
    private int playSoundCooldown = 0;

    protected boolean isActive;
    private long lastActive = -1;

    // Number of ticks that the block can be inactive before it's considered not recently active
    private final int RECENT_THRESHOLD = 100;


    /**
     * The base of all blocks that deal with electricity, make noise and potential generate ambient lighting
     *
     * @param sound - the sound path of this block
     * @param name - full name of this block
     * @param maxEnergy - how much energy this block can store
     */
    public TileEntityEffectsBlock(String sound, String name, double maxEnergy) {
        super(name, maxEnergy);

        // TODO: Have subclasses pass in a static SoundEvent so we avoid per-instance # of SoundEvents for same sound
        soundEvent = new SoundEvent(new ResourceLocation(Mekanism.MODID, "tile." + sound));
    }


    protected float getInitialVolume() {
        return 1.0f;
    }


    // Protected way for subclasses to swap out a sound
    @SideOnly(Side.CLIENT)
    protected void setSoundEvent(SoundEvent event) {
        this.soundEvent = event;

        // Stop the active sound if it's playing, since underlying sound might be changing
        SoundHandler.stopTileSound(getPos());
    }

    @SideOnly(Side.CLIENT)
    private void updateSound() {
        // If machine sounds are disabled, noop
        if (!MekanismConfig.client.enableMachineSounds) {
            return;
        }

        if (getActive() && !isInvalid()) {
            // If sounds are being muted, we can attempt to start them on every tick, only to have them
            // denied by the event bus, so use a cooldown period that ensures we're only trying once every
            // second or so to start a sound.
            if (--playSoundCooldown > 0) {
                return;
            }

            if (activeSound == null || !Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(activeSound)) {
                activeSound = SoundHandler.startTileSound(soundEvent.getSoundName(), getInitialVolume(), getPos());
                playSoundCooldown = 20;
            }
        } else {
            if (activeSound != null) {
                SoundHandler.stopTileSound(getPos());
                activeSound = null;
                playSoundCooldown = 0;
            }
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if (world.isRemote) {
            updateSound();
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (world.isRemote) {
            updateSound();
        }

        if (world.isRemote && !isActive && lastActive > 0) {
            long updateDiff = world.getTotalWorldTime() - lastActive;
            if (updateDiff > RECENT_THRESHOLD) {
                MekanismUtils.updateBlock(world, getPos());
                lastActive = -1;
            }
        }
    }

    @Override
    public boolean getActive() {
        return isActive;
    }

    @Override
    public void setActive(boolean active) {
        boolean stateChange = (isActive != active);

        if (stateChange) {
            isActive = active;
            Mekanism.packetHandler
                  .sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())),
                        new Range4D(Coord4D.get(this)));
        }
    }

    public boolean wasActiveRecently() {
        // If the machine is currently active or it flipped off within our threshold,
        // we'll consider it recently active.
        return isActive || (lastActive > 0 && (world.getTotalWorldTime() - lastActive) < RECENT_THRESHOLD);
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            boolean newActive = dataStream.readBoolean();

            boolean stateChange = (newActive != isActive);
            isActive = newActive;

            if (stateChange && !isActive) {
                // Switched off; note the time
                lastActive = world.getTotalWorldTime();
            } else if (stateChange && isActive) {
                // Switching on; if lastActive is not currently set, trigger a lighting update
                // and make sure lastActive is clear
                if (lastActive == -1) {
                    MekanismUtils.updateBlock(world, getPos());
                }
                lastActive = -1;
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);

        data.add(isActive);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);
        isActive = nbtTags.getBoolean("isActive");
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        nbtTags.setBoolean("isActive", isActive);

        return nbtTags;
    }
}
