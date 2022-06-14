package mekanism.common.advancements;

import javax.annotation.Nullable;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record MekanismAdvancement(ResourceLocation path, String title, String description, @Nullable MekanismAdvancement parent) {

    public MekanismAdvancement(@Nullable MekanismAdvancement parent, String name) {
        this(parent, Mekanism.MODID, name);
    }

    public MekanismAdvancement(@Nullable MekanismAdvancement parent, String modid, String name) {
        //Note: specified mekanism modid is the grouping for the advancements, where the first one is the mod adding the advancement
        this(new ResourceLocation(modid, Mekanism.MODID + "/" + name), name, name, parent);
    }

    public MekanismAdvancement {
        title = Util.makeDescriptionId("advancements", new ResourceLocation(path.getNamespace(), title + ".title"));
        description = Util.makeDescriptionId("advancements", new ResourceLocation(path.getNamespace(), description + ".description"));
    }

    public Component translateTitle() {
        return TextComponentUtil.translate(title);
    }

    public Component translateDescription() {
        return TextComponentUtil.translate(description);
    }
}