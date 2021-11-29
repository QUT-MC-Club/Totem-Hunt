package io.github.haykam821.totemhunt.game.role;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class PlayerRole extends Role {
	private static final Text NAME = new TranslatableText("text.totemhunt.role.player").formatted(Formatting.GRAY);

	@Override
	public Text getName() {
		return NAME;
	}
}