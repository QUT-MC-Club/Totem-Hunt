package io.github.haykam821.totemhunt.game;

import io.github.haykam821.totemhunt.game.phase.TotemHuntActivePhase;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.widget.BossBarWidget;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;

public class TotemHuntBar {
	private static final BossBar.Style STYLE = BossBar.Style.PROGRESS;
	private static final BossBar.Color COLOR = BossBar.Color.RED;
	private static final Formatting FORMATTING = Formatting.RED;
	private static final Text TITLE = new TranslatableText("text.totemhunt.bar.invulnerability");

	private final TotemHuntActivePhase phase;
	private final BossBarWidget widget;
	private boolean closed = false;

	public TotemHuntBar(TotemHuntActivePhase phase, GlobalWidgets widgets) {
		this.phase = phase;

		this.widget = widgets.addBossBar(this.getBarTitle(TITLE, FORMATTING), COLOR, STYLE);
		this.phase.getGameSpace().addResource(this.widget);
	}

	public void tick() {
		if (this.closed) return;

		int ticksElapsed = this.phase.getTicksElapsed();
		int invulnerabilityTicks = this.phase.getConfig().getInvulnerabilityTicks();

		if (ticksElapsed > invulnerabilityTicks) {
			this.widget.close();
			this.closed = true;
		} else {
			this.widget.setProgress((invulnerabilityTicks - ticksElapsed) / (float) invulnerabilityTicks);
		}
	}

	private Text getBarTitle(Text customText, Formatting formatting) {
		Text gameName = new TranslatableText("gameType.totemhunt.totemhunt").formatted(Formatting.BOLD);
		return new LiteralText("").append(gameName).append(" - ").append(customText).formatted(formatting);
	}
}
