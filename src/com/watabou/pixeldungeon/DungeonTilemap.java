/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Image;
import com.watabou.noosa.NoosaScript;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.Tilemap;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.Point;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class DungeonTilemap extends Tilemap {

	public static final int SIZE = 16;

	private static DungeonTilemap instance;

	Tilemap mGroundLayer;
	Tilemap mDecoLayer;

	int[] mGroundMap;
	int[] mDecoMap;
	
	public DungeonTilemap() {
		super(Dungeon.level.tilesTex(), new TextureFilm(Dungeon.level.tilesTex(), SIZE, SIZE));

		int levelWidth = Dungeon.level.getWidth();
		map(Dungeon.level.map, levelWidth);

		String tilesEx = Dungeon.level.tilesTexEx();
		if (tilesEx != null) {
			mGroundLayer = new Tilemap(tilesEx, new TextureFilm(tilesEx, SIZE, SIZE));
			mGroundMap = new int[Dungeon.level.getWidth() * Dungeon.level.getHeight()];
			mGroundLayer.map(buildGroundMap(), levelWidth);

			mDecoLayer = new Tilemap(tilesEx, new TextureFilm(tilesEx, SIZE, SIZE));
			mDecoMap = new int[Dungeon.level.getWidth() * Dungeon.level.getHeight()];
			mDecoLayer.map(buildDecoMap(), levelWidth);
		}

		instance = this;
	}

	private int decoCell(int tileType) {
		switch (tileType) {
		case Terrain.GRASS:
			return 3 * 16 + Random.Int(0, 5);
		case Terrain.HIGH_GRASS:
			return 3 * 16 + Random.Int(6, 8);

		case Terrain.PEDESTAL:
			return 8 * 16 + 0;
		case Terrain.STATUE:
		case Terrain.STATUE_SP:
			return 8 * 16 + 1;

		case Terrain.DOOR:
			return 5 * 16 + 0;
		case Terrain.OPEN_DOOR:
			return 5 * 16 + 1;

		case Terrain.LOCKED_DOOR:
			return 5 * 16 + 2;

		case Terrain.LOCKED_EXIT:
			return 5 * 16 + 3;
		case Terrain.UNLOCKED_EXIT:
			return 5 * 16 + 4;

		case Terrain.ENTRANCE:
			return 5 * 16 + 5;

		case Terrain.EXIT:
			return 5 * 16 + 6;

		case Terrain.BARRICADE:
			return 7*16 + Random.Int(3,5);
		
		case Terrain.BOOKSHELF:
			return 7*16 + Random.Int(0,2);
			
		case Terrain.EMBERS:
			return 6*16 + Random.Int(0,2);
			
		case Terrain.EMPTY_DECO:
			return 9*16 + Random.Int(0,2);
			
		case Terrain.WALL_DECO:
			return 10*16 + Random.Int(0,2);
			
		case Terrain.ALCHEMY:
			return 4*16 + 2;
			
		case Terrain.EMPTY_WELL:
			return 4*16 + 0;
			
		case Terrain.WELL:
			return 4*16 + 1;
			
		case Terrain.TOXIC_TRAP:
			return 12 * 16 + 0;
		case Terrain.FIRE_TRAP:
			return 12 * 16 + 1;
		case Terrain.PARALYTIC_TRAP:
			return 12 * 16 + 2;
		case Terrain.INACTIVE_TRAP:
			return 12 * 16 + 7;
		case Terrain.POISON_TRAP:
			return 12 * 16 + 3;
		case Terrain.ALARM_TRAP:
			return 12 * 16 + 4;
		case Terrain.LIGHTNING_TRAP:
			return 12 * 16 + 5;
		case Terrain.GRIPPING_TRAP:
			return 12 * 16 + 8;
		case Terrain.SUMMONING_TRAP:
			return 12 * 16 + 6;
		
			
		default:
			return 15;
		}
	}

	private int[] buildDecoMap() {
		for (int i = 0; i < mDecoMap.length; i++) {
			mDecoMap[i] = decoCell(Dungeon.level.map[i]);
		}

		return mDecoMap;
	}

	private int groundCell(int tileType) {

		if (tileType >= Terrain.WATER_TILES) {
			return 14 * 16 + tileType - Terrain.WATER_TILES;
		}

		switch (tileType) {
		case Terrain.EMPTY:
		case Terrain.GRASS:
		case Terrain.EMPTY_DECO:
		case Terrain.EMPTY_WELL:
		case Terrain.SECRET_ALARM_TRAP:
		case Terrain.SECRET_FIRE_TRAP:
		case Terrain.SECRET_GRIPPING_TRAP:
		case Terrain.SECRET_LIGHTNING_TRAP:
		case Terrain.SECRET_PARALYTIC_TRAP:
		case Terrain.SECRET_POISON_TRAP:
		case Terrain.SECRET_SUMMONING_TRAP:
		case Terrain.SECRET_TOXIC_TRAP:
		case Terrain.INACTIVE_TRAP:
		case Terrain.ALARM_TRAP:
		case Terrain.FIRE_TRAP:
		case Terrain.GRIPPING_TRAP:
		case Terrain.LIGHTNING_TRAP:
		case Terrain.PARALYTIC_TRAP:
		case Terrain.POISON_TRAP:
		case Terrain.SUMMONING_TRAP:
		case Terrain.TOXIC_TRAP:
		case Terrain.ENTRANCE:
		case Terrain.EXIT:
		case Terrain.EMBERS:
		case Terrain.PEDESTAL:
		case Terrain.BARRICADE:
		case Terrain.HIGH_GRASS:
		case Terrain.SIGN:
		case Terrain.WELL:
		case Terrain.STATUE:
		case Terrain.BOOKSHELF:
			return Random.Int(0, 2);

		case Terrain.WALL:
		case Terrain.WALL_DECO:
		case Terrain.DOOR:
		case Terrain.OPEN_DOOR:
		case Terrain.LOCKED_DOOR:
		case Terrain.LOCKED_EXIT:
		case Terrain.UNLOCKED_EXIT:
		case Terrain.SECRET_DOOR:
			return 16 + Random.Int(0, 2);

		case Terrain.EMPTY_SP:
		case Terrain.STATUE_SP:
		case Terrain.ALCHEMY:
			return 2 * 16 + Random.Int(0, 2);

		case Terrain.CHASM_FLOOR:
			return 11 * 16 + 0;
		case Terrain.CHASM_FLOOR_SP:
			return 11 * 16 + 1;
		case Terrain.CHASM_WALL:
			return 11 * 16 + 2;
		case Terrain.CHASM_WATER:
			return 11 * 16 + 3;
		case Terrain.CHASM:
			return 11 * 16 + 4;

		default:
			return 15;
		}
	}

	private int[] buildGroundMap() {
		for (int i = 0; i < mGroundMap.length; i++) {
			mGroundMap[i] = groundCell(Dungeon.level.map[i]);
		}

		return mGroundMap;
	}

	public int screenToTile(int x, int y) {
		Point p = camera().screenToCamera(x, y).offset(this.point().negate()).invScale(SIZE).floor();
		return Dungeon.level.cell(p.x, p.y);
	}

	@Override
	public boolean overlapsPoint(float x, float y) {
		return true;
	}

	public void discover(int pos, int oldValue) {

		final Image tile = tile(oldValue);
		tile.point(tileToWorld(pos));

		// For bright mode
		tile.rm = tile.gm = tile.bm = rm;
		tile.ra = tile.ga = tile.ba = ra;
		getParent().add(tile);

		getParent().add(new AlphaTweener(tile, 0, 0.6f) {
			protected void onComplete() {
				tile.killAndErase();
				killAndErase();
			}
		});
	}

	public static PointF tileToWorld(int pos) {
		return new PointF(pos % Dungeon.level.getWidth(), pos / Dungeon.level.getWidth()).scale(SIZE);
	}

	public static PointF tileCenterToWorld(int pos) {
		return new PointF((pos % Dungeon.level.getWidth() + 0.5f) * SIZE,
				(pos / Dungeon.level.getWidth() + 0.5f) * SIZE);
	}

	public static Image tile(int index) {
		Image img = new Image(instance.texture);
		img.frame(instance.tileset.get(index));
		return img;
	}

	@Override
	public boolean overlapsScreenPoint(int x, int y) {
		return true;
	}

	@Override
	public void draw() {

		if (mGroundLayer != null && mDecoLayer != null) {
			mGroundLayer.draw();
			mDecoLayer.draw();
			return;
		} else {
			super.draw();

			NoosaScript script = NoosaScript.get();

			texture.bind();

			script.uModel.valueM4(matrix);
			script.lighting(rm, gm, bm, am, ra, ga, ba, aa);

			if (!updated.isEmpty()) {
				updateVertices();
			}

			script.camera(camera);
			script.drawQuadSet(quads, size);
		}
	}

	public void updateAll() {
		if (mGroundLayer != null && mDecoLayer != null) {
			buildGroundMap();
			buildDecoMap();
			mGroundLayer.updateRegion().set(0, 0, Dungeon.level.getWidth(), Dungeon.level.getHeight());
			mDecoLayer.updateRegion().set(0, 0, Dungeon.level.getWidth(), Dungeon.level.getHeight());
			return;
		} else {
			updated.set(0, 0, Dungeon.level.getWidth(), Dungeon.level.getHeight());
		}
		
	}

	public void updateCell(int cell) {
		if (mGroundLayer != null && mDecoLayer != null) {
			mGroundMap[cell] = groundCell(Dungeon.level.map[cell]);
			mDecoMap[cell] = decoCell(Dungeon.level.map[cell]);
			mGroundLayer.updateRegion().union(cell % Dungeon.level.getWidth(), cell / Dungeon.level.getWidth());
			mDecoLayer.updateRegion().union(cell % Dungeon.level.getWidth(), cell / Dungeon.level.getWidth());
		} else {
		
			updated.union(cell % Dungeon.level.getWidth(), cell / Dungeon.level.getWidth());
		}
	}
}
