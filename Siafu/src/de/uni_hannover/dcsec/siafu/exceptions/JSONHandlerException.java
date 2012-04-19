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

package de.uni_hannover.dcsec.siafu.exceptions;

/**
 * @author C. Szongott
 */
public class JSONHandlerException extends Exception {

	private static final long serialVersionUID = -2202726483935757228L;

	public JSONHandlerException(String message) {
		super(message);
	}

}
