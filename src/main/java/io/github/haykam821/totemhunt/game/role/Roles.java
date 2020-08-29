package io.github.haykam821.totemhunt.game.role;

import io.github.haykam821.totemhunt.Main;
import net.minecraft.util.Identifier;

public enum Roles {
	PLAYER("player", new PlayerRole()),
	HOLDER("holder", new HolderRole()),
	HUNTER("hunter", new HunterRole());

	private Role role;

	private Roles(String path, Role role) {
		this.role = role;

		Identifier id = new Identifier(Main.MOD_ID, path);
		Role.REGISTRY.register(id, role);
	}

	public Role getRole() {
		return this.role;
	}

	public static void initialize() {
		return;
	}
}