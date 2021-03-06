package com.bergerkiller.bukkit.common.reflection.classes;

import java.util.Locale;

import net.minecraft.server.v1_5_R2.TileEntitySkull;

import org.bukkit.Chunk;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_5_R2.block.CraftBlockState;
import org.bukkit.material.MaterialData;

import com.bergerkiller.bukkit.common.collections.ClassMap;
import com.bergerkiller.bukkit.common.reflection.ClassTemplate;
import com.bergerkiller.bukkit.common.reflection.FieldAccessor;
import com.bergerkiller.bukkit.common.reflection.MethodAccessor;
import com.bergerkiller.bukkit.common.reflection.NMSClassTemplate;
import com.bergerkiller.bukkit.common.utils.BlockUtil;
import com.bergerkiller.bukkit.common.utils.CommonUtil;
import com.bergerkiller.bukkit.common.utils.LogicUtil;

public class BlockStateRef {
	public static final ClassTemplate<?> TEMPLATE = ClassTemplate.create(CommonUtil.getCBClass("block.CraftBlockState"));
	private static final ClassMap<TileInstantiator> tileToInst = new ClassMap<TileInstantiator>();
	private static final ClassMap<TileInstantiator> stateToInst = new ClassMap<TileInstantiator>();
	public static final FieldAccessor<World> world = TEMPLATE.getField("world");
	public static final FieldAccessor<Chunk> chunk = TEMPLATE.getField("chunk");
	public static final FieldAccessor<Integer> x = TEMPLATE.getField("x");
	public static final FieldAccessor<Integer> y = TEMPLATE.getField("y");
	public static final FieldAccessor<Integer> z = TEMPLATE.getField("z");
	public static final FieldAccessor<Integer> type = TEMPLATE.getField("type");
	public static final FieldAccessor<MaterialData> data = TEMPLATE.getField("data");
	public static final FieldAccessor<Byte> light = TEMPLATE.getField("light");

	private static void registerInst(TileInstantiator inst) {
		tileToInst.put(inst.TILE.getType(), inst);
		stateToInst.put(inst.STATE.getType(), inst);
	}

	static {
		// Initialize some instantiators
		registerInst(new TileInstantiator("Sign") {
			private final FieldAccessor<String[]> state_lines = STATE.getField("lines");
			private final FieldAccessor<String[]> tile_lines = TILE.getField("lines");

			@Override
			protected void apply(BlockState state, Object tile) {
				state_lines.set(state, LogicUtil.cloneArray(tile_lines.get(tile)));
			}
		});
		registerInst(new TileInstantiator("Skull") {
			private final FieldAccessor<String> state_player = STATE.getField("player");
			private final FieldAccessor<SkullType> state_type = STATE.getField("skullType");
			private final FieldAccessor<Byte> state_rotation = STATE.getField("rotation");
			private final MethodAccessor<SkullType> state_getSkullType = STATE.getMethod("getSkullType", int.class);

			@Override
			protected void apply(BlockState state, Object tile) {
				TileEntitySkull t = (TileEntitySkull) tile;
				state_player.set(state, t.getExtraType());
				state_type.set(state, state_getSkullType.invoke(null, t.getSkullType()));
				state_rotation.set(state, (byte) t.getRotation());
			}
		});
		registerInst(new TileInstantiator("Command", "CommandBlock", "commandBlock") {
			private final FieldAccessor<String> state_command = STATE.getField("command");
			private final FieldAccessor<String> tile_command = TILE.getField("b");
			private final FieldAccessor<String> state_name = STATE.getField("name");
			private final MethodAccessor<String> tile_getName = TILE.getMethod("getName");

			@Override
			protected void apply(BlockState state, Object tile) {
				state_command.set(state, tile_command.get(tile));
				state_name.set(state, tile_getName.invoke(tile));
			}
		});
		registerInst(new TileInstantiator("Furnace"));
		registerInst(new TileInstantiator("Dispenser"));
		registerInst(new TileInstantiator("Chest"));
		registerInst(new TileInstantiator("Dropper"));
		registerInst(new TileInstantiator("Beacon"));
		registerInst(new TileInstantiator("Hopper"));
		registerInst(new TileInstantiator("Chest"));
		registerInst(new TileInstantiator("MobSpawner", "CreatureSpawner", "spawner"));
		registerInst(new TileInstantiator("Note", "NoteBlock", "note"));
		registerInst(new TileInstantiator("RecordPlayer", "Jukebox", "jukebox"));
	}

	public static Object toTileEntity(BlockState state) {
		TileInstantiator inst = stateToInst.get(state);
		if (inst == null) {
			return TileEntityRef.getFromWorld(state.getBlock());
		} else {
			return inst.getTileHandle(state);
		}
	}

	public static BlockState toBlockState(Block block) {
		Object tileEntity = TileEntityRef.getFromWorld(block);
		if (tileEntity != null) {
			TileInstantiator inst = tileToInst.get(tileEntity);
			if (inst != null) {
				return inst.newInstance(tileEntity);
			}
		}
		// All BlockState types REQUIRE a tile entity, just return the default BlockState here
		return new CraftBlockState(block);
	}

	public static BlockState toBlockState(Object tileEntity) {
		TileInstantiator inst = tileToInst.get(tileEntity);
		if (inst == null) {
			return toBlockState(TileEntityRef.getBlock(tileEntity));
		} else {
			return inst.newInstance(tileEntity);
		}
	}

	private static class TileInstantiator {
		private final FieldAccessor<Object> tileField;
		private final FieldAccessor<World> secondWorld;
		protected final ClassTemplate<? extends BlockState> STATE;
		protected final ClassTemplate<?> TILE;

		public TileInstantiator(String name) {
			this(name, name, name.toLowerCase(Locale.ENGLISH));
		}

		@SuppressWarnings("unchecked")
		public TileInstantiator(String tileName, String stateName, String tileFieldName) {
			this.TILE = NMSClassTemplate.create("TileEntity" + tileName);
			this.STATE = ClassTemplate.create((Class<? extends BlockState>) CommonUtil.getCBClass("block.Craft" + stateName));
			this.tileField = this.STATE.getField(tileFieldName);
			// Second world, yes, Bukkit is stupid enough to have two world fields. LOL.
			this.secondWorld = this.STATE.getField("world");
		}

		public Object getTileHandle(Object state) {
			return tileField.get(state);
		}

		protected void apply(BlockState state, Object tile) {
		}

		public BlockState newInstance(Object tileEntity) {
			final BlockState state = STATE.newInstance(false);
			final Block block = TileEntityRef.getBlock(tileEntity);
			final int typeId = block.getTypeId();
			tileField.set(state, tileEntity);
			world.set(state, block.getWorld());
			secondWorld.set(state, block.getWorld());
			chunk.set(state, block.getChunk());
			type.set(state, typeId);
			light.set(state, block.getLightLevel());
			x.set(state, block.getX());
			y.set(state, block.getY());
			z.set(state, block.getZ());
			data.set(state, BlockUtil.getData(typeId, block.getData()));
			this.apply(state, tileEntity);
			return state;
		}
	}
}
