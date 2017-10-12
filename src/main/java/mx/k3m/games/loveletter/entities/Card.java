package mx.k3m.games.loveletter.entities;

public enum Card {
	
	GUARD("GUARD", "Escoge", 1),
	PRIEST("PRIEST", "Mira", 2),
	BARON("BARON", "Compara", 3),
	HANDMAID("HANDMAID", "PROTECCION", 4),
	PRINCE("PRINCE", "descartar y jalar", 5),
	KING("KING", "cambiar", 6),
	COUNTESS("COUNTESS", "descartar", 7),
	PRINCESS("PRINCESS", "perder", 8);
	

	private String name;
	private String powerText;
	private int number;
	
	Card(String name, String powerText, int number) {
		this.name = name;
		this.powerText = powerText;
		this.number = number;
	}
	
	public String getPowerText() {
		return powerText;
	}
	public void setPowerText(String powerText) {
		this.powerText = powerText;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}
	
	
}
