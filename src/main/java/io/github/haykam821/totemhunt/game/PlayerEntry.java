package io.github.haykam821.totemhunt.game;

import java.util.Set;

import io.github.haykam821.totemhunt.game.phase.TotemHuntActivePhase;
import io.github.haykam821.totemhunt.game.role.Role;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

public class PlayerEntry {
	private final TotemHuntActivePhase phase;
	private final ServerPlayerEntity player;
	private Role role;

	public PlayerEntry(TotemHuntActivePhase phase, ServerPlayerEntity player, Role role) {
		this.phase = phase;
		this.player = player;
		this.role = role;
	}

	public TotemHuntActivePhase getPhase() {
		return this.phase;
	}

	public ServerPlayerEntity getPlayer() {
		return this.player;
	}

	public Role getRole() {
		return this.role;
	}

	public void spawn(ServerWorld world, Vec3d spawn) {
		this.player.changeGameMode(GameMode.ADVENTURE);
		this.role.apply(this);

		this.player.teleport(world, spawn.getX(), spawn.getY(), spawn.getZ(), Set.of(), 0, 0, true);

		this.player.sendMessage(Text.translatable("text.totemhunt.role_spawn", this.role.getName()), true);
	}

	public void changeRole(Role role, boolean notify) {
		this.role.unapply(this);
		role.apply(this);

		this.role = role;

		if (notify) {
			this.player.sendMessage(Text.translatable("text.totemhunt.role_change", this.role.getName()), true);
		}
	}

	@Override
	public String toString() {
		return "PlayerEntry{player=" + this.player + ", role=" + this.role + "}";
	}
}