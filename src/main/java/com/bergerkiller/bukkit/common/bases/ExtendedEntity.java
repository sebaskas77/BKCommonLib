package com.bergerkiller.bukkit.common.bases;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.minecraft.server.v1_5_R1.Entity;
import net.minecraft.server.v1_5_R1.EntityPlayer;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_5_R1.CraftSound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.bergerkiller.bukkit.common.bases.mutable.LocationAbstract;
import com.bergerkiller.bukkit.common.bases.mutable.VectorAbstract;
import com.bergerkiller.bukkit.common.internal.CommonNMS;
import com.bergerkiller.bukkit.common.reflection.classes.EntityRef;
import com.bergerkiller.bukkit.common.utils.CommonUtil;
import com.bergerkiller.bukkit.common.utils.MathUtil;
import com.bergerkiller.bukkit.common.utils.WorldUtil;
import com.bergerkiller.bukkit.common.wrappers.DataWatcher;

/**
 * Extends the methods provided by the Entity Bukkit class.
 * 
 * @param <T> - type of Entity
 */
public class ExtendedEntity<T extends org.bukkit.entity.Entity> {
	public final LocationAbstract loc = new LocationAbstract() {
		public World getWorld() {return ExtendedEntity.this.getWorld();}
		public LocationAbstract setWorld(World world) {return this;}
		public double getX() {return ExtendedEntity.this.h().locX;}
		public double getY() {return ExtendedEntity.this.h().locY;}
		public double getZ() {return ExtendedEntity.this.h().locZ;}
		public LocationAbstract setX(double x) {ExtendedEntity.this.h().locX = x; return this;}
		public LocationAbstract setY(double y) {ExtendedEntity.this.h().locY = y; return this;}
		public LocationAbstract setZ(double z) {ExtendedEntity.this.h().locZ = z; return this;}
		public float getYaw() {return ExtendedEntity.this.h().yaw;}
		public float getPitch() {return ExtendedEntity.this.h().pitch;}
		public LocationAbstract setYaw(float yaw) {ExtendedEntity.this.h().yaw = yaw; return this;}
		public LocationAbstract setPitch(float pitch) {ExtendedEntity.this.h().pitch = pitch; return this;}
	};
	public final LocationAbstract last = new LocationAbstract() {
		public World getWorld() {return ExtendedEntity.this.getWorld();}
		public LocationAbstract setWorld(World world) {return this;}
		public double getX() {return ExtendedEntity.this.h().lastX;}
		public double getY() {return ExtendedEntity.this.h().lastY;}
		public double getZ() {return ExtendedEntity.this.h().lastZ;}
		public LocationAbstract setX(double x) {ExtendedEntity.this.h().lastX = x; return this;}
		public LocationAbstract setY(double y) {ExtendedEntity.this.h().lastY = y; return this;}
		public LocationAbstract setZ(double z) {ExtendedEntity.this.h().lastZ = z; return this;}
		public float getYaw() {return ExtendedEntity.this.h().lastYaw;}
		public float getPitch() {return ExtendedEntity.this.h().lastPitch;}
		public LocationAbstract setYaw(float yaw) {ExtendedEntity.this.h().lastYaw = yaw; return this;}
		public LocationAbstract setPitch(float pitch) {ExtendedEntity.this.h().lastPitch = pitch; return this;}
	};
	public final VectorAbstract vel = new VectorAbstract() {
		public double getX() {return ExtendedEntity.this.h().motX;}
		public double getY() {return ExtendedEntity.this.h().motY;}
		public double getZ() {return ExtendedEntity.this.h().motZ;}
		public VectorAbstract setX(double x) {ExtendedEntity.this.h().motX = x; return this;}
		public VectorAbstract setY(double y) {ExtendedEntity.this.h().motY = y; return this;}
		public VectorAbstract setZ(double z) {ExtendedEntity.this.h().motZ = z; return this;}
	};

	/**
	 * The minimum x/y/z velocity distance, above which the entity is considered to be moving
	 */
	public static final double MIN_MOVE_SPEED = 0.001;
	/**
	 * The internally-stored Bukkit Entity instance
	 */
	protected T entity;

	/**
	 * Constructs a new Extended Entity with the initial entity specified
	 * 
	 * @param entity to use
	 */
	public ExtendedEntity(T entity) {
		setEntity(entity);
	}

	/**
	 * Sets the backing Bukkit Entity
	 * 
	 * @param entity to set to
	 */
	protected void setEntity(T entity) {
		this.entity = entity;
	}

	/**
	 * Gets the backing Bukkit Entity
	 * 
	 * @return entity
	 */
	public T getEntity() {
		return entity;
	}

	/**
	 * Private method for access the NMS Entity handle
	 * 
	 * @return Entity handle
	 */
	private Entity h() {
		return getHandle(Entity.class);
	}

	/**
	 * Gets the Entity handle
	 * 
	 * @return the Entity handle
	 */
	public Object getHandle() {
		return CommonNMS.getNative(entity);
	}

	/**
	 * Gets the Entity handle, and automatically casts it to a given type
	 * 
	 * @param type to cast to
	 * @return the NMS entity handle, cast to the given type
	 */
	public <H> H getHandle(Class<H> type) {
		return CommonUtil.tryCast(getHandle(), type);
	}

	/**
	 * Obtains the DataWatcher to update and keep track of Entity metadata
	 * 
	 * @return Entity meta data watcher
	 */
	public DataWatcher getMetaData() {
		return new DataWatcher(getHandle(Entity.class).getDataWatcher());
	}

	public int getChunkX() {
		return EntityRef.chunkX.get(getHandle());
	}

	public void setChunkX(int value) {
		EntityRef.chunkX.set(getHandle(), value);
	}

	public int getChunkY() {
		return EntityRef.chunkY.get(getHandle());
	}

	public void setChunkY(int value) {
		EntityRef.chunkY.set(getHandle(), value);
	}

	public int getChunkZ() {
		return EntityRef.chunkZ.get(getHandle());
	}

	public void setChunkZ(int value) {
		EntityRef.chunkZ.set(getHandle(), value);
	}

	/**
	 * Obtains the Entity head rotation angle, or 0.0 if this Entity has no head.
	 * 
	 * @return Head rotation, if available
	 */
	public float getHeadRotation() {
		return getHandle(Entity.class).ao();
	}

	public double getMovedX() {
		return loc.getX() - last.getX();
	}

	public double getMovedY() {
		return loc.getY() - last.getY();
	}

	public double getMovedZ() {
		return loc.getZ() - last.getZ();
	}

 	public boolean hasMovedHorizontally() {
 		return Math.abs(this.getMovedX()) > MIN_MOVE_SPEED || Math.abs(this.getMovedZ()) > MIN_MOVE_SPEED;
 	}
 
 	public boolean hasMovedVertically() {
 		return Math.abs(this.getMovedX()) > MIN_MOVE_SPEED;
 	}
 
 	public boolean hasMoved() {
 		return hasMovedHorizontally() || hasMovedVertically();
 	}
 
	public double getMovedXZDistance() {
		return MathUtil.length(getMovedX(), getMovedZ());
	}

	public double getMovedXZDistanceSquared() {
		return MathUtil.lengthSquared(getMovedX(), getMovedZ());
	}

	public double getMovedDistance() {
		return MathUtil.length(getMovedX(), getMovedY(), getMovedZ());
	}

	public double getMovedDistanceSquared() {
		return MathUtil.lengthSquared(getMovedX(), getMovedY(), getMovedZ());
	}

	public boolean isMovingHorizontally() {
		return vel.x.abs() > MIN_MOVE_SPEED || vel.z.abs() > MIN_MOVE_SPEED;
	}

	public boolean isMovingVertically() {
		return vel.y.abs() > MIN_MOVE_SPEED;
	}

	public boolean isMoving() {
		return isMovingHorizontally() || isMovingVertically();
	}

	public void setWorld(World world) {
		final Entity handle = getHandle(Entity.class);
		handle.world = CommonNMS.getNative(world);
		handle.dimension = WorldUtil.getDimension(world);
	}

	public void setDead(boolean dead) {
		getHandle(Entity.class).dead = dead;
	}

	public float getHeight() {
		return getHandle(Entity.class).height;
	}

	public void setHeight(float height) {
		getHandle(Entity.class).height = height;
	}

	public float getLength() {
		return getHandle(Entity.class).length;
	}

	public void setLength(float length) {
		getHandle(Entity.class).length = length;
	}

	public boolean isOnGround() {
		return getHandle(Entity.class).onGround;
	}

	public void setOnGround(boolean onGround) {
		getHandle(Entity.class).onGround = onGround;
	}

	public boolean isPositionChanged() {
		return EntityRef.positionChanged.get(getHandle());
	}

	public void setPositionChanged(boolean changed) {
		EntityRef.positionChanged.set(getHandle(), changed);
	}

	public boolean isVelocityChanged() {
		return EntityRef.velocityChanged.get(getHandle());
	}

	public void setVelocityChanged(boolean changed) {
		EntityRef.velocityChanged.set(getHandle(), changed);
	}

	/**
	 * Gets whether the entity is hitting something, like an Entity or Block.
	 * If this returns True, then the Entity is unable to move freely.
	 * 
	 * @return True if movement is impaired, False if not
	 */
	public boolean isMovementImpaired() {
		// Note: this variable is simply wrongly deobfuscated!
		return getHandle(Entity.class).positionChanged;
	}

	/**
	 * Sets whether the entity is hitting something, and as a result can not move freely
	 * 
	 * @param impaired state to set to
	 */
	public void setMovementImpaired(boolean impaired) {
		// Note: this variable is simply wrongly deobfuscated!
		getHandle(Entity.class).positionChanged = impaired;
	}

	public Random getRandom() {
		return EntityRef.random.get(getHandle());
	}

	/**
	 * Plays a sound for the Entity, but with a slightly random pitch
	 * 
	 * @param sound to play
	 * @param volume to play at
	 * @param pitch (average) to play at
	 */
	public void makeRandomSound(Sound sound, float volume, float pitch) {
		makeRandomSound(CraftSound.getSound(sound), volume, pitch);
	}

	/**
	 * Plays a sound for the Entity, but with a slightly random pitch
	 * 
	 * @param soundName to play
	 * @param volume to play at
	 * @param pitch (average) to play at
	 */
	public void makeRandomSound(String soundName, float volume, float pitch) {
		final Random rand = getRandom();
		makeSound(soundName, volume, MathUtil.clamp(pitch + 0.4f * (rand.nextFloat() - rand.nextFloat()), 0.0f, 1.0f));
	}

	public void makeSound(Sound sound, float volume, float pitch) {
		makeSound(CraftSound.getSound(sound), volume, pitch);
	}

	public void makeSound(String soundName, float volume, float pitch) {
		final Entity handle = getHandle(Entity.class);
		handle.world.makeSound(handle, soundName, volume, pitch);
	}

	public void makeStepSound(org.bukkit.block.Block block) {
		makeStepSound(block.getX(), block.getY(), block.getZ(), block.getTypeId());
	}

	public void makeStepSound(int blockX, int blockY, int blockZ, int typeId) {
		if (CommonNMS.isValidBlockId(typeId)) {
			EntityRef.playStepSound(getHandle(), blockX, blockY, blockZ, typeId);
		}
	}

	public List<MetadataValue> getMetadata(String arg0) {
		return entity.getMetadata(arg0);
	}

	public boolean hasMetadata(String arg0) {
		return entity.hasMetadata(arg0);
	}

	public void removeMetadata(String arg0, Plugin arg1) {
		entity.removeMetadata(arg0, arg1);
	}

	public void setMetadata(String arg0, MetadataValue arg1) {
		entity.setMetadata(arg0, arg1);
	}

	public boolean eject() {
		return entity.eject();
	}

	public int getEntityId() {
		return entity.getEntityId();
	}

	public float getFallDistance() {
		return entity.getFallDistance();
	}

	public int getFireTicks() {
		return entity.getFireTicks();
	}

	public EntityDamageEvent getLastDamageCause() {
		return entity.getLastDamageCause();
	}

	public Location getLocation() {
		return entity.getLocation();
	}

	public Location getLastLocation() {
		return last.toLocation();
	}

	public Location getLocation(Location arg0) {
		return entity.getLocation(arg0);
	}

	public int getMaxFireTicks() {
		return entity.getMaxFireTicks();
	}

	public void setFootLocation(double x, double y, double z, float yaw, float pitch) {
		getHandle(Entity.class).setPositionRotation(x, y, z, yaw, pitch);
	}

	public void setLocation(double x, double y, double z, float yaw, float pitch) {
		getHandle(Entity.class).setLocation(x, y, z, yaw, pitch);
	}

	public void setRotation(float yaw, float pitch) {
		EntityRef.setRotation(getHandle(), yaw, pitch);
	}

	public void setPosition(double x, double y, double z) {
		getHandle(Entity.class).setPosition(x, y, z);
	}

	public List<org.bukkit.entity.Entity> getNearbyEntities(double radius) {
		return this.getNearbyEntities(radius, radius, radius);
	}

	public List<org.bukkit.entity.Entity> getNearbyEntities(double radX, double radY, double radZ) {
		return WorldUtil.getNearbyEntities(this.getEntity(), radX, radY, radZ);
	}

	public org.bukkit.entity.Entity getPassenger() {
		return entity.getPassenger();
	}

	public Player getPlayerPassenger() {
		return CommonUtil.tryCast(getPassenger(), Player.class);
	}

	public boolean hasPassenger() {
		return getHandle(Entity.class).passenger != null;
	}

	public boolean hasPlayerPassenger() {
		return getHandle(Entity.class).passenger instanceof EntityPlayer;
	}

	/**
	 * Gets whether this type of Entity is a type of Vehicle, allowing entities to enter it
	 * 
	 * @return True if this is a Vehicle, False if not
	 */
	public boolean isVehicle() {
		return entity instanceof Vehicle;
	}

	public Server getServer() {
		return entity.getServer();
	}

	public int getTicksLived() {
		return entity.getTicksLived();
	}

	public EntityType getType() {
		return entity.getType();
	}

	public UUID getUniqueId() {
		return entity.getUniqueId();
	}

	public org.bukkit.entity.Entity getVehicle() {
		return entity.getVehicle();
	}

	public Vector getVelocity() {
		return entity.getVelocity();
	}

	public World getWorld() {
		return entity.getWorld();
	}

	public boolean isDead() {
		return entity.isDead();
	}

	public boolean isEmpty() {
		return entity.isEmpty();
	}

	public boolean isInsideVehicle() {
		return entity.isInsideVehicle();
	}

	public boolean isValid() {
		return entity.isValid();
	}

	public boolean leaveVehicle() {
		return entity.leaveVehicle();
	}

	public void playEffect(EntityEffect arg0) {
		entity.playEffect(arg0);
	}

	public void remove() {
		entity.remove();
	}

	public void setFallDistance(float arg0) {
		entity.setFallDistance(arg0);
	}

	public void setFireTicks(int arg0) {
		entity.setFireTicks(arg0);
	}

	public void setLastDamageCause(EntityDamageEvent arg0) {
		entity.setLastDamageCause(arg0);
	}

	public boolean setPassenger(org.bukkit.entity.Entity arg0) {
		return entity.setPassenger(arg0);
	}

	public void setTicksLived(int arg0) {
		entity.setTicksLived(arg0);
	}

	public void setVelocity(Vector arg0) {
		entity.setVelocity(arg0);
	}

	public void setVelocity(double motX, double motY, double motZ) {
		final Entity handle = getHandle(Entity.class);
		handle.motX = motX;
		handle.motY = motY;
		handle.motZ = motZ;
	}

	public boolean teleport(Location arg0) {
		return entity.teleport(arg0);
	}

	public boolean teleport(org.bukkit.entity.Entity arg0) {
		return entity.teleport(arg0);
	}

	public boolean teleport(Location arg0, TeleportCause arg1) {
		return entity.teleport(arg0, arg1);
	}

	public boolean teleport(org.bukkit.entity.Entity arg0, TeleportCause arg1) {
		return entity.teleport(arg0, arg1);
	}

	/**
	 * Spawns an item as if dropped by this Entity
	 * 
	 * @param material of the item to drop
	 * @param amount of the material to drop
	 * @param force to drop at
	 * @return the dropped Item
	 */
	public Item spawnItemDrop(Material material, int amount, float force) {
		return CommonNMS.getItem(getHandle(Entity.class).a(material.getId(), amount, force));
	}

	/**
	 * Spawns an item as if dropped by this Entity
	 * 
	 * @param item to drop
	 * @param force to drop at
	 * @return the dropped Item
	 */
	public Item spawnItemDrop(org.bukkit.inventory.ItemStack item, float force) {
		return CommonNMS.getItem(getHandle(Entity.class).a(CommonNMS.getNative(item), force));
	}

}