
package thermalexpansion.api.tileentity;

import java.util.List;

import net.minecraft.tileentity.TileEntity;

/**
 * This interface is implemented on Tesseract Tile Entities.
 * 
 * @author King Lemming
 * 
 */
public interface ITesseract {

    /**
     * Returns a list of the connected input Tesseracts as Tile Entities. The list will only contain
     * valid inputs; disabled Tesseracts will not be shown. This will allow you to determine where
     * they are and what is around them.
     */
    public List<TileEntity> getValidInputLinks();

    /**
     * Returns a list of the connected output Tesseracts as Tile Entities. The list will only
     * contain valid outputs; disabled Tesseracts will not be shown. This will allow you to
     * determine where they are and what is around them.
     */
    public List<TileEntity> getValidOutputLinks();

}
