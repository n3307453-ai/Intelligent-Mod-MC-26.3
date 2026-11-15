package dev.franekrr2.intelligentmod;

import com.mojang.brigadier.CommandDispatcher;
import java.util.Locale;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class ModCommands {
	private ModCommands() {}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx,
			Commands.CommandSelection env) {
		dispatcher.register(Commands.literal("intelligent")
				.then(Commands.literal("status").executes(c -> {
					ServerPlayer p = c.getSource().getPlayerOrException();
					float heat = HeatManager.heatAt(p.level(), p.blockPosition());
					float temp = p.getAttachedOrElse(ModAttachments.AMBIENT_TEMP, 20f);
					c.getSource().sendSuccess(() -> Component.translatable("command.intelligent_mod.status",
							String.format(Locale.ROOT, "%.1f", ThirstManager.get(p)),
							String.format(Locale.ROOT, "%.1f", heat),
							String.format(Locale.ROOT, "%.0f", temp)), false);
					return 1;
				})));
	}
}
