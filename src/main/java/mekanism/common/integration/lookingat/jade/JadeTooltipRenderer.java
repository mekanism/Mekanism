package mekanism.common.integration.lookingat.jade;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.common.integration.lookingat.LookingAtElement;
import mekanism.common.integration.lookingat.LookingAtUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;

public class JadeTooltipRenderer implements IBlockComponentProvider, IEntityComponentProvider {

    static final JadeTooltipRenderer INSTANCE = new JadeTooltipRenderer();

    @Override
    public ResourceLocation getUid() {
        return JadeConstants.TOOLTIP_RENDERER;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        append(tooltip, accessor, config);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        append(tooltip, accessor, config);
    }

    private void append(ITooltip tooltip, Accessor<?> accessor, IPluginConfig config) {
        LookingAtUtils.appendHwylaTooltip(accessor.getServerData(), tooltip::add, (lastText, element, name) -> {
            if (config.get(name)) {
                tooltip.add(new MekElement(lastText, element).tag(name));
            }
        });
    }

    private static class MekElement extends Element {

        @Nullable
        private final Component text;
        private final LookingAtElement element;

        public MekElement(@Nullable Component text, LookingAtElement element) {
            this.element = element;
            this.text = text;
        }

        @Override
        public Vec2 getSize() {
            int width = element.getWidth();
            int height = element.getHeight() + 2;
            if (text != null) {
                width = Math.max(width, 96);
                height += 14;
            }
            return new Vec2(width, height);
        }

        @Override
        public void render(PoseStack poseStack, float x, float y, float maxX, float maxY) {
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