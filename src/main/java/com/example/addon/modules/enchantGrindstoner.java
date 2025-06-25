
package meteordevelopment.meteorclient.systems.modules.player;
import com.example.addon.AddonTemplate;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;


public class enchantGrindstoner extends Module {
private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

private void lookAtNearestGrindstone() {
if (mc.currentScreen != null && mc.currentScreen.getTitle().getString().contains("Grindstone")) {
    return;
}

    BlockPos grindstonePos = findNearestGrindstone();
    if (grindstonePos == null) {
        error("No grindstone found within 5 blocks");
        running = false;
        toggle();
        return;
    }

    Vec3d targetPos = Vec3d.ofCenter(grindstonePos);
    Vec3d eyePos = mc.player.getEyePos();
    Vec3d direction = targetPos.subtract(eyePos).normalize();

    float yaw = (float)Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90f;
    float pitch = (float)-Math.toDegrees(Math.asin(direction.y));

    mc.player.setYaw(MathHelper.wrapDegrees(yaw));
    mc.player.setPitch(MathHelper.clamp(pitch, -90f, 90f));

    Direction side = Direction.UP;
    BlockHitResult hitResult = new BlockHitResult(targetPos, side, grindstonePos, false);
    ActionResult result = mc.interactionManager.interactBlock(mc.player, mc.player.getActiveHand(), hitResult);

    if (!result.isAccepted()) {
        ChatUtils.sendMsg(Text.of("Failed to open grindstone"));
        return;
    }
}

private ItemStack findFirstEnchantedArmor() {
    for (int i = 0; i < mc.player.getInventory().size(); i++) {
        // prevent armor from getting grindstoned (36 = boots, 37 = leggings, 38 = chestplate, 39 = helmet)
        if (i >= 36 && i <= 39) continue;

        ItemStack stack = mc.player.getInventory().getStack(i);
        if (!stack.isEmpty() && stack.hasEnchantments()) {
            //ChatUtils.sendMsg(Text.of("Found enchanted item: " + stack.getName().getString() + " in slot " + i));
            InvUtils.move().from(i).toId(1); 
            return stack;
        }
    }
    return null;
}



private BlockPos findNearestGrindstone() {
    int range = 4; 
    BlockPos playerPos = mc.player.getBlockPos();
    BlockPos nearestGrindstone = null;
    double nearestDistance = Double.MAX_VALUE;

    for (int x = -range; x <= range; x++) {
        for (int y = -range; y <= range; y++) {
            for (int z = -range; z <= range; z++) {
                BlockPos pos = playerPos.add(x, y, z);

                if (mc.world.getBlockState(pos).getBlock() == net.minecraft.block.Blocks.GRINDSTONE) {
                    double distance = playerPos.getSquaredDistance(pos);
                    
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestGrindstone = pos.toImmutable();
                    }
                }
            }
        }
    }
    
    return nearestGrindstone;
}

        public enchantGrindstoner() {
        super(AddonTemplate.CATEGORY, "enchant-grindstoner", "Grindstones all enchanted items. Be careful"); // TODO: fix infinite loop of grindstoning curse of vanishing items
    // TODO also add settings for how long the delay should be, whitelisted items etc
    }

private Thread grindstoneThread;
private boolean running = false;

@Override
public void onActivate() {
    running = true;
    grindstoneThread = new Thread(() -> {
        try {
            while (running) { 

       Thread.sleep(50);
            if (mc.currentScreen == null || !mc.currentScreen.getTitle().getString().contains("Repair & Disenchant")) { // check the ui if it has this text therefore grindstone is open
                lookAtNearestGrindstone();
                Thread.sleep(200);
            }

                if (findFirstEnchantedArmor() == null) {
                    error("No enchanted armor in inventory, disabling.");
                    toggle();
                    break;
                }

                Thread.sleep(250);
                InvUtils.move().fromId(2).toHotbar(0);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    });
    grindstoneThread.start();
}

@Override
public void onDeactivate() {
    running = false;  
    if (grindstoneThread != null) {
        grindstoneThread.interrupt();
    }
}
}