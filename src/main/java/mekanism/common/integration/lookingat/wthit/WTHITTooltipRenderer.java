package mekanism.common.integration.lookingat.wthit;

import com.mojang.blaze3d.vertex.PoseStack;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.ITooltipComponent;
import mekanism.common.integration.lookingat.LookingAtElement;
import mekanism.common.integration.lookingat.LookingAtUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class WTHITTooltipRenderer implements IBlockComponentProvider, IEntityComponentProvider {

    static final WTHITTooltipRenderer INSTANCE = new WTHITTooltipRenderer();

    @Override
    public void appendBody(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
        append(tooltip, accessor.getServerData(), config);
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        append(tooltip, accessor.getServerData(), config);
    }

    private void append(ITooltip tooltip, CompoundTag data, IPluginConfig config) {
        LookingAtUtils.appendHwylaTooltip(data, tooltip::addLine, (lastText, element, name) -> {
            if (config.getBoolean(name)) {
                tooltip.addLine(new MekElement(lastText, element));
            }
        });
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
        public void render(PoseStack poseStack, int x, int y, float delta) {
            if (text != null) {
                LookingAtElement.renderScaledText(Minecraft.getInstance(), poseStack, x + 4, y + 3, 0xFFFFFF, 92, text);
                y += 13;
            }
            poseStack.pushPose();
            poseStack.translate(x, y, 0);
            element.render(poseStack, 0, 1);
            poseStack.popPose();
        }
    }
}