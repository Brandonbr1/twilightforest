package twilightforest.data.custom;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import oshi.util.tuples.Pair;
import twilightforest.TwilightForestMod;
import twilightforest.item.recipe.TFRecipes;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class TransformationPowderProvider implements DataProvider {
	private final DataGenerator generator;
	private final String modId;
	private final ExistingFileHelper helper;
	protected final Map<String, Pair<EntityType<?>, EntityType<?>>> builders = Maps.newLinkedHashMap();

	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();

	public TransformationPowderProvider(DataGenerator generator, final String modId, final ExistingFileHelper helper) {
		this.generator = generator;
		this.modId = modId;
		this.helper = helper;
	}

	public abstract void registerTransforms();

	@Override
	public void run(HashCache cache) throws IOException {
		this.builders.clear();
		this.registerTransforms();
		this.builders.forEach((name, transform) -> {
			List<String> list = builders.keySet().stream()
					.filter(s -> ForgeRegistries.ENTITIES.containsKey(transform.getA().getRegistryName()))
					.filter(s -> ForgeRegistries.ENTITIES.containsKey(transform.getB().getRegistryName()))
					.filter(s -> !this.builders.containsKey(s))
					.filter(this::missing)
					.collect(Collectors.toList());

			if (!list.isEmpty()) {
				throw new IllegalArgumentException(String.format("Duplicate Transformation Powder Transformations: %s", list.stream().map(Objects::toString).collect(Collectors.joining(", "))));
			} else {

				JsonObject obj = serializeToJson(transform.getA(), transform.getB());
				Path path = createPath(new ResourceLocation(modId, name));
				try {
					String s = GSON.toJson(obj);
					String s1 = SHA1.hashUnencodedChars(s).toString();
					if (!Objects.equals(cache.getHash(path), s1) || !Files.exists(path)) {
						Files.createDirectories(path.getParent());
						BufferedWriter bufferedwriter = Files.newBufferedWriter(path);

						try {
							bufferedwriter.write(s);
						} catch (Throwable throwable1) {
							if (bufferedwriter != null) {
								try {
									bufferedwriter.close();
								} catch (Throwable throwable) {
									throwable1.addSuppressed(throwable);
								}
							}

							throw throwable1;
						}

						if (bufferedwriter != null) {
							bufferedwriter.close();
						}
					}

					cache.putNew(path, s1);
				} catch (IOException ioexception) {
					TwilightForestMod.LOGGER.error("Couldn't save Transformation Powder recipe to {}", path, ioexception);
				}
			}
		});
	}

	private boolean missing(String name) {
		return helper == null || !helper.exists(new ResourceLocation(modId, name), new ExistingFileHelper.ResourceType(net.minecraft.server.packs.PackType.SERVER_DATA, ".json", "crumble_horn/"));
	}

	private Path createPath(ResourceLocation name) {
		return generator.getOutputFolder().resolve("data/" + name.getNamespace() + "/recipes/transformation_powder/" + name.getPath() + ".json");
	}

	private JsonObject serializeToJson(EntityType<?> transformFrom, EntityType<?> transformTo) {
		JsonObject jsonobject = new JsonObject();

		jsonobject.addProperty("type", TFRecipes.TRANSFORMATION_SERIALIZER.get().getRegistryName().toString());
		jsonobject.addProperty("from", ForgeRegistries.ENTITIES.getKey(transformFrom).toString());
		jsonobject.addProperty("to", ForgeRegistries.ENTITIES.getKey(transformTo).toString());
		return jsonobject;
	}

	@Override
	public String getName() {
		return modId + " Transformation Powder Transformations";
	}

	//helper methods
	public void addSingleTransform(EntityType<?> from, EntityType<?> to) {
		builders.put(from.getRegistryName().getPath() + "_to_" + to.getRegistryName().getPath(), new Pair<>(from, to));
	}

	public void addTwoWayTransform(EntityType<?> from, EntityType<?> to) {
		builders.put(from.getRegistryName().getPath() + "_to_" + to.getRegistryName().getPath(), new Pair<>(from, to));
		builders.put(to.getRegistryName().getPath() + "_to_" + from.getRegistryName().getPath(), new Pair<>(to, from));
	}
}
