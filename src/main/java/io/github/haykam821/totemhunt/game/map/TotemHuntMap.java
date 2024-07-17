package io.github.haykam821.totemhunt.game.map;

import java.util.List;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator;

public class TotemHuntMap {
	private final MapTemplate template;
	private final BlockBounds waitingSpawn;
	private final List<BlockBounds> spawns;
	private final Vec3d guideTextPos;

	public TotemHuntMap(MapTemplate template, BlockBounds waitingSpawn, List<BlockBounds> spawns, BlockBounds guideTextBounds) {
		this.template = template;
		this.waitingSpawn = waitingSpawn;
		this.spawns = spawns;
		this.guideTextPos = guideTextBounds == null ? null : guideTextBounds.center();
	}

	public BlockBounds getWaitingSpawn() {
		return this.waitingSpawn;
	}

	public List<BlockBounds> getSpawns() {
		return this.spawns;
	}

	public Vec3d getGuideTextPos() {
		return this.guideTextPos;
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}
}
