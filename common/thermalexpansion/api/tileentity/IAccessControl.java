
package thermalexpansion.api.tileentity;

public interface IAccessControl {

    public boolean isPublic();

    public boolean isFriends();

    public boolean isPrivate();

    public boolean setAccessMode(byte mode);
}
