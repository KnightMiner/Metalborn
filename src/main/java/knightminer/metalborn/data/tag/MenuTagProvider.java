package knightminer.metalborn.data.tag;

import knightminer.metalborn.Metalborn;
import knightminer.metalborn.core.Registration;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.data.BuiltinRegistryTagProvider;
import slimeknights.mantle.datagen.MantleTags;

import java.util.concurrent.CompletableFuture;

/** Tag provider for menu */
public class MenuTagProvider extends BuiltinRegistryTagProvider<MenuType<?>> {
  public MenuTagProvider(PackOutput packOutput, CompletableFuture<Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
    super(packOutput, BuiltInRegistries.MENU, lookupProvider, Metalborn.MOD_ID, existingFileHelper);
  }

  @Override
  protected void addTags(Provider pProvider) {
    tag(MantleTags.MenuTypes.REPLACEABLE).add(Registration.METALBORN_MENU.get());
    tag(MantleTags.MenuTypes.REPLACEABLE).add(Registration.FORGE_MENU.get());
  }
}
