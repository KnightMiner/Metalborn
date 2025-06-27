package knightminer.metalborn.block;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Registration;
import knightminer.metalborn.menu.ForgeMenu;
import knightminer.metalborn.recipe.ForgeRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.RangedWrapper;
import slimeknights.mantle.block.entity.NameableBlockEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static knightminer.metalborn.block.ForgeInventory.FUEL_SLOT;
import static knightminer.metalborn.block.ForgeInventory.RESULT_SLOT;
import static net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.BURN_COOL_SPEED;

/** Block entity logic for the metal forge*/
public class ForgeBlockEntity extends NameableBlockEntity implements ContainerData, RecipeHolder {
  /** Ticking logic for serverside */
  public static final BlockEntityTicker<ForgeBlockEntity> SERVER_TICKER = (level, pos, state, be) -> be.serverTick();
  private static final Component TITLE = Metalborn.component("gui", "forge");
  // data slots
  public static final int DATA_SLOT_COUNT = 4;
  public static final int INDEX_FUEL = 0;
  public static final int INDEX_FUEL_DURATION = 1;
  public static final int INDEX_RECIPE_PROGRESS = 2;
  public static final int INDEX_RECIPE_DURATION = 3;

  // fuel
  /** Current fuel amount */
  private int fuel = 0;
  /** Max fuel amount */
  private int fuelDuration = 0;

  // current recipe
  /** Progress through the current recipe */
  int recipeProgress = 0;
  /** Duration of the current recipe */
  private int recipeDuration = 0;
  /** Recipe currently in progress. If null, no recipe is in progress */
  private ForgeRecipe currentRecipe;

  // recipe caches
  /** Helper to perform recipe lookups */
  private final RecipeManager.CachedCheck<CraftingContainer,ForgeRecipe> quickCheck = RecipeManager.createCheck(Registration.FORGE_RECIPE.get());
  /** Map of all recipes used since a player last interacted */
  private final Object2IntOpenHashMap<ResourceLocation> recipesUsed = new Object2IntOpenHashMap<>();

  // inventory
  /** Inventory holding all items for this furnace */
  private final ForgeCraftingInventory inventory = new ForgeCraftingInventory(this);
  /** General use item handler, exposed on null side */
  private LazyOptional<IItemHandler> itemHandler;
  /** Item handlers exposed on all 6 block sides */
  private LazyOptional<IItemHandler>[] sideHandlers;

  protected ForgeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state, TITLE);
    initializeCaps();
  }

  public ForgeBlockEntity(BlockPos pos, BlockState state) {
    this(Registration.FORGE_BLOCK_ENTITY.get(), pos, state);
  }

  @Override
  public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player pPlayer) {
    return new ForgeMenu(id, playerInventory, this);
  }


  /* Recipe logic */

  /** Checks if we can smelt the given recipe */
  private boolean canForge(ForgeRecipe recipe) {
    // must of course have a recipe
    assert level != null;
    ItemStack recipeResult = recipe.assemble(inventory, level.registryAccess());
    // recipe must have a result
    if (recipeResult.isEmpty()) {
      return false;
    }
    // if we have nothing in the output, just need to fit the new stack
    ItemStack currentResult = inventory.getStackInSlot(RESULT_SLOT);
    if (currentResult.isEmpty()) {
      return recipeResult.getCount() <= inventory.getSlotLimit(RESULT_SLOT);
    }

    // must match the current output
    if (!ItemStack.isSameItemSameTags(currentResult, recipeResult)) {
      return false;
    }
    // must fit in the output
    int resultSize = currentResult.getCount() + recipeResult.getCount();
    return resultSize <= inventory.getSlotLimit(RESULT_SLOT) && resultSize <= currentResult.getMaxStackSize();
  }

  /** Crafts the recipe result */
  private void forge(ForgeRecipe recipe) {
    assert level != null;

    ItemStack recipeResult = recipe.assemble(inventory, level.registryAccess());
    ItemStack currentResult = inventory.getStackInSlot(RESULT_SLOT);
    // if nothing is in the inventory currently, then replace it
    if (currentResult.isEmpty()) {
      inventory.setStackInSlot(RESULT_SLOT, recipeResult.copy());
    // grow the input; not doing full NBT check as we already validated it
    } else if (currentResult.is(recipeResult.getItem())) {
      currentResult.grow(recipeResult.getCount());
    }

    // shrink all inputs
    for (int i = 0; i < ForgeInventory.GRID_SIZE; i++) {
      inventory.getStackInSlot(ForgeInventory.GRID_START + i).shrink(1);
    }
  }

  /** Called on inventory change to update the current recipe */
  public void resetRecipe() {
    assert level != null;
    recipeProgress = 0;
    currentRecipe = quickCheck.getRecipeFor(inventory, level).orElse(null);
    recipeDuration = currentRecipe != null ? currentRecipe.getCookingTime() : 0;
  }

  private void serverTick() {
    assert level != null;

    boolean wasFueled = fuel > 0;
    boolean didChange = false;
    if (wasFueled) {
      this.fuel -= 1;
    }

    ItemStack fuel = this.inventory.getStackInSlot(FUEL_SLOT);
    boolean hasInputs = !this.inventory.isEmpty();
    boolean hasFuel = !fuel.isEmpty();

    // if we have fuel, or possibly have a recipe, it's time to do some work
    boolean isFueled = this.fuel > 0;
    if (isFueled || hasFuel && hasInputs) {
      // no inputs means we can't craft, so clear current recipe
      if (!hasInputs) {
        recipeDuration = 0;
        currentRecipe = null;
      }
      // a non-zero duration with a null recipe indicates we were working on a recipe before world close, so refetch recipe
      else if (recipeDuration > 0 && currentRecipe == null) {
        currentRecipe = quickCheck.getRecipeFor(inventory, level).orElse(null);
        if (currentRecipe == null) {
          recipeDuration = 0;
        } else {
          recipeDuration = currentRecipe.getCookingTime();
        }
      }

      // if we have a valid recipe, time to work
      if (currentRecipe != null && canForge(currentRecipe)) {
        // do we need fuel? if so try and find it
        if (!isFueled) {
          this.fuel = ForgeHooks.getBurnTime(fuel, Registration.FORGE_RECIPE.get());
          this.fuelDuration = this.fuel;
          // if we found fuel, consume fuel
          if (this.fuel > 0) {
            didChange = true;
            fuel.shrink(1);
            if (fuel.isEmpty()) {
              inventory.setStackInSlot(FUEL_SLOT, fuel.getCraftingRemainingItem());
              // if we got a container, but it won't fit, pop it into the world
            } else if (fuel.hasCraftingRemainingItem()) {
              double x = (level.random.nextFloat() * 0.5F) + 0.25D + worldPosition.getX();
              double y = (level.random.nextFloat() * 0.5F) + 0.25D + worldPosition.getY();
              double z = (level.random.nextFloat() * 0.5F) + 0.25D + worldPosition.getZ();
              ItemEntity entity = new ItemEntity(level, x, y, z, fuel.getCraftingRemainingItem());
              entity.setDefaultPickUpDelay();
              level.addFreshEntity(entity);
            }
          }
        }

        // if we have fuel (either had or just found it), we can smelt
        if (this.fuel > 0) {
          this.recipeProgress += 1;
          if (this.recipeProgress >= this.recipeDuration) {
            // shrink inputs and craft result
            this.forge(currentRecipe);
            this.setRecipeUsed(currentRecipe);

            // update current recipe
            resetRecipe();
            didChange = true;
          }
          // if we don't have fuel, tick down this recipe
        } else if (this.recipeProgress > 0) {
          this.recipeProgress = Mth.clamp(this.recipeProgress - BURN_COOL_SPEED, 0, this.recipeDuration);
        }
      } else {
        // can't forge the recipe? can't have any progress
        this.recipeProgress = 0;
      }
    } else if (this.recipeProgress > 0) {
      this.recipeProgress = Mth.clamp(this.recipeProgress - BURN_COOL_SPEED, 0, this.recipeDuration);
    }

    // if our fuel state changed, update the block state
    if (wasFueled != this.fuel > 0) {
      didChange = true;
      BlockState state = getBlockState().setValue(ForgeBlock.LIT, this.fuel > 0);
      level.setBlock(worldPosition, state, 3);
    }

    // if anything happened, update the save data
    if (didChange) {
      setChanged(level, worldPosition, getBlockState());
    }
  }


  /* Used recipes */

  @Override
  public void setRecipeUsed(@Nullable Recipe<?> pRecipe) {
    if (pRecipe != null) {
      this.recipesUsed.addTo(pRecipe.getId(), 1);
    }
  }

  @Override
  @Nullable
  public Recipe<?> getRecipeUsed() {
    return null;
  }

  @Override
  public void awardUsedRecipes(Player player, List<ItemStack> items) {}

  /** Awards criteria and recipes for recently crafted items */
  public void awardUsedRecipesAndPopExperience(ServerPlayer player) {
    List<Recipe<?>> list = this.getRecipesToAwardAndPopExperience(player.serverLevel(), player.position());
    player.awardRecipes(list);

    // alert everyone what we crafted
    List<ItemStack> resultStacks = inventory.getAllItems();
    for (Recipe<?> recipe : list) {
      if (recipe != null) {
        player.triggerRecipeCrafted(recipe, resultStacks);
      }
    }
    this.recipesUsed.clear();
  }

  /** Gets a list of all recently crafted recipes */
  public List<Recipe<?>> getRecipesToAwardAndPopExperience(ServerLevel level, Vec3 location) {
    List<Recipe<?>> recipes = new ArrayList<>();

    // drop experience for all crafted recipes
    for (Object2IntMap.Entry<ResourceLocation> entry : this.recipesUsed.object2IntEntrySet()) {
      level.getRecipeManager().byKey(entry.getKey()).ifPresent(recipe -> {
        recipes.add(recipe);
        if (recipe instanceof ForgeRecipe forgeRecipe) {
          AbstractFurnaceBlockEntity.createExperience(level, location, entry.getIntValue(), forgeRecipe.getExperience());
        }
      });
    }

    return recipes;
  }


  /* Capability */

  /** Gets the inventory instance */
  public IItemHandlerModifiable getInventory() {
    return inventory;
  }

  /** Creates all lazy optionals for all sides */
  @SuppressWarnings("unchecked")
  private void initializeCaps() {
    itemHandler = LazyOptional.of(() -> inventory);
    // each side will get a different slot to allow some autocrafting
    // interestingly, this matches up to fuel on top, result on bottom, and four input slots on the four sides
    sideHandlers = new LazyOptional[6];
    for (int i = 0; i < 6; i++) {
      int fi = i;
      sideHandlers[i] = LazyOptional.of(() -> new RangedWrapper(inventory, fi, fi + 1));
    }
  }

  @Override
  public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
    if (cap == ForgeCapabilities.ITEM_HANDLER) {
      if (side == null) {
        return itemHandler.cast();
      }
      return sideHandlers[side.get3DDataValue()].cast();
    }
    return super.getCapability(cap, side);
  }

  @Override
  public void invalidateCaps() {
    super.invalidateCaps();
    initializeCaps();
  }


  /* Data slot syncing */

  @Override
  public int getCount() {
    return DATA_SLOT_COUNT;
  }

  @Override
  public int get(int index) {
    return switch (index) {
      case INDEX_FUEL -> fuel;
      case INDEX_FUEL_DURATION -> fuelDuration;
      case INDEX_RECIPE_PROGRESS -> recipeProgress;
      case INDEX_RECIPE_DURATION -> recipeDuration;
      default -> 0;
    };
  }

  @Override
  public void set(int index, int value) {
    switch (index) {
      case INDEX_FUEL -> fuel = value;
      case INDEX_FUEL_DURATION -> fuelDuration = value;
      case INDEX_RECIPE_PROGRESS -> recipeProgress = value;
      case INDEX_RECIPE_DURATION -> recipeDuration = value;
    }
  }


  /* NBT */

  private static final String KEY_ITEMS = "Items";
  private static final String KEY_SIZE = "Size";
  private static final String KEY_FUEL = "fuel";
  private static final String KEY_FUEL_DURATION = "fuel_duration";
  private static final String KEY_RECIPE_PROGRESS = "recipe_progress";
  private static final String KEY_RECIPE_DURATION = "recipe_duration";
  private static final String KEY_RECIPES_USED = "recipes_used";

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    tag.remove(KEY_SIZE); // we don't want the inventory size changing due to NBT
    inventory.deserializeNBT(tag);
    fuel = tag.getInt(KEY_FUEL);
    fuelDuration = tag.getInt(KEY_FUEL_DURATION);
    recipeProgress = tag.getInt(KEY_RECIPE_PROGRESS);
    recipeDuration = tag.getInt(KEY_RECIPE_DURATION);

    // load extra recipes
    if (tag.contains(KEY_RECIPES_USED, Tag.TAG_COMPOUND)) {
      CompoundTag compoundtag = tag.getCompound(KEY_RECIPES_USED);
      for (String key : compoundtag.getAllKeys()) {
        ResourceLocation id = ResourceLocation.tryParse(key);
        if (id != null) {
          this.recipesUsed.put(id, compoundtag.getInt(key));
        }
      }
    }
  }

  @Override
  public void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    tag.put(KEY_ITEMS, Objects.requireNonNull(inventory.serializeNBT().get(KEY_ITEMS)));
    tag.putInt(KEY_FUEL, fuel);
    tag.putInt(KEY_FUEL_DURATION, fuelDuration);
    tag.putInt(KEY_RECIPE_PROGRESS, recipeProgress);
    tag.putInt(KEY_RECIPE_DURATION, recipeDuration);

    // write recipes used
    if (!recipesUsed.isEmpty()) {
      CompoundTag recipes = new CompoundTag();
      for (Object2IntMap.Entry<ResourceLocation> entry : recipesUsed.object2IntEntrySet()) {
        recipes.putInt(entry.getKey().toString(), entry.getIntValue());
      }
      tag.put(KEY_RECIPES_USED, recipes);
    }
  }
}
