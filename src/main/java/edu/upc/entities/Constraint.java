package edu.upc.entities;

import edu.upc.atdputils.ConstraintType;

public class Constraint {

	private ConstraintType constrainType;
	private Activity activityA;
	private Activity activityB;

	public Constraint() {
		activityA = new Activity(null, null);
		activityB = new Activity(null, null);
	}

	public Constraint(ConstraintType constrainType, Activity activityA, Activity activityB) {
		super();
		this.constrainType = constrainType;
		this.setActivityA(activityA);
		this.setActivityB(activityB);
	}

	public Constraint(ConstraintType constrainType, Activity activityA) {
		super();
		this.constrainType = constrainType;
		this.setActivityA(activityA);
		this.setActivityB(null);
	}

	public ConstraintType getConstrainType() {
		return constrainType;
	}

	public void setConstrainType(ConstraintType constrainType) {
		this.constrainType = constrainType;
	}

	public Activity getActivityA() {
		return activityA;
	}

	public void setActivityA(Activity activityA) {
		this.activityA = activityA;
	}

	public Activity getActivityB() {
		return activityB;
	}

	public void setActivityB(Activity activityB) {
		this.activityB = activityB;
	}

	@Override
	public String toString() {
		if (activityA == null && activityB == null)
			return null;
		String constraint = null;
		if (constrainType != null)
			constraint = constrainType + "(";
		else
			constraint = "(";
		if (activityA != null) {
			if (activityA != null) {
				constraint += "A=" + activityA.getAction().getWord();
				if (activityA.getPatient() != null)
					constraint += " " + activityA.getPatient().getShortText();
			}
		}
		if (activityB != null) {
			if (activityB != null) {
				constraint += ", B=" + activityB.getAction().getWord();
				if (activityB.getPatient() != null)
					constraint += " " + activityB.getPatient().getShortText();
			}
		}
		constraint += ")";
		return constraint;

	}

}
