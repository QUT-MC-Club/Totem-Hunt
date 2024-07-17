package io.github.haykam821.totemhunt.game.map;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class GuideText {	
	private static final Formatting FORMATTING = Formatting.GOLD;

	private static final Text TEXT = Text.empty()
		.append(Text.translatable("gameType.totemhunt.totemhunt").formatted(Formatting.BOLD))
		.append(ScreenTexts.LINE_BREAK)
		.append(Text.translatable("text.totemhunt.guide.pass_totem"))
		.append(ScreenTexts.LINE_BREAK)
		.append(Text.translatable("text.totemhunt.guide.hunter_attacks_players"))
		.append(ScreenTexts.LINE_BREAK)
		.append(Text.translatable("text.totemhunt.guide.avoid_hunter"))
		.formatted(FORMATTING);

	private GuideText() {
		return;
	}

	public static ElementHolder createElementHolder() {
		TextDisplayElement element = new TextDisplayElement(TEXT);

		element.setBillboardMode(BillboardMode.CENTER);
		element.setLineWidth(400);

		ElementHolder holder = new ElementHolder();
		holder.addElement(element);

		return holder;
	}
}
