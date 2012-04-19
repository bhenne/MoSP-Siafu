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

import java.util.Calendar;

/**
 * @author C. Szongott
 */
public class DurationTime {

	private int duration;
	private Calendar timestamp;
	
	public DurationTime(Calendar timestamp) {
		this.duration = 0;
		this.timestamp = timestamp;
	}
	
	public DurationTime(int i, Calendar timestamp) {
		this.duration = i;
		this.timestamp = timestamp;
	}
	
	public int getDuration (){
		return this.duration;
	}
	
	public Calendar getTimestamp(){
		return this.timestamp;
	}
}
