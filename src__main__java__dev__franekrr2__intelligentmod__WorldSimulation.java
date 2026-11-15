package dev.franekrr2.intelligentmod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Serce moda: susza, parowanie, deszcz, powodzie i zapalanie drewna.
 * Działa tylko wokół graczy (promień SAMPLE_RADIUS), żeby nie obciążać serwera.
 */
public final class WorldSimulation {
	private WorldSimulation() {}

	private static int tickCounter = 0;
	/** Pozycje wody, które wyparowały - w deszczu wracają (jeziora się napełniają). */
	private static final Map<ResourceKey<Level>, Set<Long>> EVAPORATED = new HashMap<>();
	/** Woda z powodzi - po deszczu jest osuszana. */
	private static final Map<ResourceKey<Level>, Set<Long>> FLOOD = new HashMap<>();

	public static void onServerTick(MinecraftServer server) {
		if (++tickCounter % 20 != 0) return;
		boolean slow = tickCounter % 40 == 0;

		for (ServerLevel level : server.getAllLevels()) {
			List<ServerPlayer> players = level.players();
			if (players.isEmpty()) continue;

			if (slow) HeatManager.update(level, players);
			for (ServerPlayer p : players) {
				ThirstManager.tick(p, level);
				sample(level, p);
			}
			if (slow) {
				refillEvaporated(level);
				drainFlood(level);
				igniteWood(level);
			}
		}
	}

	// ------------------------------------------------------------------ losowanie kolumn

	private static void sample(ServerLevel level, ServerPlayer player) {
		RandomSource r = level.getRandom();
		int R = ModConfig.SAMPLE_RADIUS;
		for (int i = 0; i < ModConfig.SAMPLE_ATTEMPTS; i++) {
			int x = player.getBlockX() + r.nextInt(R * 2 + 1) - R;
			int z = player.getBlockZ() + r.nextInt(R * 2 + 1) - R;
			if (!level.hasChunkAt(new BlockPos(x, 0, z))) continue;
			int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
			process(level, new BlockPos(x, y, z), r);
		}
	}

	private static void process(ServerLevel level, BlockPos pos, RandomSource r) {
		BlockState st = level.getBlockState(pos);
		float heat = HeatManager.heatAt(level, pos);
		BlockPos above = pos.above();
		boolean rain = level.isRainingAt(above);

		// 1) Sucha Ziemia: ratuje ją woda w pobliżu albo deszcz (gdy nie jest zbyt gorąco)
		if (st.is(ModBlocks.DRY_EARTH)) {
			if (hasWaterNearby(level, pos, 3) || (rain && heat < ModConfig.HEAT_WARM)) {
				level.setBlock(pos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
			}
			return;
		}

		// 2) Ciepło wysusza trawę i ziemię
		if ((st.is(Blocks.GRASS_BLOCK) || st.is(Blocks.DIRT)) && heat >= ModConfig.HEAT_WARM) {
			float chance = Math.min(0.6f, (heat - 1f) * 0.15f);
			if (r.nextFloat() < chance && !hasWaterNearby(level, pos, 4)) {
				level.setBlock(pos, ModBlocks.DRY_EARTH.defaultBlockState(), Block.UPDATE_ALL);
			}
			return;
		}

		// 3) Upał odparowuje wodę
		if (st.is(Blocks.WATER) && level.getFluidState(pos).isSource() && heat >= ModConfig.HEAT_HOT) {
			if (r.nextFloat() < Math.min(0.5f, (heat - 3f) * 0.08f)) {
				level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
				EVAPORATED.computeIfAbsent(level.dimension(), k -> new HashSet<>()).add(pos.asLong());
			}
			return;
		}

		// 4) Deszcz: kałuże w dziurach, powodzie podczas burzy
		if (rain && level.getBlockState(above).isAir() && st.isFaceSturdy(level, pos, Direction.UP)) {
			int walls = 0;
			for (Direction d : Direction.Plane.HORIZONTAL) {
				BlockPos n = above.relative(d);
				if (level.getBlockState(n).isFaceSturdy(level, n, d.getOpposite())) walls++;
			}
			if (walls >= 3 && r.nextFloat() < 0.05f) {
				level.setBlock(above, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
				return;
			}
			if (level.isThundering() && r.nextFloat() < 0.12f) {
				Set<Long> flood = FLOOD.computeIfAbsent(level.dimension(), k -> new HashSet<>());
				if (flood.size() < ModConfig.FLOOD_LIMIT) {
					for (Direction d : Direction.Plane.HORIZONTAL) {
						BlockPos n = pos.relative(d);
						if (level.getBlockState(n).is(Blocks.WATER) && level.getFluidState(n).isSource()) {
							level.setBlock(above, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
							flood.add(above.asLong());
							break;
						}
					}
				}
			}
		}
	}

	private static boolean hasWaterNearby(ServerLevel level, BlockPos center, int radius) {
		BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				for (int dy = -1; dy <= 1; dy++) {
					m.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
					if (level.getFluidState(m).is(FluidTags.WATER)) return true;
				}
			}
		}
		return false;
	}

	// ------------------------------------------------------------------ jeziora i powodzie

	private static void refillEvaporated(ServerLevel level) {
		Set<Long> set = EVAPORATED.get(level.dimension());
		if (set == null || set.isEmpty()) return;
		RandomSource r = level.getRandom();
		int budget = 40;
		for (Long l : new ArrayList<>(set)) {
			if (budget-- <= 0) break;
			BlockPos p = BlockPos.of(l);
			if (!level.hasChunkAt(p)) continue;
			BlockState st = level.getBlockState(p);
			if (!st.isAir()) { set.remove(l); continue; }          // ktoś już tu coś postawił
			if (level.isRainingAt(p) && r.nextFloat() < 0.35f) {   // deszcz napełnia zbiornik
				level.setBlock(p, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
				set.remove(l);
			}
		}
	}

	private static void drainFlood(ServerLevel level) {
		Set<Long> set = FLOOD.get(level.dimension());
		if (set == null || set.isEmpty() || level.isRaining()) return;
		int budget = 40;
		for (Long l : new ArrayList<>(set)) {
			if (budget-- <= 0) break;
			BlockPos p = BlockPos.of(l);
			if (!level.hasChunkAt(p)) continue;
			if (level.getBlockState(p).is(Blocks.WATER) && level.getFluidState(p).isSource()) {
				level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			}
			set.remove(l);
		}
	}

	// ------------------------------------------------------------------ pożary

	private static boolean isWooden(BlockState s) {
		return s.is(BlockTags.PLANKS) || s.is(BlockTags.LOGS_THAT_BURN) || s.is(BlockTags.WOODEN_SLABS)
				|| s.is(BlockTags.WOODEN_STAIRS) || s.is(BlockTags.WOODEN_FENCES) || s.is(BlockTags.WOODEN_DOORS)
				|| s.is(BlockTags.WOODEN_TRAPDOORS) || s.is(Blocks.CHEST) || s.is(Blocks.BARREL)
				|| s.is(Blocks.BOOKSHELF) || s.is(Blocks.CRAFTING_TABLE);
	}

	/** Gdy w chunku jest bardzo gorąco, drewno przy płonących piecach zaczyna się palić. */
	private static void igniteWood(ServerLevel level) {
		RandomSource r = level.getRandom();
		int budget = 64;
		for (BlockPos furnace : HeatManager.litSources(level)) {
			if (budget-- <= 0) break;
			if (HeatManager.heatAt(level, furnace) < ModConfig.HEAT_HOT || r.nextFloat() > 0.10f) continue;
			for (int t = 0; t < 12; t++) {
				BlockPos p = furnace.offset(r.nextInt(7) - 3, r.nextInt(5) - 2, r.nextInt(7) - 3);
				if (!level.hasChunkAt(p) || !isWooden(level.getBlockState(p))) continue;
				for (Direction d : Direction.values()) {
					BlockPos ap = p.relative(d);
					if (!level.getBlockState(ap).isAir()) continue;
					BlockState fire = BaseFireBlock.getState(level, ap);
					if (fire.canSurvive(level, ap)) {
						level.setBlock(ap, fire, Block.UPDATE_ALL_IMMEDIATE);
						break;
					}
				}
				break;
			}
		}
	}
}
