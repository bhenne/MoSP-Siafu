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
import java.util.HashMap;

import de.uni_hannover.dcsec.siafu.cafe1.Constants.Activity;
import static de.uni_hannover.dcsec.siafu.cafe1.Constants.Fields.ACTIVITY;

/**
 * 
 * @author P. Salomon
 */
public class TimeCounter {

	private HashMap<Activity, LongValue> timeMap;

	public TimeCounter() {
		timeMap = new HashMap<Activity, LongValue>();
	}

	public void addTime(Agent a, long time) {
		Activity agentActivity = (Activity) a.get(ACTIVITY);
		if (timeMap.containsKey(agentActivity)) {
			timeMap.get(agentActivity).value += time;
			timeMap.get(agentActivity).count++;
		} else
			timeMap.put(agentActivity, new LongValue(time));
	}

	public void printTimes() {
		for (Activity key : timeMap.keySet()) {
			System.out
					.println(key.flatten()
							+ "::"
							+ ((timeMap.get(key).value * 10000) / timeMap
									.get(key).count) + " :"
							+ timeMap.get(key).count);
		}
		timeMap.clear();
	}

}

class LongValue {
	public long value;
	public int count;

	public LongValue(long v) {
		value = v;
		count = 1;
	}
}
