package mekanism.client.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Supplier;
import mekanism.api.math.MathUtils;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.INestedRadialMode;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.text.EnumColor;
import mekanism.client.render.MekanismRenderPipelines;
import mekanism.client.render.lib.ScrollIncrementer;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.radial.IGenericRadialModeItem;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketRadialModeChange;
import mekanism.common.util.StatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

//TODO: Do we Automatically want to go through nested things if there is only one option and potentially not even allow opening it?
// For now no as it might be confusing to people what the menu is relating to especially on the Meka-Tool but it is worth thinking more about
public class GuiRadialSelector extends Screen {

    private static final Identifier BACK_BUTTON = Mekanism.rl("radial/back");
    private static final float DRAWS = 720;

    private static final float INNER = 40, OUTER = 100;
    private static final float MIDDLE_DISTANCE = (INNER + OUTER) / 2F;
    private static final float SELECT_RADIUS = 10, SELECT_RADIUS_WITH_PARENT = 20;

    private final ScrollIncrementer scrollIncrementer = new ScrollIncrementer(true);
    private final Deque<RadialData<?>> parents = new ArrayDeque<>();
    private final Supplier<@Nullable Player> playerSupplier;
    private final EquipmentSlot slot;

    private RadialData<?> radialData;
    @Nullable
    private IRadialMode selection = null;
    private boolean overBackButton = false;
    private boolean updateOnClose = true;

    public GuiRadialSelector(EquipmentSlot slot, RadialData<?> radialData, Supplier<@Nullable Player> playerSupplier) {
        super(MekanismLang.RADIAL_SCREEN.translate());
        this.slot = slot;
        this.radialData = radialData;
        this.playerSupplier = playerSupplier;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // center of screen
        float centerX = guiGraphics.guiWidth() / 2F;
        float centerY = guiGraphics.guiHeight() / 2F;
        render(guiGraphics, mouseX, mouseY, centerX, centerY, radialData);
    }

    private <MODE extends IRadialMode> void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float centerX, float centerY, RadialData<MODE> radialData) {
        // Calculate number of available modes to switch between
        List<MODE> modes = radialData.getModes();
        int activeModes = modes.size();
        if (activeModes == 0) {
            //If for some reason none are available try going up a level first, and if that fails close the screen
            RadialData<?> parent = parents.pollLast();
            if (parent == null) {
                onClose();
            } else {
                this.radialData = parent;
            }
            return;
        }
        float angleSize = 360F / activeModes;

        Matrix3x2fStack matrix = guiGraphics.pose();
        matrix.pushMatrix();
        matrix.translate(centerX, centerY);

        // draw base
        // Note: While there might be slightly better performance only drawing part of the Torus given
        // other bits may be drawn by hovering or current selection, it is not practical to do so due
        // to floating point precision causing some values to have gaps in the torus, and also the light
        // colors occasionally being harder to see without the added back layer torus
        drawTorus(guiGraphics, centerX, centerY, 0, 360, 0.3F, 0.3F, 0.3F, 0.5F);

        MODE current = getCurrent(radialData);
        if (current == null) {
            //See if the radial data has a default fallback to go to
            current = radialData.getDefaultMode(modes);
        }
        // Draw segments
        //Calculate the proper section to highlight as green based on the radial data in case indexing can be optimized or
        // some pieces are actually disabled
        int section = radialData.indexNullable(modes, current);
        if (current != null && section != -1) {
            // draw current selected if any is selected
            float startAngle = -90F + 360F * (-0.5F + section) / activeModes;
            EnumColor color = current.color();
            if (color == null) {
                drawTorus(guiGraphics, centerX, centerY, startAngle, angleSize, 0.4F, 0.4F, 0.4F, 0.7F);
            } else {
                drawTorus(guiGraphics, centerX, centerY, startAngle, angleSize, color.getColor(0), color.getColor(1), color.getColor(2), 0.3F);
            }
        }

        // Draw current hovered selection and selection highlighter
        double xDiff = mouseX - centerX;
        double yDiff = mouseY - centerY;
        double distanceFromCenter = Mth.length(xDiff, yDiff);
        if (distanceFromCenter > (parents.isEmpty() ? SELECT_RADIUS : SELECT_RADIUS_WITH_PARENT)) {
            // draw mouse selection highlight
            float angle = (float) (Mth.RAD_TO_DEG * Mth.atan2(yDiff, xDiff));
            float modeSize = 180F / activeModes;
            drawTorus(guiGraphics, centerX, centerY, angle - modeSize, angleSize, 0.8F, 0.8F, 0.8F, 0.3F);

            float selectionAngle = StatUtils.wrapDegrees(angle + modeSize + 90F);
            int selectionDrawnPos = (int) (selectionAngle * (activeModes / 360F));
            selection = modes.get(selectionDrawnPos);

            // draw hovered selection
            drawTorus(guiGraphics, centerX, centerY, -90F + 360F * (-0.5F + selectionDrawnPos) / activeModes, angleSize, 0.6F, 0.6F, 0.6F, 0.7F);
        } else {
            selection = null;
        }

        record PositionedText(float x, float y, Component text) {}
        List<PositionedText> textToDraw = new ArrayList<>(parents.isEmpty() ? activeModes : activeModes + 1);
        //Draw back button if needed
        if (!parents.isEmpty()) {
            overBackButton = distanceFromCenter <= SELECT_RADIUS_WITH_PARENT;
            if (overBackButton) {
                drawTorus(guiGraphics, centerX, centerY, 0, 360, 0, SELECT_RADIUS_WITH_PARENT, 0.8F, 0.8F, 0.8F, 0.3F);
            } else {
                drawTorus(guiGraphics, centerX, centerY, 0, 360, 0, SELECT_RADIUS_WITH_PARENT, 0.3F, 0.3F, 0.3F, 0.5F);
            }
            // draw icon
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACK_BUTTON, -12, -18, 24, 24);
            textToDraw.add(new PositionedText(0, 0, MekanismLang.BACK.translate()));
        } else {
            overBackButton = false;
        }

        // Icons
        int position = 0;
        for (MODE mode : modes) {
            float degrees = 270 + 360 * ((float) position++ / activeModes);
            float angle = Mth.DEG_TO_RAD * degrees;
            float x = Mth.cos(angle) * MIDDLE_DISTANCE;
            float y = Mth.sin(angle) * MIDDLE_DISTANCE;
            // draw icon
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, mode.icon(), Math.round(x - 12), Math.round(y - 20), 24, 24);
            // queue label
            textToDraw.add(new PositionedText(x, y, mode.sliceName()));
        }

        if (!textToDraw.isEmpty()) {
            // Labels (has to be separate from icons or the icons occasionally will get extra artifacts for some reason and also then we can't batch them)
            //TODO: Is delaying them even necessary anymore?
            boolean whiteRadialText = MekanismConfig.client.whiteRadialText.get();
            for (PositionedText toDraw : textToDraw) {
                matrix.pushMatrix();
                matrix.translate(toDraw.x, toDraw.y);
                matrix.scale(0.8F, 0.8F);
                Component text = toDraw.text;
                if (whiteRadialText) {
                    text = text.copy().withStyle(ChatFormatting.RESET);
                }
                float x = -font.width(text) / 2F;
                guiGraphics.text(font, text, (int) x, font.lineHeight, 0xCCFFFFFF, true);
                matrix.popMatrix();
            }
        }
        matrix.popMatrix();
    }

    @Override
    public void removed() {
        if (updateOnClose) {
            updateSelection(radialData);
        }
        super.removed();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        // handle & ignore all key events
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        return deltaY != 0 && mouseScrolled(radialData, scrollIncrementer.scroll(deltaY)) || super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    private <MODE extends IRadialMode> boolean mouseScrolled(RadialData<MODE> radialData, int shift) {
        if (shift == 0) {
            return true;
        }
        List<MODE> modes = radialData.getModes();
        if (!modes.isEmpty()) {
            //Should always be true but handle just in case
            MODE current = getCurrent(radialData);
            int index = radialData.indexNullable(modes, current);
            if (index != -1) {//Skip if we couldn't find a selected one (such as for the nested radials)
                selection = MathUtils.getByIndexMod(modes, index + shift);
                updateSelection(radialData);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        updateSelection(radialData);
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }

    @Override
    public void extractTransparentBackground(GuiGraphicsExtractor graphics) {
        //NO-OP, ensure the background isn't blurred
        //graphics.fillGradient(0, 0, this.width, this.height, 0xc0101010, 0xd0101010);
    }

    @Nullable
    private <MODE extends IRadialMode> MODE getCurrent(RadialData<MODE> radialData) {
        Player player = playerSupplier.get();
        if (player != null) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.getItem() instanceof IGenericRadialModeItem item) {
                return item.getMode(stack, radialData);
            }
        }
        return null;
    }

    private <MODE extends IRadialMode> void updateSelection(final RadialData<MODE> radialData) {
        //Only update it if we have a selection, and it is different from the current value
        if (selection != null && playerSupplier.get() != null) {
            if (selection instanceof INestedRadialMode nested && nested.hasNestedData()) {
                parents.push(radialData);
                //noinspection ConstantConditions: not null, validated by hasNestedData
                this.radialData = nested.nestedData();
                //Reset immediately rather than next render frame
                selection = null;
            } else if (!selection.equals(getCurrent(radialData))) {
                //Only bother syncing if we don't already have that mode selected
                List<Identifier> path = new ArrayList<>(parents.size());
                RadialData<?> previousParent = null;
                for (RadialData<?> parent : parents) {
                    if (previousParent != null) {
                        path.add(parent.getIdentifier());
                    }
                    previousParent = parent;
                }
                if (previousParent != null) {
                    path.add(radialData.getIdentifier());
                }
                int networkRepresentation = radialData.tryGetNetworkRepresentation(selection);
                if (networkRepresentation != -1) {
                    //TODO: If we ever add a radial type where the network representation may be negative,
                    // re-evaluate how we do this type validation
                    PacketUtils.sendToServer(new PacketRadialModeChange(slot, path, networkRepresentation));
                }
            }
        } else if (overBackButton) {
            //Reset immediately rather than next render frame
            overBackButton = false;
            RadialData<?> parent = parents.pollLast();
            if (parent != null) {
                this.radialData = parent;
            }
        }
    }

    public boolean hasMatchingData(EquipmentSlot slot, RadialData<?> data) {
        if (this.slot == slot) {
            RadialData<?> firstData = parents.peekFirst();
            //If there is an initial root parent compare it, otherwise we are currently at the root so compare to our current one
            return firstData == null ? radialData.equals(data) : firstData.equals(data);
        }
        return false;
    }

    @SuppressWarnings("ConstantConditions")//not null, validated by hasNestedData
    public void tryInheritCurrentPath(@Nullable Screen screen) {
        if (screen instanceof GuiRadialSelector old) {
            //Try to calculate the expected sub radial if we are going from radial selector to radial selector
            // as if they both have the same path odds are only some minor option changed, so we might as well
            // go to the same sub one
            RadialData<?> previousParent = null;
            for (RadialData<?> parent : old.parents) {
                if (previousParent != null && radialData.getIdentifier().equals(previousParent.getIdentifier())) {
                    INestedRadialMode nestedMode = radialData.fromIdentifier(parent.getIdentifier());
                    if (nestedMode == null || !nestedMode.hasNestedData()) {
                        //Can't go any deeper end
                        return;
                    }
                    //Update the current radial depth
                    parents.push(radialData);
                    radialData = nestedMode.nestedData();
                }
                previousParent = parent;
            }
            if (previousParent != null && radialData.getIdentifier().equals(previousParent.getIdentifier())) {
                INestedRadialMode nestedMode = radialData.fromIdentifier(old.radialData.getIdentifier());
                if (nestedMode != null && nestedMode.hasNestedData()) {
                    //Update the current radial depth
                    parents.push(radialData);
                    radialData = nestedMode.nestedData();
                    // and mark the previous one to not change values when closing
                    // (as it isn't "fully" closing, just changing views)
                    old.updateOnClose = false;
                }
            }
        }
    }

    public boolean shouldHideCrosshair() {
        return !parents.isEmpty();
    }

    private void drawTorus(GuiGraphicsExtractor guiGraphics, float centerX, float centerY, float startAngle, float sizeAngle, float red, float green, float blue, float alpha) {
        drawTorus(guiGraphics, centerX, centerY, startAngle, sizeAngle, INNER, OUTER, red, green, blue, alpha);
    }

    private void drawTorus(GuiGraphicsExtractor guiGraphics, float centerX, float centerY, float startAngle, float sizeAngle, float inner, float outer, float red, float green, float blue, float alpha) {
        ScreenRectangle scissorArea = guiGraphics.peekScissorStack();
        ScreenRectangle bounds = new ScreenRectangle((int) (centerX - outer), (int) (centerY - outer), (int) outer * 2, (int) outer * 2);
        guiGraphics.submitGuiElementRenderState(new TorusRenderState(centerX, centerY, startAngle, sizeAngle, inner, outer, red, green, blue, alpha, scissorArea,
              scissorArea == null ? bounds : scissorArea.intersection(bounds)));
    }

    private record TorusRenderState(float centerX, float centerY, float startAngle, float sizeAngle, float inner, float outer, float red, float green, float blue,
                                    float alpha, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds) implements GuiElementRenderState {

        @Override
        public void buildVertices(VertexConsumer vertexConsumer) {
            float draws = DRAWS * (sizeAngle / 360F);
            for (int i = 0; i <= draws; i++) {
                float degrees = startAngle + (i / DRAWS) * 360;
                float angle = Mth.DEG_TO_RAD * degrees;
                float cos = Mth.cos(angle);
                float sin = Mth.sin(angle);
                vertexConsumer.addVertex(centerX + outer * cos, centerY + outer * sin, 0)
                      .setColor(red, green, blue, alpha);
                vertexConsumer.addVertex(centerX + inner * cos, centerY + inner * sin, 0)
                      .setColor(red, green, blue, alpha);
            }
        }

        @Override
        public RenderPipeline pipeline() {
            return MekanismRenderPipelines.GUI_TRIANGLE_STRIP;
        }

        @Override
        public TextureSetup textureSetup() {
            return TextureSetup.noTexture();
        }
    }
}