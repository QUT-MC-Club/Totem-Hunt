package io.github.haykam821.totemhunt.game.map;

import io.github.haykam821.totemhunt.game.TotemHuntConfig;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.MapTemplateMetadata;
import xyz.nucleoid.plasmid.map.template.MapTemplateSerializer;
import xyz.nucleoid.plasmid.map.template.TemplateRegion;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class TotemHuntMapBuilder {
	private final TotemHuntConfig config;

	public TotemHuntMapBuilder(TotemHuntConfig config) {
		this.config = config;
	}

	public TotemHuntMap create() {
		try {
			MapTemplate template = MapTemplateSerializer.INSTANCE.loadFromResource(this.config.getMap());

			BlockBounds waitingSpawn = getWaitingSpawn(template.getMetadata());
			List<BlockBounds> spawns = getSpawns(template.getMetadata(), waitingSpawn);

			return new TotemHuntMap(template, waitingSpawn, spawns);
		} catch (IOException e) {
			throw new GameOpenException(new LiteralText("Failed to load template"), e);
		}
	}

	public static BlockBounds getWaitingSpawn(MapTemplateMetadata metadata) {
		BlockBounds spawn = metadata.getFirstRegionBounds("waiting_spawn");
		if (spawn != null) return spawn;

		spawn = metadata.getFirstRegionBounds("spawn");
		if (spawn != null) return spawn;

		return BlockBounds.of(BlockPos.ORIGIN);
	}

	public static List<BlockBounds> getSpawns(MapTemplateMetadata metadata, BlockBounds waitingSpawn) {
		List<BlockBounds> spawns = metadata.getRegions("spawn").sorted((regionA, regionB) -> {
			if (regionB.getData() == null) return -1;
			if (regionA.getData() == null) return 1;

			return regionB.getData().getInt("Priority") - regionA.getData().getInt("Priority");
		}).map(TemplateRegion::getBounds).collect(Collectors.toList());

		if (spawns.isEmpty()) {
			spawns.add(waitingSpawn);
		}
		return spawns;
	}
}
