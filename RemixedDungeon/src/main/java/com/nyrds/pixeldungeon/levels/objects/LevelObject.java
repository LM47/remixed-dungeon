package com.nyrds.pixeldungeon.levels.objects;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.levels.objects.sprites.LevelObjectSprite;
import com.nyrds.pixeldungeon.mechanics.HasPositionOnLevel;
import com.nyrds.pixeldungeon.mechanics.LevelHelpers;
import com.nyrds.pixeldungeon.utils.Position;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class LevelObject implements Bundlable, Presser, HasPositionOnLevel {

    @Packable
    protected int pos = Level.INVALID_CELL;

    @Packable
    protected int layer = 0;

    @Packable(defaultValue = "levelObjects/objects.png")
    protected String textureFile = "levelObjects/objects.png";

    @Packable(defaultValue = "0")
    protected int imageIndex = 0;

    public LevelObjectSprite sprite;

    public LevelObject(int pos) {
        this.pos = pos;
    }

    public int image() {
        return imageIndex;
    }

    void setupFromJson(Level level, JSONObject obj) throws JSONException {
        textureFile = obj.optString("textureFile", textureFile);
        imageIndex = obj.optInt("imageIndex", imageIndex);
    }

    public boolean interact(Char hero) {
        return true;
    }

    public boolean stepOn(Char hero) {
        return true;
    }

    public boolean nonPassable(Char ch) {
        return false;
    }

    protected void remove() {
        Dungeon.level.remove(this);
        sprite.kill();
    }

    public void burn() {
    }

    public void freeze() {
    }

    public void poison() {
    }

    public void bump(Presser presser) {
    }

    public void discover() {
    }

    public boolean secret() {
        return false;
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
    }

    @Override
    public void storeInBundle(Bundle bundle) {
    }

    public boolean dontPack() {
        return false;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        if (sprite != null) {
            sprite.move(this.pos, pos);
            Dungeon.level.levelObjectMoved(this);
        }

        this.pos = pos;
    }

    public abstract String desc();

    public abstract String name();

    public String texture() {
        return textureFile;
    }

    public boolean pushable(Char hero) {
        return false;
    }

    public boolean push(Char hero) {

        if(!pushable(hero)) {
            return false;
        }

        int nextCell = LevelHelpers.pushDst(hero, this, true);

        Level level = hero.level();

        if (!level.cellValid(nextCell)) {
            return false;
        }

        Char ch = Actor.findChar(nextCell);

        if(ch != null) {
            return false;
        }

        if (level.getLevelObject(nextCell, layer) != null) {
            return false;
        } else {
            level.press(nextCell, this);

            setPos(nextCell);
            level.levelObjectMoved(this);
        }

        return true;
    }

    public void fall() {
        if (sprite != null) {
            sprite.fall();
        }
        Dungeon.level.remove(this);
    }

    @Override
    public boolean affectLevelObjects() {
        return false;
    }

    public int getSpriteXS() {
        return 16;
    }

    public int getSpriteYS() {
        return 16;
    }

    public int getLayer() {
        return layer;
    }

    public Position getPosition() {
        return new Position(Dungeon.level.levelId, pos);
    }

    public void resetVisualState(){}
}
