package mekanism.common.integration.lookingat.wthit;

import com.mojang.blaze3d.vertex.PoseStack;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IDataReader;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.ITooltipComponent;
import mekanism.common.integration.lookingat.ILookingAtElement;
import mekanism.common.integration.lookingat.LookingAtElement;
import mekanism.common.integration.lookingat.TextElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class WTHITTooltipRenderer implements IBlockComponentProvider, IEntityComponentProvider {

    static final WTHITTooltipRenderer INSTANCE = new WTHITTooltipRenderer();

    @Override
    public void appendBody(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
        append(tooltip, accessor.getData(), config);
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        append(tooltip, accessor.getData(), config);
    }

    private void append(ITooltip tooltip, IDataReader dataReader, IPluginConfig config) {
        WTHITLookingAtHelper helper = dataReader.get(WTHITLookingAtHelper.TYPE);
        if (helper != null) {
            //We have mek data, add an empty line for it so that we know to skip rendering the builtin types
            tooltip.setLine(MekanismWTHITPlugin.MEK_DATA);
            Component lastText = null;
            //Copy the data we need and have from the server and pass it on to the tooltip rendering
            for (ILookingAtElement element : helper.elements) {
                if (element instanceof TextElement textElement) {
                    if (lastText != null) {
                        //Fallback to printing the last text
                        tooltip.addLine(lastText);
                    }
                    lastText = textElement.text();
                    continue;
                }
                if (config.getBoolean(element.getID())) {
                    tooltip.addLine(new MekElement(lastText, (LookingAtElement) element));
                }
                lastText = null;
            }
            if (lastText != null) {
                tooltip.addLine(lastText);
            }
        }
    }

    private record MekElement(@Nullable Component text, LookingAtElement element) implements ITooltipComponent {

        @Override
        public int getWidth() {
            if (text == null) {
                return element.getWidth();
            }
            return Math.max(element.getWidth(), 96);
        }

        @Override
        public int getHeight() {
            if (text == null) {
                return element.getHeight() + 2;
            }
            return element.getHeight() + 16;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int x, int y, DeltaTracker delta) {
            if (text != null) {
                LookingAtElement.renderScaledText(Minecraft.getInstance(), guiGraphics, x + 4, y + 3, 0xFFFFFF, 92, text);
                y += 13;
            }
            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(x, y, 0);
            element.render(guiGraphics, 0, 1);
            pose.popPose();
        }
    }
}