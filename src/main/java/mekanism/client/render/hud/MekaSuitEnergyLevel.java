package mekanism.client.render.hud;

import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.MathUtils;
import mekanism.client.gui.element.bar.GuiBar;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.GuiLayer;
import org.jetbrains.annotations.NotNull;

public class MekaSuitEnergyLevel implements GuiLayer {

    public static final MekaSuitEnergyLevel INSTANCE = new MekaSuitEnergyLevel();
    private static final Identifier POWER_BAR = MekanismUtils.getResource(ResourceType.GUI_BAR, "horizontal_power_long.png");

    private MekaSuitEnergyLevel() {
    }

    @Override
    public void render(@NotNull GuiGraphicsExtractor graphics, @NotNull DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.gameMode.canHurtPlayer() || minecraft.options.hideGui || !MekanismConfig.client.hudRenderMekaSuitEnergyBar.get()) {
            //canHurtPlayer is a copy of vanilla check for if the armor level can render
            return;
        }
        long capacity = 0L, stored = 0L;
        for (ItemStack stack : MekanismUtils.getArmorSlots(minecraft.player)) {
            if (stack.getItem() instanceof ItemMekaSuitArmor) {
                IEnergyContainer container = StorageUtils.getEnergyContainer(stack, 0);
                if (container != null) {
                    capacity = MathUtils.addClamped(capacity, container.getMaxEnergy());
                    stored = MathUtils.addClamped(stored, container.getEnergy());
                }
            }
        }
        if (capacity != 0L) {
            int x = graphics.guiWidth() / 2 - 91;
            int y = graphics.guiHeight() - minecraft.gui.leftHeight + 2;
            int length = (int) Math.round(((double) stored / capacity) * 79);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, GuiBar.BAR, x, y, 81, 6);
            graphics.blit(RenderPipelines.GUI_TEXTURED, POWER_BAR, x + 1, y + 1, 0, 0, length, 4, length, 4, 79, 4);
            minecraft.gui.leftHeight += 8;
        }
    }
}