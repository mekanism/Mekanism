package mekanism.common;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import mekanism.client.sound.SoundHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.PacketFlamethrowerData.FlamethrowerDataMessage;
import mekanism.common.network.PacketFlamethrowerData.FlamethrowerPacket;
import mekanism.common.network.PacketJetpackData;
import mekanism.common.network.PacketScubaTankData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class PlayerState {

    private Set<UUID> activeJetpacks = new HashSet<>();
    private Set<UUID> activeGasmasks = new HashSet<>();
    private Set<UUID> activeFlamethrowers = new HashSet<>();

    private World world;

    public void clear() {
        activeJetpacks.clear();
        activeGasmasks.clear();
        activeFlamethrowers.clear();
    }

    public void clearPlayer(EntityPlayer p) {
        activeJetpacks.remove(p.getUniqueID());
        activeGasmasks.remove(p.getUniqueID());
        activeFlamethrowers.remove(p.getUniqueID());
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
                Mekanism.packetHandler.sendToServer(PacketJetpackData.JetpackDataMessage.UPDATE(uuid, isActive));
            }

            // Start a sound playing if the person is now flying
            if (isActive && MekanismConfig.current().client.enablePlayerSounds.val()) {
                SoundHandler.startSound(world.getPlayerEntityByUUID(uuid), "jetpack");
            }
        }
    }

    public boolean isJetpackOn(EntityPlayer p) {
        return activeJetpacks.contains(p.getName());
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
                Mekanism.packetHandler
                      .sendToServer(PacketScubaTankData.ScubaTankDataMessage.UPDATE(uuid, isActive));
            }

            // Start a sound playing if the person is now using a gasmask
            if (isActive && MekanismConfig.current().client.enablePlayerSounds.val()) {
                SoundHandler.startSound(world.getPlayerEntityByUUID(uuid), "gasmask");
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
        boolean alreadyActive = activeFlamethrowers.contains(uuid);
        boolean changed = alreadyActive != isActive;

        if (alreadyActive && !isActive) {
            activeFlamethrowers.remove(uuid); // On -> off
        } else if (!alreadyActive && isActive) {
            activeFlamethrowers.add(uuid); // Off -> on
        }

        // If something changed and we're in a remote world, take appropriate action
        if (changed && world.isRemote) {
            // If the player is the "local" player, we need to tell the server the state has changed
            if (isLocal) {
                Mekanism.packetHandler.sendToServer(new FlamethrowerDataMessage(FlamethrowerPacket.UPDATE, null,
                      uuid, isActive));
            }

            // Start a sound playing if the person is now using a flamethrower
            if (isActive && MekanismConfig.current().client.enablePlayerSounds.val()) {
                SoundHandler.startSound(world.getPlayerEntityByUUID(uuid), "flamethrower");
            }
        }
    }

    public boolean isFlamethrowerOn(EntityPlayer p) {
        return activeFlamethrowers.contains(p.getUniqueID());
    }

}
