package mekanism.client.gui.element.scroll;

import com.mojang.math.Axis;
import java.util.List;
import java.util.function.Supplier;
import mekanism.api.math.MathUtils;
import mekanism.api.robit.RobitSkin;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.client.model.robit.RobitSkinManager;
import mekanism.client.pip.RobitSkinPreviewPiP;
import mekanism.common.MekanismLang;
import mekanism.common.entity.EntityRobit;
import mekanism.common.registries.MekanismRobitSkins;
import mekanism.common.registries.MekanismRobitSkins.SkinLookup;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public class GuiRobitSkinSelectScroll extends GuiElement {

    private static final int SLOT_DIMENSIONS = 48;
    private static final int SLOT_COUNT = 3;
    private static final int INNER_DIMENSIONS = SLOT_DIMENSIONS * SLOT_COUNT;

    private final GuiScrollBar scrollBar;

    private final Supplier<@Nullable List<ResourceKey<RobitSkin>>> unlockedSkins;
    private ResourceKey<RobitSkin> selectedSkin;
    private float rotation;
    private int ticks;
    @Nullable
    private ResourceKey<RobitSkin> lastSkin;
    @Nullable
    private Tooltip lastTooltip;
    @Nullable
    private ScreenRectangle cachedTooltipRect;

    public GuiRobitSkinSelectScroll(IGuiWrapper gui, int x, int y, EntityRobit robit, Supplier<@Nullable List<ResourceKey<RobitSkin>>> unlockedSkins) {
        super(gui, x, y, INNER_DIMENSIONS + 12, INNER_DIMENSIONS);
        this.selectedSkin = robit.getSkinId();
        this.unlockedSkins = unlockedSkins;
        scrollBar = addChild(new GuiScrollBar(gui, relativeX + INNER_DIMENSIONS, relativeY, INNER_DIMENSIONS,
              () -> getUnlockedSkins() == null ? 0 : Mth.ceil((double) getUnlockedSkins().size() / SLOT_COUNT), () -> SLOT_COUNT));
    }

    @Nullable
    private List<ResourceKey<RobitSkin>> getUnlockedSkins() {
        return unlockedSkins.get();
    }

    public ResourceKey<RobitSkin> getSelectedSkin() {
        return selectedSkin;
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        List<ResourceKey<RobitSkin>> skins = getUnlockedSkins();
        if (skins != null) {
            //Every ten ticks consider the skin to change
            //TODO - 26.2: This is actually every ten frames, as changing the frame limit in the minecraft options changes the speed at which the robits rotates
            int index = ticks / MekanismUtils.TICKS_PER_HALF_SECOND;
            float oldRot = rotation;
            rotation = Mth.wrapDegrees(rotation + 0.5F);
            float rot = Mth.rotLerp(partialTicks, oldRot, rotation);
            Quaternionf rotation = Axis.YP.rotationDegrees(rot);
            int slotStart = scrollBar.getCurrentSelection() * SLOT_COUNT, max = SLOT_COUNT * SLOT_COUNT;
            for (int i = 0; i < max; i++) {
                int slotX = relativeX + (i % SLOT_COUNT) * SLOT_DIMENSIONS, slotY = relativeY + (i / SLOT_COUNT) * SLOT_DIMENSIONS;
                int slot = slotStart + i;
                if (slot < skins.size()) {
                    ResourceKey<RobitSkin> skin = skins.get(slot);
                    if (skin == selectedSkin) {
                        renderSlotBackground(guiGraphics, slotX, slotY, GuiInnerScreen.SCREEN);
                    } else {
                        renderSlotBackground(guiGraphics, slotX, slotY, GuiElementHolder.HOLDER);
                    }
                    SkinLookup skinLookup = MekanismRobitSkins.lookup(gui().registryAccess(), skins.get(slot));
                    List<Identifier> textures = skinLookup.textures();
                    //Translate to the proper position and do our best job at centering it
                    Identifier texture = MathUtils.getByIndexMod(textures, index);
                    guiGraphics.submitPictureInPictureRenderState(new RobitSkinPreviewPiP.State(
                          getGuiLeft() + slotX, getGuiTop() + slotY,
                          SLOT_DIMENSIONS,
                          guiGraphics.peekScissorStack(),
                          rotation,
                          RobitSkinManager.get().getBaked(skinLookup.skin(), texture)
                    ));
                } else {
                    renderSlotBackground(guiGraphics, slotX, slotY, GuiElementHolder.HOLDER);
                }
            }
        }
    }

    private static void renderSlotBackground(GuiGraphicsExtractor guiGraphics, int slotX, int slotY, Identifier resource) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resource, slotX, slotY, SLOT_DIMENSIONS, SLOT_DIMENSIONS);
    }

    @Override
    public void renderForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        List<ResourceKey<RobitSkin>> skins = getUnlockedSkins();
        if (skins != null) {
            int xAxis = mouseX - getGuiLeft(), yAxis = mouseY - getGuiTop();
            int slotX = (xAxis - relativeX) / SLOT_DIMENSIONS, slotY = (yAxis - relativeY) / SLOT_DIMENSIONS;
            if (slotX >= 0 && slotY >= 0 && slotX < SLOT_COUNT && slotY < SLOT_COUNT) {
                int slotStartX = relativeX + slotX * SLOT_DIMENSIONS, slotStartY = relativeY + slotY * SLOT_DIMENSIONS;
                if (xAxis >= slotStartX && xAxis < slotStartX + SLOT_DIMENSIONS && yAxis >= slotStartY && yAxis < slotStartY + SLOT_DIMENSIONS) {
                    //Only draw the selection hover layer if we are actually rendering over a slot, and another window isn't blocking our mouse
                    // Note: Currently we have no other windows that could be in front of it
                    int slot = (slotY + scrollBar.getCurrentSelection()) * SLOT_COUNT + slotX;
                    if (checkWindows(mouseX, mouseY, slot < skins.size())) {
                        guiGraphics.fill(slotStartX, slotStartY, slotStartX + SLOT_DIMENSIONS, slotStartY + SLOT_DIMENSIONS, 0x70FFEA00);
                    }
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        ticks++;
    }

    @Override
    protected ScreenRectangle getTooltipRectangle(int mouseX, int mouseY) {
        return cachedTooltipRect == null ? super.getTooltipRectangle(mouseX, mouseY) : cachedTooltipRect;
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        ResourceKey<RobitSkin> skin = getSkin(mouseX, mouseY, true);
        if (skin == null) {
            lastTooltip = null;
        } else if (lastSkin != skin) {
            lastTooltip = TooltipUtils.create(MekanismLang.ROBIT_SKIN.translate(RobitSkin.getTranslatedName(skin)));
        }
        lastSkin = skin;
        setTooltip(lastTooltip);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double xDelta, double yDelta) {
        return scrollBar.adjustScroll(yDelta) || super.mouseScrolled(mouseX, mouseY, xDelta, yDelta);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        super.onClick(event, isDoubleClick);
        ResourceKey<RobitSkin> skin = getSkin(event.x(), event.y(), false);
        if (skin != null) {
            selectedSkin = skin;
        }
    }

    @Nullable
    private ResourceKey<RobitSkin> getSkin(double mouseX, double mouseY, boolean updateTooltipRect) {
        List<ResourceKey<RobitSkin>> skins = getUnlockedSkins();
        if (skins != null) {
            int slotX = (int) ((mouseX - getX()) / SLOT_DIMENSIONS), slotY = (int) ((mouseY - getY()) / SLOT_DIMENSIONS);
            if (slotX >= 0 && slotY >= 0 && slotX < SLOT_COUNT && slotY < SLOT_COUNT) {
                int slot = (slotY + scrollBar.getCurrentSelection()) * SLOT_COUNT + slotX;
                if (slot < skins.size()) {
                    if (updateTooltipRect) {
                        cachedTooltipRect = new ScreenRectangle(getX() + slotX * SLOT_DIMENSIONS, getY() + slotY * SLOT_DIMENSIONS, SLOT_DIMENSIONS, SLOT_DIMENSIONS);
                    }
                    return skins.get(slot);
                }
            }
        }
        if (updateTooltipRect) {
            cachedTooltipRect = null;
        }
        return null;
    }
}