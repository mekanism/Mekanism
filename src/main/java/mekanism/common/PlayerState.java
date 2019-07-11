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

public class PlayerState {

    private Set<UUID> activeJetpacks = new HashSet<>();
    private Set<UUID> activeGasmasks = new HashSet<>();
    private Set<UUID> activeFlamethrowers = new HashSet<>();

    private World world;

    public void clear() {
        activeJetpacks.clear();
        activeGasmasks.clear();
        activeFlamethrowers.clear();
        SoundHandler.clearPlayerSounds();
    }

    public void clearPlayer(UUID uuid) {
        activeJetpacks.remove(uuid);
        activeGasmasks.remove(uuid);
        activeFlamethrowers.remove(uuid);
        SoundHandler.clearPlayerSounds(uuid);
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
                SoundHandler.startSound(world.getPlayerEntityByUUID(uuid), SoundType.JETPACK);
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
                SoundHandler.startSound(world.getPlayerEntityByUUID(uuid), SoundType.GAS_MASK);
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

        //TODO: Fix it not enabling the idle sound when changing to a flame thrower for the first time
        // The reason this happens is because alreadyActive is false, and isActive is false as well.
        // Realistically, we want to remove the isActive check below as a sound plays for idle as well,
        // and instead have this stuff run when a player is HOLDING a flame thrower. We do however, want
        // the activeFlamethrowers set to continue working as it does so this would require a bit larger
        // of a change and I am leaving it be for now as it is not a major bug.

        // If something changed and we're in a remote world, take appropriate action
        if (changed && world.isRemote) {
            // If the player is the "local" player, we need to tell the server the state has changed
            if (isLocal) {
                Mekanism.packetHandler.sendToServer(FlamethrowerDataMessage.UPDATE(uuid, isActive));
            }

            // Start a sound playing if the person is now using a flamethrower
            if (isActive && MekanismConfig.current().client.enablePlayerSounds.val()) {
                SoundHandler.startSound(world.getPlayerEntityByUUID(uuid), SoundType.FLAMETHROWER);
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