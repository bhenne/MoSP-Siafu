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

import de.nec.nle.siafu.exceptions.PlaceTypeUndefinedException;
import de.uni_hannover.dcsec.siafu.model.Place;
import de.uni_hannover.dcsec.siafu.model.World;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

/**
 * 
 * @author P. Salomon
 * @author C. Szongott
 */
public class SeatManager {
	private static final boolean DEBUG = false;

	private static SeatManager instance = null;

	private ArrayList<Place> freeSeats = new ArrayList<Place>();
	private Map<Place, String> occupiedSeats = new HashMap<Place, String>();
	private Random random;

	private SeatManager() {
	}

	public static SeatManager getInstance() {
		if (instance == null) {
			instance = new SeatManager();
		}
		return instance;
	}

	public void init(World world, String type, long seed) {
		random = new Random(seed);
		setup(world, type);
	}

	public void init(World world, String type) {
		random = new Random();
		setup(world, type);
	}

	private void setup(World world, String type) {
		try {
			Collection<Place> seats = world.getPlacesOfType(type);
			freeSeats = new ArrayList<Place>(seats);
		} catch (PlaceTypeUndefinedException ex) {
			ex.printStackTrace();
		}
	}

	public synchronized Place getFreeSeat(String agentName) {
		if (freeSeats.isEmpty())
			return null;
		else {
			int r = random.nextInt(freeSeats.size());
			Place emptySeat = freeSeats.remove(r);

			assert (!occupiedSeats.containsKey(emptySeat));
			if (occupiedSeats.containsKey(emptySeat)) {
				new Exception("SeatManager: Platz ist nicht leer");
				System.exit(1);
			}
			occupiedSeats.put(emptySeat, agentName);
			if (DEBUG)
				printStatus();
			return emptySeat;
		}
	}

	public synchronized void freePlace(Place p) {
		if (occupiedSeats.remove(p) != null) {
			freeSeats.add(p);
		} else {
			new Exception("Freizugebender Platz war nicht besetzt").printStackTrace();
		}
		if (DEBUG)printStatus();
	}

	private void printStatus() {
		System.out.println("=====PlaceManagerStatus=====");
		for (Entry<Place, String> e : occupiedSeats.entrySet()) {
			System.out.println(e.getKey() + " = " + e.getValue());
		}
	}
}
