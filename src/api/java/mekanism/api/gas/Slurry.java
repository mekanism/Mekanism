package mekanism.api.gas;

import mekanism.api.MekanismAPI;
import net.minecraft.item.Item;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

//TODO: Eventually make this directly be a "chemical" instead of a "gas"
public class Slurry extends Gas {

    private Tag<Item> oreTag;

    public Slurry(boolean isClean, int tint, Tag<Item> oreTag) {
        //TODO: Rename texture path
        super(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "block/liquid/liquid" + (isClean ? "_clean" : "") + "_ore"));
        setTint(tint);
        setVisible(false);
        this.oreTag = oreTag;
    }

    public Tag<Item> getOreTag() {
        return oreTag;
    }
}