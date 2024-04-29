package mekanism.client.render.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.client.gui.GuiUtils;
import mekanism.client.render.HUDRenderer;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.tags.MekanismTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MekanismHUD implements LayeredDraw.Layer {

    public static final MekanismHUD INSTANCE = new MekanismHUD();
    private static final EquipmentSlot[] EQUIPMENT_ORDER = {EquipmentSlot.OFFHAND, EquipmentSlot.MAINHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
                                                            EquipmentSlot.FEET};

    private final HUDRenderer hudRenderer = new HUDRenderer();

    private MekanismHUD() {
    }

    @Nullable
    private IItemHUDProvider getHudProvider(ItemStack stack) {
        if (stack.getItem() instanceof IItemHUDProvider hudProvider) {
            //mekanism does this
            return hudProvider;
        }
        IModuleContainer container = IModuleHelper.INSTANCE.getModuleContainer(stack);
        return container != null ? (list, player, s, slotType) -> list.addAll(container.getHUDStrings(player)) : null;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player != null && !player.isSpectator() && MekanismConfig.client.enableHUD.get()) {
            int count = 0;
            List<List<Component>> renderStrings = new ArrayList<>();
            for (EquipmentSlot slotType : EQUIPMENT_ORDER) {
                ItemStack stack = player.getItemBySlot(slotType);
                IItemHUDProvider hudProvider = getHudProvider(stack);
                if (hudProvider != null) {
                    count += makeComponent(hudProvider, player, stack, slotType, renderStrings, IItemHUDProvider::addHUDStrings);
                }
            }
            if (Mekanism.hooks.CuriosLoaded) {
                IItemHandler inv = CuriosIntegration.getCuriosInventory(player);
                if (inv != null) {
                    for (int i = 0, slots = inv.getSlots(); i < slots; i++) {
                        ItemStack stack = inv.getStackInSlot(i);
                        IItemHUDProvider hudProvider = getHudProvider(stack);
                        if (hudProvider != null) {
                            count += makeComponent(hudProvider, player, stack, null, renderStrings,
                                  (provider, l, plyr, s, ignored) -> provider.addCurioHUDStrings(l, plyr, s));
                        }
                    }
                }
            }
            Font font = minecraft.font;
            boolean reverseHud = MekanismConfig.client.reverseHUD.get();
            int maxTextHeight = graphics.guiHeight();
            if (count > 0) {
                float hudScale = MekanismConfig.client.hudScale.get();
                int xScale = (int) (graphics.guiWidth() / hudScale);
                int yScale = (int) (graphics.guiHeight() / hudScale);
                int start = (renderStrings.size() * 2) + (count * 9);
                int y = yScale - start;
                maxTextHeight = (int) (y * hudScale);
                PoseStack pose = graphics.pose();
                pose.pushPose();
                pose.scale(hudScale, hudScale, hudScale);

                int backgroundColor = minecraft.options.getBackgroundColor(0.0F);
                if (backgroundColor != 0) {
                    //If we need to render the background behind it based on accessibility options
                    // calculate how big an area we need and draw it
                    int maxTextWidth = 0;
                    for (List<Component> group : renderStrings) {
                        for (Component text : group) {
                            int textWidth = font.width(text);
                            if (textWidth > maxTextWidth) {
                                maxTextWidth = textWidth;
                            }
                        }
                    }
                    int x = reverseHud ? xScale - maxTextWidth - 2 : 2;
                    GuiUtils.drawBackdrop(graphics, Minecraft.getInstance(), x, y, maxTextWidth, maxTextHeight, 0xFFFFFFFF);
                }

                for (List<Component> group : renderStrings) {
                    for (Component text : group) {
                        int textWidth = font.width(text);
                        //Align text to right if hud is reversed, otherwise align to the left
                        //Note: that we always offset by 2 pixels from the edge of the screen regardless of how it is aligned
                        int x = reverseHud ? xScale - textWidth - 2 : 2;
                        graphics.drawString(font, text, x, y, 0xFFC8C8C8);
                        y += 9;
                    }
                    y += 2;
                }
                pose.popPose();
            }

            if (player.getItemBySlot(EquipmentSlot.HEAD).is(MekanismTags.Items.MEKASUIT_HUD_RENDERER)) {
                hudRenderer.renderHUD(minecraft, graphics, font, partialTicks, graphics.guiWidth(), graphics.guiHeight(), maxTextHeight, reverseHud);
            }
        }
    }

    private int makeComponent(IItemHUDProvider hudProvider, Player player, ItemStack stack, EquipmentSlot slot, List<List<Component>> initial, HudComponentBuilder builder) {
        List<Component> list = new ArrayList<>();
        builder.add(hudProvider, list, player, stack, slot);
        int size = list.size();
        if (size > 0) {
            initial.add(list);
        }
        return size;
    }

    @FunctionalInterface
    private interface HudComponentBuilder {

        void add(IItemHUDProvider hudProvider, List<Component> existing, Player player, ItemStack stack, EquipmentSlot slot);
    }
}