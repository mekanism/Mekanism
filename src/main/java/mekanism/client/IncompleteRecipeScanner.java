package mekanism.client;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class IncompleteRecipeScanner {

    private static final Component RECIPE_WARNING = MekanismLang.LOG_FORMAT.translateColored(EnumColor.RED, MekanismLang.MEKANISM/*, EnumColor.GRAY*/, MekanismLang.RECIPE_WARNING.translate());
    private static boolean foundIncompleteRecipes = false;

    @SubscribeEvent
    public static void recipes(OnDatapackSyncEvent event) {
        //player is logging in
        ServerPlayer player = event.getPlayer();
        if (player != null) {
            if (foundIncompleteRecipes) {
                sendMessageToPlayer(player);
            }
            //skip running scan on player login, should have run at start or last reload
            return;
        }

        //run the scan
        foundIncompleteRecipes = MekanismRecipeType.checkIncompleteRecipes(event.getPlayerList().getServer());

        //if broken, message any players online
        if (foundIncompleteRecipes) {
            List<ServerPlayer> players = event.getPlayerList().getPlayers();
            if (!players.isEmpty()) {
                players.forEach(IncompleteRecipeScanner::sendMessageToPlayer);
            }
        }
    }

    private static void sendMessageToPlayer(ServerPlayer player) {
        player.sendSystemMessage(RECIPE_WARNING);
    }

    @SubscribeEvent
    public static void serverStarted(ServerStartedEvent event) {
        //run the scan. In theory there will be no players at this point, so shouldn't need to send message
        foundIncompleteRecipes = MekanismRecipeType.checkIncompleteRecipes(event.getServer());
    }
}
