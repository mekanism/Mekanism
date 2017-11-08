package mekanism.common.base;

import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TileNetworkList extends NonNullList<Object>
{
	public TileNetworkList(){
		super(new ArrayList<>(), null);
	}

	public TileNetworkList(@Nonnull List<Object> contents){
		super(contents, null);
	}

	public static TileNetworkList of(@Nonnull Object... contents){
		return new TileNetworkList(Arrays.asList(contents));
	}
}
