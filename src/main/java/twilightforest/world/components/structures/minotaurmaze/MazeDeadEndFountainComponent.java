package twilightforest.world.components.structures.minotaurmaze;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import twilightforest.block.TFBlocks;
import twilightforest.world.registration.TFFeature;

import java.util.Random;

public class MazeDeadEndFountainComponent extends MazeDeadEndComponent {

	public MazeDeadEndFountainComponent(StructurePieceSerializationContext ctx, CompoundTag nbt) {
		this(MinotaurMazePieces.TFMMDEF, nbt);
	}

	public MazeDeadEndFountainComponent(StructurePieceType piece, CompoundTag nbt) {
		super(piece, nbt);
	}

	public MazeDeadEndFountainComponent(StructurePieceType type, TFFeature feature, int i, int x, int y, int z, Direction rotation) {
		super(type, feature, i, x, y, z, rotation);
	}

	@Override
	public void postProcess(WorldGenLevel world, StructureFeatureManager manager, ChunkGenerator generator, Random rand, BoundingBox sbb, ChunkPos chunkPosIn, BlockPos blockPos) {
		// normal doorway
		super.postProcess(world, manager, generator, rand, sbb, chunkPosIn, blockPos);

		// back wall brick
		this.generateBox(world, sbb, 1, 1, 4, 4, 4, 4, TFBlocks.MAZESTONE_BRICK.get().defaultBlockState(), AIR, false);

		// water
		this.placeBlock(world, Blocks.WATER.defaultBlockState(), 2, 3, 4, sbb);
		this.placeBlock(world, Blocks.WATER.defaultBlockState(), 3, 3, 4, sbb);

		// receptacle
		this.placeBlock(world, AIR, 2, 0, 3, sbb);
		this.placeBlock(world, AIR, 3, 0, 3, sbb);
	}
}
