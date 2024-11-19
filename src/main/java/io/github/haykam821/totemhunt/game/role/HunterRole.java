package io.github.haykam821.totemhunt.game.role;

import java.util.Arrays;
import java.util.List;

import io.github.haykam821.totemhunt.game.PlayerEntry;
import io.github.haykam821.totemhunt.game.phase.TotemHuntActivePhase;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.api.util.ItemStackBuilder;

public class HunterRole extends Role {
	private static final Text NAME = Text.translatable("text.totemhunt.role.hunter").formatted(Formatting.RED);

	private static final ItemStack SWORD = createUnbreakableStack(Items.IRON_SWORD);

	@Override
	public Text getName() {
		return NAME;
	}

	@Override
	public boolean hasTeam() {
		return true;
	}

	@Override
	public DyeColor getColor() {
		return DyeColor.RED;
	}

	@Override
	public void onGiveTotem(TotemHuntActivePhase phase, PlayerEntry from, PlayerEntry to) {
		if (phase.isInvulnerabilityPeriod()) {
			Text message = Text.translatable("text.totemhunt.cannot_attack_invulnerable_player").formatted(Formatting.RED);
			from.getPlayer().sendMessage(message, false);

			return;
		}

		from.getPhase().endGame(from, to);
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
	public List<ItemStack> getArmor(RegistryWrapper.WrapperLookup registries) {
		ItemStack helmet = createUnbreakableBindingStack(Items.IRON_HELMET, registries);
		ItemStack chestplate = createUnbreakableBindingStack(Items.IRON_CHESTPLATE, registries);
		ItemStack leggings = createUnbreakableBindingStack(Items.IRON_LEGGINGS, registries);
		ItemStack boots = createUnbreakableBindingStack(Items.IRON_BOOTS, registries);

		return Arrays.asList(helmet, chestplate, leggings, boots);
	}

	private static ItemStack createUnbreakableStack(ItemConvertible item) {
		return ItemStackBuilder.of(item)
			.setUnbreakable()
			.build();
	}

	private static ItemStack createUnbreakableBindingStack(ItemConvertible item, RegistryWrapper.WrapperLookup registries) {
		ItemStack stack = createUnbreakableStack(item);
		stack.addEnchantment(registries.getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.BINDING_CURSE), 1);
		return stack;
	}
}