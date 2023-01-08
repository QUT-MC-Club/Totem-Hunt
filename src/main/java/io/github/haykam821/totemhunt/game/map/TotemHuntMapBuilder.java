package io.github.haykam821.totemhunt.game.map;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import io.github.haykam821.totemhunt.game.TotemHuntConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateMetadata;
import xyz.nucleoid.map_templates.MapTemplateSerializer;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.game.GameOpenException;

public class TotemHuntMapBuilder {
	private final TotemHuntConfig config;

	public TotemHuntMapBuilder(TotemHuntConfig config) {
		this.config = config;
	}

	public TotemHuntMap create(MinecraftServer server) {
		try {
			MapTemplate template = MapTemplateSerializer.loadFromResource(server, this.config.getMap());

			BlockBounds waitingSpawn = getWaitingSpawn(template.getMetadata());
			List<BlockBounds> spawns = getSpawns(template.getMetadata(), waitingSpawn);

			return new TotemHuntMap(template, waitingSpawn, spawns);
		} catch (IOException e) {
			throw new GameOpenException(Text.translatable("text.totemhunt.template_load_failed"), e);
		}
	}

	public static BlockBounds getWaitingSpawn(MapTemplateMetadata metadata) {
		BlockBounds spawn = metadata.getFirstRegionBounds("waiting_spawn");
		if (spawn != null) return spawn;

		spawn = metadata.getFirstRegionBounds("spawn");
		if (spawn != null) return spawn;

		return BlockBounds.ofBlock(BlockPos.ORIGIN);
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
