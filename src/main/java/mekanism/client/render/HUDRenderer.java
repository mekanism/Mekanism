package mekanism.client.render;

import java.util.function.Predicate;
import mekanism.api.MekanismAPITags;
import mekanism.api.gear.IClientModuleHelper;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.client.pip.CompassPiP;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.TypedInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

//TODO - 1.20: Decide if we want font rendering in this to support GuiUtils#drawBackdrop and if so how to best go about it
public class HUDRenderer {

    private static final EquipmentSlot[] EQUIPMENT_ORDER = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND,
                                                            EquipmentSlot.OFFHAND};
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    //TODO - 26.2: Remove padding gui sprites so they don't take as much space on the atlas?
    private static final Identifier[] ARMOR_ICONS = {Mekanism.rl("hud/mekasuit_helmet"), Mekanism.rl("hud/mekasuit_chest"),
                                                     Mekanism.rl("hud/mekasuit_leggings"), Mekanism.rl("hud/mekasuit_boots")};
    private static final Identifier TOOL_ICON = Mekanism.rl("hud/mekatool");

    private int lastSubtitleGuiTick = -1;
    private int lastSubtitleWidth = 0;
    private long lastTick = -1;
    private float prevRotationYaw;
    private float prevRotationPitch;

    public void renderHUD(Minecraft minecraft, Player player, GuiGraphicsExtractor guiGraphics, Font font, DeltaTracker delta, int screenWidth, int screenHeight,
          int maxTextHeight, boolean reverseHud) {
        update(minecraft.level, player);
        if (MekanismConfig.client.hudOpacity.get() < 0.05F) {
            return;
        }
        Matrix3x2fStack matrix = guiGraphics.pose();
        matrix.pushMatrix();
        float yawJitter = -absSqrt(player.yHeadRot - prevRotationYaw);
        float pitchJitter = -absSqrt(player.getXRot() - prevRotationPitch);
        matrix.translate(yawJitter, pitchJitter);
        int audibleSubtitlesWidth = MekanismConfig.client.hudAvoidSoundSubtitleOverlay.get() ? getAudibleSubtitlesWidth(minecraft, font) : 0;
        if (MekanismConfig.client.hudCompassEnabled.get()) {
            //renderCompass(player, font, guiGraphics, delta, screenWidth, screenHeight, maxTextHeight, reverseHud, audibleSubtitlesWidth);
            matrix.pushMatrix();
            //Reversed hud causes the compass to render on the right side of the screen
            int posX = reverseHud ? screenWidth - 125 - audibleSubtitlesWidth : 25;
            //Pin the compass above the bottom of the screen and also above the text hud that may render below it
            int posY = Math.min(screenHeight - 20, maxTextHeight) - 80;
            matrix.translate(posX - 125, posY - 125);
            guiGraphics.submitPictureInPictureRenderState(new CompassPiP.State(new Matrix3x2f(matrix), guiGraphics.peekScissorStack(),
                  MekanismLang.GENERIC_BLOCK_POS.translate(player.getBlockX(), player.getBlockY(), player.getBlockZ()),
                  Mth.PI - Mth.DEG_TO_RAD * player.getViewYRot(delta.getGameTimeDeltaPartialTick(true))
            ));
            matrix.popMatrix();
        }

        renderMekaSuitEnergyIcons(player, font, guiGraphics);
        renderMekaSuitModuleIcons(player, font, guiGraphics, screenWidth, screenHeight, reverseHud, audibleSubtitlesWidth);

        matrix.popMatrix();
    }

    private void update(Level level, Player player) {
        // if we're just now rendering the HUD after a pause, reset the pitch/yaw trackers
        if (lastTick == -1 || level.getGameTime() - lastTick > 1 || !level.tickRateManager().runsNormally()) {
            prevRotationYaw = player.getYRot();
            prevRotationPitch = player.getXRot();
        }
        //TODO - 26.2: Can we do this via DeltaTracker#getGameTimeDeltaTicks or DeltaTracker#getRealtimeDeltaTicks?
        lastTick = level.getGameTime();
        float yawDiff = player.yHeadRot - prevRotationYaw;
        float pitchDiff = player.getXRot() - prevRotationPitch;
        float jitter = MekanismConfig.client.hudJitter.get();
        prevRotationYaw += yawDiff / jitter;
        prevRotationPitch += pitchDiff / jitter;
    }

    private static float absSqrt(float val) {
        float ret = Mth.sqrt(Math.abs(val));
        return val < 0 ? -ret : ret;
    }

    private void renderMekaSuitEnergyIcons(Player player, Font font, GuiGraphicsExtractor guiGraphics) {
        Matrix3x2fStack pose = guiGraphics.pose();
        pose.pushMatrix();
        pose.translate(10, 10);
        int posX = 0;
        Predicate<TypedInstance<Item>> showArmorPercent = item -> item.is(MekanismAPITags.Items.MODULE_CONTAINERS_ARMOR);
        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            posX += renderEnergyIcon(player, font, guiGraphics, posX, ARMOR_ICONS[i], ARMOR_SLOTS[i], showArmorPercent);
        }
        Predicate<TypedInstance<Item>> showToolPercent = item -> item.is(MekanismAPITags.Items.MODULE_CONTAINERS_HELD);
        for (EquipmentSlot hand : EquipmentSlotGroup.HAND) {
            posX += renderEnergyIcon(player, font, guiGraphics, posX, TOOL_ICON, hand, showToolPercent);
        }
        pose.popMatrix();
    }

    private int renderEnergyIcon(Player player, Font font, GuiGraphicsExtractor guiGraphics, int posX, Identifier icon, EquipmentSlot slot, Predicate<TypedInstance<Item>> showPercent) {
        ItemAccess itemAccess = ItemAccessUtils.forEntitySlot(player, slot);
        if (showPercent.test(itemAccess.getResource())) {
            renderHUDElement(font, guiGraphics, posX, 0, IClientModuleHelper.INSTANCE.hudElementPercent(icon, StorageUtils.getEnergyRatio(itemAccess)), false);
            return 48;
        }
        return 0;
    }

    private void renderMekaSuitModuleIcons(Player player, Font font, GuiGraphicsExtractor guiGraphics, int screenWidth, int screenHeight,
          boolean reverseHud, int subtitlesWidth) {
        int startX = screenWidth - 10;
        int curY = screenHeight - 10;
        //Render any elements that might be on modules in the meka suit while worn or on the meka tool while held
        for (EquipmentSlot type : EQUIPMENT_ORDER) {
            ItemStack stack = player.getItemBySlot(type);
            IModuleContainer moduleContainer = IModuleHelper.INSTANCE.getModuleContainer(stack);
            if (moduleContainer != null) {
                for (IHUDElement element : moduleContainer.getHUDElements(player, stack)) {
                    curY -= 18;
                    if (reverseHud) {
                        //Align the mekasuit module icons to the left of the screen
                        renderHUDElement(font, guiGraphics, 10, curY, element, false);
                    } else {
                        //Align the mekasuit module icons to the right of the screen
                        int elementWidth = subtitlesWidth + 24 + font.width(element.getText());
                        renderHUDElement(font, guiGraphics, startX - elementWidth, curY, element, true);
                    }
                }
            }
        }
    }

    /// Based on how [SubtitleOverlay#extractRenderState(GuiGraphicsExtractor)] calculates the width
    private int getAudibleSubtitlesWidth(Minecraft minecraft, Font font) {
        if (!minecraft.options.showSubtitles().get() || minecraft.gui.hud.subtitleOverlay.audibleSubtitles.isEmpty()) {
            //Subtitles are disabled or none are currently showing, don't bother calculating a width
            return 0;
        }
        if (lastSubtitleGuiTick != minecraft.gui.hud.getGuiTicks()) {
            lastSubtitleGuiTick = minecraft.gui.hud.getGuiTicks();
            int maxWidth = 0;
            for (SubtitleOverlay.Subtitle subtitle : minecraft.gui.hud.subtitleOverlay.audibleSubtitles) {
                //Note: We know all subtitles here are still active, so we can skip checking
                maxWidth = Math.max(maxWidth, font.width(subtitle.getText()));
            }
            //Note: This mirrors vanilla having them as separate pieces for calculating the width instead of just doing font.width("< > ")
            // Presumably this is because Font#width returns Math#ceil, so the below has a chance of being larger than doing it as a single calculation
            maxWidth += font.width("<") + font.width(" ") + font.width(">") + font.width(" ");

            lastSubtitleWidth = maxWidth;
        }
        return lastSubtitleWidth;
    }

    private void renderHUDElement(Font font, GuiGraphicsExtractor guiGraphics, int x, int y, IHUDElement element,
          boolean iconRight) {
        int color = element.getColor();
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, element.getIcon(), iconRight ? x + font.width(element.getText()) + 2 : x, y, 16, 16, color);
        guiGraphics.text(font, element.getText(), iconRight ? x : x + 18, y + 5, color, false);
    }
}