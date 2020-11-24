package io.github.haykam821.totemhunt.game.map;

import io.github.haykam821.totemhunt.game.TotemHuntConfig;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.MapTemplateSerializer;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.io.IOException;

public class TotemHuntMapBuilder {
	private final TotemHuntConfig config;

	public TotemHuntMapBuilder(TotemHuntConfig config) {
		this.config = config;
	}

	public TotemHuntMap create() {
		try {
			MapTemplate template = MapTemplateSerializer.INSTANCE.loadFromResource(this.config.getMap());

			BlockBounds spawn = template.getMetadata().getFirstRegionBounds("spawn");
			if (spawn == null) {
				spawn = BlockBounds.of(new BlockPos(0, 0, 0));
			}

			return new TotemHuntMap(template, spawn);
		} catch (IOException e) {
			throw new GameOpenException(new LiteralText("Failed to load template"), e);
		}
	}
}
