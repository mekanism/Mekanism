package mekanism.client.render.ctm;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.ResourceLocation;

/**
 * Chisel Face, basically a list of IChiselTexture's
 */
public final class ChiselFace {

    private List<ChiselTextureCTM> textureList;

    private List<ChiselFace> childFaces;

    private ResourceLocation location;

    private EnumWorldBlockLayer layer;

    public ChiselFace(ResourceLocation location) {
        this(new ArrayList<>(), new ArrayList<>(), location);
    }

    public ChiselFace(ResourceLocation location, EnumWorldBlockLayer layer) {
        this(location);
        setLayer(layer);
    }

    public ChiselFace(List<ChiselTextureCTM> textureList, List<ChiselFace> childFaces, ResourceLocation location) {
        this.textureList = textureList;
        this.childFaces = childFaces;
        this.location = location;
    }

    public List<ChiselTextureCTM> getTextureList(){
        List<ChiselTextureCTM> list = new ArrayList<>();
        list.addAll(this.textureList);
        for (ChiselFace face : childFaces){
            list.addAll(face.getTextureList());
        }
        return list;
    }

    public void addTexture(ChiselTextureCTM texture){
        this.textureList.add(texture);
    }

    public void addChildFace(ChiselFace face){
        this.childFaces.add(face);
    }

    public boolean removeTexture(ChiselTextureCTM texture){
        return this.textureList.remove(texture);
    }

    public boolean removeChildFace(ChiselFace face){
        return this.childFaces.remove(face);
    }

    public TextureAtlasSprite getParticle(){
        if (textureList.get(0) != null) {
            return textureList.get(0).getParticle();
        }
        else {
            return childFaces.get(0).getParticle();
        }
    }

    public ResourceLocation getLocation(){
        return this.location;
    }


    public void setLayer(EnumWorldBlockLayer layer){
        this.layer = layer;
    }

    public EnumWorldBlockLayer getLayer(){
        return this.layer;
    }

}
