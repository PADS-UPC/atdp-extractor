package edu.upc.entities;

public class ActivityAtdp extends Activity {
	private String atdp_id;

	public ActivityAtdp(Activity activity) {
		super(activity.getId(), activity.getAgent(), activity.getAction(), activity.getPatient());
		super.setConflictDestinations(activity.getConflictDestinations());
		super.setConflictOrigins(activity.getConflictOrigins());
		super.setPattern(activity.getPattern());
		super.setRole(activity.getRole());
		super.setSequenceDestinations(activity.getConflictDestinations());
		super.setSequenceOrigins(activity.getSequenceOrigins());
		super.setText(activity.getText());
	}

	public ActivityAtdp(String id, Action action) {
		super(id, action);
	}

	public ActivityAtdp(String id, Agent agent, Action action, Patient patient) {
		super(id, agent, action, patient);
	}

	public String getAtdp_id() {
		return atdp_id;
	}

	public void setAtdp_id(String atdp_id) {
		this.atdp_id = atdp_id;
	}

}
