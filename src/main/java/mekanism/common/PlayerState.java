package mekanism.common;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import mekanism.client.sound.PlayerSound.SoundType;
import mekanism.client.sound.SoundHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.PacketFlamethrowerData.FlamethrowerDataMessage;
import mekanism.common.network.PacketJetpackData.JetpackDataMessage;
import mekanism.common.network.PacketScubaTankData.ScubaTankDataMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class PlayerState {

    private Set<UUID> activeJetpacks = new HashSet<>();
    private Set<UUID> activeGasmasks = new HashSet<>();
    private Set<UUID> activeFlamethrowers = new HashSet<>();

    private World world;

    public void clear() {
        activeJetpacks.clear();
        activeGasmasks.clear();
        activeFlamethrowers.clear();
        if (FMLCommonHandler.instance().getSide().isClient()) {
            SoundHandler.clearPlayerSounds();
        }
    }

    public void clearPlayer(UUID uuid) {
        activeJetpacks.remove(uuid);
        activeGasmasks.remove(uuid);
        activeFlamethrowers.remove(uuid);
        if (FMLCommonHandler.instance().getSide().isClient()) {
            SoundHandler.clearPlayerSounds(uuid);
        }
    }

    public void init(World world) {
        this.world = world;
    }

    // ----------------------
    //
    // Jetpack state tracking
    //
    // ----------------------

    public void setJetpackState(UUID uuid, boolean isActive, boolean isLocal) {
        boolean alreadyActive = activeJetpacks.contains(uuid);
        boolean changed = alreadyActive != isActive;
        if (alreadyActive && !isActive) {
            // On -> off
            activeJetpacks.remove(uuid);
        } else if (!alreadyActive && isActive) {
            // Off -> on
            activeJetpacks.add(uuid);
        }

        // If something changed and we're in a remote world, take appropriate action
        if (changed && world.isRemote) {
            // If the player is the "local" player, we need to tell the server the state has changed
            if (isLocal) {
                Mekanism.packetHandler.sendToServer(JetpackDataMessage.UPDATE(uuid, isActive));
            }

            // Start a sound playing if the person is now flying
            if (isActive && MekanismConfig.current().client.enablePlayerSounds.val()) {
                SoundHandler.startSound(world, uuid, SoundType.JETPACK);
            }
        }
    }

    public boolean isJetpackOn(EntityPlayer p) {
        return activeJetpacks.contains(p.getUniqueID());
    }

    public Set<UUID> getActiveJetpacks() {
        return activeJetpacks;
    }

    public void setActiveJetpacks(Set<UUID> newActiveJetpacks) {
        for (UUID activeUser : newActiveJetpacks) {
            setJetpackState(activeUser, true, false);
        }
    }

    // ----------------------
    //
    // Gasmask state tracking
    //
    // ----------------------

    public void setGasmaskState(UUID uuid, boolean isActive, boolean isLocal) {
        boolean alreadyActive = activeGasmasks.contains(uuid);
        boolean changed = alreadyActive != isActive;
        if (alreadyActive && !isActive) {
            activeGasmasks.remove(uuid); // On -> off
        } else if (!alreadyActive && isActive) {
            activeGasmasks.add(uuid); // Off -> on
        }

        // If something changed and we're in a remote world, take appropriate action
        if (changed && world.isRemote) {
            // If the player is the "local" player, we need to tell the server the state has changed
            if (isLocal) {
                Mekanism.packetHandler.sendToServer(ScubaTankDataMessage.UPDATE(uuid, isActive));
            }

            // Start a sound playing if the person is now using a gasmask
            if (isActive && MekanismConfig.current().client.enablePlayerSounds.val()) {
                SoundHandler.startSound(world, uuid, SoundType.GAS_MASK);
            }
        }
    }

    public boolean isGasmaskOn(EntityPlayer p) {
        return activeGasmasks.contains(p.getUniqueID());
    }

    public Set<UUID> getActiveGasmasks() {
        return activeGasmasks;
    }

    public void setActiveGasmasks(Set<UUID> newActiveGasmasks) {
        for (UUID activeUser : newActiveGasmasks) {
            setGasmaskState(activeUser, true, false);
        }
    }

    // ----------------------
    //
    // Flamethrower state tracking
    //
    // ----------------------

    public void setFlamethrowerState(UUID uuid, boolean isActive, boolean isLocal) {
        setFlamethrowerState(uuid, isActive, isActive, isLocal);
    }

    public void setFlamethrowerState(UUID uuid, boolean hasFlameThrower, boolean isActive, boolean isLocal) {
        boolean alreadyActive = activeFlamethrowers.contains(uuid);
        boolean changed = alreadyActive != isActive;
        if (alreadyActive && !isActive) {
            activeFlamethrowers.remove(uuid); // On -> off
        } else if (!alreadyActive && isActive) {
            activeFlamethrowers.add(uuid); // Off -> on
        }

        if (world.isRemote) {
            boolean startSound;
            // If something changed and we're in a remote world, take appropriate action
            if (changed) {
                // If the player is the "local" player, we need to tell the server the state has changed
                if (isLocal) {
                    Mekanism.packetHandler.sendToServer(FlamethrowerDataMessage.UPDATE(uuid, isActive));
                }

                // Start a sound playing if the person is now using a flamethrower
                startSound = isActive;
            } else {
                //Start the sound if it isn't already active, and still isn't, but has a flame thrower
                // This allows us to catch and start playing the idle sound
                //TODO: Currently this only happens for the local player as "having" a flame thrower is not
                // synced from server to client. This is not that big a deal, though may be something we want
                // to look into eventually
                startSound = !isActive && hasFlameThrower;
                //Note: If they just continue to hold (but not use) a flame thrower it "will" continue having this
                // attempt to start the sound. This is not a major deal as the uuid gets checked before attempting
                // to retrieve the player or actually creating a new sound object.
            }
            if (startSound && MekanismConfig.current().client.enablePlayerSounds.val()) {
                SoundHandler.startSound(world, uuid, SoundType.FLAMETHROWER);
            }
        }
    }

    public boolean isFlamethrowerOn(EntityPlayer p) {
        return activeFlamethrowers.contains(p.getUniqueID());
    }

    public Set<UUID> getActiveFlamethrowers() {
        return activeFlamethrowers;
    }

    public void setActiveFlamethrowers(Set<UUID> newActiveFlamethrowers) {
        for (UUID activeUser : newActiveFlamethrowers) {
            setFlamethrowerState(activeUser, true, false);
        }
    }

}