package dev.franekrr2.intelligentmod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Liczy "ocieplenie" na chunk: im więcej płonących pieców, tym cieplej.
 * Dane liczone są tylko wokół graczy (nie zapisują się - odtwarzają się same w ciągu sekund).
 */
public final class HeatManager {
	private HeatManager() {}

	private static final Map<ResourceKey<Level>, Map<Long, Float>> HEAT = new HashMap<>();
	private static final Map<ResourceKey<Level>, List<BlockPos>> SOURCES = new HashMap<>();

	public static void update(ServerLevel level, List<ServerPlayer> players) {
		int radius = ModConfig.HEAT_SCAN_CHUNK_RADIUS;
		Set<Long> chunks = new HashSet<>();
		for (ServerPlayer p : players) {
			int pcx = p.getBlockX() >> 4;
			int pcz = p.getBlockZ() >> 4;
			for (int dx = -radius; dx <= radius; dx++) {
				for (int dz = -radius; dz <= radius; dz++) {
					chunks.add(ChunkPos.asLong(pcx + dx, pcz + dz));
				}
			}
		}

		Map<Long, Float> direct = new HashMap<>();
		List<BlockPos> sources = new ArrayList<>();
		for (long c : chunks) {
			LevelChunk chunk = level.getChunkSource().getChunkNow(ChunkPos.getX(c), ChunkPos.getZ(c));
			if (chunk == null) continue;
			float value = 0f;
			for (BlockEntity be : chunk.getBlockEntities().values()) {
				BlockState st = be.getBlockState();
				if (!st.hasProperty(BlockStateProperties.LIT) || !st.getValue(BlockStateProperties.LIT)) continue;
				if (be instanceof AbstractFurnaceBlockEntity) {
					value += st.is(Blocks.BLAST_FURNACE) ? ModConfig.HEAT_BLAST_FURNACE : ModConfig.HEAT_FURNACE;
					sources.add(be.getBlockPos().immutable());
				} else if (be instanceof CampfireBlockEntity) {
					value += ModConfig.HEAT_CAMPFIRE;
				}
			}
			if (value > 0f) direct.put(c, value);
		}

		Map<Long, Float> total = new HashMap<>();
		for (long c : chunks) {
			int cx = ChunkPos.getX(c);
			int cz = ChunkPos.getZ(c);
			float sum = direct.getOrDefault(c, 0f);
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					if (dx == 0 && dz == 0) continue;
					sum += ModConfig.HEAT_NEIGHBOUR_FACTOR * direct.getOrDefault(ChunkPos.asLong(cx + dx, cz + dz), 0f);
				}
			}
			if (sum > 0.01f) total.put(c, sum);
		}
		HEAT.put(level.dimension(), total);
		SOURCES.put(level.dimension(), sources);
	}

	public static float heatAt(ServerLevel level, BlockPos pos) {
		Map<Long, Float> m = HEAT.get(level.dimension());
		return m == null ? 0f : m.getOrDefault(ChunkPos.asLong(pos), 0f);
	}

	public static List<BlockPos> litSources(ServerLevel level) {
		return SOURCES.getOrDefault(level.dimension(), Collections.emptyList());
	}
}
