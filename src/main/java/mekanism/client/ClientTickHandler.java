package mekanism.client;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.radial.RadialData;
import mekanism.client.gui.GuiRadialSelector;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.hud.MekanismStatusOverlay;
import mekanism.client.render.lib.ScrollIncrementer;
import mekanism.client.sound.GeigerSound;
import mekanism.client.sound.SoundHandler;
import mekanism.common.CommonPlayerTickHandler;
import mekanism.common.Mekanism;
import mekanism.common.base.KeySync;
import mekanism.common.base.holiday.HolidayManager;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.mekasuit.ModuleVisionEnhancementUnit;
import mekanism.common.item.gear.ItemHDPEElytra;
import mekanism.common.item.gear.ItemJetpack;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.item.gear.ItemScubaTank;
import mekanism.common.item.interfaces.IJetpackItem;
import mekanism.common.item.interfaces.IJetpackItem.JetpackMode;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.radial.IGenericRadialModeItem;
import mekanism.common.lib.radiation.ClientRadiation;
import mekanism.common.lib.radiation.RadiationScale;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketModeChange;
import mekanism.common.network.to_server.PacketPortableTeleporterTeleport;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.MekanismUtils;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FogType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * Client-side tick handler for Mekanism. Used mainly for the update check upon startup.
 *
 * @author AidanBrady
 */
public class ClientTickHandler {

    private static final Minecraft minecraft = Minecraft.getInstance();
    private static final RandomSource rand = RandomSource.create();
    private static final Map<Player, TeleportData> portableTeleports = new Object2ObjectArrayMap<>(1);
    private static final ScrollIncrementer scrollIncrementer = new ScrollIncrementer(true);

    private static boolean visionEnhancement = false;

    public static void reset() {
        portableTeleports.clear();
        visionEnhancement = false;
    }

    private boolean isConnected;

    public static boolean isJetpackInUse(Player player, ItemStack jetpack) {
        if (!player.isSpectator() && !jetpack.isEmpty()) {
            JetpackMode mode = ((IJetpackItem) jetpack.getItem()).getJetpackMode(jetpack);
            boolean guiOpen = minecraft.screen != null;
            boolean ascending = minecraft.player.input.jumping;
            boolean rising = ascending && !guiOpen;
            if (mode == JetpackMode.NORMAL || mode == JetpackMode.VECTOR) {
                return rising;
            } else if (mode == JetpackMode.HOVER) {
                boolean descending = minecraft.player.input.shiftKeyDown;
                if (!rising || descending) {
                    return !CommonPlayerTickHandler.isOnGroundOrSleeping(player);
                }
                return true;
            }
        }
        return false;
    }

    public static boolean isScubaMaskOn(Player player) {
        if (player != minecraft.player) {
            return Mekanism.playerState.isScubaMaskOn(player);
        }
        return CommonPlayerTickHandler.isScubaMaskOn(player, player.getItemBySlot(EquipmentSlot.CHEST));
    }

    public static boolean isGravitationalModulationOn(Player player) {
        if (player != minecraft.player) {
            return Mekanism.playerState.isGravitationalModulationOn(player);
        }
        return CommonPlayerTickHandler.isGravitationalModulationOn(player);
    }

    public static boolean isVisionEnhancementOn(Player player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!player.getCooldowns().isOnCooldown(head.getItem())) {
            IModuleContainer container = IModuleHelper.INSTANCE.getModuleContainer(head);
            if (container != null) {
                IModule<ModuleVisionEnhancementUnit> module = container.getIfEnabled(MekanismModules.VISION_ENHANCEMENT_UNIT);
                return module != null && module.hasEnoughEnergy(head, MekanismConfig.gear.mekaSuitEnergyUsageVisionEnhancement);
            }
        }
        return false;
    }

    public static void portableTeleport(Player player, InteractionHand hand, FrequencyIdentity identity) {
        //TODO - 1.21: Handle and validate the delay takes place server side
        int delay = MekanismConfig.gear.portableTeleporterDelay.get();
        if (delay == 0) {
            PacketUtils.sendToServer(new PacketPortableTeleporterTeleport(hand, identity));
        } else {
            portableTeleports.put(player, new TeleportData(hand, identity, minecraft.level.getGameTime() + delay));
        }
    }

    @SubscribeEvent
    public void onStartTracking(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide && event.getEntity() instanceof Player player && MekanismConfig.client.enablePlayerSounds.get()) {
            SoundHandler.startFlamethrowerSound(player);
        }
    }

    @SubscribeEvent
    public void onJoinServer(ClientPlayerNetworkEvent.LoggingIn event) {
        if (!isConnected) {//Note: This should always be true when the event is fired
            isConnected = true;
            MekanismClient.launchClient(event.getConnection());
        }
    }

    @SubscribeEvent
    public void onLeaveServer(ClientPlayerNetworkEvent.LoggingOut event) {
        //Note: We check if the client has actually connected before handling this, as this event is also called when the client is setting up the server
        if (isConnected) {
            isConnected = false;
            MekanismClient.reset();
        }
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.Pre event) {
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        boolean tickingNormally = MekanismRenderer.isRunningNormally();
        HolidayManager.notify(minecraft.player);

        //Reboot player sounds if needed
        SoundHandler.restartSounds();

        if (tickingNormally) {
            ClientRadiation.tickClient(minecraft.player);
        }

        UUID playerUUID = minecraft.player.getUUID();
        // Update player's state for various items; this also automatically notifies server if something changed and
        // kicks off sounds as necessary
        ItemStack jetpack = IJetpackItem.getActiveJetpack(minecraft.player);
        boolean jetpackInUse = isJetpackInUse(minecraft.player, jetpack);
        Mekanism.playerState.setJetpackState(playerUUID, jetpackInUse, true);
        Mekanism.playerState.setScubaMaskState(playerUUID, isScubaMaskOn(minecraft.player), true);
        Mekanism.playerState.setGravitationalModulationState(playerUUID, isGravitationalModulationOn(minecraft.player), true);

        if (tickingNormally) {
            //Note: If we aren't ticking normally we can skip adding the particles or checking if the time matches as we know the time hasn't changed
            for (Iterator<Entry<Player, TeleportData>> iter = portableTeleports.entrySet().iterator(); iter.hasNext(); ) {
                Entry<Player, TeleportData> entry = iter.next();
                Player player = entry.getKey();
                for (int i = 0; i < 100; i++) {
                    double x = player.getX() + rand.nextDouble() - 0.5D;
                    double y = player.getY() + rand.nextDouble() * 2 - 2D;
                    double z = player.getZ() + rand.nextDouble() - 0.5D;
                    minecraft.level.addParticle(ParticleTypes.PORTAL, x, y, z, 0, 1, 0);
                }
                TeleportData data = entry.getValue();
                if (minecraft.level.getGameTime() == data.teleportTime) {
                    PacketUtils.sendToServer(new PacketPortableTeleporterTeleport(data.hand, data.identity));
                    iter.remove();
                }
            }
        }

        if (!jetpack.isEmpty()) {
            ItemStack primaryJetpack = IJetpackItem.getPrimaryJetpack(minecraft.player);
            if (!primaryJetpack.isEmpty()) {
                JetpackMode primaryMode = ((IJetpackItem) primaryJetpack.getItem()).getJetpackMode(primaryJetpack);
                JetpackMode mode = IJetpackItem.getPlayerJetpackMode(minecraft.player, primaryMode, p -> p.input.jumping);
                MekanismClient.updateKey(minecraft.player.input.jumping, KeySync.ASCEND);
                double jetpackThrust = ((IJetpackItem) primaryJetpack.getItem()).getJetpackThrust(primaryJetpack);
                if (jetpackInUse && IJetpackItem.handleJetpackMotion(minecraft.player, mode, jetpackThrust, p -> p.input.jumping)) {
                    minecraft.player.resetFallDistance();
                }
            }
        }

        if (isScubaMaskOn(minecraft.player) && minecraft.player.getAirSupply() == minecraft.player.getMaxAirSupply()) {
            for (MobEffectInstance effect : minecraft.player.getActiveEffects()) {
                if (MekanismUtils.shouldSpeedUpEffect(effect)) {
                    for (int i = 0; i < 9; i++) {
                        MekanismUtils.speedUpEffectSafely(minecraft.player, effect);
                    }
                }
            }
        }

        if (isVisionEnhancementOn(minecraft.player)) {
            visionEnhancement = true;
            // adds if it doesn't exist, otherwise tops off duration to 220. equal or less than 200 will make vision flickers
            minecraft.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 11 * SharedConstants.TICKS_PER_SECOND, 0, false, false, false));
        } else if (visionEnhancement) {
            visionEnhancement = false;
            MobEffectInstance effect = minecraft.player.getEffect(MobEffects.NIGHT_VISION);
            if (effect != null && effect.getDuration() <= 11 * SharedConstants.TICKS_PER_SECOND) {
                //Only remove it if it is our effect and not one that has a longer remaining duration
                minecraft.player.removeEffect(MobEffects.NIGHT_VISION);
            }
        }

        if (minecraft.screen == null || minecraft.screen instanceof GuiRadialSelector) {
            if (!MekKeyHandler.isRadialPressed() || (!updateSelectorRenderer(EquipmentSlot.MAINHAND) && !updateSelectorRenderer(EquipmentSlot.OFFHAND))) {
                if (minecraft.screen != null) {
                    //If we currently have a radial selector gui open but shouldn't close it
                    minecraft.setScreen(null);
                }
            }
        }

        if (tickingNormally && MekanismConfig.client.enablePlayerSounds.get()) {
            RadiationScale scale = ClientRadiation.getClientScale();
            if (scale != RadiationScale.NONE && !SoundHandler.radiationSoundMap.containsKey(scale)) {
                GeigerSound sound = GeigerSound.create(minecraft.player, scale);
                SoundHandler.radiationSoundMap.put(scale, sound);
                SoundHandler.playSound(sound);
            }
        }
    }

    private boolean updateSelectorRenderer(EquipmentSlot slot) {
        if (minecraft.player != null) {
            ItemStack stack = minecraft.player.getItemBySlot(slot);
            if (stack.getItem() instanceof IGenericRadialModeItem item) {
                RadialData<?> radialData = item.getRadialData(stack);
                if (radialData != null) {
                    if (!(minecraft.screen instanceof GuiRadialSelector screen) || !screen.hasMatchingData(slot, radialData)) {
                        GuiRadialSelector newSelector = new GuiRadialSelector(slot, radialData, () -> minecraft.player);
                        newSelector.tryInheritCurrentPath(minecraft.screen);
                        minecraft.setScreen(newSelector);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onMouseEvent(MouseScrollingEvent event) {
        if (MekanismConfig.client.allowModeScroll.get() && minecraft.player != null && minecraft.player.isShiftKeyDown()) {
            double delta = event.getScrollDeltaY();
            if (delta != 0 && IModeItem.isModeItem(minecraft.player, EquipmentSlot.MAINHAND)) {
                int shift = scrollIncrementer.scroll(delta);
                if (shift != 0) {
                    MekanismStatusOverlay.INSTANCE.setTimer();
                    PacketUtils.sendToServer(new PacketModeChange(EquipmentSlot.MAINHAND, shift));
                }
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onFogLighting(ViewportEvent.ComputeFogColor event) {
        if (visionEnhancement) {
            float oldRatio = 0.1F;
            float newRatio = 1 - oldRatio;
            float red = oldRatio * event.getRed();
            float green = oldRatio * event.getGreen();
            float blue = oldRatio * event.getBlue();
            event.setRed(red + newRatio * 0.4F);
            event.setGreen(green + newRatio * 0.8F);
            event.setBlue(blue + newRatio * 0.4F);
        }
    }

    @SubscribeEvent
    public void onFog(ViewportEvent.RenderFog event) {
        if (visionEnhancement && event.getCamera().getEntity() instanceof Player player) {
            IModule<ModuleVisionEnhancementUnit> module = IModuleHelper.INSTANCE.getIfEnabled(player, EquipmentSlot.HEAD, MekanismModules.VISION_ENHANCEMENT_UNIT);
            if (module != null) {
                //This near plane is the same as spectators have set for lava and powdered snow
                event.setNearPlaneDistance(-8.0F);
                if (event.getFarPlaneDistance() < 20) {
                    float scalar;
                    if (event.getType() == FogType.LAVA) {
                        //Special handling for lava which is usually either at 1 or 3
                        scalar = 24 * event.getFarPlaneDistance();
                    } else {
                        //Shortly before 27 this ends up being 192, but we want to get it beforehand, so we just allow numbers below 20
                        scalar = 5 + 2.5F * (float) Math.pow(Math.E, 0.16F * event.getFarPlaneDistance());
                    }
                    //192 is roughly equivalent to what spectators have lava, powdered snow, and a couple other bounds for fog are,
                    // so we want to make sure we don't go above that
                    event.setFarPlaneDistance(Math.min(192, scalar));
                }
                //Scale the distance based on the number of installed modules
                event.scaleFarPlaneDistance(((float) Math.pow(module.getInstalledCount(), 1.25)) / module.getUntypedData().getMaxStackSize());
                //Cancel the event to ensure our changes are applied
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void recipesUpdated(RecipesUpdatedEvent event) {
        //Note: Dedicated servers first connection the server sends recipes then tags, and on reload sends tags then recipes.
        // We ignore this fact and only clear the cache in the recipes updated event however, as the cache should already be
        // empty on our initial connection, and even if it isn't the client has no way to query the recipes and cause the
        // caches to be initialized before the tags are then received as we lazily initialize our recipe caches.
        MekanismRecipeType.clearCache();
    }

    @SubscribeEvent
    public void renderEntityPre(RenderLivingEvent.Pre<?, ?> evt) {
        EntityModel<?> model = evt.getRenderer().getModel();
        if (model instanceof HumanoidModel<?> humanoidModel) {
            //If the entity has a biped model, then see if it is wearing a meka suit, in which case we want to hide various parts of the model
            setModelVisibility(evt.getEntity(), humanoidModel, false);
        }
    }

    @SubscribeEvent
    public void renderEntityPost(RenderLivingEvent.Post<?, ?> evt) {
        EntityModel<?> model = evt.getRenderer().getModel();
        if (model instanceof HumanoidModel<?> humanoidModel) {
            //Undo model visibility changes we made to ensure that other entities of the same type are properly visible
            setModelVisibility(evt.getEntity(), humanoidModel, true);
        }
    }

    private static void setModelVisibility(LivingEntity entity, HumanoidModel<?> entityModel, boolean showModel) {
        if (entity.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ItemMekaSuitArmor) {
            entityModel.head.visible = showModel;
            entityModel.hat.visible = showModel;
            if (entityModel instanceof PlayerModel<?> playerModel) {
                playerModel.ear.visible = showModel;
            }
        }
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof ItemMekaSuitArmor) {
            entityModel.body.visible = showModel;
            if (!(entity instanceof ArmorStand)) {
                //Don't adjust arms for armor stands as the model will end up changing them anyway and then we may incorrectly activate them
                entityModel.leftArm.visible = showModel;
                entityModel.rightArm.visible = showModel;
            }
            if (entityModel instanceof PlayerModel<?> playerModel) {
                playerModel.cloak.visible = showModel;
                playerModel.jacket.visible = showModel;
                playerModel.leftSleeve.visible = showModel;
                playerModel.rightSleeve.visible = showModel;
            } else if (entityModel instanceof ArmorStandModel armorStandModel) {
                armorStandModel.rightBodyStick.visible = showModel;
                armorStandModel.leftBodyStick.visible = showModel;
                armorStandModel.shoulderStick.visible = showModel;
            }
        } else if (itemHidesCape(chest.getItem()) && entityModel instanceof PlayerModel<?> playerModel) {
            //Hide the player's cape if they have an HDPE elytra as it will be part of the elytra's layer and shouldn't be rendered
            playerModel.cloak.visible = showModel;
        }
        if (entity.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof ItemMekaSuitArmor) {
            entityModel.leftLeg.visible = showModel;
            entityModel.rightLeg.visible = showModel;
            if (entityModel instanceof PlayerModel<?> playerModel) {
                playerModel.leftPants.visible = showModel;
                playerModel.rightPants.visible = showModel;
            }
        }
    }

    private static boolean itemHidesCape(Item item) {
        return item instanceof ItemHDPEElytra || item instanceof ItemJetpack || item instanceof ItemScubaTank;
    }

    private record TeleportData(InteractionHand hand, FrequencyIdentity identity, long teleportTime) {
    }
}