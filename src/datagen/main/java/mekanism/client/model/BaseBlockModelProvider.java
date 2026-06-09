package mekanism.client.model;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

public abstract class BaseBlockModelProvider /*extends BlockModelProvider */ {

    public BaseBlockModelProvider(PackOutput output, String modid) {
        //super(output, modid, existingFileHelper);
    }

    //@Override
    public String getName() {
        return "Block model provider: ";// + modid;
    }

    public boolean textureExists(Identifier texture) {
        return false;//existingFileHelper.exists(texture, PackType.CLIENT_RESOURCES, ".png", "textures");
    }
}