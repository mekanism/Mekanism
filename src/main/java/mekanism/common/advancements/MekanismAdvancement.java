package mekanism.common.advancements;

import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record MekanismAdvancement(ResourceLocation path, String title, String description) {

    public MekanismAdvancement(String name) {
        //TODO: Do we actually need to path to have the modid duplicated like this
        this(Mekanism.rl(Mekanism.MODID + "/" + name), name, name);
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