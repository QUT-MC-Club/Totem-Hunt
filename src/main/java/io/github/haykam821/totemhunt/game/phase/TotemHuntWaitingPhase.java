package io.github.haykam821.totemhunt.game.phase;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import io.github.haykam821.totemhunt.game.TotemHuntConfig;
import io.github.haykam821.totemhunt.game.map.GuideText;
import io.github.haykam821.totemhunt.game.map.TotemHuntMap;
import io.github.haykam821.totemhunt.game.map.TotemHuntMapBuilder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameResult;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class TotemHuntWaitingPhase {
	private final GameSpace gameSpace;
	private final ServerWorld world;
	private final TotemHuntMap map;
	private final TotemHuntConfig config;

	private HolderAttachment guideText;

	public TotemHuntWaitingPhase(GameSpace gameSpace, ServerWorld world, TotemHuntMap map, TotemHuntConfig config) {
		this.gameSpace = gameSpace;
		this.world = world;
		this.map = map;
		this.config = config;
	}

	public static GameOpenProcedure open(GameOpenContext<TotemHuntConfig> context) {
		TotemHuntMapBuilder mapBuilder = new TotemHuntMapBuilder(context.config());
		TotemHuntMap map = mapBuilder.create(context.server());

		RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
			.setGenerator(map.createGenerator(context.server()));

		return context.openWithWorld(worldConfig, (activity, world) -> {
			TotemHuntWaitingPhase phase = new TotemHuntWaitingPhase(activity.getGameSpace(), world, map, context.config());

			GameWaitingLobby.addTo(activity, context.config().getPlayerConfig());

			TotemHuntActivePhase.setRules(activity);

			// Listeners
			activity.listen(GameActivityEvents.ENABLE, phase::enable);
			activity.listen(GamePlayerEvents.OFFER, phase::offerPlayer);
			activity.listen(PlayerDamageEvent.EVENT, phase::onPlayerDamage);
			activity.listen(PlayerDeathEvent.EVENT, phase::onPlayerDeath);
			activity.listen(GameActivityEvents.REQUEST_START, phase::requestStart);
		});
	}

	private void enable() {
		// Spawn guide text
		Vec3d guideTextPos = this.map.getGuideTextPos();

		if (guideTextPos != null) {
			ElementHolder holder = GuideText.createElementHolder();
			this.guideText = ChunkAttachment.of(holder, world, guideTextPos);
		}
	}

	public GameResult requestStart() {
		TotemHuntActivePhase.open(this.gameSpace, this.world, this.map, this.config, this.guideText);
		return GameResult.ok();
	}

	public PlayerOfferResult offerPlayer(PlayerOffer offer) {
		return offer.accept(this.world, this.map.getWaitingSpawn().center()).and(() -> {
			offer.player().changeGameMode(GameMode.ADVENTURE);
		});
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
		Vec3d center = this.map.getWaitingSpawn().center();
		player.teleport(this.world, center.getX(), center.getY(), center.getZ(), 0, 0);
	}
}
