package mekanism.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Supplier;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.lib.ScrollIncrementer;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.radial.IGenericRadialModeItem;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.INestedRadialMode;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.common.network.to_server.PacketRadialModeChange;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.StatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO: Do we Automatically want to go through nested things if there is only one option and potentially not even allow opening it?
// For now no as it might be confusing to people what the menu is relating to especially on the Meka-Tool but it is worth thinking more about
public class GuiRadialSelector extends Screen {

    private static final float DRAWS = 300;

    private static final float INNER = 40, OUTER = 100;
    private static final float MIDDLE_DISTANCE = (INNER + OUTER) / 2F;
    private static final float SELECT_RADIUS = 10, SELECT_RADIUS_WITH_PARENT = 20;

    private final ScrollIncrementer scrollIncrementer = new ScrollIncrementer(true);
    private final Deque<RadialData<?>> parents = new ArrayDeque<>();
    private final Supplier<Player> playerSupplier;
    private final EquipmentSlot slot;

    @NotNull
    private RadialData<?> radialData;
    private IRadialMode selection = null;
    private boolean overBackButton = false;
    private boolean updateOnClose = true;

    public GuiRadialSelector(EquipmentSlot slot, @NotNull RadialData<?> radialData, Supplier<Player> playerSupplier) {
        super(MekanismLang.RADIAL_SCREEN.translate());
        this.slot = slot;
        this.radialData = radialData;
        this.playerSupplier = playerSupplier;
    }

    @Override
    public void render(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTick) {
        //Ensure it is initialized, it should be but just in case
        if (minecraft != null) {
            // center of screen
            float centerX = minecraft.getWindow().getGuiScaledWidth() / 2F;
            float centerY = minecraft.getWindow().getGuiScaledHeight() / 2F;
            render(matrix, mouseX, mouseY, centerX, centerY, radialData);
        }
    }

    private <MODE extends IRadialMode> void render(@NotNull PoseStack matrix, int mouseX, int mouseY, float centerX, float centerY, RadialData<MODE> radialData) {
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

        matrix.pushPose();
        matrix.translate(centerX, centerY, 0);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();

        // draw base
        // Note: While there might be slightly better performance only drawing part of the Torus given
        // other bits may be drawn by hovering or current selection, it is not practical to do so due
        // to floating point precision causing some values to have gaps in the torus, and also the light
        // colors occasionally being harder to see without the added back layer torus
        RenderSystem.setShaderColor(0.3F, 0.3F, 0.3F, 0.5F);
        drawTorus(matrix, 0, 360);

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
            EnumColor color = current.color();
            if (color == null) {
                RenderSystem.setShaderColor(0.4F, 0.4F, 0.4F, 0.7F);
            } else {
                MekanismRenderer.color(color, 0.3F);
            }
            float startAngle = -90F + 360F * (-0.5F + section) / activeModes;
            drawTorus(matrix, startAngle, angleSize);
        }

        // Draw current hovered selection and selection highlighter
        double xDiff = mouseX - centerX;
        double yDiff = mouseY - centerY;
        double distanceFromCenter = Mth.length(xDiff, yDiff);
        if (distanceFromCenter > (parents.isEmpty() ? SELECT_RADIUS : SELECT_RADIUS_WITH_PARENT)) {
            // draw mouse selection highlight
            float angle = (float) (Mth.RAD_TO_DEG * Mth.atan2(yDiff, xDiff));
            float modeSize = 180F / activeModes;
            RenderSystem.setShaderColor(0.8F, 0.8F, 0.8F, 0.3F);
            drawTorus(matrix, angle - modeSize, angleSize);

            float selectionAngle = StatUtils.wrapDegrees(angle + modeSize + 90F);
            int selectionDrawnPos = (int) (selectionAngle * (activeModes / 360F));
            selection = modes.get(selectionDrawnPos);

            // draw hovered selection
            RenderSystem.setShaderColor(0.6F, 0.6F, 0.6F, 0.7F);
            drawTorus(matrix, -90F + 360F * (-0.5F + selectionDrawnPos) / activeModes, angleSize);
        } else {
            selection = null;
        }

        record PositionedText(float x, float y, Component text) {}
        List<PositionedText> textToDraw = new ArrayList<>(parents.isEmpty() ? activeModes : activeModes + 1);
        //Draw back button if needed
        if (!parents.isEmpty()) {
            overBackButton = distanceFromCenter <= SELECT_RADIUS_WITH_PARENT;
            if (overBackButton) {
                RenderSystem.setShaderColor(0.8F, 0.8F, 0.8F, 0.3F);
            } else {
                RenderSystem.setShaderColor(0.3F, 0.3F, 0.3F, 0.5F);
            }
            drawTorus(matrix, 0, 360, 0, SELECT_RADIUS_WITH_PARENT);
            MekanismRenderer.resetColor();
            RenderSystem.enableTexture();
            // draw icon, note: shader is set by blit
            RenderSystem.setShaderTexture(0, MekanismUtils.getResource(ResourceType.GUI_RADIAL, "back.png"));
            blit(matrix, -12, -18, 24, 24, 0, 0, 18, 18, 18, 18);
            textToDraw.add(new PositionedText(0, 0, MekanismLang.BACK.translate()));
        } else {
            overBackButton = false;
        }

        MekanismRenderer.resetColor();

        // Icons
        RenderSystem.enableTexture();
        int position = 0;
        for (MODE mode : modes) {
            float degrees = 270 + 360 * ((float) position++ / activeModes);
            float angle = Mth.DEG_TO_RAD * degrees;
            float x = Mth.cos(angle) * MIDDLE_DISTANCE;
            float y = Mth.sin(angle) * MIDDLE_DISTANCE;
            // draw icon, note: shader is set by blit
            RenderSystem.setShaderTexture(0, mode.icon());
            blit(matrix, Math.round(x - 12), Math.round(y - 20), 24, 24, 0, 0, 18, 18, 18, 18);
            // queue label
            textToDraw.add(new PositionedText(x, y, mode.sliceName()));
        }

        // Labels (has to be separate from icons or the icons occasionally will get extra artifacts for some reason)
        boolean whiteRadialText = MekanismConfig.client.whiteRadialText.get();
        for (PositionedText toDraw : textToDraw) {
            matrix.pushPose();
            matrix.translate(toDraw.x, toDraw.y, 0);
            matrix.scale(0.6F, 0.6F, 0.6F);
            Component text = toDraw.text;
            if (whiteRadialText) {
                text = text.copy().withStyle(ChatFormatting.RESET);
            }
            font.drawShadow(matrix, text, -font.width(text) / 2F, 8, 0xCCFFFFFF);
            matrix.popPose();
        }

        MekanismRenderer.resetColor();
        matrix.popPose();
    }

    @Override
    public void removed() {
        if (updateOnClose) {
            updateSelection(radialData);
        }
        super.removed();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // handle & ignore all key events
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return delta != 0 && mouseScrolled(radialData, scrollIncrementer.scroll(delta)) || super.mouseScrolled(mouseX, mouseY, delta);
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        updateSelection(radialData);
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void drawTorus(PoseStack matrix, float startAngle, float sizeAngle) {
        drawTorus(matrix, startAngle, sizeAngle, INNER, OUTER);
    }

    private void drawTorus(PoseStack matrix, float startAngle, float sizeAngle, float inner, float outer) {
        RenderSystem.setShader(GameRenderer::getPositionShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder vertexBuffer = tesselator.getBuilder();
        vertexBuffer.begin(Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION);
        Matrix4f matrix4f = matrix.last().pose();
        float draws = DRAWS * (sizeAngle / 360F);
        for (int i = 0; i <= draws; i++) {
            float degrees = startAngle + (i / DRAWS) * 360;
            float angle = Mth.DEG_TO_RAD * degrees;
            float cos = Mth.cos(angle);
            float sin = Mth.sin(angle);
            vertexBuffer.vertex(matrix4f, outer * cos, outer * sin, 0).endVertex();
            vertexBuffer.vertex(matrix4f, inner * cos, inner * sin, 0).endVertex();
        }
        tesselator.end();
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
                List<ResourceLocation> path = new ArrayList<>(parents.size());
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
                    Mekanism.packetHandler().sendToServer(new PacketRadialModeChange(slot, path, networkRepresentation));
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
}