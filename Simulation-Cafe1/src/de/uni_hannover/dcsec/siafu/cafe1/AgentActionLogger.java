/*
 * Copyright 2012 Distributed Computing & Security Group, Leibniz Universität Hannover
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

import de.uni_hannover.dcsec.siafu.model.Agent;
import de.uni_hannover.dcsec.siafu.model.Floor;
import de.uni_hannover.dcsec.siafu.model.World;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static de.uni_hannover.dcsec.siafu.cafe1.Constants.Fields.*;
import de.nec.nle.siafu.types.BooleanType;
import de.nec.nle.siafu.types.IntegerNumber;
import java.util.Date;

/**
 * 
 * @author P. Salomon
 * @author C. Szongott
 */
public class AgentActionLogger {

	public final static String ENTER = "ENTER";
	public final static String LEAVE = "LEAVE";
	public final static String INFECT = "INFECT";

	World world;
	File file;

	public AgentActionLogger(final World world, String filename) {
		this.world = world;
		file = new File(filename + ".txt");
		init();

	}

	private void init() {
		try {
			FileWriter fw = new FileWriter(this.file, false);
			Date dt = new Date();
			fw.write("Simulation starts: " + dt.toString());
			fw.write(System.getProperty("line.separator"));
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void logInfection(final Agent a, final Agent infectious) {
		try {
			FileWriter fw = new FileWriter(this.file, true);
			int infectionTime = ((IntegerNumber) a.get(INFECTION_TIME))
					.getNumber();
			int floorA = ((Floor) a.get(CURRENT_FLOOR)).getLevel();
			int floorInfectious = ((Floor) infectious.get(CURRENT_FLOOR))
					.getLevel();
			float distanceBetweenAgents = world
					.getDistanceBetweenAgentsInMeters(a, infectious);
			String msg = "I " + infectionTime + " " + "p" + a.getName() + " "
					+ "p" + infectious.getName() + " X X X X X INCAFE X X X "
					+ floorA + " " + floorInfectious + " "
					+ distanceBetweenAgents + " " + a.getPos() + " "
					+ infectious.getPos();

			fw.write(msg);
			fw.write(System.getProperty("line.separator"));
			fw.flush();
			fw.close();

			// log to main simulation
			world.getSimulation().getControl().getOutput().logToMainSim(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Deprecated
	public void logAgentAction(final Agent a, String type) {
		Boolean zombie = ((BooleanType) a.get(INFECTED)).getValue();
		Floor currentFloor = ((Floor) a.get(CURRENT_FLOOR));
		try {
			FileWriter fw = new FileWriter(this.file, true);

			if (type.equals(ENTER)) {
				fw.write("Agent enters building:: Agent:" + a.getName()
						+ " floor:'" + currentFloor.getLevel() + "' time:'"
						+ world.getTime().getTimeInMillis() + "' infected:"
						+ zombie);
			} else if (type.equals(LEAVE)) {
				fw.write("Agent leaves building:: Agent:'" + a.getName()
						+ " floor:'" + currentFloor.getLevel() + "' time:'"
						+ world.getTime().getTimeInMillis() + "' infected:"
						+ zombie);
			}

			fw.write(System.getProperty("line.separator"));
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
