package com.nyrds.pixeldungeon.levels.objects;

import com.nyrds.Packable;
import com.nyrds.android.util.Util;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Animation;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.GLog;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class PortalGate extends Deco {

	protected boolean animationRunning = false;

	@Packable
	protected boolean activated = false;

	@Packable
	protected boolean used = false;

	@Packable
	protected boolean infiniteUses = false;

	@Packable
	protected int uses;

	protected Animation activation;
	protected Animation activatedLoop;


	public PortalGate(){
		this(-1);
	}

	public PortalGate(int pos) {
		super(pos);
	}


	@Override
	void setupFromJson(Level level, JSONObject obj) throws JSONException {

		object_desc = "portalGate";

		super.setupFromJson(level, obj);
		if(obj.has("uses")){
			uses = obj.getInt("uses");
		} else {
			infiniteUses = true;
		}
	}

	@Override
	public boolean interact(Char hero) {
		if(hero instanceof Hero) {
			return portalInteract((Hero)hero);
		}
		return false;
	}

	public void useUp(){
		if (!infiniteUses){
			uses = uses - 1;
			if (uses < 1){
				used = true;
			}
		}

	}

	@Override
	public boolean stepOn(Char hero) {
		return false;
	}

	@Override
	public String desc() {
		if(activated){
			return Game.getVar(R.string.PortalGate_Desc_Activated);
		}
		return super.desc();
	}


	protected void playStartUpAnim(){
		animationRunning = true;

		sprite.playAnim(activation, () -> {
			playActiveLoop();
			activated = true;
			animationRunning = false;
			GLog.w(Game.getVar(R.string.PortalGate_Activated));
		});
	}

	private void playActiveLoop(){
		sprite.playAnim(activatedLoop, Util.nullCallback);
	}

	@Override
	public void resetVisualState() {
		super.resetVisualState();

		if(activation==null) {
			activation = loadAnimation("activation");
			activatedLoop = loadAnimation("activatedLoop");
		}

		if(activated) {
			playActiveLoop();
		}
	}

	public abstract boolean portalInteract(Hero hero);
}
