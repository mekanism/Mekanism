package universalelectricity.core.electricity;

import java.util.List;

import universalelectricity.core.block.IConductor;

public interface IConductorRegistry
{

	void register(IConductor conductor);

	void cleanConductors();

	void resetAllConnections();

	List<IConductor> getConductors();
}
