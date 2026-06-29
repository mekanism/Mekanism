package mekanism.common;

import java.util.function.Predicate;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.inventory.container.item.PortableQIODashboardContainer;
import mekanism.common.lib.frequency.FrequencyControllerManager;
import mekanism.common.lib.radiation.RadiationManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

public class CommonWorldTickHandler {

    public boolean flushTagAndRecipeCaches;
    public boolean monitoringCardboardBox;
    @Nullable
    public Predicate<ItemStack> fallbackItemCollector;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(EntityJoinLevelEvent event) {
        //If we are in the middle of breaking a block using a cardboard box, cancel any items
        // that are dropped, we do this at highest priority to ensure we cancel it the same tick
        // before forge replaces items with custom item entities with a tick delay
        // We also cancel any experience orbs from spawning as things like the furnace will store
        // how much xp they have but also try to drop it on replace
        if (monitoringCardboardBox) {
            Entity entity = event.getEntity();
            if (entity instanceof ItemEntity || entity instanceof ExperienceOrb) {
                entity.discard();
                event.setCanceled(true);
            }
        } else if (!event.getLevel().isClientSide() && fallbackItemCollector != null && event.getEntity() instanceof ItemEntity entity && fallbackItemCollector.test(entity.getItem())) {
            //If we have a fallback item collector active and the entity that is being added is an item,
            // try to let our fallback collector handle the item and keep track of it instead of actually adding it to the world
            entity.discard();
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BreakBlockEvent event) {
        BlockState state = event.getState();
        //Skip empty block, and check if the player has access to destroy it
        //Note: The level should always be an instance of Level based on what is passed to the constructor of BreakBlockEvent,
        // but we instance check it just to be safe
        if (!state.isAir() && event.getLevel() instanceof Level level && !IBlockSecurityUtils.INSTANCE.canAccess(event.getPlayer(), level, event.getPos())) {
            //TODO - 26.2 Do we need to use event.setNotifyClient ?
            //If they don't because it is something that is locked, then cancel the event
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onTick(ServerTickEvent.Post event) {
        boolean tickingNormally = event.getServer().tickRateManager().runsNormally();
        FrequencyControllerManager.tick(tickingNormally);
    }

    @SubscribeEvent
    public void onTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            RadiationManager.get().tickServerWorld(level);
            //Note: We flush the tag and recipe cache, and also perform retrogen, regardless of if the ticks are frozen or not
            if (flushTagAndRecipeCaches) {
                //Loop all open containers and if it is a portable qio dashboard force refresh the window's recipes
                for (ServerPlayer player : level.players()) {
                    if (player.containerMenu instanceof PortableQIODashboardContainer qioDashboard) {
                        for (byte index = 0; index < IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS; index++) {
                            qioDashboard.getCraftingWindow(index).invalidateRecipe(level);
                        }
                    }
                }
                flushTagAndRecipeCaches = false;
            }
        }
    }
}