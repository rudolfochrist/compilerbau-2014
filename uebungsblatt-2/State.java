package de.fhmannheim.com.blatt2.uebung3;

public enum State {
	PLUS,
	MINUS,
	MAL,
	INIT;

	public Float calc(float current, String newVal) {
		float newFloat = Float.parseFloat(newVal);
		
		switch(this) {
			case PLUS: return current + newFloat;
			case MINUS: return current - newFloat;
			case MAL: return current * newFloat;
			default: return newFloat;
		}
	}
}
