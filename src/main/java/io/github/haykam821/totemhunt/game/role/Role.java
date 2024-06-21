package io.github.haykam821.totemhunt.game.role;

import java.util.ArrayList;
import java.util.List;

import io.github.haykam821.totemhunt.game.PlayerEntry;
import io.github.haykam821.totemhunt.game.phase.TotemHuntActivePhase;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.common.team.GameTeamConfig;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

public abstract class Role {
	public static final TinyRegistry<Role> REGISTRY = TinyRegistry.create();

	private GameTeamKey teamKey;
	private GameTeamConfig teamConfig;

	public abstract Text getName();

	public boolean hasTeam() {
		return false;
	}

	public DyeColor getColor() {
		return null;
	}

	private GameTeamKey getTeamKey() {
		if (this.teamKey == null) {
			Identifier id = REGISTRY.getIdentifier(this);
			this.teamKey = new GameTeamKey(id.toUnderscoreSeparatedString());
		}

		return this.teamKey;
	}

	public void registerTeam(TeamManager teamManager) {
		if (this.hasTeam()) {
			if (this.teamConfig == null) {
				this.teamConfig = GameTeamConfig.builder()
					.setName(this.getName())
					.setColors(GameTeamConfig.Colors.from(this.getColor()))
					.build();
			}

			teamManager.addTeam(this.getTeamKey(), this.teamConfig);
		}
	}

	public void onGiveTotem(TotemHuntActivePhase phase, PlayerEntry from, PlayerEntry to) {
		from.changeRole(Roles.PLAYER.getRole(), true);
		to.changeRole(Roles.HOLDER.getRole(), true);
	}

	public boolean canTransferTo(Role role) {
		return false;
	}

	public void unapply(PlayerEntry entry) {
		entry.getPlayer().getInventory().clear();

		// Update team
		if (this.hasTeam()) {
			entry.getPhase().getTeamManager().removePlayerFrom(entry.getPlayer(), this.getTeamKey());
		}
	}

	public void apply(PlayerEntry entry) {
		ServerPlayerEntity player = entry.getPlayer();

		int slot = 0;
		List<ItemStack> hotbar = this.getHotbar();
		for (ItemStack stack : hotbar) {
			player.getInventory().setStack(slot, stack);
			slot += 1;
		}

		slot = 3;
		List<ItemStack> armor = this.getArmor();
		for (ItemStack stack : armor) {
			player.getInventory().armor.set(slot, stack);
			slot -= 1;
		}

		// Update inventory
		player.currentScreenHandler.sendContentUpdates();
		player.playerScreenHandler.onContentChanged(player.getInventory());

		// Update team
		if (this.hasTeam()) {
			entry.getPhase().getTeamManager().addPlayerTo(entry.getPlayer(), this.getTeamKey());
		}
	}

	public List<ItemStack> getHotbar() {
		return new ArrayList<>();
	}

	public List<ItemStack> getArmor() {
		return new ArrayList<>();
	}
}