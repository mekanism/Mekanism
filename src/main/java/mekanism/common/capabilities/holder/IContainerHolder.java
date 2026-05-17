package mekanism.common.capabilities.holder;

import com.mojang.logging.annotations.FieldsAreNonnullByDefault;
import java.util.List;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FieldsAreNonnullByDefault
public interface IContainerHolder<CONTAINER> extends IHolder {

    @NotNull
    List<CONTAINER> getContainers(@Nullable Direction side);
}