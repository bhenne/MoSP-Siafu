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

package de.uni_hannover.dcsec.siafu.behaviormodels;

import java.util.ArrayList;
import java.util.Collection;

import de.uni_hannover.dcsec.siafu.exceptions.JSONHandlerException;
import de.uni_hannover.dcsec.siafu.model.Agent;
import de.uni_hannover.dcsec.siafu.model.World;
import de.nec.nle.siafu.types.BooleanType;
import de.nec.nle.siafu.types.FloatNumber;
import de.nec.nle.siafu.types.IntegerNumber;
import de.nec.nle.siafu.types.Text;
import java.util.Iterator;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Extensions of this class define the behaviour of the agents. The methos
 * createAgents is called when the world is created, and doIteration, at each
 * iteration of the simulator.
 * 
 * implementing these abstract methods, you can define how the agents in the
 * simulation will behave.<br>
 * 
 * You must provide one extension of this class for the simulator to work.
 * 
 * @author M. Martin
 * @author P. Salomon
 * @author B. Henne
 * @author C. Szongott
 * 
 */
public abstract class BaseAgentModel {
	/** The simulated world. */
	protected World world;

	/**
	 * Instantiate a BaseAgentModel.
	 * 
	 * @param world
	 *            the simulation's world
	 */
	public BaseAgentModel(final World world) {
		this.world = world;
	}

	/**
	 * This callback method must return the population of the group simulator,
	 * be it by creating new Person classes or using a random generator like
	 * PersonGenerator. Note that after this call, the info fields of the agents
	 * are locked. This means that you have to put in here whatever fields you
	 * thin you will need, in your simulation, even if with null values.
	 * 
	 * @return an ArrayList with the Agents to be simulated
	 */
	public abstract ArrayList<Agent> createAgents();

	/*
	 * Falls eine wärend der Simulation neue Agents hinzugefügt werden
	 */
	public Agent addAgent(String type, String parameter) {
		return null;
	}

	/**
	 * Read JSON parameters. If same named parameter exists in agent's InfoKeys,
	 * its value is replaced, otherwise data is added to another hashmap.
	 * 
	 * @author P. Salomon
	 * @author B. Henne
	 */
	protected void addJSONParameterToAgent(Agent a, String parameter) {
		JSONObject jsonObject = null;
		// if (!parameter.equals("")) {
		// System.out.println("BaseAgentModel:addJSONParameterToAgent: "
		// + parameter);
		// }
		{
			try {
				jsonObject = new JSONObject(parameter);
			} catch (JSONException ex) {
				System.out.println("Parameter is no json:" + parameter);
			}
			try {
				Iterator it = jsonObject.keys();
				Set agent_keys = a.getInfoKeys();
				while (it.hasNext()) {
					String key = (String) it.next();
					if (agent_keys.contains(key)) {
						// maybe incomplete parsing
						Class type = a.get(key).getClass();
						if (type.equals(Text.class)) {
							a.set(key, new Text(jsonObject.getString(key)));
							// System.out.println("set:"+key+" = "+jsonObject.getString(key));
						} else if (type.equals(BooleanType.class)) {
							a.set(key,
									new BooleanType(jsonObject.getBoolean(key)));
							// System.out.println("set:"+key+" = "+jsonObject.getBoolean(key));
						} else if (type.equals(IntegerNumber.class)) {
							a.set(key,
									new IntegerNumber(jsonObject.getInt(key)));
							// System.out.println("set:"+key+" = "+jsonObject.getBoolean(key));
						} else if (type.equals(FloatNumber.class)) {
							a.set(key, new FloatNumber(jsonObject.getInt(key)));
							// System.out.println("set:"+key+" = "+jsonObject.getBoolean(key));
						} else
							System.out.println("Cant parse parametertype:"
									+ type.getSimpleName());
					}
					a.addData(key, jsonObject.get(key));
					// Object value = jsonObject.get(key);
					// if (value instanceof String)
					// a.addData(key, jsonObject.getString(key));
					// else if (value instanceof Boolean)
					// a.addData(key,
					// Boolean.toString(jsonObject.getBoolean(key)));
					// else if (value instanceof Integer)
					// a.addData(key, Integer.toString(jsonObject.getInt(key)));
					// else if (value instanceof Long)
					// a.addData(key, Long.toString(jsonObject.getLong(key)));
					// else if (value instanceof Double)
					// a.addData(key,
					// Double.toString(jsonObject.getDouble(key)));
				}

			} catch (JSONException ex) {
				System.out.println("Error at JSON parsing:" + parameter);
			}
		}
	}

	/**
	 * Writes back all the values of previously existing fields in the JSON data
	 * map
	 * 
	 * @param a
	 *            Agent
	 */
	@SuppressWarnings({ "static-access", "rawtypes" })
	protected void writeParametersToJSON(Agent a) {
		try {
			JSONObject jsonObj = new JSONObject(a.getJsonData());
			Iterator it = jsonObj.keys();

			while (it.hasNext()) {
				String key = (String) it.next();
				if (a.getInfoKeys().contains(key)) {
					Object agentParameter = a.get(key);
					if (agentParameter != null) {
						if (agentParameter instanceof BooleanType) {
							a.addData(key,
									((BooleanType) agentParameter).getValue());
						} else if (agentParameter instanceof Text) {
							a.addData(key, ((Text) agentParameter).getText());
						} else if (agentParameter instanceof IntegerNumber) {
							a.addData(key, ((IntegerNumber) agentParameter)
									.getNumber());
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	protected String extractAttributeFromJSONAttributes(String attributeKey,
			String parameter) throws Exception {
		JSONObject jsonObject = null;
		String idStr = "";
		try {
			jsonObject = new JSONObject(parameter);
			if (jsonObject.has(attributeKey)) {
				idStr = String.valueOf(jsonObject.getInt(attributeKey));
			} else {
				throw new JSONHandlerException(
						"Could not extract id from JSON data");
			}
		} catch (JSONException ex) {
			ex.printStackTrace();
		}
		return idStr;
	}

	/**
	 * This callback method is called at each iteration of the simulation. By
	 * modifying the agents in the agents parameter, you can define what their
	 * next actions are going to be, how their internal info fields are supposed
	 * to evolve, etc.. Note that you have access to the World, and so can get
	 * all sort of contextual and Place information to help you model the
	 * behavior you need.
	 * 
	 * @param agents
	 *            a Collection containing the agents in the simulation, ready
	 *            for your manipulation.
	 */
	public abstract void doIteration(final Collection<Agent> agents);
}
