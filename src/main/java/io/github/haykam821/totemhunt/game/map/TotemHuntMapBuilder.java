package io.github.haykam821.totemhunt.game.map;

import java.util.concurrent.CompletableFuture;

import io.github.haykam821.totemhunt.game.TotemHuntConfig;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.game.map.template.MapTemplateSerializer;
import xyz.nucleoid.plasmid.util.BlockBounds;

public class TotemHuntMapBuilder {
	private final TotemHuntConfig config;

	public TotemHuntMapBuilder(TotemHuntConfig config) {
		this.config = config;
	}

	public CompletableFuture<TotemHuntMap> create() {
		return MapTemplateSerializer.INSTANCE.load(this.config.getMap()).thenApply(template -> {
			BlockBounds spawn = template.getFirstRegion("spawn");
			if (spawn == null) {
				return new TotemHuntMap(template, BlockBounds.of(new BlockPos(0, 0, 0)));
			}

			return new TotemHuntMap(template, spawn);
		});
	}
}