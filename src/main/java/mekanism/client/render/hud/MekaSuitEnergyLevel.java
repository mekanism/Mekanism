package mekanism.client.render.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.element.bar.GuiBar;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class MekaSuitEnergyLevel implements IGuiOverlay {

    private static final ResourceLocation POWER_BAR = MekanismUtils.getResource(ResourceType.GUI_BAR, "horizontal_power_long.png");

    @Override
    public void render(ForgeGui gui, PoseStack poseStack, float partialTicks, int width, int height) {
        if (!Minecraft.getInstance().options.hideGui && gui.shouldDrawSurvivalElements()) {
            gui.setupOverlayRenderState(true, false);
            FloatingLong capacity = FloatingLong.ZERO, stored = FloatingLong.ZERO;
            for (ItemStack stack : Minecraft.getInstance().player.getArmorSlots()) {
                if (stack.getItem() instanceof ItemMekaSuitArmor) {
                    IEnergyContainer container = StorageUtils.getEnergyContainer(stack, 0);
                    if (container != null) {
                        capacity = capacity.plusEqual(container.getMaxEnergy());
                        stored = stored.plusEqual(container.getEnergy());
                    }
                }
            }
            if (!capacity.isZero()) {
                int x = width / 2 - 91;
                int y = height - gui.leftHeight + 2;
                int length = (int) Math.round(stored.divide(capacity).doubleValue() * 79);
                GuiUtils.renderExtendedTexture(poseStack, GuiBar.BAR, 2, 2, x, y, 81, 6);
                RenderSystem.setShaderTexture(0, POWER_BAR);
                GuiComponent.blit(poseStack, x + 1, y + 1, length, 4, 0, 0, length, 4, 79, 4);
                gui.leftHeight += 8;
            }
        }
    }
}