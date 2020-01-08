package mekanism.common;

import mekanism.api.text.EnumColor;
import mekanism.common.network.PacketBoxBlacklist;
import mekanism.common.network.PacketClearRecipeCache;
import mekanism.common.network.PacketFlamethrowerData;
import mekanism.common.network.PacketFreeRunnerData;
import mekanism.common.network.PacketJetpackData;
import mekanism.common.network.PacketMekanismTags;
import mekanism.common.network.PacketScubaTankData;
import mekanism.common.network.PacketSecurityUpdate;
import mekanism.common.network.PacketSecurityUpdate.SecurityPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommonPlayerTracker {

    private static final ITextComponent ALPHA_WARNING;

    static {
        TranslationTextComponent hereComponent = MekanismLang.ALPHA_WARNING_HERE.translate();
        hereComponent.getStyle().setUnderlined(true).setColor(TextFormatting.BLUE).setClickEvent(new ClickEvent(Action.OPEN_URL, "https://github.com/mekanism/Mekanism#alpha-status"));
        ALPHA_WARNING = MekanismLang.LOG_FORMAT.translateColored(EnumColor.RED, MekanismLang.MEKANISM, EnumColor.GRAY, MekanismLang.ALPHA_WARNING.translate(hereComponent));
    }

    public CommonPlayerTracker() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerLoginEvent(PlayerLoggedInEvent event) {
        MinecraftServer server = event.getPlayer().getServer();
        if (!event.getPlayer().world.isRemote) {
            if (server == null || !server.isSinglePlayer()) {
                //Mekanism.packetHandler.sendTo(new PacketConfigSync(MekanismConfigOld.local()), (ServerPlayerEntity) event.getPlayer());
                //TODO: Is this correct or should it be formatted/unformatted text component
                Mekanism.logger.info("Sent config to '" + event.getPlayer().getDisplayName().getString() + ".'");
            }
            Mekanism.packetHandler.sendTo(new PacketBoxBlacklist(), (ServerPlayerEntity) event.getPlayer());
            syncChangedData((ServerPlayerEntity) event.getPlayer());
            Mekanism.packetHandler.sendTo(new PacketSecurityUpdate(SecurityPacket.FULL, null, null), (ServerPlayerEntity) event.getPlayer());

            Mekanism.packetHandler.sendTo(new PacketMekanismTags(Mekanism.instance.getTagManager()), (ServerPlayerEntity) event.getPlayer());
            Mekanism.packetHandler.sendTo(new PacketClearRecipeCache(), (ServerPlayerEntity) event.getPlayer());
            event.getPlayer().sendMessage(ALPHA_WARNING);
        }
    }

    @SubscribeEvent
    public void onPlayerLogoutEvent(PlayerLoggedOutEvent event) {
        Mekanism.playerState.clearPlayer(event.getPlayer().getUniqueID());
        Mekanism.freeRunnerOn.remove(event.getPlayer().getUniqueID());
    }

    @SubscribeEvent
    public void onPlayerDimChangedEvent(PlayerChangedDimensionEvent event) {
        Mekanism.playerState.clearPlayer(event.getPlayer().getUniqueID());
        Mekanism.freeRunnerOn.remove(event.getPlayer().getUniqueID());
        if (!event.getPlayer().world.isRemote) {
            syncChangedData((ServerPlayerEntity) event.getPlayer());
        }
    }

    private void syncChangedData(ServerPlayerEntity player) {
        // TODO: Coalesce all these sync events into a single message
        Mekanism.packetHandler.sendTo(PacketJetpackData.FULL(Mekanism.playerState.getActiveJetpacks()), player);
        Mekanism.packetHandler.sendTo(PacketScubaTankData.FULL(Mekanism.playerState.getActiveGasmasks()), player);
        Mekanism.packetHandler.sendTo(PacketFlamethrowerData.FULL(Mekanism.playerState.getActiveFlamethrowers()), player);
        Mekanism.packetHandler.sendTo(new PacketFreeRunnerData(PacketFreeRunnerData.FreeRunnerPacket.FULL, null, false), player);
    }
}