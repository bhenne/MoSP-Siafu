/*
 * (c) 2011-2012, Distributed Computing & Security Group, Leibniz Universitaet Hannover
 * 
 * This file is part of an extension of the Siafu simulator connect to our
 * work in the field of Mobile Security & Prvacy (MoSP) simulation. 
 * 
 * Siafu as well as its extension is free software; you can redistribute it 
 * and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of the License, 
 * or (at your option) any later version.
 * 
 * Siafu as well as its extension is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.uni_hannover.dcsec.siafu.externalCommand;

import de.uni_hannover.dcsec.siafu.control.Controller;
import de.uni_hannover.dcsec.siafu.model.Agent;

/**
 * 
 * @author P. Salomon
 * @author B. Henne
 */
public class Output {

	private Controller control;

	public Output(final Controller control, int port) {
		this.control = control;
	}

	public void agentLeave(Agent a) {
		System.out.println(control.getWorld().getWorldName() + ": AgentLeave "
				+ a.getName() + " " + a.getJsonData());
		control.getCommandListener().sendAgentToSim("main", a.getType(),
				a.getJsonData());
	}

	public void logToMainSim(String ascii_msg) {
		// double bad hack, 1. using person data channel as log channel, 2. not
		// using type field for this purpose
		// cannot use only type to mark as log, because MoSP simulation
		// currently does not use type, but ascii_msg parsing for starting with
		// "L"
		control.getCommandListener().sendAgentToSim("main", "LogToMain",
				"L" + ascii_msg);
	}

	public void iterationFinish() {
		control.getCommandListener().sendStepDone();
		System.out.println("send StepDone " + control.getWorld().getTick());
	}

}
