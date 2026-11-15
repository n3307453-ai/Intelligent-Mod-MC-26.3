package dev.franekrr2.intelligentmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Ruchome Piaski: jak Powder Snow, ale gęstsze i groźniejsze - bardzo mocno spowalnia,
 * wciąga w dół, a gdy głowa jest pod powierzchnią, gracz się dusi. Ma własną
 * animowaną teksturę oraz bąbelki piasku.
 */
public class QuicksandBlock extends Block {
	private static final VoxelShape OUTLINE = Block.box(0, 0, 0, 16, 14, 16);

	public QuicksandBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return Shapes.empty(); // wpadasz do środka
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return OUTLINE;
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
			InsideBlockEffectApplier effectApplier, boolean isPrecise) {
		// x/z bardzo wolno, y: powoli toniesz, skok prawie nic nie daje
		entity.makeStuckInBlock(state, new Vec3(0.3, 0.15, 0.3));

		if (level.isClientSide()) {
			RandomSource r = level.getRandom();
			if (r.nextInt(4) == 0) {
				level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()),
						entity.getX(), pos.getY() + 0.95, entity.getZ(), 0.0, 0.08, 0.0);
			}
		} else if (entity instanceof LivingEntity && level instanceof ServerLevel server
				&& entity.tickCount % 20 == 0
				&& BlockPos.containing(entity.getEyePosition()).equals(pos)) {
			entity.hurtServer(server, server.damageSources().inWall(), 2.0f); // głowa pod piaskiem = duszenie
		}
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextInt(8) == 0) {
			level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()),
					pos.getX() + random.nextDouble(), pos.getY() + 0.88, pos.getZ() + random.nextDouble(),
					0.0, 0.04, 0.0);
		}
	}
}
