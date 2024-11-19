package io.github.haykam821.totemhunt.game.map;

import java.util.List;
import java.util.Set;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.world.generator.TemplateChunkGenerator;

public class TotemHuntMap {
	private final MapTemplate template;
	private final Vec3d waitingSpawn;
	private final List<BlockBounds> spawns;
	private final Vec3d guideTextPos;

	public TotemHuntMap(MapTemplate template, BlockBounds waitingSpawn, List<BlockBounds> spawns, BlockBounds guideTextBounds) {
		this.template = template;
		this.waitingSpawn = waitingSpawn.center();
		this.spawns = spawns;
		this.guideTextPos = guideTextBounds == null ? null : guideTextBounds.center();
	}

	public Vec3d getWaitingSpawn() {
		return this.waitingSpawn;
	}

	public void teleportToWaitingSpawn(ServerPlayerEntity player, ServerWorld world) {
		player.teleport(world, this.waitingSpawn.getX(), this.waitingSpawn.getY(), this.waitingSpawn.getZ(), Set.of(), 0, 0, true);
	}

	public Vec3d getSpawn(int index) {
		BlockBounds spawn = this.spawns.get(index % this.spawns.size());
		return spawn.center();
	}

	public Vec3d getGuideTextPos() {
		return this.guideTextPos;
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}
}
