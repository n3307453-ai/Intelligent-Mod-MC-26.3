package dev.franekrr2.intelligentmod.client;

import dev.franekrr2.intelligentmod.IntelligentMod;
import dev.franekrr2.intelligentmod.ModAttachments;
import dev.franekrr2.intelligentmod.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public class IntelligentModClient implements ClientModInitializer {
	private static final Identifier LOGO = IntelligentMod.id("textures/gui/logo.png");
	private static final int LOGO_W = 512, LOGO_H = 288;

	private static KeyMapping infoKey;

	@Override
	public void onInitializeClient() {
		// Prawy Shift = ekran "o modzie" (zmienisz w Sterowaniu)
		infoKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.intelligent_mod.info", InputConstants.Type.KEYSYM, InputConstants.KEY_RSHIFT,
				KeyMapping.Category.register(IntelligentMod.id("main"))));
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (infoKey.consumeClick()) {
				if (mc.screen == null) mc.setScreenAndShow(new InfoScreen());
			}
		});

		// HUD: logo w rogu + pasek wody
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, IntelligentMod.id("hud"),
				IntelligentModClient::renderHud);

		// Menu główne: logo + "Made by FranekRR2 / Sagzamon"
		ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
			if (screen instanceof TitleScreen) {
				ScreenEvents.afterExtract(screen).register((s, graphics, mouseX, mouseY, tickDelta) -> {
					graphics.blit(RenderPipelines.GUI_TEXTURED, LOGO, 8, 8, 0f, 0f, 160, 90, LOGO_W, LOGO_H, LOGO_W, LOGO_H);
					graphics.text(Minecraft.getInstance().font,
							Component.translatable("menu.intelligent_mod.credits"), 12, 100, 0xFFFFFFFF, true);
				});
			}
		});
	}

	private static void renderHud(GuiGraphicsExtractor g, DeltaTracker tracker) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.options.hideGui) return;
		int w = mc.getWindow().getGuiScaledWidth();
		int h = mc.getWindow().getGuiScaledHeight();

		// logo w prawym górnym rogu
		g.blit(RenderPipelines.GUI_TEXTURED, LOGO, w - 84, 4, 0f, 0f, 80, 45, LOGO_W, LOGO_H, LOGO_W, LOGO_H);

		if (mc.player.isCreative() || mc.player.isSpectator()) return;

		float thirst = mc.player.getAttachedOrElse(ModAttachments.THIRST, ModConfig.THIRST_MAX);
		float temp = mc.player.getAttachedOrElse(ModAttachments.AMBIENT_TEMP, 20f);

		int x = w / 2 + 10;
		int y = h - 58;
		boolean blink = (Util.getMillis() / 400) % 2 == 0;
		int fillColor = thirst <= 5f ? (blink ? 0xFFFF5555 : 0xFF992222) : 0xFF3FA9FF;

		g.fill(x - 1, y - 1, x + 82, y + 7, 0xAA000000);
		g.fill(x, y, x + Math.round(81f * thirst / ModConfig.THIRST_MAX), y + 6, fillColor);
		g.text(mc.font, Component.translatable("hud.intelligent_mod.thirst"), x, y - 10, 0xFF8FD0FF, true);
		g.text(mc.font, String.format("%d°C", Math.round(temp)), x + 52, y - 10, temp >= 35 ? 0xFFFF8855 : 0xFFDDDDDD, true);
	}
}
