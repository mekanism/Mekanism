/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.statements;

import java.util.Collection;

import javax.annotation.Nonnull;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public interface ITriggerProvider {
    void addInternalTriggers(Collection<ITriggerInternal> triggers, IStatementContainer container);

    void addInternalSidedTriggers(Collection<ITriggerInternalSided> triggers, IStatementContainer container, @Nonnull EnumFacing side);

    /** Returns the list of triggers available to a gate next to the given block. */
    void addExternalTriggers(Collection<ITriggerExternal> triggers, @Nonnull EnumFacing side, TileEntity tile);
}
