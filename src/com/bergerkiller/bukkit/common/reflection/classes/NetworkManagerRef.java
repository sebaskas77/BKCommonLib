package com.bergerkiller.bukkit.common.reflection.classes;

import net.minecraft.server.NetworkManager;

import com.bergerkiller.bukkit.common.reflection.FieldAccessor;
import com.bergerkiller.bukkit.common.reflection.SafeField;

public class NetworkManagerRef {
	public static FieldAccessor<Integer> queueSize = new SafeField<Integer>(NetworkManager.class, "y");
}
