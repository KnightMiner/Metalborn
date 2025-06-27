package knightminer.metalborn.block;

import knightminer.metalborn.core.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.block.InventoryBlock;
import slimeknights.mantle.util.BlockEntityHelper;

/** Block logic for the metal forge */
public class ForgeBlock extends BaseEntityBlock {
  public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
  public static final BooleanProperty LIT = BlockStateProperties.LIT;

  public ForgeBlock(Properties props) {
    super(props);
    this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
  }


  /* Block state */

  @Override
  protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
    builder.add(FACING, LIT);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext pContext) {
    return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, pContext.getHorizontalDirection().getOpposite());
  }

  @Override
  public BlockState rotate(BlockState state, Rotation rotation) {
    return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
  }

  @Override
  public BlockState mirror(BlockState state, Mirror mirror) {
    return state.rotate(mirror.getRotation(state.getValue(FACING)));
  }


  /* Block entity */

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new ForgeBlockEntity(pos, state);
  }

  @Override
  @Nullable
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState pState, BlockEntityType<T> check) {
    return level.isClientSide ? null : BlockEntityHelper.castTicker(check, Registration.FORGE_BLOCK_ENTITY.get(), ForgeBlockEntity.SERVER_TICKER);
  }

  @Override
  public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
    if (level.isClientSide()) {
      return InteractionResult.SUCCESS;
    }
    MenuProvider menuProvider = getMenuProvider(state, level, pos);
    if (menuProvider != null) {
      player.openMenu(menuProvider);
    }
    return InteractionResult.CONSUME;
  }

  @Override
  public RenderShape getRenderShape(BlockState pState) {
    return RenderShape.MODEL;
  }

  @Override
  public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
    if (stack.hasCustomHoverName() && level.getBlockEntity(pos) instanceof ForgeBlockEntity forge) {
      forge.setCustomName(stack.getHoverName());
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean pIsMoving) {
    if (!oldState.is(newState.getBlock())) {
      if (level.getBlockEntity(pos) instanceof ForgeBlockEntity forge) {
        if (level instanceof ServerLevel server) {
          InventoryBlock.dropInventoryItems(level, pos, forge.getInventory());
          forge.getRecipesToAwardAndPopExperience(server, Vec3.atCenterOf(pos));
        }
        level.updateNeighbourForOutputSignal(pos, this);
      }
      super.onRemove(oldState, level, pos, newState, pIsMoving);
    }
  }

  // TODO: comparator support

  /* Particles */

  @Override
  public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
    if (state.getValue(LIT)) {
      double x = pos.getX() + 0.5;
      double y = pos.getY();
      double z = pos.getZ() + 0.5;
      if (random.nextDouble() < 0.1) {
        level.playLocalSound(x, y, z, SoundEvents.BLASTFURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1, 1, false);
      }

      Direction direction = state.getValue(FACING);
      Direction.Axis axis = direction.getAxis();
      double horOffset = random.nextDouble() * 0.6 - 0.3;
      double xOffset = axis == Direction.Axis.X ? direction.getStepX() * 0.52 : horOffset;
      double yOffset = random.nextDouble() * 9 / 16;
      double zOffset = axis == Direction.Axis.Z ? direction.getStepZ() * 0.52 : horOffset;
      level.addParticle(ParticleTypes.SMOKE, x + xOffset, y + yOffset, z + zOffset, 0, 0, 0);
    }
  }
}
