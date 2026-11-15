package dev.franekrr2.intelligentmod;

import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {
	private ModBlocks() {}

	public static final Block DRY_EARTH = register("dry_earth", Block::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_YELLOW).strength(0.5f).sound(SoundType.GRAVEL));

	public static final Block QUICKSAND = register("quicksand", QuicksandBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(0.5f).sound(SoundType.SAND)
					.noOcclusion().isViewBlocking((s, l, p) -> false).isSuffocating((s, l, p) -> false));

	private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, IntelligentMod.id(name));
		Block block = factory.apply(props.setId(blockKey));
		Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, IntelligentMod.id(name));
		Registry.register(BuiltInRegistries.ITEM, itemKey,
				new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
		return block;
	}

	public static void init() {
		ResourceKey<CreativeModeTab> tabKey = ResourceKey.create(Registries.CREATIVE_MODE_TAB, IntelligentMod.id("main"));
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, tabKey,
				CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
						.title(Component.translatable("itemGroup.intelligent_mod"))
						.icon(() -> new ItemStack(DRY_EARTH.asItem()))
						.displayItems((params, output) -> {
							output.accept(DRY_EARTH);
							output.accept(QUICKSAND);
						})
						.build());
	}
}
