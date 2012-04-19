/*
 * Copyright NEC Europe Ltd. 2006-2007
 *  and 2012 Distributed Computing & Security Group, Leibniz Universität Hannover
 * 
 * This file is part of the MoSP simulation Siafu context simulator.
 * 
 * Siafu is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * Siafu is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.uni_hannover.dcsec.siafu.cafe1;

import de.nec.nle.siafu.types.FlatData;
import de.nec.nle.siafu.types.Publishable;
import de.nec.nle.siafu.types.Text;

/**
 * A list of the constants used by this simulation. None of this is strictly
 * needed, but it makes referring to certain values easier and less error prone.
 * 
 * @author M. Martin
 * @author C. Szongott
 */
public class Constants {

	public static final String STAIR_TYPE = "stairs";

	// public static final int POPULATION = 1;

	/** Blur in minutes from the average work end time. */
	// public static final int VISIT_END_BLUR = 20;

	/** Average amount of visiting minutes. */
	// public static final int AVERAGE_VISIT_MINUTES = 45;

	/** Radius in meter where devices get infected **/
	// public static final float WLAN_INFECTION_RADIUS = 2.0f;

	/** Time that is needed to infect a device in seconds **/
	// public static final int INFECTION_DURATION = 15;

	/**
	 * The names of the fields in each agent object.
	 */
	static class Fields {
		/** The agent's current activity. */
		public static final String ACTIVITY = "Activity";

		public static final String TYPE = "Type";

		// public static final String INFECTIONS_ONGOING = "InfectionsOngoing";

		/** The type of seat. */
		public static final String SEAT = "Seat";

		public static final String ID = "p_id";

		// public static final String NEXT_TIME_INTERNET_USAGE =
		// "NextTimeInternetUsage";

		public static final String INFECTED = "p_infected_mobile";

		public static final String CAFE_WAITING_MIN = "p_CAFE_WAITING_MIN";

		public static final String CAFE_WAITING_MAX = "p_CAFE_WAITING_MAX";

		public static final String TIME_INTERNET_CAFE = "p_TIME_INTERNET_CAFE";

		public static final String NEED_INTERNET_OFFSET = "p_need_internet_offset";

		public static final String INFECTION_DURATION = "p_INFECTION_DURATION";

		public static final String INFECTION_RADIUS = "p_INFECTION_RADIUS";

		public static final String INFECTION_TIME = "p_infection_time";

		public static final String CONNECTION_PARTNER = "ConnectionPartner";

		public static final String CONNECTION_DURATION = "ConnectionDuration";

		public static final String NO_CONNECTION_PARTNER = "###NO_PARTNER###";

		/** The time at which the agent enters the building. */
		public static final String START_VISIT = "StartVisit";

		/** The time at which the agent leaves the building. */
		public static final String END_VISIT = "EndVisit";

		// public static final String NEXT_QUESTION = "NextQuestion";

		public static final String DESTINATION_AFTER_CHANGE_FLOOR = "destAfterChangeFloor";

		public static final String ACTIVITY_AFTER_CHANGE_FLOOR = "ActivityAfterChangeFloor";

		/** A temporary destination. */
		public static final String TEMPORARY_DESTINATION = "TemporaryDestination";

		public static final String CURRENT_FLOOR = "current_floor";

		public static final String TARGET_FLOOR = "target_floor";
	}

	/**
	 * List of possible activies. This is implemented as an enum because it
	 * helps us in switch statements. Like the rest of the constants in this
	 * class, they could also have been coded directly in the model
	 */
	enum Activity implements Publishable {

		LEAVING_CAFE("GoingHome"),

		IS_HOME("IsHome"),

		RESTING("Resting"),

		AT_SEAT("AtDesk"),

		GOING_2_STAIR("Going2Stair"),

		GOING_2_SEAT("Going2Desk"),

		// GOING_2_ASK("Going2Ask"),

		GOING_2_POINT("Going2Point"),

		WANDER_AROUND("WanderAround");

		/** Human readable desription of the activity. */
		private String description;

		/**
		 * Get the description of the activity.
		 * 
		 * @return a string describing the activity
		 */
		public String toString() {
			return description;
		}

		/**
		 * Build an instance of Activity which keeps a human readable
		 * description for when it's flattened.
		 * 
		 * @param description
		 *            the humanreadable description of the activity
		 */
		private Activity(final String description) {
			this.description = description;
		}

		/**
		 * Flatten the description of the activity.
		 * 
		 * @return a flatenned text with the description of the activity
		 */
		public FlatData flatten() {
			return new Text(description).flatten();
		}
	}
}
