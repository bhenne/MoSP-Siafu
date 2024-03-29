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

package de.uni_hannover.dcsec.siafu.externalCommand;

/**
 * This class serves as a reference for the names in the external command
 * listener.
 * 
 * @author M. Martin
 * @author P. Salomon
 * @author C. Szongott
 * 
 */
public class CommandNames {

	/**
	 * Mark an agent in the simulator.
	 */
	public static final String MARK = "mark";

	/**
	 * Unmark an agent in the simulator.
	 */
	public static final String UNMARK = "unmark";

	/**
	 * Move an agent.
	 */
	public static final String MOVE = "move";

	/**
	 * Allow the user to be controlled by the simulation or vice versa.
	 */
	public static final String AUTO = "auto";

	/**
	 * Change the image of an agent in the simulation.
	 */
	public static final String IMAGE = "image";

	/**
	 * Change the image of the agent to the one before the previous change.
	 */
	public static final String SET_PREVIOUS_IMAGE = "setpreviousimage";

	/**
	 * Get context information from a user.
	 */
	public static final String GET_CONTEXT = "getcontext";

	/**
	 * Set agent specific context information.
	 */
	public static final String SET_CONTEXT = "setcontext";

	/**
	 * Find other agents near the agent.
	 */
	public static final String FIND_NEARBY_AGENTS = "findnearagent";

	/**
	 * Find other agents near the agent.
	 */
	public static final String FIND_NEARBY_PLACES = "findnearplace";

	/**
	 * Make the agent invisible.
	 */
	public static final String HIDE = "hide";

	/**
	 * Make the agent visible.
	 */
	public static final String UNHIDE = "unhide";

	/**
	 * Represents all of the agents, in the methods that allow it.
	 */
	public static final String ALL = "all";

	public static final String SIM = "sim";

	/**
	 * Fügt neuen Agenten in die Simulatiuon ein
	 * 
	 * psa
	 */
	public static final String ADD_AGENT = "addagent";

	public static final String ADD_AGENTS = "addagents";

	/**
	 * Lässt die Simulation um x Schritte weiterlaufen
	 * 
	 * psa
	 */
	public static final String DO_STEPS = "dosteps";

	/**
	 * Gib zurück ob die Simulation gerade läuft
	 * 
	 * psa
	 */
	public static final String IS_RUNNING = "isrunning";

}
