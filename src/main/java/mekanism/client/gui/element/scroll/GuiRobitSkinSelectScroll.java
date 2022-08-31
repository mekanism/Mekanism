package mekanism.client.gui.element.scroll;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
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
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadUtils;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.entity.EntityRobit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

public class GuiRobitSkinSelectScroll extends GuiElement {

    private static final int SLOT_DIMENSIONS = 48;
    private static final int SLOT_COUNT = 3;
    private static final int INNER_DIMENSIONS = SLOT_DIMENSIONS * SLOT_COUNT;

    private final GuiScrollBar scrollBar;

    private final Supplier<List<RobitSkin>> unlockedSkins;
    private final EntityRobit robit;
    private RobitSkin selectedSkin;
    private float rotation;
    private int ticks;

    public GuiRobitSkinSelectScroll(IGuiWrapper gui, int x, int y, EntityRobit robit, Supplier<List<RobitSkin>> unlockedSkins) {
        super(gui, x, y, INNER_DIMENSIONS + 12, INNER_DIMENSIONS);
        this.robit = robit;
        this.selectedSkin = this.robit.getSkin();
        this.unlockedSkins = unlockedSkins;
        scrollBar = addChild(new GuiScrollBar(gui, relativeX + INNER_DIMENSIONS, y, INNER_DIMENSIONS,
              () -> getUnlockedSkins() == null ? 0 : (int) Math.ceil((double) getUnlockedSkins().size() / SLOT_COUNT), () -> SLOT_COUNT));
    }

    private List<RobitSkin> getUnlockedSkins() {
        return unlockedSkins.get();
    }

    public RobitSkin getSelectedSkin() {
        return selectedSkin;
    }

    @Override
    public void drawBackground(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        List<RobitSkin> skins = getUnlockedSkins();
        if (skins != null) {
            Lighting.setupForFlatItems();
            //Every ten ticks consider the skin to change
            int index = ticks / 10;
            float oldRot = rotation;
            rotation = Mth.wrapDegrees(rotation - 0.5F);
            float rot = Mth.rotLerp(partialTicks, oldRot, rotation);
            QuadTransformation rotation = QuadTransformation.rotateY(rot);
            int slotStart = scrollBar.getCurrentSelection() * SLOT_COUNT, max = SLOT_COUNT * SLOT_COUNT;
            for (int i = 0; i < max; i++) {
                int slotX = x + (i % SLOT_COUNT) * SLOT_DIMENSIONS, slotY = y + (i / SLOT_COUNT) * SLOT_DIMENSIONS;
                int slot = slotStart + i;
                if (slot < skins.size()) {
                    RobitSkin skin = skins.get(slot);
                    if (skin == selectedSkin) {
                        renderSlotBackground(matrix, slotX, slotY, GuiInnerScreen.SCREEN, GuiInnerScreen.SCREEN_SIZE);
                    } else {
                        renderSlotBackground(matrix, slotX, slotY, GuiElementHolder.HOLDER, GuiElementHolder.HOLDER_SIZE);
                    }
                    renderRobit(matrix, skins.get(slot), slotX, slotY, rotation, index);
                } else {
                    renderSlotBackground(matrix, slotX, slotY, GuiElementHolder.HOLDER, GuiElementHolder.HOLDER_SIZE);
                }
            }
            Lighting.setupFor3DItems();
        }
    }

    private static void renderSlotBackground(@NotNull PoseStack matrix, int slotX, int slotY, ResourceLocation resource, int size) {
        GuiUtils.renderBackgroundTexture(matrix, resource, size, size, slotX, slotY, SLOT_DIMENSIONS, SLOT_DIMENSIONS, 256, 256);
    }

    @Override
    public void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        List<RobitSkin> skins = getUnlockedSkins();
        if (skins != null) {
            int xAxis = mouseX - getGuiLeft(), yAxis = mouseY - getGuiTop();
            int slotX = (xAxis - relativeX) / SLOT_DIMENSIONS, slotY = (yAxis - relativeY) / SLOT_DIMENSIONS;
            if (slotX >= 0 && slotY >= 0 && slotX < SLOT_COUNT && slotY < SLOT_COUNT) {
                int slotStartX = relativeX + slotX * SLOT_DIMENSIONS, slotStartY = relativeY + slotY * SLOT_DIMENSIONS;
                if (xAxis >= slotStartX && xAxis < slotStartX + SLOT_DIMENSIONS && yAxis >= slotStartY && yAxis < slotStartY + SLOT_DIMENSIONS) {
                    //Only draw the selection hover layer if we are actually rendering over a slot
                    int slot = (slotY + scrollBar.getCurrentSelection()) * SLOT_COUNT + slotX;
                    if (slot < skins.size()) {
                        fill(matrix, slotStartX, slotStartY, slotStartX + SLOT_DIMENSIONS, slotStartY + SLOT_DIMENSIONS, 0x70FFEA00);
                        MekanismRenderer.resetColor();
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
    public void renderToolTip(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        RobitSkin skin = getSkin(mouseX, mouseY);
        if (skin != null) {
            displayTooltips(matrix, mouseX, mouseY, MekanismLang.ROBIT_SKIN.translate(skin));
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return scrollBar.adjustScroll(delta) || super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        RobitSkin skin = getSkin(mouseX, mouseY);
        if (skin != null) {
            selectedSkin = skin;
        }
    }

    private RobitSkin getSkin(double mouseX, double mouseY) {
        List<RobitSkin> skins = getUnlockedSkins();
        if (skins != null) {
            int slotX = (int) ((mouseX - x) / SLOT_DIMENSIONS), slotY = (int) ((mouseY - y) / SLOT_DIMENSIONS);
            if (slotX >= 0 && slotY >= 0 && slotX < SLOT_COUNT && slotY < SLOT_COUNT) {
                int slot = (slotY + scrollBar.getCurrentSelection()) * SLOT_COUNT + slotX;
                if (slot < skins.size()) {
                    return skins.get(slot);
                }
            }
        }
        return null;
    }

    private void renderRobit(PoseStack matrix, RobitSkin skin, int x, int y, QuadTransformation rotation, int index) {
        List<ResourceLocation> textures = skin.getTextures();
        if (textures.isEmpty()) {
            Mekanism.logger.error("Failed to render skin: {}, as it has no textures.", skin.getRegistryName());
            return;
        }
        BakedModel model = MekanismModelCache.INSTANCE.getRobitSkin(skin);
        if (model == null) {
            Mekanism.logger.warn("Failed to render skin: {} as it does not have a model.", skin.getRegistryName());
            return;
        }
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer builder = buffer.getBuffer(RobitSpriteUploader.RENDER_TYPE);
        matrix.pushPose();
        //Translate to the proper position and do our best job at centering it
        matrix.translate(x + SLOT_DIMENSIONS, y + (int) (0.8 * SLOT_DIMENSIONS), 0);
        matrix.scale(SLOT_DIMENSIONS, SLOT_DIMENSIONS, SLOT_DIMENSIONS);
        matrix.mulPose(Vector3f.ZP.rotationDegrees(180));
        PoseStack.Pose matrixEntry = matrix.last();
        ModelData modelData = ModelData.builder().with(EntityRobit.SKIN_TEXTURE_PROPERTY, MathUtils.getByIndexMod(textures, index)).build();
        List<BakedQuad> quads = model.getQuads(null, null, robit.level.random, modelData, null);
        //TODO: Ideally at some point we will want to be able to have the rotations happen via the matrix stack
        // so that we aren't having to transform the quads directly
        quads = QuadUtils.transformBakedQuads(quads, rotation);
        for (BakedQuad quad : quads) {
            builder.putBulkData(matrixEntry, quad, 1, 1, 1, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        }
        buffer.endBatch(RobitSpriteUploader.RENDER_TYPE);

        matrix.popPose();
    }
}