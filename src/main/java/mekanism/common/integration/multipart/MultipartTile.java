package mekanism.common.integration.multipart;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipartTile;
import net.minecraft.tileentity.TileEntity;

public class MultipartTile implements IMultipartTile {

    private TileEntity owner;
    private String id;
    private IPartInfo partInfo;

    public MultipartTile(TileEntity tile, String s) {
        owner = tile;
        id = s;
    }

    @Override
    public void setPartInfo(IPartInfo info) {
        partInfo = info;
    }

    @Override
    public TileEntity getTileEntity() {
        return owner;
    }

    public String getID() {
        return id;
    }

    public IPartInfo getInfo() {
        return partInfo;
    }
}
