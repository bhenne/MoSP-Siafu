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

package de.uni_hannover.dcsec.siafu.control;

import java.util.Calendar;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.XMLConfiguration;

import de.uni_hannover.dcsec.siafu.behaviormodels.BaseAgentModel;
import de.uni_hannover.dcsec.siafu.behaviormodels.BaseContextModel;
import de.uni_hannover.dcsec.siafu.behaviormodels.BaseWorldModel;
import de.nec.nle.siafu.exceptions.GUINotReadyException;
import de.uni_hannover.dcsec.siafu.graphics.markers.Marker;
import de.uni_hannover.dcsec.siafu.model.Agent;
import de.uni_hannover.dcsec.siafu.model.SimulationData;
import de.uni_hannover.dcsec.siafu.model.Trackable;
import de.uni_hannover.dcsec.siafu.model.World;
import de.uni_hannover.dcsec.siafu.output.CSVPrinter;
import de.uni_hannover.dcsec.siafu.output.NullPrinter;
import de.uni_hannover.dcsec.siafu.output.SimulatorOutputPrinter;
import java.util.ArrayList;
import java.util.Stack;

/**
 * The simulation class implements the <code>Runnable</code> that performs the
 * simulation itself.
 * <p>
 * In detail, the simulation thread handles the evolution of agent, context and
 * world according to their respective models, and prints out the agent
 * information using the configured <code>SimulationOutputPrinter</code>.
 * 
 * @author M. Martin
 * @author P. Salomon
 * @author B. Henne
 * @author C. Szongott
 * 
 */
public class Simulation implements Runnable {

	/**
	 * The <object>Controller</object> that governs this run of the Context
	 * Simulator.
	 */
	private final Controller control;

	/** Whether the simulation should end. */
	private boolean ended;

	/**
	 * The jar file or folder containing the simulation data.
	 */
	private SimulationData simData;

	/**
	 * Specifies whether the simulation is currently paused or not. See
	 * <code>isPaused()</code> for details.
	 */
	private boolean paused;

	/**
	 * The simulation's world. This is only a reference to the
	 * <code>World</code> in <code>control</code>.
	 */
	private World world;

	/**
	 * The simulation's time, held in a <code>Calendar</code> object.
	 */
	private Calendar time;

	/**
	 * The Agent Model, as defined by the configuration file.
	 * <p>
	 * The agent model determines the behaviour of an agent, deciding what he
	 * does next at each iteration, and how it changes over time.
	 */
	private BaseAgentModel agentModel;

	/**
	 * The World Model, as defined by the configuration file.
	 * <p>
	 * The world model handles the simulated places, how they evolve, and the
	 * events they generate.
	 */
	private BaseWorldModel worldModel;

	/**
	 * The Context Model, as thefined by the configuration file.
	 * <p>
	 * The context model handles the evolution of the context variables over
	 * time.
	 */
	private BaseContextModel contextModel;

	/**
	 * The simulation time that ellapses between each iteration.
	 */
	private int iterationStep;

	/**
	 * The tick count that elapsed since simulation start
	 */
	private int ticks = 0;

	/**
	 * The output printer for the simulator. The simulation thread calls on this
	 * object to print per iteration reports on the agents.
	 */
	private SimulatorOutputPrinter outputPrinter;

	/**
	 * Whether the simulation is already running.
	 */
	private boolean simulationRunning;

	/** The configuration of the running simulation. */
	private Configuration simulationConfig;

	/**
	 * The simulator's config (in opposition to the simulation's config).
	 */
	private XMLConfiguration siafuConfig;

	/**
	 * Liste von Agenten die von Außen in die Simulation eingefügt werden (z.B.
	 * über TCP)
	 * 
	 * psa
	 */
	private Stack<Agent> incommingAgents;

	/**
	 * Soll die Simulation automatisch weiterlaufen oder von Außen getaktet
	 * werden
	 */
	private boolean isAutomaticIteration;

	/**
	 * Anzahl der Schritte bis die Simulation wieder stoppt. (Nur wenn von Außen
	 * getaktet)
	 */
	private int iterationStepsToDo = 0;

	/**
	 * Find out if the simulation is already running.
	 * 
	 * @return true if the simulation is running
	 */
	public boolean isSimulationRunning() {
		return simulationRunning;
	}

	public Controller getControl() {
		return this.control;
	}

	/**
	 * Build a <code>Simulation</code> object and start a thread that governs
	 * it.
	 * 
	 * @param simulationPath
	 *            the path to the simulation data, which includes maps, sprites,
	 *            behavior models, etc...
	 * @param control
	 *            the simulation <code>Controller</code>
	 */
	public Simulation(final String simulationPath, final Controller control) {
		this.simData = SimulationData.getInstance(simulationPath);
		this.siafuConfig = control.getSiafuConfig();
		this.simulationConfig = simData.getConfigFile();
		this.control = control;
		this.incommingAgents = new Stack<Agent>();

		World.setShouldPrefillCache(control.getSiafuConfig().getBoolean(
				"ui.gradientcache.prefill"));

		World.setCacheSize(control.getSiafuConfig().getInt(
				"ui.gradientcache.size"));

		if (this.simulationConfig.containsKey("automaticiteration")) {
			if (this.simulationConfig.getBoolean("automaticiteration"))
				this.isAutomaticIteration = true;
			else
				this.isAutomaticIteration = false;
		} else
			this.isAutomaticIteration = true;

		System.out.println("Auto Int:" + this.isAutomaticIteration);

		new Thread(this, "Simulation thread").start();
	}

	/**
	 * Get the simulation's world.
	 * 
	 * @return the simulation's world
	 */
	public World getWorld() {
		return world;
	}

	/**
	 * Get the simulation's ticks.
	 */
	public int getTicks() {
		return ticks;
	}

	/**
	 * Sets the output type of the simulator. This is done by assigning an
	 * implementation of <code>SimulatorOutputPrinter</code> to simPrinter of
	 * the type provided in the parameter.
	 * 
	 * @param type
	 *            the output printer type. Currently, only "null" and "cvs" are
	 *            supported.
	 * @return an instance of the SimulatorOutputPrinter of the given type
	 * 
	 * @see de.nec.nle.siafu.output.SimulatorOutputPrinter
	 */
	private SimulatorOutputPrinter createOutputPrinter(final String type) {

		if (type.equalsIgnoreCase("csv")) {
			System.out.println("CSV output selected");
			return new CSVPrinter(world, siafuConfig);
		} else if (type.equalsIgnoreCase("null")) {
			return new NullPrinter();
		} else {
			throw new RuntimeException(
					"Unknown ouput type in the configuration");
		}
	}

	/**
	 * Starts the simulation.
	 */
	public void run() {
		this.world = new World(this, simData);
		this.time = world.getTime();
		this.iterationStep = simulationConfig.getInt("iterationstep");
		this.agentModel = world.getAgentModel();
		this.worldModel = world.getWorldModel();
		this.contextModel = world.getContextModel();
		this.outputPrinter = createOutputPrinter(siafuConfig
				.getString("output.type"));

		Controller.getProgress().reportSimulationStarted();
		simulationRunning = true;
		// int h = time.get(Calendar.HOUR_OF_DAY);
		int m = time.get(Calendar.MINUTE);
		if (!isAutomaticIteration)
			this.control.setPaused(true);

		while (!isEnded()) {
			/*
			 * if(time.after(world.getEndTime())) {
			 * System.out.println("End simulation, no time left!"); break; }
			 */

			if (!isAutomaticIteration) {
				if (iterationStepsToDo == 0) {
					if (!isPaused()) {
						this.control.getOutput().iterationFinish(); // step done
						this.control.setPaused(true);
					}
				} else
					iterationStepsToDo--;
			}

			// if (h != time.get(Calendar.HOUR_OF_DAY)) {
			// h = time.get(Calendar.HOUR_OF_DAY);
			// System.out.println("Time: " + h);
			// System.out.println("#people:"+world.getPeople().size());
			// }
			if (m != time.get(Calendar.MINUTE)) {
				m = time.get(Calendar.MINUTE);
				System.out.print(".");
			}

			if (!isPaused()) {
				tickTime();
				ticks++;
				System.out.println(ticks + " ("
						+ this.getWorld().getWorldName() + ")");
				worldModel.doIteration(world.getPlaces());
				agentModel.doIteration(world.getPeople());
				contextModel.doIteration(world.getOverlays());
			}
			addOrRemoveAgents();
			moveAgents();
			control.scheduleDrawing();
			outputPrinter.notifyIterationConcluded();
		}
		simulationRunning = false;

		outputPrinter.cleanup();
		Controller.getProgress().reportSimulationEnded();
	}

	/**
	 * Stop looping the simulatio and, well, kill the thread.
	 */
	public void die() {
		ended = true;
	}

	/**
	 * Pauses the simulation. See <code>isPaused()</code> for details. If the
	 * gui is not used, this method has no effect.
	 * 
	 * @param state
	 *            a boolean with the value true will pause the simulation, while
	 *            false will resume it.
	 */
	public synchronized void setPaused(final boolean state) {
		// if (control.isGuiUsed()) {
		paused = state;
		// }
	}

	/**
	 * Reports on the pause state of the simulation.
	 * <p>
	 * When paused, time stops, and all automatic behaviour is interrupted.
	 * However, using the GUI, the user can still manipulate agents in his
	 * control, or update variables through the external command interface.
	 * 
	 * @return true if the simulation is paused, false otherwise
	 */
	public synchronized boolean isPaused() {
		return paused;
	}

	/**
	 * Find out if the simulation is ended or ending.
	 * 
	 * @return true if the simulation is ending or has already ended
	 */
	private synchronized boolean isEnded() {
		return ended;
	}

	/**
	 * Handles all of the movements of the agents. Note that an agent is allowed
	 * to move only if the simulation is not paused or if he is being controlled
	 * by the user.
	 * 
	 */
	private void moveAgents() {
		for (Agent a : world.getPeople()) {

			if (!isPaused() || !a.isOnAuto()) {
				a.moveTowardsDestination();
			}
		}
	}

	private void addOrRemoveAgents() {
		ArrayList<Agent> removeList = new ArrayList<Agent>();

		for (Agent a : world.getPeople()) {
			if (a.isDeleted())
				removeList.add(a);
		}

		for (Agent a : removeList) {
			control.getOutput().agentLeave(a);
			world.deleteAgent(a);
		}

		while (!incommingAgents.empty()) {
			world.addAgent(incommingAgents.pop());
		}

	}

	/**
	 * Increases the simulation time by <code>iterationStep</code>, defined in
	 * the configuration file.
	 */
	private void tickTime() {
		time.add(Calendar.SECOND, iterationStep);
	}

	/**
	 * Add a Marker to the simulation GUI. If the GUI is not ready, this method
	 * performs no action a GUINotReadyException is thrown. If the GUI is not
	 * being used this method returns silently and does nothing.
	 * 
	 * @param m
	 *            the marker to add
	 * @throws GUINotReadyException
	 *             if the GUI can not draw the mark at the moment.
	 */
	public void addMarker(final Marker m) throws GUINotReadyException {
		control.addMarker(m);
	}

	/**
	 * Remove all Markers from the simulation GUI. If the GUI is not ready, this
	 * method performs no action a GUINotReadyException is thrown. If the GUI is
	 * not being used this method returns silently and does nothing.
	 * 
	 * @throws GUINotReadyException
	 *             if the GUI can not draw the mark at the moment.
	 */
	public void unMarkAll() throws GUINotReadyException {
		control.unMarkAll();
	}

	/**
	 * Remove the Marker for this Trackable from the simulation GUI F. If the
	 * GUI is not ready, this method performs no action a GUINotReadyException
	 * is thrown. If the GUI is not being used this method returns silently and
	 * does nothing.
	 * 
	 * @param t
	 *            the Trackable to unmark
	 * @throws GUINotReadyException
	 *             if the GUI can not draw the mark at the moment.
	 */
	public void unMark(final Trackable t) throws GUINotReadyException {
		control.unMark(t);
	}

	/**
	 * Find out if the given Trackable is marked in the GUI. If the GUI is not
	 * ready, this method throws a GUINotReadyException. If the GUI is not being
	 * used this method returns false.
	 * 
	 * @param t
	 *            the Trackable about which we are asking
	 * @return true if the trackable has been marked, false otherwise
	 * @throws GUINotReadyException
	 *             if the GUI can not draw the mark at the moment.
	 */
	public boolean isMarked(final Trackable t) throws GUINotReadyException {
		return control.isMarked(t);
	}

	public String addAgent(String type, String parameter) {
		Agent.unlockInfoFields();
		;
		Agent agent = getWorld().getAgentModel().addAgent(type, parameter);
		Agent.lockInfoFields();
		if (agent != null) {
			incommingAgents.push(agent);

			return agent.getName();
		} else
			return "no agent added!";
	}

	/**
	 * Get the Simulation's Data object.
	 * 
	 * @return the SimulationData for the running simulation
	 */
	public SimulationData getSimulationData() {
		return simData;
	}

	public boolean isAutomaticIteration() {
		return this.isAutomaticIteration;
	}

	public int addSteps(int steps) {
		iterationStepsToDo += steps;
		return iterationStepsToDo;
	}
}
