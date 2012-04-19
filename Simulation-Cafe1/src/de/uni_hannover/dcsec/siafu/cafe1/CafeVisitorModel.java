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

import static de.uni_hannover.dcsec.siafu.cafe1.Constants.Fields.ACTIVITY;
import static de.uni_hannover.dcsec.siafu.cafe1.Constants.Fields.ACTIVITY_AFTER_CHANGE_FLOOR;
import static de.uni_hannover.dcsec.siafu.cafe1.Constants.Fields.CAFE_WAITING_MAX;
import static de.uni_hannover.dcsec.siafu.cafe1.Constants.Fields.CAFE_WAITING_MIN;
import static de.uni_hannover.dcsec.siafu.cafe1.Constants.Fields.CONNECTION_DURATION;
import static de.uni_hannover.dcsec.siafu.cafe1.Constants.Fields.CONNECTION_PARTNER;
import static de.uni_hannover.dcsec.siafu.cafe1.Constants.Fields.CURRENT_FLOOR;
import static de.uni_hannover.dcsec.siafu.cafe1.Constants.Fields.DESTINATION_AFTER_CHANGE_FLOOR;
import static de.uni_hannover.dcsec.siafu.cafe1.Constants.Fields.END_VISIT;
import static de.uni_hannover.dcsec.siafu.cafe1.Constants.Fields.ID;
import static de.uni_hannover.dcsec.siafu.cafe1.Constants.Fields.INFECTED;
import static de.uni_hannover.dcsec.siafu.cafe1.Constants.Fields.INFECTION_DURATION;
import static de.uni_hannover.dcsec.siafu.cafe1.Constants.Fields.INFECTION_RADIUS;
import static de.uni_hannover.dcsec.siafu.cafe1.Constants.Fields.INFECTION_TIME;
import static de.uni_hannover.dcsec.siafu.cafe1.Constants.Fields.NEED_INTERNET_OFFSET;
import static de.uni_hannover.dcsec.siafu.cafe1.Constants.Fields.NO_CONNECTION_PARTNER;
import static de.uni_hannover.dcsec.siafu.cafe1.Constants.Fields.SEAT;
import static de.uni_hannover.dcsec.siafu.cafe1.Constants.Fields.START_VISIT;
import static de.uni_hannover.dcsec.siafu.cafe1.Constants.Fields.TARGET_FLOOR;
import static de.uni_hannover.dcsec.siafu.cafe1.Constants.Fields.TIME_INTERNET_CAFE;
import static de.uni_hannover.dcsec.siafu.cafe1.Constants.Fields.TYPE;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.uni_hannover.dcsec.siafu.behaviormodels.BaseAgentModel;
import de.nec.nle.siafu.exceptions.AgentNotFoundException;
import de.nec.nle.siafu.exceptions.InfoUndefinedException;
import de.nec.nle.siafu.exceptions.PlaceNotFoundException;
import de.nec.nle.siafu.exceptions.PlaceTypeUndefinedException;
import de.uni_hannover.dcsec.siafu.model.Agent;
import de.uni_hannover.dcsec.siafu.model.Floor;
import de.uni_hannover.dcsec.siafu.model.Place;
import de.uni_hannover.dcsec.siafu.model.World;
import de.nec.nle.siafu.types.BooleanType;
import de.nec.nle.siafu.types.EasyTime;
import de.nec.nle.siafu.types.FloatNumber;
import de.nec.nle.siafu.types.IntegerNumber;
import de.nec.nle.siafu.types.Text;
import de.uni_hannover.dcsec.siafu.cafe1.Constants.Activity;

/**
 * 
 * @author C. Szongott
 * 
 */
public class CafeVisitorModel extends BaseAgentModel {

	// DEBUGGING
	boolean DEBUG_CONNECTION = false;
	boolean DEBUG_INFECTION = false;
	boolean DEBUG_TICK = false;
	boolean DEBUG_MODEL_INIT = true;
	boolean DEBUG_AGENT_INIT = false;

	/** the cafe's entry and exit */
	private Place entryExit;
	private SeatManager seatManager;

	private Collection<Place> up_stairs;
	private Collection<Place> down_stairs;

	private AgentActionLogger logger;

	private static String SEAT_TYPE = "seats";

	/**
	 * Instantiates this agent model.
	 * 
	 * @param world
	 *            the simulation's world
	 */
	public CafeVisitorModel(final World world) {
		super(world);
		try {
			entryExit = world.getPlacesOfType("exit").iterator().next();
			up_stairs = world.getPlacesOfType("stairs_up");
			down_stairs = world.getPlacesOfType("stairs_down");

		} catch (PlaceTypeUndefinedException e) {
			throw new RuntimeException("The door's or stairs undefined", e);
		}

		seatManager = SeatManager.getInstance();
		long seed = world.getRandomSeed();
		if (DEBUG_MODEL_INIT) System.out.println("Random-seed: " + seed);
		seatManager.init(world, SEAT_TYPE, seed);

		logger = new AgentActionLogger(world, "agent_log");
	}

	private Agent createCafeVisitor(String name, int lengthOfStay) {
		Agent a = new Agent(name, entryExit.getPos(), "HumanBlue", world);

		Place seat = seatManager.getFreeSeat(name);
		if (seat == null) {
			a.set(ACTIVITY, Activity.LEAVING_CAFE);
		} else {
			a.set(ACTIVITY, Activity.RESTING);
		}
		a.setVisible(false);
		a.set(SEAT, seat);
		a.set(TYPE, new Text("MobileInfectWiggler"));

		Calendar time = world.getTime();
		EasyTime startVisit = new EasyTime(time.get(Calendar.HOUR_OF_DAY),
				time.get(Calendar.MINUTE));

		EasyTime endVisit = new EasyTime(startVisit).shift(0, lengthOfStay);

		// DEBUG BLOCK BEGIN
		if (DEBUG_AGENT_INIT) {
			System.out.println("startVisit: " + startVisit);
			System.out.println("length of stay: " + lengthOfStay);
			System.out.println("endVisit: " + endVisit);
		}
		// DEBUG BLOCK END

		a.set(START_VISIT, startVisit);
		a.set(END_VISIT, endVisit);
		a.set(ACTIVITY_AFTER_CHANGE_FLOOR, Activity.RESTING);
		a.set(DESTINATION_AFTER_CHANGE_FLOOR, entryExit);

		a.set(INFECTED, new BooleanType(false));
		a.set(CURRENT_FLOOR, entryExit.getFloor());
		a.set(TARGET_FLOOR, entryExit.getFloor());

		a.set(CONNECTION_DURATION, new IntegerNumber(-1));
		a.set(CONNECTION_PARTNER, new Text(NO_CONNECTION_PARTNER));
		a.set(NEED_INTERNET_OFFSET, new IntegerNumber(-1));

		return a;
	}

	/**
	 * Handle the agents by checking if they need to respond to an event, go to
	 * the toilet or go/come home.
	 * 
	 * @param agents
	 *            the people in the simulation
	 */
	@Override
	public void doIteration(final Collection<Agent> agents) {
		Calendar time = world.getTime();
		EasyTime now = new EasyTime(time.get(Calendar.HOUR_OF_DAY),
				time.get(Calendar.MINUTE));

		Iterator<Agent> peopleIt = agents.iterator();
		while (peopleIt.hasNext()) {
			Agent a = peopleIt.next();
			handlePerson(a, now);
		}
	}

	/**
	 * Handle the people in the simulation.
	 * 
	 * @param a
	 *            the agent to handle
	 * @param now
	 *            the current time
	 * @throws Exception
	 */
	private void handlePerson(final Agent a, final EasyTime now) {
		// DEBUG
		if (DEBUG_TICK)
			System.out.println(a.getName() + " is at tick " + world.getTick());

		if (!a.isOnAuto()) {
			return; // This guy's being managed by the user interface
		}

		try {
			switch ((Activity) a.get(ACTIVITY)) {

			case RESTING:
				if (now.isAfter((EasyTime) a.get(START_VISIT))
						&& now.isBefore((EasyTime) a.get(END_VISIT))) {
					// logger.logAgentAction(a, AgentActionLogger.ENTER);
					a.setVisible(true);
					if (((Text) a.get(TYPE)).getText().equals(
							"MobileInfectWiggler")) {
						goToSeat(a);
					} else if (((Text) a.get(TYPE)).getText().equals("wiggler")) {
						searchRandomPoint(a);
					} else {
						new Exception("wrong agent type detected.");
					}
				}
				break;
			case LEAVING_CAFE:
				if (a.isAtDestination()) {
					// logger.logAgentAction(a, AgentActionLogger.LEAVE);
					goToSleep(a);
				}
				break;
			case GOING_2_SEAT:
				if (a.isAtDestination()) {
					beAtSeat(a, now);
				}
				break;
			case AT_SEAT:
				int timeInternetCafe = ((IntegerNumber) a
						.get(TIME_INTERNET_CAFE)).getNumber();
				int needInternetOffset = ((IntegerNumber) a
						.get(NEED_INTERNET_OFFSET)).getNumber();

				if ((((world.getTick() % timeInternetCafe) - needInternetOffset) == 0)
						|| !((Text) a.get(CONNECTION_PARTNER)).getText()
								.equals(NO_CONNECTION_PARTNER)) {
					accessInternet(a, now);
				}
				if (now.isAfter((EasyTime) a.get(END_VISIT))) {
					seatManager.freePlace((Place) a.get(SEAT));
					goHome(a);
				}
				break;
			case GOING_2_STAIR:
				if (a.isAtDestination()) {
					reachStair(a);
				}
				break;
			case IS_HOME:
				break;

			case GOING_2_POINT:
				if (a.isAtDestination()) {
					a.set(ACTIVITY, Activity.WANDER_AROUND);
				}
				break;

			case WANDER_AROUND:
				if (now.isAfter((EasyTime) a.get(END_VISIT))) {
					goHome(a);
				}
				a.wander(30);
				break;

			default:
				throw new RuntimeException("Unknown Activity:"
						+ a.get(ACTIVITY).toString());
			}
		} catch (InfoUndefinedException e) {
			throw new RuntimeException("Unknown info requested for " + a, e);
		}
	}

	private void accessInternet(Agent a, EasyTime now) {

		// Stop and return, if agent is already infected
		if (((BooleanType) a.get(INFECTED)).getValue())
			return;

		float radius = (float) ((FloatNumber) a.get(INFECTION_RADIUS))
				.getNumber();

		ArrayList<Agent> allAgentsNear = world.findAgentsNearAgent(a, radius,
				true);

		ArrayList<Agent> infectedAgentsNear = new ArrayList<Agent>();

		// filter out not infected agents
		for (Agent x : allAgentsNear) {
			if (((BooleanType) x.get(INFECTED)).getValue()) {
				infectedAgentsNear.add(x);
			}
		}

		// DEBUG BLOCK BEGIN
		if (DEBUG_CONNECTION) {
			System.out.println("=============Agent " + a.getName()
					+ "==================");
			System.out.println("05: tick: " + world.getTick());
			System.out
					.println("06: need_internet_offset : "
							+ ((IntegerNumber) a.get(NEED_INTERNET_OFFSET))
									.getNumber());
			System.out.println("10: agentsNear:");
			for (Agent x : infectedAgentsNear) {
				System.out.println(x.getName());
			}
			System.out.println("20: Agent " + a.getName()
					+ " has connection partner "
					+ ((Text) a.get(CONNECTION_PARTNER)).getText());
			System.out.println("25: connection duration "
					+ ((IntegerNumber) a.get(CONNECTION_DURATION)).getNumber());
			System.out.println("30: Agent "
					+ a.getName()
					+ " no connection partner? "
					+ ((Text) a.get(CONNECTION_PARTNER)).getText().equals(
							NO_CONNECTION_PARTNER));
		}
		// DEBUG BLOCK END

		if (((Text) a.get(CONNECTION_PARTNER)).getText().equals(
				NO_CONNECTION_PARTNER)) {

			// DEBUG
			if (DEBUG_CONNECTION)
				System.out.println("40: # of infected agents near: "
						+ infectedAgentsNear.size());

			if (infectedAgentsNear.size() != 0) {
				Random generator = new Random();
				int randomInt = generator.nextInt(infectedAgentsNear.size());
				int i = 0;
				for (Agent nearOne : infectedAgentsNear) {
					if (i == randomInt) {
						a.set(CONNECTION_PARTNER, new Text(nearOne.getName()));
						a.set(CONNECTION_DURATION, new IntegerNumber(0));
					}
					i++;
				}
			}
		} else {
			try {
				// DEBUG BLOCK BEGIN
				if (DEBUG_CONNECTION) {
					System.out.println("50: Agent " + a.getName()
							+ " has CONNECTION_PARTNER "
							+ ((Text) a.get(CONNECTION_PARTNER)).getText());
					System.out.println("99: All people in world: "
							+ world.getPeople());
				}
				// DEBUG BLOCK END
				Agent connectionPartner = world.getPersonByName(((Text) a
						.get(CONNECTION_PARTNER)).getText());
				if (infectedAgentsNear.contains(connectionPartner)) {
					int currentConnectionDuration = ((IntegerNumber) a
							.get(CONNECTION_DURATION)).getNumber();
					currentConnectionDuration++;
					a.set(CONNECTION_DURATION, new IntegerNumber(
							currentConnectionDuration));

					if (currentConnectionDuration >= ((IntegerNumber) a
							.get(INFECTION_DURATION)).getNumber()) {

						// DEBUG
						if (DEBUG_INFECTION) {
							System.out.println("=====BEGIN INFECTION DEBUG");
							System.out.println("Agent " + a.getName()
									+ " infected at " + world.getTick());
							System.out.println("CONNECTION_PARTNER: "
									+ ((Text) a.get(CONNECTION_PARTNER))
											.getText());
							System.out.println("CONNECTION_DURATION: "
									+ ((IntegerNumber) a
											.get(CONNECTION_DURATION))
											.getNumber());
							System.out.println("=====END INFECTION DEBUG");
						}

						infect(a, connectionPartner);
					}
				} else {
					a.set(CONNECTION_PARTNER, new Text(NO_CONNECTION_PARTNER));
					a.set(CONNECTION_DURATION, new IntegerNumber(-1));
				}
			} catch (AgentNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private void infect(final Agent a, final Agent infectious) {
		a.set(INFECTED, new BooleanType(true));
		a.setImage("HumanYellow");
		a.set(CONNECTION_PARTNER, new Text(NO_CONNECTION_PARTNER));
		a.set(CONNECTION_DURATION, new IntegerNumber(-1));
		a.set(INFECTION_TIME, new IntegerNumber(safeLongToInt(world.getTick())));
		logger.logInfection(a, infectious);
	}

	private void goToSleep(final Agent a) {
		a.set(ACTIVITY, Activity.IS_HOME);
		a.setVisible(false);
		writeParametersToJSON(a);
		a.delete();
	}

	private void goHome(final Agent a) {
		if (entryExit.distanceFrom(a.getPos()) > 20000) {
			a.set(ACTIVITY, Activity.GOING_2_STAIR);
			a.set(ACTIVITY_AFTER_CHANGE_FLOOR, Activity.LEAVING_CAFE);
			a.set(DESTINATION_AFTER_CHANGE_FLOOR, entryExit);
			a.set(TARGET_FLOOR, entryExit.getFloor());
			goToStair(a);
		} else {
			a.setDestination(entryExit);
			a.set(ACTIVITY, Activity.LEAVING_CAFE);
		}
	}

	private void goToSeat(final Agent a) {
		Place desk = (Place) a.get(SEAT);

		if (desk.distanceFrom(a.getPos()) < 20000) {
			a.set(ACTIVITY, Activity.GOING_2_SEAT);
			a.setDestination(desk);
		} else {
			a.set(ACTIVITY_AFTER_CHANGE_FLOOR, Activity.GOING_2_SEAT);
			a.set(DESTINATION_AFTER_CHANGE_FLOOR, desk);
			a.set(TARGET_FLOOR, desk.getFloor());
			goToStair(a);
		}
	}

	private void beAtSeat(final Agent a, final EasyTime now) {
		a.set(ACTIVITY, Activity.AT_SEAT);
	}

	private void goToStair(final Agent a) {
		Floor currentFloor = ((Floor) a.get(CURRENT_FLOOR));
		Floor targetFloor = ((Floor) a.get(TARGET_FLOOR));
		Collection<Place> stairs;
		if (currentFloor.getLevel() > targetFloor.getLevel())
			stairs = down_stairs;
		else
			stairs = up_stairs;
		for (Place stair : stairs) {
			if (stair.getFloor().getLevel() == currentFloor.getLevel()) {
				a.setDestination(stair);
				a.set(ACTIVITY, Activity.GOING_2_STAIR);
				return;
			}
		}

	}

	private void reachStair(final Agent a) {
		Floor currentFloor = ((Floor) a.get(CURRENT_FLOOR));
		Floor targetFloor = ((Floor) a.get(TARGET_FLOOR));
		Collection<Place> stairs;
		Floor newFloor;
		int nextLevel = 0;
		if (currentFloor.getLevel() > targetFloor.getLevel()) {
			stairs = up_stairs;
			nextLevel = currentFloor.getLevel() - 1;
		} else {
			stairs = down_stairs;
			nextLevel = currentFloor.getLevel() + 1;
		}
		for (Place stair : stairs) {
			if (stair.getFloor().getLevel() == nextLevel) {
				a.setPos(stair.getPos());

				newFloor = stair.getFloor();
				a.set(CURRENT_FLOOR, newFloor);
				if (newFloor.getLevel() == targetFloor.getLevel())
					reachTargetFloor(a);
				else
					goToStair(a);
				return;
			}
		}
	}

	private void reachTargetFloor(final Agent a) {
		switch ((Activity) a.get(ACTIVITY_AFTER_CHANGE_FLOOR)) {

		case GOING_2_SEAT:
			goToSeat(a);
			break;
		case LEAVING_CAFE:
			goHome(a);
			break;
		case GOING_2_POINT:
			goToPoint(a, (Place) a.get(DESTINATION_AFTER_CHANGE_FLOOR));
			break;
		default:
			new Exception(
					"Agent "
							+ a
							+ " reached target floor and can not determine new acitivity");
		}
	}

	private void searchRandomPoint(final Agent a) {
		try {
			Place point = world.getRandomPlaceOfType(SEAT_TYPE);
			Floor curr_floor = (Floor) a.get(CURRENT_FLOOR);
			Floor target_floor = point.getFloor();

			if (curr_floor.getLevel() == target_floor.getLevel()) {
				goToPoint(a, point);
			} else {
				a.set(DESTINATION_AFTER_CHANGE_FLOOR, point);
				a.set(ACTIVITY_AFTER_CHANGE_FLOOR, Activity.GOING_2_POINT);
				a.set(TARGET_FLOOR, target_floor);
				goToStair(a);
			}
		} catch (PlaceNotFoundException ex) {
			Logger.getLogger(CafeVisitorModel.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

	private void goToPoint(final Agent a, final Place p) {
		a.set(ACTIVITY, Activity.GOING_2_POINT);
		a.setDestination(p);
	}

	@Override
	public Agent addAgent(String type, String attributes) {
		if (type.equals("MobileInfectWiggler")) {
			String agentName = null;
			int infectionDuration = -1;
			float infectionRadius = -1;
			int pCafeWaitingMINSeconds = -1;
			int pCafeWaitingMAXSeconds = -1;
			int timeInternetCafe = -1;
			int needInternetOffset = -1;
			int infectionTime = -1;
			try {
				agentName = extractAttributeFromJSONAttributes(ID, attributes);
				infectionDuration = Integer
						.valueOf(extractAttributeFromJSONAttributes(
								INFECTION_DURATION, attributes));
				infectionRadius = Float
						.valueOf(extractAttributeFromJSONAttributes(
								INFECTION_RADIUS, attributes));
				pCafeWaitingMINSeconds = Integer
						.valueOf(extractAttributeFromJSONAttributes(
								CAFE_WAITING_MIN, attributes));
				pCafeWaitingMAXSeconds = Integer
						.valueOf(extractAttributeFromJSONAttributes(
								CAFE_WAITING_MAX, attributes));
				timeInternetCafe = Integer
						.valueOf(extractAttributeFromJSONAttributes(
								TIME_INTERNET_CAFE, attributes));
				needInternetOffset = Integer
						.valueOf(extractAttributeFromJSONAttributes(
								NEED_INTERNET_OFFSET, attributes));
				infectionTime = Integer
						.valueOf(extractAttributeFromJSONAttributes(
								INFECTION_TIME, attributes));
			} catch (Exception e) {
				e.printStackTrace();
			}

			int visitSeconds = pCafeWaitingMAXSeconds - pCafeWaitingMINSeconds;

			Random generator = new Random();
			int currentAgentVisitTime = (generator.nextInt(visitSeconds) + pCafeWaitingMINSeconds) / 60;

			Agent a = createCafeVisitor(agentName, currentAgentVisitTime);
			a.set(INFECTION_DURATION, new IntegerNumber(infectionDuration));
			a.set(INFECTION_RADIUS, new FloatNumber(infectionRadius));
			a.set(CAFE_WAITING_MIN, new IntegerNumber(pCafeWaitingMINSeconds));
			a.set(CAFE_WAITING_MAX, new IntegerNumber(pCafeWaitingMAXSeconds));
			a.set(TIME_INTERNET_CAFE, new IntegerNumber(timeInternetCafe));
			a.set(NEED_INTERNET_OFFSET, new IntegerNumber(needInternetOffset));
			a.set(INFECTION_TIME, new IntegerNumber(infectionTime));

			addJSONParameterToAgent(a, attributes);
			if (((BooleanType) a.get(INFECTED)).getValue()) {
				a.setImage("HumanYellow");
			} else {
				a.setImage("HumanBlue");
			}
			return a;
		} else {
			return null;
		}
	}

	public static int safeLongToInt(long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(l
					+ " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}

	@Override
	public ArrayList<Agent> createAgents() {
		ArrayList<Agent> agentList = new ArrayList<Agent>();
		return agentList;
	}
}
