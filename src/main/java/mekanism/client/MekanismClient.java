package mekanism.client;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import mekanism.client.model.robit.RobitSkinManager;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.render.tileentity.RenderSPS;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.base.IModModule;
import mekanism.common.lib.radiation.ClientRadiation;
import mekanism.common.lib.security.SecurityData;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketKey;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jspecify.annotations.Nullable;

@Mod(value = Mekanism.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Mekanism.MODID, value = Dist.CLIENT)
public class MekanismClient {

    public static final Map<UUID, SecurityData> clientSecurityMap = new Object2ObjectOpenHashMap<>();
    public static final Map<UUID, String> clientUUIDMap = new Object2ObjectOpenHashMap<>();
    private static boolean isConnected;
    private static RecipeMap clientRecipeMap = RecipeMap.EMPTY;

    public MekanismClient(ModContainer container, IEventBus modEventBus) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.register(RobitSpriteUploader.class);
        modEventBus.register(RobitSkinManager.class);
    }

    public static void updateKey(KeyMapping key, int type) {
        updateKey(key.isDown(), type);
    }

    public static void updateKey(boolean pressed, int type) {
        if (Minecraft.getInstance().player != null) {
            UUID playerUUID = Minecraft.getInstance().player.getUUID();
            boolean down = Minecraft.getInstance().gui.screen() == null && pressed;
            if (down != Mekanism.keyMap.has(playerUUID, type)) {
                PacketUtils.sendToServer(new PacketKey(type, down));
                Mekanism.keyMap.update(playerUUID, type, down);
            }
        }
    }

    /// Reset things that aren't needed between levels or would leak
    public static void resetDimensionChange() {
        Mekanism.playerState.clear(true);
        Mekanism.activeVibrators.clear();
        ClientRadiation.resetClient();
        RenderSPS.clearBoltRenderers();
        TransmitterNetworkRegistry.clearClientNetworks();
        RenderTickHandler.clearQueued();

        for (IModModule module : Mekanism.modulesLoaded) {
            module.resetClientDimensionChanged();
        }
    }

    public static void reset() {
        clientSecurityMap.clear();
        clientUUIDMap.clear();
        clientRecipeMap = RecipeMap.EMPTY;

        ClientTickHandler.reset();
        SoundHandler.radiationSoundMap.clear();
        MekanismRecipeType.clearCache();

        resetDimensionChange();

        for (IModModule module : Mekanism.modulesLoaded) {
            module.resetClient();
        }
    }

    public static void launchClient(Connection connection) {
        for (IModModule module : Mekanism.modulesLoaded) {
            module.launchClient(connection);
        }
    }

    @Nullable
    public static Level tryGetClientWorld() {
        return Minecraft.getInstance().level;
    }

    @Nullable
    public static Player tryGetClientPlayer() {
        return Minecraft.getInstance().player;
    }

    @SubscribeEvent
    public static void onCloneRespawn(ClientPlayerNetworkEvent.Clone event) {
        if (event.getOldPlayer().level() != event.getNewPlayer().level()) {
            resetDimensionChange();
        }
    }

    public static String getModId(ItemStack stack) {
        return MekanismUtils.getModId(Objects.requireNonNull(Minecraft.getInstance().level, "No active Level").registryAccess(), stack);
    }

    @SubscribeEvent
    public static void onJoinServer(ClientPlayerNetworkEvent.LoggingIn event) {
        if (!isConnected) {//Note: This should always be true when the event is fired
            isConnected = true;
            launchClient(event.getConnection());
        }
    }

    @SubscribeEvent
    public static void onLeaveServer(ClientPlayerNetworkEvent.LoggingOut event) {
        //Note: We check if the client has actually connected before handling this, as this event is also called when the client is setting up the server
        if (isConnected) {
            isConnected = false;
            reset();
        }
    }

    @SubscribeEvent
    public static void recipesUpdated(RecipesReceivedEvent event) {
        //Note: Dedicated servers first connection the server sends recipes then tags, and on reload sends tags then recipes.
        // We ignore this fact and only clear the cache in the recipes updated event however, as the cache should already be
        // empty on our initial connection, and even if it isn't the client has no way to query the recipes and cause the
        // caches to be initialized before the tags are then received as we lazily initialize our recipe caches.
        MekanismRecipeType.clearCache();
        clientRecipeMap = event.getRecipeMap();
    }

    public static RecipeMap clientRecipes() {
        return clientRecipeMap;
    }
}