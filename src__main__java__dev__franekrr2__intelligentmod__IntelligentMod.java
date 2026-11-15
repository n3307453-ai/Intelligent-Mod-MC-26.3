package dev.franekrr2.intelligentmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntelligentMod implements ModInitializer {
	public static final String MOD_ID = "intelligent_mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModBlocks.init();
		ModAttachments.init();

		ServerTickEvents.END_SERVER_TICK.register(WorldSimulation::onServerTick);
		UseItemCallback.EVENT.register(ThirstManager::onUseItem);
		CommandRegistrationCallback.EVENT.register(ModCommands::register);

		// Ruchome Piaski w pustyniach i na plażach
		BiomeModifications.addFeature(
				BiomeSelectors.includeByKey(Biomes.DESERT, Biomes.BEACH),
				GenerationStep.Decoration.TOP_LAYER_MODIFICATION,
				ResourceKey.create(Registries.PLACED_FEATURE, id("quicksand_patch")));

		LOGGER.info("Intelligent Mod loaded - made by FranekRR2 / Sagzamon");
	}
}
