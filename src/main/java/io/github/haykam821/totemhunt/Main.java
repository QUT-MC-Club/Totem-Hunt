package io.github.haykam821.totemhunt;

import io.github.haykam821.totemhunt.game.TotemHuntConfig;
import io.github.haykam821.totemhunt.game.phase.TotemHuntWaitingPhase;
import io.github.haykam821.totemhunt.game.role.Roles;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.api.game.GameType;

public class Main implements ModInitializer {
	private static final String MOD_ID = "totemhunt";

	private static final Identifier TOTEM_HUNT_ID = Main.identifier("totemhunt");
	public static final GameType<TotemHuntConfig> TOTEM_HUNT_TYPE = GameType.register(TOTEM_HUNT_ID, TotemHuntConfig.CODEC, TotemHuntWaitingPhase::open);

	@Override
	public void onInitialize() {
		Roles.initialize();
	}

	public static Identifier identifier(String path) {
		return Identifier.of(MOD_ID, path);
	}
}