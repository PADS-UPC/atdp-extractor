package edu.upc.atdputils;

public enum ConstraintType {

	PRECEDENCE("Precedence"),
	RESPONSE("Response"),
	SUCCESSION("Succession"),
	NONCOOCCURRENCE("Noncooccurrence"),
	INIT("Init"),
	END("End"),
	EXISTENCE("Existence"),
	ABSENCE("Absence");

	private final String constraintName;

	ConstraintType (String constraintName) {
		this.constraintName = constraintName;
	}

	public String getConstraintName() {
		return constraintName;
	}

	public static ConstraintType getType(String string) {
		for (ConstraintType type : ConstraintType.values()) {
			if (type.toString().equalsIgnoreCase(string)) {
				return type;
			}
		}
		return null;
	}
}
