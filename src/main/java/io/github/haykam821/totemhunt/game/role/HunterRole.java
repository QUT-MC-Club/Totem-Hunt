package io.github.haykam821.totemhunt.game.role;

import java.util.Arrays;
import java.util.List;

import io.github.haykam821.totemhunt.game.PlayerEntry;
import io.github.haykam821.totemhunt.game.phase.TotemHuntActivePhase;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

public class HunterRole extends Role {
	private static final Text NAME = new LiteralText("Hunter").formatted(Formatting.RED);

	private static final ItemStack SWORD = getUnbreakableStack(Items.IRON_SWORD);
	private static final ItemStack HELMET = getUnbreakableBindingStack(Items.IRON_HELMET);
	private static final ItemStack CHESTPLATE = getUnbreakableBindingStack(Items.IRON_CHESTPLATE);
	private static final ItemStack LEGGINGS = getUnbreakableBindingStack(Items.IRON_LEGGINGS);
	private static final ItemStack BOOTS = getUnbreakableBindingStack(Items.IRON_BOOTS);

	@Override
	public Text getName() {
		return NAME;
	}

	@Override
	public void onGiveTotem(TotemHuntActivePhase phase, PlayerEntry from, PlayerEntry to) {
		if (phase.isInvulnerabilityPeriod()) {
			Text message = new TranslatableText("text.totemhunt.cannot_attack_invulnerable_player").formatted(Formatting.RED);
			from.getPlayer().sendMessage(message, false);

			return;
		}

		from.getPhase().endGame(from.getPlayer(), to.getPlayer());
	}

	@Override
	public boolean canTransferTo(Role role) {
		return role instanceof HolderRole;
	}

	@Override
	public List<ItemStack> getHotbar() {
		return Arrays.asList(SWORD.copy());
	}

	@Override
	public List<ItemStack> getArmor() {
		return Arrays.asList(HELMET.copy(), CHESTPLATE.copy(), LEGGINGS.copy(), BOOTS.copy());
	}

	private static ItemStack getUnbreakableStack(ItemConvertible item) {
		return ItemStackBuilder.of(item)
			.setUnbreakable()
			.build();
	}

	private static ItemStack getUnbreakableBindingStack(ItemConvertible item) {
		ItemStack stack = getUnbreakableStack(item);
		stack.addEnchantment(Enchantments.BINDING_CURSE, 1);
		return stack;
	}
}