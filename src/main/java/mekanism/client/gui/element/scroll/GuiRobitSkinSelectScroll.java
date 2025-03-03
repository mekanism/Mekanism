package mekanism.client.gui.element.scroll;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import java.util.function.Supplier;
import mekanism.api.math.MathUtils;
import mekanism.api.robit.RobitSkin;
import mekanism.client.RobitSpriteUploader;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.client.model.MekanismModelCache;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.entity.EntityRobit;
import mekanism.common.registries.MekanismRobitSkins;
import mekanism.common.registries.MekanismRobitSkins.SkinLookup;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class GuiRobitSkinSelectScroll extends GuiElement {

    private static final int SLOT_DIMENSIONS = 48;
    private static final int SLOT_COUNT = 3;
    private static final int INNER_DIMENSIONS = SLOT_DIMENSIONS * SLOT_COUNT;

    private final GuiScrollBar scrollBar;

    private final Supplier<List<ResourceKey<RobitSkin>>> unlockedSkins;
    private final EntityRobit robit;
    private ResourceKey<RobitSkin> selectedSkin;
    private float rotation;
    private int ticks;
    @Nullable
    private ResourceKey<RobitSkin> lastSkin;
    @Nullable
    private Tooltip lastTooltip;
    @Nullable
    private ScreenRectangle cachedTooltipRect;

    public GuiRobitSkinSelectScroll(IGuiWrapper gui, int x, int y, EntityRobit robit, Supplier<List<ResourceKey<RobitSkin>>> unlockedSkins) {
        super(gui, x, y, INNER_DIMENSIONS + 12, INNER_DIMENSIONS);
        this.robit = robit;
        this.selectedSkin = this.robit.getSkin();
        this.unlockedSkins = unlockedSkins;
        scrollBar = addChild(new GuiScrollBar(gui, relativeX + INNER_DIMENSIONS, relativeY, INNER_DIMENSIONS,
              () -> getUnlockedSkins() == null ? 0 : Mth.ceil((double) getUnlockedSkins().size() / SLOT_COUNT), () -> SLOT_COUNT));
    }

    private List<ResourceKey<RobitSkin>> getUnlockedSkins() {
        return unlockedSkins.get();
    }

    public ResourceKey<RobitSkin> getSelectedSkin() {
        return selectedSkin;
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        List<ResourceKey<RobitSkin>> skins = getUnlockedSkins();
        if (skins != null) {
            Lighting.setupForFlatItems();
            //Every ten ticks consider the skin to change
            int index = ticks / MekanismUtils.TICKS_PER_HALF_SECOND;
            float oldRot = rotation;
            rotation = Mth.wrapDegrees(rotation - 0.5F);
            float rot = Mth.rotLerp(partialTicks, oldRot, rotation);
            Quaternionf rotation = Axis.YP.rotationDegrees(rot);
            int slotStart = scrollBar.getCurrentSelection() * SLOT_COUNT, max = SLOT_COUNT * SLOT_COUNT;
            for (int i = 0; i < max; i++) {
                int slotX = relativeX + (i % SLOT_COUNT) * SLOT_DIMENSIONS, slotY = relativeY + (i / SLOT_COUNT) * SLOT_DIMENSIONS;
                int slot = slotStart + i;
                if (slot < skins.size()) {
                    ResourceKey<RobitSkin> skin = skins.get(slot);
                    if (skin == selectedSkin) {
                        renderSlotBackground(guiGraphics, slotX, slotY, GuiInnerScreen.SCREEN, GuiInnerScreen.SCREEN_SIZE);
                    } else {
                        renderSlotBackground(guiGraphics, slotX, slotY, GuiElementHolder.HOLDER, GuiElementHolder.HOLDER_SIZE);
                    }
                    renderRobit(guiGraphics, skins.get(slot), slotX, slotY, rotation, index);
                } else {
                    renderSlotBackground(guiGraphics, slotX, slotY, GuiElementHolder.HOLDER, GuiElementHolder.HOLDER_SIZE);
                }
            }
            Lighting.setupFor3DItems();
        }
    }

    private static void renderSlotBackground(@NotNull GuiGraphics guiGraphics, int slotX, int slotY, ResourceLocation resource, int size) {
        GuiUtils.renderBackgroundTexture(guiGraphics, resource, size, size, slotX, slotY, SLOT_DIMENSIONS, SLOT_DIMENSIONS, 256, 256);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
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
                        guiGraphics.fill(RenderType.guiOverlay(), slotStartX, slotStartY, slotStartX + SLOT_DIMENSIONS, slotStartY + SLOT_DIMENSIONS, 0x70FFEA00);
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

    @NotNull
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
    public void onClick(double mouseX, double mouseY, int button) {
        super.onClick(mouseX, mouseY, button);
        ResourceKey<RobitSkin> skin = getSkin(mouseX, mouseY, false);
        if (skin != null) {
            selectedSkin = skin;
        }
    }

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

    private void renderRobit(GuiGraphics guiGraphics, ResourceKey<RobitSkin> skinKey, int x, int y, Quaternionf rotation, int index) {
        SkinLookup skinLookup = MekanismRobitSkins.lookup(robit.level().registryAccess(), skinKey);
        List<ResourceLocation> textures = skinLookup.skin().textures();
        if (textures.isEmpty()) {
            Mekanism.logger.error("Failed to render skin: {}, as it has no textures.", skinLookup.location());
            return;
        }
        BakedModel model = MekanismModelCache.INSTANCE.getRobitSkin(skinLookup);
        if (model == null) {
            Mekanism.logger.warn("Failed to render skin: {} as it does not have a model.", skinLookup.location());
            return;
        }
        MultiBufferSource.BufferSource buffer = guiGraphics.bufferSource();
        VertexConsumer builder = buffer.getBuffer(RobitSpriteUploader.RENDER_TYPE);
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        //Translate to the proper position and do our best job at centering it
        pose.translate(x + SLOT_DIMENSIONS, y + (int) (0.8 * SLOT_DIMENSIONS), 0);
        pose.scale(SLOT_DIMENSIONS, SLOT_DIMENSIONS, SLOT_DIMENSIONS);
        pose.mulPose(Axis.ZP.rotationDegrees(180));
        pose.rotateAround(rotation,  0.5F, 0.0F, 0.5F);
        PoseStack.Pose matrixEntry = pose.last();
        ModelData modelData = ModelData.of(EntityRobit.SKIN_TEXTURE_PROPERTY, MathUtils.getByIndexMod(textures, index));
        for (BakedQuad quad : model.getQuads(null, null, robit.level().random, modelData, null)) {
            builder.putBulkData(matrixEntry, quad, 1, 1, 1, 1, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        }
        buffer.endBatch(RobitSpriteUploader.RENDER_TYPE);

        pose.popPose();
    }
}