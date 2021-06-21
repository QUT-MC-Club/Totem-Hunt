package io.github.haykam821.totemhunt.game.phase;

import io.github.haykam821.totemhunt.game.TotemHuntConfig;
import io.github.haykam821.totemhunt.game.map.TotemHuntMap;
import io.github.haykam821.totemhunt.game.map.TotemHuntMapBuilder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.fantasy.BubbleWorldConfig;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.StartResult;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;
import xyz.nucleoid.plasmid.game.event.OfferPlayerListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDamageListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.RequestStartListener;
import xyz.nucleoid.plasmid.game.player.JoinResult;

public class TotemHuntWaitingPhase {
	private final GameSpace gameSpace;
	private final TotemHuntMap map;
	private final TotemHuntConfig config;

	public TotemHuntWaitingPhase(GameSpace gameSpace, TotemHuntMap map, TotemHuntConfig config) {
		this.gameSpace = gameSpace;
		this.map = map;
		this.config = config;
	}

	public static GameOpenProcedure open(GameOpenContext<TotemHuntConfig> context) {
		TotemHuntMapBuilder mapBuilder = new TotemHuntMapBuilder(context.getConfig());
		TotemHuntMap map = mapBuilder.create();

		BubbleWorldConfig worldConfig = new BubbleWorldConfig()
			.setGenerator(map.createGenerator(context.getServer()))
			.setDefaultGameMode(GameMode.ADVENTURE);

		return context.createOpenProcedure(worldConfig, game -> {
			TotemHuntWaitingPhase phase = new TotemHuntWaitingPhase(game.getSpace(), map, context.getConfig());

			GameWaitingLobby.applyTo(game, context.getConfig().getPlayerConfig());

			TotemHuntActivePhase.setRules(game);

			// Listeners
			game.on(PlayerAddListener.EVENT, phase::addPlayer);
			game.on(PlayerDamageListener.EVENT, phase::onPlayerDamage);
			game.on(PlayerDeathListener.EVENT, phase::onPlayerDeath);
			game.on(OfferPlayerListener.EVENT, phase::offerPlayer);
			game.on(RequestStartListener.EVENT, phase::requestStart);
		});
	}

	private boolean isFull() {
		return this.gameSpace.getPlayerCount() >= this.config.getPlayerConfig().getMaxPlayers();
	}

	public JoinResult offerPlayer(ServerPlayerEntity player) {
		return this.isFull() ? JoinResult.gameFull() : JoinResult.ok();
	}

	public StartResult requestStart() {
		PlayerConfig playerConfig = this.config.getPlayerConfig();
		if (this.gameSpace.getPlayerCount() < playerConfig.getMinPlayers()) {
			return StartResult.NOT_ENOUGH_PLAYERS;
		}

		TotemHuntActivePhase.open(this.gameSpace, this.map, this.config);
		return StartResult.OK;
	}

	public void addPlayer(ServerPlayerEntity player) {
		this.spawn(player);
	}

	public ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
		return ActionResult.FAIL;
	}

	public ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		// Respawn player at the start
		this.spawn(player);
		return ActionResult.FAIL;
	}

	private void spawn(ServerPlayerEntity player) {
		Vec3d center = this.map.getWaitingSpawn().getCenter();
		player.teleport(this.gameSpace.getWorld(), center.getX(), center.getY(), center.getZ(), 0, 0);
	}
}
