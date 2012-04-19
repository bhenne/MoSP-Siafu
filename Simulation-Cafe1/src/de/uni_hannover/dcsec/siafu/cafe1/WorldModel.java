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

import java.util.ArrayList;
import java.util.Collection;

import de.uni_hannover.dcsec.siafu.behaviormodels.BaseWorldModel;
import de.uni_hannover.dcsec.siafu.model.Place;
import de.uni_hannover.dcsec.siafu.model.World;
import de.nec.nle.siafu.types.BooleanType;

/**
 * The world model for the simulation. In this case, the conference room calls
 * for a global meeting which some staff members attend.
 * 
 * @author M. Martin
 */
public class WorldModel extends BaseWorldModel {
	/** Noon time. */
	// private static final EasyTime NOON = new EasyTime(12, 0);

	/** Time at which the meeting ends. */
	// private boolean dayEventsPlanned = false;

	/**
	 * Create the world model.
	 * 
	 * @param world
	 *            the simulation's world.
	 */
	public WorldModel(final World world) {
		super(world);
	}

	/**
	 * Add the Busy variable to the info field of all the places.
	 * 
	 * @param places
	 *            the places created so far by the images
	 */
	@Override
	public void createPlaces(final ArrayList<Place> places) {
		for (Place p : places) {
			p.set("Busy", new BooleanType(false));
		}
	}

	/**
	 * Schedule a daily meeting, and ensure all the necessary Agents are invited
	 * over to it when the time comes.
	 * 
	 * @param places
	 *            the places in the simulation.
	 */
	public void doIteration(final Collection<Place> places) {
		// Calendar time = world.getTime();
		// EasyTime now = new EasyTime(time.get(Calendar.HOUR_OF_DAY),
		// time.get(Calendar.MINUTE));
		//
		// if (now.isAfter(NOON) && dayEventsPlanned) {
		// dayEventsPlanned = false;
		// }
		// if (now.isBefore(NOON) && !dayEventsPlanned) {
		// planDayEvents();
		// }
	}

	/** Plan a meeting at 11h00. */
	private void planDayEvents() {
		/*
		 * meetingStart = new EasyTime(MEETING_START); meetingEnd = new
		 * EasyTime(meetingStart).shift(MEETING_DURATION).blur(
		 * MEETING_DURATION_BLUR);
		 */
		// dayEventsPlanned = true;
	}
}
