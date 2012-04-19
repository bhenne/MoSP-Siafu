/*
 * (c) 2012, Distributed Computing & Security Group, Leibniz Universitaet Hannover
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
import java.io.PrintWriter;

/**
 * 
 * @author P. Salomon
 */
public class SimulationCommandProcessor {

	protected final Controller controller;

	public SimulationCommandProcessor(Controller controller) {
		this.controller = controller;
	}

	public boolean processCommand(final String[] parts, PrintWriter out) {
		if (parts.length < 2)
			send("Kein Befehl angegeben!", out);
		else if (!processSimulationCommand(parts, out))
			send("Die Simulation kenn den Befehl nicht:" + parts[1], out);
		return false;
	}

	protected boolean processSimulationCommand(final String[] parts,
			PrintWriter out) {
		return false;
	}

	private void send(String text, PrintWriter out) {
		if (out != null)
			out.println(text);
	}

}
