package io.github.haykam821.totemhunt.game.phase;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.haykam821.totemhunt.game.PlayerEntry;
import io.github.haykam821.totemhunt.game.TotemHuntBar;
import io.github.haykam821.totemhunt.game.TotemHuntConfig;
import io.github.haykam821.totemhunt.game.map.TotemHuntMap;
import io.github.haykam821.totemhunt.game.role.Role;
import io.github.haykam821.totemhunt.game.role.Roles;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class TotemHuntActivePhase {
	private static final DecimalFormat FORMAT = new DecimalFormat("0.##");

	private final GameSpace gameSpace;
	private final ServerWorld world;
	private final TotemHuntMap map;
	private final TotemHuntConfig config;
	private final List<PlayerEntry> players = new ArrayList<>();
	private final TotemHuntBar timer;
	private int ticksElapsed = 0;
	private int ticksUntilClose = -1;

	public TotemHuntActivePhase(GameSpace gameSpace, ServerWorld world, TotemHuntMap map, TotemHuntConfig config, GlobalWidgets widgets) {
		this.gameSpace = gameSpace;
		this.world = world;
		this.map = map;
		this.config = config;
		this.timer = new TotemHuntBar(this, widgets);
	}

	public static void setRules(GameActivity activity) {
		activity.deny(GameRuleType.BLOCK_DROPS);
		activity.deny(GameRuleType.CRAFTING);
		activity.deny(GameRuleType.FALL_DAMAGE);
		activity.deny(GameRuleType.HUNGER);
		activity.deny(GameRuleType.PORTALS);
		activity.deny(GameRuleType.THROW_ITEMS);
	}

	public static void open(GameSpace gameSpace, ServerWorld world, TotemHuntMap map, TotemHuntConfig config) {
		gameSpace.setActivity(activity -> {
			GlobalWidgets widgets = GlobalWidgets.addTo(activity);
			TotemHuntActivePhase phase = new TotemHuntActivePhase(gameSpace, world, map, config, widgets);

			TotemHuntActivePhase.setRules(activity);

			// Listeners
			activity.listen(GameActivityEvents.ENABLE, phase::enable);
			activity.listen(GameActivityEvents.TICK, phase::tick);
			activity.listen(GamePlayerEvents.OFFER, phase::offerPlayer);
			activity.listen(GamePlayerEvents.REMOVE, phase::removePlayer);
			activity.listen(PlayerDamageEvent.EVENT, phase::onPlayerDamage);
			activity.listen(PlayerDeathEvent.EVENT, phase::onPlayerDeath);
		});
	}

	private Role getRoleByIndex(int index) {
		if (index == 0)
			return Roles.HUNTER.getRole();
		if (index > 0 && index <= this.config.getTotems())
			return Roles.HOLDER.getRole();
		return Roles.PLAYER.getRole();
	}

	private List<ServerPlayerEntity> getShuffledPlayers() {
		List<ServerPlayerEntity> players = new ArrayList<>();
		for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
			players.add(player);
		}

		Collections.shuffle(players);
		return players;
	}

	private void enable() {
		Object2IntLinkedOpenHashMap<Role> roleCounts = new Object2IntLinkedOpenHashMap<>();
		roleCounts.defaultReturnValue(0);

		int index = 0;
 		for (ServerPlayerEntity player : this.getShuffledPlayers()) {
			Role role = this.getRoleByIndex(index);
			roleCounts.addTo(role, 1);

			PlayerEntry entry = new PlayerEntry(this, player, role);
			entry.spawn(world, this.map.getSpawns().get(index % this.map.getSpawns().size()));

			this.players.add(entry);
			index += 1;
		}

		MutableText breakdown = Text.translatable("text.totemhunt.role_breakdown.header");
		for (Object2IntMap.Entry<Role> entry : roleCounts.object2IntEntrySet()) {
			Role role = entry.getKey();
			int count = entry.getIntValue();

			if (count > 0) {
				Text entryText = Text.translatable("text.totemhunt.role_breakdown.entry" + (count == 1 ? "" : ".plural"), role.getName(), count);
				breakdown.append(ScreenTexts.LINE_BREAK).append(entryText);
			}
		}
		this.gameSpace.getPlayers().sendMessage(breakdown.formatted(Formatting.GOLD));
	}

	private void tick() {
		// Decrease ticks until game end to zero
		if (this.isGameEnding()) {
			if (this.ticksUntilClose == 0) {
				this.gameSpace.close(GameCloseReason.FINISHED);
			}

			this.ticksUntilClose -= 1;
			return;
		}

		this.ticksElapsed += 1;
		this.timer.tick();
	}

	private Text getWinMessage(ServerPlayerEntity hunter, ServerPlayerEntity holder) {
		Text hunterName = hunter.getDisplayName();
		Text holderName = holder.getDisplayName();

		String time = TotemHuntActivePhase.FORMAT.format(this.ticksElapsed / (double) 20);

		return Text.translatable("text.totemhunt.totem_found", hunterName, holderName, time).formatted(Formatting.RED);
	}

	public void endGame(ServerPlayerEntity hunter, ServerPlayerEntity holder) {
		this.gameSpace.getPlayers().sendMessage(this.getWinMessage(hunter, holder));
		this.ticksUntilClose = this.config.getTicksUntilClose().get(this.world.getRandom());
	}

	private boolean isGameEnding() {
		return this.ticksUntilClose >= 0;
	}

	private void setSpectator(ServerPlayerEntity player) {
		player.changeGameMode(GameMode.SPECTATOR);
	}

	private PlayerEntry getEntryFromPlayer(ServerPlayerEntity player) {
		for (PlayerEntry entry : this.players) {
			if (player.equals(entry.getPlayer())) {
				return entry;
			}
		}
		return null;
	}

	private PlayerOfferResult offerPlayer(PlayerOffer offer) {
		return offer.accept(this.world, this.map.getWaitingSpawn().center()).and(() -> {
			this.setSpectator(offer.player());
		});
	}

	private void removePlayer(ServerPlayerEntity player) {
		if (this.isGameEnding()) return;

		PlayerEntry entry = this.getEntryFromPlayer(player);
		if (entry != null) {
			this.players.remove(entry);
			this.reassignRequiredRoles();
		}
	}

	private void reassignRequiredRoles() {
		boolean needsHunter = true;
		boolean needsHolder = true;
		for (PlayerEntry entry : this.players) {
			if (entry.getRole() == Roles.HUNTER.getRole()) {
				needsHunter = false;
			} else if (entry.getRole() == Roles.HOLDER.getRole()) {
				needsHolder = false;
			}

			if (!needsHunter && !needsHolder) {
				return;
			}
		}

		for (PlayerEntry entry : this.players) {
			if (needsHunter) {
				needsHunter = false;
				entry.changeRole(Roles.HUNTER.getRole());
			} else if (needsHolder) {
				needsHolder = false;
				entry.changeRole(Roles.HUNTER.getRole());
			}
		}
	}
	
	private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float damage) {
		if (this.isGameEnding()) return ActionResult.FAIL;
		if (!(source.getAttacker() instanceof ServerPlayerEntity)) return ActionResult.FAIL;

		PlayerEntry target = this.getEntryFromPlayer(player);
		if (target == null) return ActionResult.FAIL;

		PlayerEntry attacker = this.getEntryFromPlayer((ServerPlayerEntity) source.getAttacker());
		if (attacker == null) return ActionResult.FAIL;
	
		if (attacker.getRole().canTransferTo(target.getRole())) {
			attacker.getRole().onGiveTotem(this, attacker, target);
		}
		return ActionResult.FAIL;
	}

	private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		PlayerEntry entry = this.getEntryFromPlayer(player);
		if (entry != null) {
			entry.spawn(this.world, this.map.getWaitingSpawn());
		}

		return ActionResult.FAIL;
	}

	public GameSpace getGameSpace() {
		return this.gameSpace;
	}

	public TotemHuntConfig getConfig() {
		return this.config;
	}

	public int getTicksElapsed() {
		return this.ticksElapsed;
	}

	public boolean isInvulnerabilityPeriod() {
		return this.ticksElapsed <= this.config.getInvulnerabilityTicks();
	}

	static {
		FORMAT.setRoundingMode(RoundingMode.DOWN);
	}
}
