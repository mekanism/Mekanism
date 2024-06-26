package mekanism.common.base;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.UUID;
import mekanism.api.functions.ToFloatFunction;
import mekanism.client.sound.PlayerSound.SoundType;
import mekanism.client.sound.SoundHandler;
import mekanism.common.CommonPlayerTickHandler;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.player_data.PacketResetPlayerClient;
import mekanism.common.network.to_server.PacketGearStateUpdate;
import mekanism.common.network.to_server.PacketGearStateUpdate.GearType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.network.PacketDistributor;

public class PlayerState {

    private static final ResourceLocation STEP_ASSIST_MODIFIER_ID = Mekanism.rl("step_assist");

    private final Set<UUID> activeJetpacks = new ObjectOpenHashSet<>();
    private final Set<UUID> activeScubaMasks = new ObjectOpenHashSet<>();
    private final Set<UUID> activeGravitationalModulators = new ObjectOpenHashSet<>();

    private LevelAccessor world;

    public void clear(boolean isRemote) {
        activeJetpacks.clear();
        activeScubaMasks.clear();
        activeGravitationalModulators.clear();
        if (isRemote) {
            SoundHandler.clearPlayerSounds();
        }
    }

    public void clearPlayer(UUID uuid, boolean isRemote) {
        activeJetpacks.remove(uuid);
        activeScubaMasks.remove(uuid);
        activeGravitationalModulators.remove(uuid);
        if (isRemote) {
            SoundHandler.clearPlayerSounds(uuid);
            if (Minecraft.getInstance().player == null || Minecraft.getInstance().player.getUUID().equals(uuid)) {
                SoundHandler.radiationSoundMap.clear();
            }
        }
        RadiationManager.get().resetPlayer(uuid);
        if (!isRemote) {
            PacketDistributor.sendToAllPlayers(new PacketResetPlayerClient(uuid));
        }
    }

    public void init(LevelAccessor world) {
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

        // If something changed, and we're in a remote world, take appropriate action
        if (changed && world.isClientSide()) {
            // If the player is the "local" player, we need to tell the server the state has changed
            if (isLocal) {
                PacketUtils.sendToServer(new PacketGearStateUpdate(GearType.JETPACK, uuid, isActive));
            }

            // Start a sound playing if the person is now flying
            if (isActive && MekanismConfig.client.enablePlayerSounds.get()) {
                SoundHandler.startSound(world, uuid, SoundType.JETPACK);
            }
        }
    }

    public boolean isJetpackOn(Player p) {
        return activeJetpacks.contains(p.getUUID());
    }

    public Set<UUID> getActiveJetpacks() {
        return activeJetpacks;
    }

    // ----------------------
    //
    // Scuba Mask state tracking
    //
    // ----------------------

    public void setScubaMaskState(UUID uuid, boolean isActive, boolean isLocal) {
        boolean alreadyActive = activeScubaMasks.contains(uuid);
        boolean changed = alreadyActive != isActive;
        if (alreadyActive && !isActive) {
            activeScubaMasks.remove(uuid); // On -> off
        } else if (!alreadyActive && isActive) {
            activeScubaMasks.add(uuid); // Off -> on
        }

        // If something changed, and we're in a remote world, take appropriate action
        if (changed && world.isClientSide()) {
            // If the player is the "local" player, we need to tell the server the state has changed
            if (isLocal) {
                PacketUtils.sendToServer(new PacketGearStateUpdate(GearType.SCUBA_MASK, uuid, isActive));
            }

            // Start a sound playing if the person is now using a scuba mask
            if (isActive && MekanismConfig.client.enablePlayerSounds.get()) {
                SoundHandler.startSound(world, uuid, SoundType.SCUBA_MASK);
            }
        }
    }

    public boolean isScubaMaskOn(Player p) {
        return activeScubaMasks.contains(p.getUUID());
    }

    public Set<UUID> getActiveScubaMasks() {
        return activeScubaMasks;
    }

    // ----------------------
    //
    // Step assist state tracking
    //
    // ----------------------

    public void updateStepAssist(Player player) {
        updateAttribute(player, Attributes.STEP_HEIGHT, STEP_ASSIST_MODIFIER_ID,  CommonPlayerTickHandler::getStepBoost);
    }

    //Note: The attributes that currently use this cannot be converted to just being attributes on the items, as they can be disabled based on the player state
    private void updateAttribute(Player player, Holder<Attribute> attribute, ResourceLocation id, ToFloatFunction<Player> additionalSupplier) {
        AttributeInstance attributeInstance = player.getAttribute(attribute);
        if (attributeInstance != null) {
            AttributeModifier existing = attributeInstance.getModifier(id);
            float additional = additionalSupplier.applyAsFloat(player);
            if (existing != null) {
                if (existing.amount() == additional) {
                    //If we already have it set to the correct value just exit
                    //Note: We don't need to check for if it is equal to zero as we should never have the attribute applied then
                    return;
                }
                //Otherwise, remove the no longer valid value, so we can add it again properly
                attributeInstance.removeModifier(id);
            }
            if (additional > 0) {
                //If we should have the attribute, but we don't have it set yet, or our stored amount was different, update
                attributeInstance.addTransientModifier(new AttributeModifier(id, additional, Operation.ADD_VALUE));
            }
        }
    }

    // ----------------------
    //
    // Gravitational Modulator state tracking
    //
    // ----------------------

    public void setGravitationalModulationState(UUID uuid, boolean isActive, boolean isLocal) {
        boolean alreadyActive = activeGravitationalModulators.contains(uuid);
        boolean changed = alreadyActive != isActive;
        if (alreadyActive && !isActive) {
            activeGravitationalModulators.remove(uuid); // On -> off
        } else if (!alreadyActive && isActive) {
            activeGravitationalModulators.add(uuid); // Off -> on
        }

        // If something changed, and we're in a remote world, take appropriate action
        if (changed && world.isClientSide()) {
            // If the player is the "local" player, we need to tell the server the state has changed
            if (isLocal) {
                PacketUtils.sendToServer(new PacketGearStateUpdate(GearType.GRAVITATIONAL_MODULATOR, uuid, isActive));
            }

            // Start a sound playing if the person is now using a gravitational modulator
            if (isActive && MekanismConfig.client.enablePlayerSounds.get()) {
                SoundHandler.startSound(world, uuid, SoundType.GRAVITATIONAL_MODULATOR);
            }
        }
    }

    public boolean isGravitationalModulationOn(Player p) {
        return activeGravitationalModulators.contains(p.getUUID());
    }

    public Set<UUID> getActiveGravitationalModulators() {
        return activeGravitationalModulators;
    }
}