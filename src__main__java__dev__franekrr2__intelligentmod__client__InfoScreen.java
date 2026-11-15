package dev.franekrr2.intelligentmod.client;

import dev.franekrr2.intelligentmod.IntelligentMod;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Ekran otwierany Prawym Shiftem: kto zrobił moda i co w nim jest. */
public class InfoScreen extends Screen {
	private static final Identifier LOGO = IntelligentMod.id("textures/gui/logo.png");
	private static final String[] LINES = {
			"info.intelligent_mod.heat", "info.intelligent_mod.thirst", "info.intelligent_mod.quicksand",
			"info.intelligent_mod.dry_earth", "info.intelligent_mod.water", "info.intelligent_mod.fire"
	};

	public InfoScreen() {
		super(Component.translatable("info.intelligent_mod.title"));
	}

	@Override
	protected void init() {
		addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
				.bounds(this.width / 2 - 50, this.height - 30, 100, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(g, mouseX, mouseY, partialTick);
		int cx = this.width / 2;
		int y = 14;

		g.blit(RenderPipelines.GUI_TEXTURED, LOGO, cx - 80, y, 0f, 0f, 160, 90, 512, 288, 512, 288);
		y += 96;

		y = centered(g, Component.translatable("info.intelligent_mod.authors"), cx, y, 0xFF8FD0FF) + 4;
		y = centered(g, Component.translatable("info.intelligent_mod.version"), cx, y, 0xFFAAAAAA) + 8;
		y = centered(g, Component.translatable("info.intelligent_mod.features"), cx, y, 0xFFFFFFFF) + 2;
		for (String key : LINES) {
			y = centered(g, Component.translatable(key), cx, y, 0xFFDDDDDD);
		}
	}

	private int centered(GuiGraphicsExtractor g, Component text, int cx, int y, int color) {
		g.text(this.font, text, cx - this.font.width(text) / 2, y, color, true);
		return y + 11;
	}
}
