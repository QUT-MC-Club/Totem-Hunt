package io.github.haykam821.totemhunt.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.plasmid.game.map.template.MapTemplate;
import xyz.nucleoid.plasmid.game.map.template.TemplateChunkGenerator;
import xyz.nucleoid.plasmid.util.BlockBounds;

public class TotemHuntMap {
	private final MapTemplate template;
	private final BlockBounds spawn;

	public TotemHuntMap(MapTemplate template, BlockBounds spawn) {
		this.template = template;
		this.spawn = spawn;
	}

	public BlockBounds getSpawn() {
		return this.spawn;
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template, BlockPos.ORIGIN);
	}
}