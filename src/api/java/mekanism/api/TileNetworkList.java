package mekanism.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.util.NonNullList;
import org.apache.commons.lang3.Validate;

@Deprecated
public class TileNetworkList extends NonNullList<Object> {

    //Used for server to client
    public TileNetworkList() {
        super(new ArrayList<>(), null);
    }

    public TileNetworkList(@Nonnull List<Object> contents) {
        super(contents, null);
        Validate.noNullElements(contents);
    }

    //Used for client to server
    public static TileNetworkList withContents(@Nonnull Object... contents) {
        return new TileNetworkList(Arrays.asList(contents));
    }
}