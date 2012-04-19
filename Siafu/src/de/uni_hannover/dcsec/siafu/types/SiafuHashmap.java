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

package de.uni_hannover.dcsec.siafu.types;

import java.util.HashMap;

import de.nec.nle.siafu.types.FlatData;
import de.nec.nle.siafu.types.Publishable;

/**
 * @author C. Szongott
 */
public class SiafuHashmap extends HashMap<String, Object> implements
		Publishable {

	private static final long serialVersionUID = 2386590520249617158L;

	// TODO implement if needed
	public FlatData flatten() {
		String data = this.getClass().getSimpleName() + ":";

		data += "here is something not implemented";
		// for (String key : this.keySet()) {
		// data += key + "=" + this.get(key).get(0) + ";" + this.get(key).get(1)
		// + "#";
		// }
		// data = data.substring(0, data.lastIndexOf('#'));
		return new FlatData(data);
	}

}
