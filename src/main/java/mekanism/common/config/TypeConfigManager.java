package mekanism.common.config;

import mekanism.common.block.states.BlockStateMachine;

import java.util.HashMap;
import java.util.Map;

public class TypeConfigManager 
{
	private Map<String, Boolean> config = new HashMap<>();
	
	public boolean isEnabled(String type)
	{
		return config.get(type) != null && config.get(type);
	}

	public boolean isEnabled(BlockStateMachine.MachineType type) {
		return isEnabled(type.blockName);
	}
	
	public void setEntry(String type, boolean enabled)
	{
		config.put(type, enabled);
	}
}
