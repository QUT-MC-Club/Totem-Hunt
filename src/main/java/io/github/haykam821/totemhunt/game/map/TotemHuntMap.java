package io.github.haykam821.totemhunt.game.map;

import java.util.List;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator;

public class TotemHuntMap {
	private final MapTemplate template;
	private final BlockBounds waitingSpawn;
	private final List<BlockBounds> spawns;

	public TotemHuntMap(MapTemplate template, BlockBounds waitingSpawn, List<BlockBounds> spawns) {
		this.template = template;
		this.waitingSpawn = waitingSpawn;
		this.spawns = spawns;
	}

	public BlockBounds getWaitingSpawn() {
		return this.waitingSpawn;
	}

	public List<BlockBounds> getSpawns() {
		return this.spawns;
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}
}
