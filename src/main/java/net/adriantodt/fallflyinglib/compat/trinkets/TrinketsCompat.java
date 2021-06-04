package net.adriantodt.fallflyinglib.compat.trinkets;

import dev.emi.trinkets.api.SlotGroups;
import dev.emi.trinkets.api.Slots;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;
import net.adriantodt.fallflyinglib.event.FallFlyingCallback;
import net.adriantodt.fallflyinglib.event.PreFallFlyingCallback;
import net.adriantodt.fallflyinglib.mixin.LivingEntityAccessor;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

import static net.adriantodt.fallflyinglib.FallFlyingLib.ABILITY;

public class TrinketsCompat implements ModInitializer {
	public static final Identifier SOURCE_ID = new Identifier("trinkets", "elytra");
	public static final AbilitySource SOURCE = Pal.getAbilitySource(SOURCE_ID);

	@Override
	public void onInitialize() {
		PreFallFlyingCallback.EVENT.register(TrinketsCompat::preTick);
		FallFlyingCallback.EVENT.register(TrinketsCompat::postTick);
	}

	private static boolean shouldDamage(PlayerEntity player, ItemStack itemStack) {
		return itemStack.getItem() == Items.ELYTRA
			&& ElytraItem.isUsable(itemStack)
			&& (((LivingEntityAccessor) player).getRoll() + 1) % 20 == 0;
	}

	private static void preTick(PlayerEntity player) {
		if (player.world.isClient) {
			return;
		}
		TrinketComponent comp = TrinketsApi.getTrinketComponent(player);
		ItemStack itemStack = comp.getStack(SlotGroups.CHEST, Slots.CAPE);
		if (itemStack.getItem() == Items.ELYTRA && ElytraItem.isUsable(itemStack)) {
			Pal.grantAbility(player, ABILITY, SOURCE);
		} else {
			Pal.revokeAbility(player, ABILITY, SOURCE);
		}
	}

	private static void postTick(PlayerEntity player) {
		if (player.world.isClient || !SOURCE.grants(player, ABILITY)) {
			return;
		}
		TrinketComponent comp = TrinketsApi.getTrinketComponent(player);
		ItemStack itemStack = comp.getStack(SlotGroups.CHEST, Slots.CAPE);
		if (shouldDamage(player, itemStack)) {
			itemStack.damage(1, player, le -> le.sendEquipmentBreakStatus(EquipmentSlot.CHEST));
		}
	}
}
