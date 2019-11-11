package com.kios.airplace.LibGDX;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

public class Contacts implements ContactListener {
	@Override
	public void beginContact(Contact contact) {

	}

	@Override
	public void endContact(Contact contact) {

	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		String room = "com.kios.airplace.LibGDX.Room";
		String marker = "com.kios.airplace.LibGDX.MarkerLocation";
		String particle = "com.kios.airplace.Positioning.Particle";

		String classA = contact.getFixtureA().getBody().getUserData().getClass().getName();
		String classB = contact.getFixtureB().getBody().getUserData().getClass().getName();

		if (classA.equals(marker) && classB.equals(particle)) {
			contact.setEnabled(false);
		} else if (classA.equals(particle) && classB.equals(marker)) {
			contact.setEnabled(false);
		} else if (classA.equals(room) && classB.equals(marker)) {
			contact.setEnabled(false);
		} else if (classA.equals(marker) && classB.equals(room)) {
			contact.setEnabled(false);
		}
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {

	}
}