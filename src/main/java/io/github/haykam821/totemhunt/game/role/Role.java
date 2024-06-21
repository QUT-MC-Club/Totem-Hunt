package io.github.haykam821.totemhunt.game.role;

import java.util.ArrayList;
import java.util.List;

import io.github.haykam821.totemhunt.game.PlayerEntry;
import io.github.haykam821.totemhunt.game.phase.TotemHuntActivePhase;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

public abstract class Role {
	public static final TinyRegistry<Role> REGISTRY = TinyRegistry.create();

	public abstract Text getName();

	public void onGiveTotem(TotemHuntActivePhase phase, PlayerEntry from, PlayerEntry to) {
		from.changeRole(Roles.PLAYER.getRole(), true);
		to.changeRole(Roles.HOLDER.getRole(), true);
	}

	public boolean canTransferTo(Role role) {
		return false;
	}

	public void unapply(PlayerEntry entry) {
		entry.getPlayer().getInventory().clear();
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
	}

	public List<ItemStack> getHotbar() {
		return new ArrayList<>();
	}

	public List<ItemStack> getArmor() {
		return new ArrayList<>();
	}
}