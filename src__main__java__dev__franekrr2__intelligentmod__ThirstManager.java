package dev.franekrr2.intelligentmod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class ThirstManager {
	private ThirstManager() {}

	public static final ResourceKey<DamageType> DEHYDRATION =
			ResourceKey.create(Registries.DAMAGE_TYPE, IntelligentMod.id("dehydration"));

	// Przedmioty, które nawadniają (wykrywane przez statystykę "użyto przedmiotu")
	private static final Item[] DRINKS = {Items.POTION, Items.MILK_BUCKET, Items.HONEY_BOTTLE, Items.MELON_SLICE, Items.APPLE};
	private static final float[] DRINK_GAIN = {6f, 4f, 3f, 2f, 1f};

	private static final Map<UUID, int[]> LAST_USE = new HashMap<>();
	private static final Map<UUID, Integer> ZERO_SECONDS = new HashMap<>();
	private static final Map<UUID, Long> LAST_DRINK = new HashMap<>();

	public static float get(Player p) {
		return p.getAttachedOrElse(ModAttachments.THIRST, ModConfig.THIRST_MAX);
	}

	/** Wywoływane raz na sekundę dla każdego gracza. */
	public static void tick(ServerPlayer player, ServerLevel level) {
		float heat = HeatManager.heatAt(level, player.blockPosition());

		// temperatura otoczenia do HUD
		float biomeTemp = level.getBiome(player.blockPosition()).value().getBaseTemperature();
		float celsius = 5f + biomeTemp * 20f + heat * 4f;
		Float oldTemp = player.getAttachedOrElse(ModAttachments.AMBIENT_TEMP, 20f);
		if (Math.abs(oldTemp - celsius) > 0.5f) player.setAttached(ModAttachments.AMBIENT_TEMP, celsius);

		if (player.isCreative() || player.isSpectator()) {
			if (get(player) < ModConfig.THIRST_MAX) player.setAttached(ModAttachments.THIRST, ModConfig.THIRST_MAX);
			return;
		}

		UUID id = player.getUUID();
		float before = get(player);
		float thirst = before;

		// wykrywanie wypitych/zjedzonych nawadniających rzeczy
		int[] last = LAST_USE.get(id);
		int[] now = new int[DRINKS.length];
		for (int i = 0; i < DRINKS.length; i++) {
			now[i] = player.getStats().getValue(Stats.ITEM_USED.get(DRINKS[i]));
		}
		if (last != null) {
			for (int i = 0; i < DRINKS.length; i++) {
				if (now[i] > last[i]) thirst += DRINK_GAIN[i] * (now[i] - last[i]);
			}
		}
		LAST_USE.put(id, now);

		// ubywanie wody
		float mult = 1f;
		if (player.isSprinting()) mult *= 2f;
		if (biomeTemp >= 1.5f) mult *= 1.5f;
		if (heat >= ModConfig.HEAT_HOT) mult *= 2f;
		else if (heat >= ModConfig.HEAT_WARM) mult *= 1.4f;
		thirst -= ModConfig.THIRST_DRAIN_PER_SECOND * mult;

		// deszcz po twarzy trochę nawadnia
		if (level.isRainingAt(player.blockPosition())) thirst += ModConfig.RAIN_GAIN_PER_SECOND;

		thirst = Math.max(0f, Math.min(ModConfig.THIRST_MAX, thirst));
		player.setAttached(ModAttachments.THIRST, thirst);

		if (before > 6f && thirst <= 6f) {
			player.sendSystemMessage(Component.translatable("message.intelligent_mod.thirsty"), true);
		}

		if (thirst <= 0f) {
			int sec = ZERO_SECONDS.merge(id, 1, Integer::sum);
			if (sec % 5 == 0) player.sendSystemMessage(Component.translatable("message.intelligent_mod.dying"), true);
			if (sec >= ModConfig.DEATH_DELAY_SECONDS) {
				ZERO_SECONDS.remove(id);
				DamageSource src = new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DEHYDRATION));
				player.hurtServer(level, src, 1000f);
			}
		} else {
			ZERO_SECONDS.remove(id);
		}
	}

	/** Prawy przycisk pustą ręką na wodzie = picie. */
	public static InteractionResult onUseItem(Player player, Level level, InteractionHand hand) {
		if (hand != InteractionHand.MAIN_HAND || !player.getMainHandItem().isEmpty()) return InteractionResult.PASS;
		HitResult hit = player.pick(4.5, 0f, true);
		if (!(hit instanceof BlockHitResult bhr) || hit.getType() != HitResult.Type.BLOCK) return InteractionResult.PASS;
		if (!level.getFluidState(bhr.getBlockPos()).is(FluidTags.WATER)) return InteractionResult.PASS;

		if (player instanceof ServerPlayer sp && !sp.isCreative()) {
			long now = level.getGameTime();
			Long last = LAST_DRINK.get(sp.getUUID());
			if (last == null || now - last >= 8) {
				LAST_DRINK.put(sp.getUUID(), now);
				float t = Math.min(ModConfig.THIRST_MAX, get(sp) + ModConfig.DRINK_FROM_WATER_GAIN);
				sp.setAttached(ModAttachments.THIRST, t);
				level.playSound(null, sp.blockPosition(), SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 0.5f, 1.4f);
				if (level.getRandom().nextFloat() < ModConfig.DIRTY_WATER_CHANCE) {
					sp.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 0));
				}
			}
		}
		return InteractionResult.SUCCESS;
	}
}
