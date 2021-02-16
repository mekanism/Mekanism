package mekanism.common.inventory.container;

import mekanism.common.lib.security.ISecurityObject;

public interface ISecurityContainer {

    /**
     * @apiNote Only for use on the server, which means that it doesn't need to properly update on the client side if the stack changes
     */
    ISecurityObject getSecurityObject();
}