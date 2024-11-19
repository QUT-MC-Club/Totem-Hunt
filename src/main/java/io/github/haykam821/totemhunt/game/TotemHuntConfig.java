package io.github.haykam821.totemhunt.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.SharedConstants;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;

public class TotemHuntConfig {
	public static final MapCodec<TotemHuntConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> {
		return instance.group(
			Identifier.CODEC.fieldOf("map").forGetter(TotemHuntConfig::getMap),
			Codec.INT.optionalFieldOf("totems", 1).forGetter(TotemHuntConfig::getTotems),
			WaitingLobbyConfig.CODEC.fieldOf("players").forGetter(TotemHuntConfig::getPlayerConfig),
			Codec.INT.optionalFieldOf("guide_ticks", 20 * 30).forGetter(TotemHuntConfig::getGuideTicks),
			Codec.INT.optionalFieldOf("invulnerability_ticks", 20 * 10).forGetter(TotemHuntConfig::getInvulnerabilityTicks),
			IntProvider.NON_NEGATIVE_CODEC.optionalFieldOf("ticks_until_close", ConstantIntProvider.create(SharedConstants.TICKS_PER_SECOND * 5)).forGetter(TotemHuntConfig::getTicksUntilClose)
		).apply(instance, TotemHuntConfig::new);
	});

	private final Identifier map;
	private final int totems;
	private final WaitingLobbyConfig playerConfig;
	private final int guideTicks;
	private final int invulnerabilityTicks;
	private final IntProvider ticksUntilClose;

	public TotemHuntConfig(Identifier map, int totems, WaitingLobbyConfig playerConfig, int guideTicks, int invulnerabilityTicks, IntProvider ticksUntilClose) {
		this.map = map;
		this.totems = totems;
		this.playerConfig = playerConfig;
		this.guideTicks = guideTicks;
		this.invulnerabilityTicks = invulnerabilityTicks;
		this.ticksUntilClose = ticksUntilClose;
	}

	public Identifier getMap() {
		return this.map;
	}

	public int getTotems() {
		return this.totems;
	}

	public WaitingLobbyConfig getPlayerConfig() {
		return this.playerConfig;
	}

	public int getGuideTicks() {
		return this.guideTicks;
	}

	public int getInvulnerabilityTicks() {
		return this.invulnerabilityTicks;
	}

	public IntProvider getTicksUntilClose() {
		return this.ticksUntilClose;
	}
}