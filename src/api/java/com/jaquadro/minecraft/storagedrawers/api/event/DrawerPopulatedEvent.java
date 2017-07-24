package com.jaquadro.minecraft.storagedrawers.api.event;

import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawer;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * This event is called when a drawer has been bound to a new item.  This is
 * and opportunity for mods to cache extended data with the drawer.
 *
 * This event is also called when the drawer is changed to empty.
 */
public class DrawerPopulatedEvent extends Event
{
    public final IDrawer drawer;

    public DrawerPopulatedEvent (IDrawer drawer) {
        this.drawer = drawer;
    }
}
