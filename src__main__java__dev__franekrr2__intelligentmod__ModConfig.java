package dev.franekrr2.intelligentmod;

/** Wszystkie ustawienia balansu w jednym miejscu. Zmień i przebuduj. */
public final class ModConfig {
	private ModConfig() {}

	// --- Pragnienie ---
	public static final float THIRST_MAX = 20f;
	/** Ile punktów wody ubywa na sekundę w spokoju (1/45 = pełny pasek na ok. 15 min). */
	public static final float THIRST_DRAIN_PER_SECOND = 1f / 45f;
	/** Po ilu sekundach z pustym paskiem gracz umiera. */
	public static final int DEATH_DELAY_SECONDS = 20;
	public static final float DRINK_FROM_WATER_GAIN = 2f;
	public static final float RAIN_GAIN_PER_SECOND = 0.05f;
	/** Szansa na efekt głodu po piciu prosto z jeziora/rzeki. */
	public static final float DIRTY_WATER_CHANCE = 0.12f;

	// --- Ciepło (suma "punktów ciepła" z płonących pieców w chunku + 35% z sąsiednich) ---
	public static final float HEAT_FURNACE = 1.0f;
	public static final float HEAT_BLAST_FURNACE = 1.5f;
	public static final float HEAT_CAMPFIRE = 0.5f;
	public static final float HEAT_NEIGHBOUR_FACTOR = 0.35f;
	public static final float HEAT_WARM = 2f;   // zaczyna się wysuszanie ziemi
	public static final float HEAT_HOT = 4f;    // parowanie wody + zapalanie drewna

	// --- Symulacja świata ---
	public static final int SAMPLE_RADIUS = 40;
	public static final int SAMPLE_ATTEMPTS = 24;
	public static final int HEAT_SCAN_CHUNK_RADIUS = 4;
	public static final int FLOOD_LIMIT = 3000;
}
