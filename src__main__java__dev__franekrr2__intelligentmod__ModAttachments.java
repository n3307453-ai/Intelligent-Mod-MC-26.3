package dev.franekrr2.intelligentmod;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;

/** Dane gracza zsynchronizowane z klientem (do HUD). */
public final class ModAttachments {
	private ModAttachments() {}

	/** Poziom wody 0..20, zapisywany ze światem. */
	public static final AttachmentType<Float> THIRST = AttachmentRegistry.<Float>create(
			IntelligentMod.id("thirst"),
			b -> b.initializer(() -> ModConfig.THIRST_MAX)
					.persistent(Codec.FLOAT)
					.syncWith(ByteBufCodecs.FLOAT, AttachmentSyncPredicate.targetOnly()));

	/** Temperatura otoczenia w °C (tylko do wyświetlania). */
	public static final AttachmentType<Float> AMBIENT_TEMP = AttachmentRegistry.<Float>create(
			IntelligentMod.id("ambient_temp"),
			b -> b.initializer(() -> 20f)
					.syncWith(ByteBufCodecs.FLOAT, AttachmentSyncPredicate.targetOnly()));

	public static void init() {}
}
