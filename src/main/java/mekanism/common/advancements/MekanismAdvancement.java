package mekanism.common.advancements;

import mekanism.api.text.TextComponentUtil;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record MekanismAdvancement(ResourceLocation name, String title, String description, @Nullable MekanismAdvancement parent) {

    public MekanismAdvancement(@Nullable MekanismAdvancement parent, ResourceLocation name) {
        this(parent, name, getSubName(name.getPath()));
    }

    private MekanismAdvancement(@Nullable MekanismAdvancement parent, ResourceLocation name, String subName) {
        this(name, subName, subName, parent);
    }

    public MekanismAdvancement {
        title = Util.makeDescriptionId("advancements", name.withPath(title + ".title"));
        description = Util.makeDescriptionId("advancements", name.withPath(description + ".description"));
    }

    public Component translateTitle() {
        return TextComponentUtil.translate(title);
    }

    public Component translateDescription() {
        return TextComponentUtil.translate(description);
    }

    private static String getSubName(String path) {
        int lastSeparator = path.lastIndexOf('/');
        if (lastSeparator == -1) {
            return path;
        } else if (lastSeparator + 1 == path.length()) {
            throw new IllegalArgumentException("Unexpected name portion");
        }
        return path.substring(lastSeparator + 1);
    }
}