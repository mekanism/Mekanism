package mekanism.client.render.ctm;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * Chisel Face, basically a list of IChiselTexture's
 */
public final class CTMFace {

    private List<TextureCTM> textureList;

    private List<CTMFace> childFaces;

    private EnumWorldBlockLayer layer;

    public CTMFace() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public CTMFace(EnumWorldBlockLayer layer) {
        this();
        setLayer(layer);
    }

    public CTMFace(List<TextureCTM> textureList, List<CTMFace> childFaces) {
        this.textureList = textureList;
        this.childFaces = childFaces;
    }

    public List<TextureCTM> getTextureList(){
        List<TextureCTM> list = new ArrayList<>();
        list.addAll(this.textureList);
        for (CTMFace face : childFaces){
            list.addAll(face.getTextureList());
        }
        return list;
    }

    public void addTexture(TextureCTM texture){
        this.textureList.add(texture);
    }

    public void addChildFace(CTMFace face){
        this.childFaces.add(face);
    }

    public boolean removeTexture(TextureCTM texture){
        return this.textureList.remove(texture);
    }

    public boolean removeChildFace(CTMFace face){
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

    public void setLayer(EnumWorldBlockLayer layer){
        this.layer = layer;
    }

    public EnumWorldBlockLayer getLayer(){
        return this.layer;
    }

}
