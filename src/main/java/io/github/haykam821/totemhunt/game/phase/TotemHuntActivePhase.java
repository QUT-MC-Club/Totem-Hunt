package io.github.haykam821.totemhunt.game.phase;

import io.github.haykam821.totemhunt.game.PlayerEntry;
import io.github.haykam821.totemhunt.game.TotemHuntBar;
import io.github.haykam821.totemhunt.game.TotemHuntConfig;
import io.github.haykam821.totemhunt.game.map.TotemHuntMap;
import io.github.haykam821.totemhunt.game.role.Role;
import io.github.haykam821.totemhunt.game.role.Roles;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameLogic;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.GameOpenListener;
import xyz.nucleoid.plasmid.game.event.GameTickListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDamageListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TotemHuntActivePhase {
	private static final DecimalFormat FORMAT = new DecimalFormat("0.##");

	private final GameSpace gameSpace;
	private final ServerWorld world;
	private final TotemHuntMap map;
	private final TotemHuntConfig config;
	private final List<PlayerEntry> players = new ArrayList<>();
	private final TotemHuntBar timer;
	private int ticksElapsed = 0;

	public TotemHuntActivePhase(GameSpace gameSpace, TotemHuntMap map, TotemHuntConfig config, GlobalWidgets widgets) {
		this.gameSpace = gameSpace;
		this.world = gameSpace.getWorld();
		this.map = map;
		this.config = config;
		this.timer = new TotemHuntBar(this, widgets);
	}

	public static void setRules(GameLogic game) {
		game.setRule(GameRule.BLOCK_DROPS, RuleResult.DENY);
		game.setRule(GameRule.CRAFTING, RuleResult.DENY);
		game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
		game.setRule(GameRule.HUNGER, RuleResult.DENY);
		game.setRule(GameRule.PORTALS, RuleResult.DENY);
		game.setRule(GameRule.THROW_ITEMS, RuleResult.DENY);
	}

	public static void open(GameSpace gameSpace, TotemHuntMap map, TotemHuntConfig config) {
		gameSpace.openGame(game -> {
			GlobalWidgets widgets = new GlobalWidgets(game);
			TotemHuntActivePhase phase = new TotemHuntActivePhase(gameSpace, map, config, widgets);

			TotemHuntActivePhase.setRules(game);

			// Listeners
			game.on(GameOpenListener.EVENT, phase::open);
			game.on(GameTickListener.EVENT, phase::tick);
			game.on(PlayerAddListener.EVENT, phase::addPlayer);
			game.on(PlayerDamageListener.EVENT, phase::onPlayerDamage);
			game.on(PlayerDeathListener.EVENT, phase::onPlayerDeath);
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

	private void open() {
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

		MutableText breakdown = new LiteralText("The following roles are present:");
		for (Object2IntMap.Entry<Role> entry : roleCounts.object2IntEntrySet()) {
			Role role = entry.getKey();
			int count = entry.getIntValue();

			if (count > 0) {
				breakdown.append("\n- ").append(role.getName()).append(": " + count + " player" + (count == 1 ? "" : "s"));
			}
		}
		this.gameSpace.getPlayers().sendMessage(breakdown.formatted(Formatting.GOLD));
	}

	private void tick() {
		this.ticksElapsed += 1;
		this.timer.tick();
	}

	private Text getWinMessage(ServerPlayerEntity hunter, ServerPlayerEntity holder) {
		Text hunterName = hunter.getDisplayName();
		Text holderName = holder.getDisplayName();

		String time = TotemHuntActivePhase.FORMAT.format(this.ticksElapsed / (double) 20);

		return hunterName.shallowCopy()
				.append(" found the totem in the hands of ")
				.append(holderName)
				.append(" after " + time + " seconds!")
				.formatted(Formatting.RED);
	}

	public void endGame(ServerPlayerEntity hunter, ServerPlayerEntity holder) {
		this.gameSpace.getPlayers().sendMessage(this.getWinMessage(hunter, holder));
		this.gameSpace.close(GameCloseReason.FINISHED);
	}

	private void setSpectator(PlayerEntity player) {
		player.setGameMode(GameMode.SPECTATOR);
	}

	private PlayerEntry getEntryFromPlayer(ServerPlayerEntity player) {
		for (PlayerEntry entry : this.players) {
			if (player.equals(entry.getPlayer())) {
				return entry;
			}
		}
		return null;
	}

	private void addPlayer(ServerPlayerEntity player) {
		if (this.getEntryFromPlayer(player) == null) {
			this.setSpectator(player);
		}
	}
	
	private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float damage) {
		if (!(source.getAttacker() instanceof ServerPlayerEntity)) return ActionResult.PASS;

		PlayerEntry target = this.getEntryFromPlayer(player);
		if (target == null) return ActionResult.PASS;

		PlayerEntry attacker = this.getEntryFromPlayer((ServerPlayerEntity) source.getAttacker());
		if (attacker == null) return ActionResult.PASS;
	
		if (attacker.getRole().canTransferTo(target.getRole())) {
			attacker.getRole().onGiveTotem(this, attacker, target);
		}
		return ActionResult.PASS;
	}

	private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		PlayerEntry entry = this.getEntryFromPlayer(player);
		if (entry != null) {
			entry.spawn(this.world, this.map.getWaitingSpawn());
		}

		return ActionResult.SUCCESS;
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
