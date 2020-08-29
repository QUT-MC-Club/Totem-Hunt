package io.github.haykam821.totemhunt.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;

public class TotemHuntConfig {
	public static final Codec<TotemHuntConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Identifier.CODEC.fieldOf("map").forGetter(TotemHuntConfig::getMap),
			Codec.INT.optionalFieldOf("totems", 1).forGetter(TotemHuntConfig::getTotems),
			PlayerConfig.CODEC.fieldOf("players").forGetter(TotemHuntConfig::getPlayerConfig)
		).apply(instance, TotemHuntConfig::new);
	});

	private final Identifier map;
	private final int totems;
	private final PlayerConfig playerConfig;

	public TotemHuntConfig(Identifier map, int totems, PlayerConfig playerConfig) {
		this.map = map;
		this.totems = totems;
		this.playerConfig = playerConfig;
	}

	public Identifier getMap() {
		return this.map;
	}

	public int getTotems() {
		return this.totems;
	}

	public PlayerConfig getPlayerConfig() {
		return this.playerConfig;
	}
}