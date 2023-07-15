package mekanism.client.render.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import mekanism.client.render.HUDRenderer;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.tags.MekanismTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.items.IItemHandler;

public class MekanismHUD implements IGuiOverlay {

    private static final EquipmentSlot[] EQUIPMENT_ORDER = {EquipmentSlot.OFFHAND, EquipmentSlot.MAINHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
                                                            EquipmentSlot.FEET};

    private static final HUDRenderer hudRenderer = new HUDRenderer();

    @Override
    public void render(ForgeGui gui, PoseStack poseStack, float partialTicks, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.options.hideGui && !minecraft.player.isSpectator() && MekanismConfig.client.enableHUD.get()) {
            int count = 0;
            List<List<Component>> renderStrings = new ArrayList<>();
            for (EquipmentSlot slotType : EQUIPMENT_ORDER) {
                ItemStack stack = minecraft.player.getItemBySlot(slotType);
                if (stack.getItem() instanceof IItemHUDProvider hudProvider) {
                    count += makeComponent(list -> hudProvider.addHUDStrings(list, minecraft.player, stack, slotType), renderStrings);
                }
            }
            if (Mekanism.hooks.CuriosLoaded) {
                Optional<? extends IItemHandler> invOptional = CuriosIntegration.getCuriosInventory(minecraft.player);
                if (invOptional.isPresent()) {
                    IItemHandler inv = invOptional.get();
                    for (int i = 0, slots = inv.getSlots(); i < slots; i++) {
                        ItemStack stack = inv.getStackInSlot(i);
                        if (stack.getItem() instanceof IItemHUDProvider hudProvider) {
                            count += makeComponent(list -> hudProvider.addCurioHUDStrings(list, minecraft.player, stack), renderStrings);
                        }
                    }
                }
            }
            boolean reverseHud = MekanismConfig.client.reverseHUD.get();
            int maxTextHeight = screenHeight;
            if (count > 0) {
                float hudScale = MekanismConfig.client.hudScale.get();
                int xScale = (int) (screenWidth / hudScale);
                int yScale = (int) (screenHeight / hudScale);
                int start = (renderStrings.size() * 2) + (count * 9);
                int y = yScale - start;
                maxTextHeight = (int) (y * hudScale);
                poseStack.pushPose();
                poseStack.scale(hudScale, hudScale, hudScale);
                for (List<Component> group : renderStrings) {
                    for (Component text : group) {
                        drawString(minecraft.font, xScale, poseStack, text, reverseHud, y, 0xC8C8C8);
                        y += 9;
                    }
                    y += 2;
                }
                poseStack.popPose();
            }

            if (minecraft.player.getItemBySlot(EquipmentSlot.HEAD).is(MekanismTags.Items.MEKASUIT_HUD_RENDERER)) {
                hudRenderer.renderHUD(poseStack, partialTicks, screenWidth, screenHeight, maxTextHeight, reverseHud);
            }
        }
    }

    private int makeComponent(Consumer<List<Component>> adder, List<List<Component>> initial) {
        List<Component> list = new ArrayList<>();
        adder.accept(list);
        int size = list.size();
        if (size > 0) {
            initial.add(list);
        }
        return size;
    }

    private void drawString(Font font, int windowWidth, PoseStack matrix, Component text, boolean reverseHud, int y, int color) {
        //Note that we always offset by 2 pixels from the edge of the screen regardless of how it is aligned
        if (reverseHud) {
            //Align the text to the right
            int width = font.width(text) + 2;
            font.drawShadow(matrix, text, windowWidth - width, y, color);
        } else {
            //Align to the left
            font.drawShadow(matrix, text, 2, y, color);
        }
    }
}