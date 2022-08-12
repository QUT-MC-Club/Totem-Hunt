package io.github.haykam821.totemhunt.game.role;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class HolderRole extends Role {
	private static final Text NAME = Text.translatable("text.totemhunt.role.holder").formatted(Formatting.GOLD);
	private static final ItemStack TOTEM = new ItemStack(Items.TOTEM_OF_UNDYING);

	@Override
	public Text getName() {
		return NAME;
	}

	@Override
	public boolean canTransferTo(Role role) {
		return role instanceof PlayerRole;
	}

	@Override
	public List<ItemStack> getHotbar() {
		return Arrays.asList(TOTEM.copy());
	}
}