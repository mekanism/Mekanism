package mekanism.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.util.NonNullList;
import org.apache.commons.lang3.Validate;

public class TileNetworkList extends NonNullList<Object> {

    public TileNetworkList() {
        super(new ArrayList<>(), null);
    }

    public TileNetworkList(@Nonnull List<Object> contents) {
        super(contents, null);
        Validate.noNullElements(contents);
    }

    public static TileNetworkList withContents(@Nonnull Object... contents) {
        return new TileNetworkList(Arrays.asList(contents));
    }
}
