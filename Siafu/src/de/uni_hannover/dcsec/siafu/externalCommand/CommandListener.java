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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import de.uni_hannover.dcsec.siafu.control.Controller;
import de.uni_hannover.dcsec.siafu.externalCommand.CommandProcessor;

/**
 * MoSP simulation dispatcher connection to extended Siafu
 * 
 * @author B. Henne <henne@dcsec.uni-hannover.de>
 */
public class CommandListener implements Runnable {

	/** The listening socket. */
	private ServerSocket serverSocket;

	/** Siafu's controller. */
	private Controller control;

	/**
	 * The processor which does the actual hard work beyond command parsing and
	 * verification.
	 */
	private CommandProcessor cp;

	private ConnectionServer cs;

	/** True if the simulator has ended and the command listener shoud die. */
	private boolean ended;

	// /**
	// * Debug send cache
	// */
	// private byte[] send_cache = new byte[10000];
	// private int send_cache_p = 0;
	//
	// /**
	// * Debug receive cache
	// */
	// private byte[] receive_cache = new byte[10000];
	// private String[] receiver_cache = new String[10000];
	// private int receive_cache_p = 0;
	//
	/**
	 * Create a new command listener.
	 * 
	 * @param control
	 *            siafu's controller
	 * @param tcpPort
	 *            the listening port, extracted from Siafu's configuration file.
	 * @throws IOException
	 *             when the server socket encounters an IO error.
	 */
	public CommandListener(final Controller control, final int tcpPort)
			throws IOException {
		System.out.println("Creating the command listener.");
		this.control = control;
		this.serverSocket = new ServerSocket(tcpPort);
		System.out.println("Listening for external commands on port "
				+ this.serverSocket.getLocalPort());
		cp = new CommandProcessor(control);
	}

	/** Method to pack up and die when the simulator quits. */
	public synchronized void die() {
		ended = true;
		try {
			sendQuit();
			serverSocket.close();
		} catch (IOException e) {
			throw new RuntimeException(control.getWorld().getWorldName()
					+ ": Error closing the listening socket", e);
		}
		System.out.println(control.getWorld().getWorldName()
				+ ": Command listener closed.");
	}

	/**
	 * Start listening for commands. When one is received, parse it, and pass it
	 * to the command processor, who will react on it.
	 */
	public void run() {

		while (!ended) {
			try {
				Socket socket = serverSocket.accept();
				cs = new ConnectionServer(socket);
				System.out.println("ConnectinoServer-" + socket.toString());
				new Thread(cs, "ConnectinoServer-" + socket.toString()).start();
			} catch (IOException e) {
				if (!ended) {
					e.printStackTrace();
					control.endSimulator();
				}
			}
		}
	}

	/**
	 * Send data via ConnectionServer
	 * 
	 * @param msg
	 *            array of bytes to send
	 */
	public void send(byte[] msg) {
		if (cs != null)
			cs.send(msg);
	}

	/**
	 * Send a MoSP multisim ACK message
	 */
	public void sendACK() {
		if (cs != null)
			cs.sendACK();
	}

	/**
	 * Send a MoSP multisim Quit message
	 */
	public void sendQuit() {
		if (cs != null)
			cs.sendQuit();
	}

	/**
	 * Send a MoSP multisim StepDone message
	 */
	public void sendStepDone() {
		if (cs != null)
			cs.sendStepDone();
	}

	/**
	 * Send an Agent to another simulation
	 * 
	 * @param destinationSimulationName
	 *            Name of simulation the Agent should be sent to, used to
	 *            address simulation in simulation dispatcher
	 * @param agentType
	 *            Type of Agent that is send
	 * @param agentParameterJSON
	 *            Parameters of Agent actually represented as JSON
	 */
	public void sendAgentToSim(String destinationSimulationName,
			String agentType, String agentParameterJSON) {
		if (cs != null)
			cs.appendToGETReplyBuffer(destinationSimulationName, agentType,
					agentParameterJSON);
	}

	/**
	 * ConnectionServer for MoSP simulation dispatcher
	 * 
	 * @author B. Henne <henne@dcsec.uni-hannover.de>
	 * 
	 */
	protected class ConnectionServer implements Runnable {
		/** The socket for the ongoing comm. */
		private Socket socket;

		/**
		 * The OutputStream through which one can send responses to commands
		 * back to the dispatcher.
		 */
		private OutputStream rawOut;

		/**
		 * The InputStream through which one get messages from the dispatcher.
		 */
		private InputStream rawIn;

		/**
		 * The output buffer for coordinated output/reply on request (MoSP
		 * dispatcher GET)
		 */
		private byte[] outputBuffer;

		/**
		 * MOSP protocol bytes
		 */
		public final byte STEP_DONE_PUSH = (byte) 0xF9;
		public final byte STEP_DONE = (byte) 0xFA;
		public final byte SIM_ENDED = (byte) 0xFB;
		public final byte ACK = (byte) 0xFC;
		public final byte FIELD_SEP = (byte) 0xFD;
		public final byte MSG_SEP = (byte) 0xFE;
		public final byte MSG_END = (byte) 0xFF;
		public final byte QUIT = (byte) 0x00;
		public final byte GET_MODE = (byte) 0x01;
		public final byte PUT_MODE = (byte) 0x02;
		public final byte STEP = (byte) 0x03;
		public final byte IDENT = (byte) 0x04;

		public final boolean STEP_DONE_DEFAULTS_TO_PUSH = false;

		/**
		 * Build an instance of ConnectionServer to serve the connection
		 * received in socket
		 * 
		 * @param socket
		 *            the socket to the client who sent the original command
		 */
		public ConnectionServer(Socket socket) {
			this.socket = socket;
		}

		/**
		 * Reads data from socket until a specified byte occurs.
		 * 
		 * @param endsymbol
		 *            The byte that ends receiving. This is not included in
		 *            return value.
		 * @return received bytes as byte array without the endsymbol.
		 * @throws IOException
		 */
		private byte[] receiveUntil(byte endsymbol) throws IOException {
			int SIZE = 4;
			int len = 0;
			byte b;
			byte[] buf = new byte[SIZE];
			byte[] data = new byte[len];
			while (true) {
				b = (byte) rawIn.read();
				// receiver_cache[receive_cache_p] = "recvUntil " + (b & 0xFF);
				// receive_cache[receive_cache_p++] = b;
				if (b == endsymbol) {
					byte[] tbuff = new byte[data.length + len];
					System.arraycopy(data, 0, tbuff, 0, data.length);
					System.arraycopy(buf, 0, tbuff, data.length, len);
					return tbuff;
				} else {
					buf[len++] = (byte) b;
				}
				if (len >= SIZE) {
					byte[] tbuff = new byte[data.length + SIZE];
					System.arraycopy(data, 0, tbuff, 0, data.length);
					System.arraycopy(buf, 0, tbuff, data.length, SIZE);
					data = tbuff;
					buf = new byte[SIZE];
					len = 0;
				}
			}
		}

		/**
		 * Run this thread, the main message processor.
		 */
		public void run() {
			int int255 = 0;
			rawIn = null;
			rawOut = null;
			outputBuffer = new byte[0];
			try {

				rawOut = socket.getOutputStream();
				rawIn = socket.getInputStream();

				byte b;
				while (!ended) {
					synchronized (this) {
						b = (byte) rawIn.read();
						// receiver_cache[receive_cache_p] = "run " + (b &
						// 0xFF);
						// receive_cache[receive_cache_p++] = b;
						if (b == STEP) {
							byte[] number = receiveUntil(MSG_END);
							processStep(number);
							try {
								this.wait();
							} catch (InterruptedException e) {
								System.err
										.println("Error waiting for STEP_DONE(_PUSH)");
								e.printStackTrace();
							}
						} else if (b == PUT_MODE) {
							byte[] blob = receiveUntil(MSG_END);
							processPUT(blob);
						} else if (b == GET_MODE) {
							processGET();
						} else if (b == IDENT) {
							processIdentify();
						} else if (b == QUIT) {
							processQuit();
						} else {
							long mynewint = b & 0xFF;
							int255++;
							System.out.println(control.getWorld()
									.getWorldName()
									+ ": CommandListener.run() ignored: char #"
									+ mynewint);
							if (int255 > 10000) {
								System.err
										.println("Received 10.000 empty bytes, something seems not to be ok, EXITING.");
								control.endSimulator();
							}
						}
					}
				}
			} catch (IOException e) {
				System.err.println(control.getWorld().getWorldName()
						+ ": Error processing an external command client: "
						+ e.getMessage());
			} finally {
				try {
					if (rawIn != null) {
						rawIn.close();
					}
					if (rawOut != null) {
						rawOut.close();
					}
					if (!socket.isClosed()) {
						socket.close();
					}
				} catch (IOException e) {

				}
			}
		}

		/**
		 * Queue multiple bytes to outputBuffer for reply to GET request
		 * 
		 * @param data
		 *            Data (byte array) to be send on GET request. a complete
		 *            message without separator.
		 */
		public void appendToGETReplyBuffer(byte[] data) {
			byte[] c = new byte[outputBuffer.length + data.length];
			System.arraycopy(outputBuffer, 0, c, 0, outputBuffer.length);
			System.arraycopy(data, 0, c, outputBuffer.length, data.length);
			outputBuffer = c;
		}

		/**
		 * Queue a single byte to outputBuffer for reply to GET request
		 * 
		 * @param data
		 *            Data (single byte) to be send on GET request.
		 */
		public void appendToGETReplyBuffer(byte data) {
			appendToGETReplyBuffer(new byte[] { data });
		}

		/**
		 * Queue data of Agent to outputBuffer for reply to GET request
		 * 
		 * @param destination
		 *            Name of simulation the Agent should be sent to, used to
		 *            address simulation in simulation dispatcher
		 * @param agentType
		 *            Type of Agent that is send
		 * @param agentParameters
		 *            Parameters of Agent actually represented as JSON
		 */
		public void appendToGETReplyBuffer(String destination,
				String agentType, String agentParameters) {
			if (outputBuffer.length > 0) {
				appendToGETReplyBuffer(MSG_SEP);
			}
			appendToGETReplyBuffer(encodeStringToASCIIBytes(destination));
			appendToGETReplyBuffer(FIELD_SEP);
			appendToGETReplyBuffer(encodeStringToASCIIBytes(agentType));
			appendToGETReplyBuffer(FIELD_SEP);
			appendToGETReplyBuffer(encodeStringToASCIIBytes(agentParameters));
			// sending as reply to get
		}

		/**
		 * Convert a String (a Java String is UTF-8) to a ASCII-encoded byte
		 * array
		 * 
		 * @param string
		 *            String to encode
		 * @return byte array ASCII representation of string
		 */
		public byte[] encodeStringToASCIIBytes(String string) {
			Charset charset = Charset.forName("US-ASCII");
			CharsetEncoder encoder = charset.newEncoder();
			ByteBuffer bbuf = null;
			try {
				bbuf = encoder.encode(CharBuffer.wrap(string));
			} catch (CharacterCodingException e) {
				System.err.println(control.getWorld().getWorldName()
						+ ": Could not encode message to US-ASCII");
				e.printStackTrace();
			}
			return bbuf.array();
		}

		/**
		 * Converts a byte array ASCII representation to a String
		 * 
		 * @param bytearray
		 *            A byte array (US-ASCII text)
		 * @return A String
		 */
		public String decodeASCIIBytesToString(byte[] bytearray) {
			Charset charset = Charset.forName("US-ASCII");
			CharsetDecoder decoder = charset.newDecoder();
			ByteBuffer bbuf = ByteBuffer.wrap(bytearray);
			CharBuffer cbuf = null;
			try {
				cbuf = decoder.decode(bbuf);
			} catch (CharacterCodingException e) {
				System.err.println(control.getWorld().getWorldName()
						+ ": Could not decode message to String");
				e.printStackTrace();
			}
			return cbuf.toString();
		}

		/**
		 * Send a generic message to the MoSP dispatcher.
		 * 
		 * @param msg
		 *            the message to send
		 */
		protected void send(final byte[] msg) {
			try {
				rawOut.write(msg);
				// for (int i=0; i < msg.length; i++)
				// send_cache[send_cache_p++] = msg[i];
			} catch (IOException e) {
				System.err.println(control.getWorld().getWorldName()
						+ ": Could not send message");
				e.printStackTrace();
			}
		}

		/**
		 * Send a byte to the MoSP dispatcher.
		 * 
		 * @param b
		 *            byte
		 */
		protected void send(byte b) {
			send(new byte[] { b });
		}

		/**
		 * Process the MoSP multisim protocol <Quit> command
		 */
		private void processQuit() {
			System.out.println(control.getWorld().getWorldName()
					+ ": Received QUIT signal via CommandListener");
			control.endSimulator();
		}

		/**
		 * Process and answer the MoSP multisim protocol <Identify> command
		 */
		private void processIdentify() {
			String simulationType = "siafu";
			String simulationName = control.getWorld().getWorldName();
			long simulationConnectionID = control.getWorld().getConnectionID();
			send(encodeStringToASCIIBytes(simulationType));
			send(FIELD_SEP);
			send(encodeStringToASCIIBytes(simulationName));
			send(FIELD_SEP);
			send(encodeStringToASCIIBytes(Long
					.toString((simulationConnectionID))));
			send(MSG_END);
		}

		/**
		 * Process and ACK the MoSP multisim protocol <Step n> command
		 */
		private void processStep(byte[] number) {
			int n = Integer.parseInt(new String(number));
			send(ACK);
			// System.cd out.println("Sent ACK, now step " + n);
			cp.doSteps(n);
		}

		/**
		 * Process and answer the MoSP multisim protocol <GET> command
		 */
		private void processGET() {
			send(outputBuffer);
			send(MSG_END);
			try {
				byte b = (byte) rawIn.read();
				// receiver_cache[receive_cache_p] = "processGET" + (b & 0xFF);
				// receive_cache[receive_cache_p++] = b;
				if (b != ACK)
					System.err.println(control.getWorld().getWorldName()
							+ ": Waited for ACK of GET reply/PUSH, got "
							+ (b & 0xFF));
			} catch (IOException e) {
				System.err.println(control.getWorld().getWorldName()
						+ ": Something went wrong during GET reply");
				e.printStackTrace();
			}
			outputBuffer = new byte[0];
		}

		/**
		 * Process and ACK the MoSP multisim protocol <PUT> command
		 */
		private void processPUT(byte[] messageblob) {
			int start = 0;
			if (messageblob.length > 0) {
				for (int pos = 0; pos < messageblob.length; pos++) {
					if (messageblob[pos] == MSG_SEP) {
						byte[] message = new byte[pos - start];
						System.arraycopy(messageblob, start, message, 0,
								message.length);
						processMessageFromOtherSimulation(message);
						if (pos + 1 < messageblob.length) {
							start = pos + 1;
						}
					}
				}
				byte[] message = new byte[messageblob.length - start];
				System.arraycopy(messageblob, start, message, 0, message.length);
				processMessageFromOtherSimulation(message);
			}
			// ACK, if everything is processed
			send(ACK);

		}

		/**
		 * Process message from another simulator got by MoSP multisim protocol
		 * <PUT> command
		 */
		private void processMessageFromOtherSimulation(byte[] message) {
			int fieldSepPostion = -1;
			// System.out.println("Message received: "+new String(message));
			for (int pos = 0; pos < message.length; pos++) {
				if (message[pos] == FIELD_SEP) {
					fieldSepPostion = pos;
				}
			}
			byte[] bType = new byte[fieldSepPostion];
			System.arraycopy(message, 0, bType, 0, fieldSepPostion);
			byte[] bParam = new byte[message.length - (fieldSepPostion + 1)];
			System.arraycopy(message, fieldSepPostion + 1, bParam, 0,
					bParam.length);
			String type = new String(bType);
			String param = new String(bParam);

			// add Agent
			System.out.println(control.getWorld().getWorldName()
					+ ": addAgent(" + type + ", " + param + ")");
			cp.addAgent(type, param);
		}

		/**
		 * Send an ACK message back to the dispatcher.
		 */
		private void sendACK() {
			send(ACK);
		}

		/**
		 * Send an QUIT message back to the dispatcher.
		 */
		private void sendQuit() {
			send(QUIT);
		}

		/**
		 * Send a STEP_DONE message back to the dispatcher.
		 */

		private void sendStepDone() {
			sendStepDone(STEP_DONE_DEFAULTS_TO_PUSH);
		}

		/**
		 * Send a STEP_DONE with optional PUSH of GET response.
		 * 
		 * @param push_data
		 *            if false, send STEP_DONE; if true send STEP_DONE_PUSH plus
		 *            GET reply wating for ACK.
		 */
		private void sendStepDone(boolean push_data) {
			synchronized (this) {
				if ((!push_data) || (outputBuffer.length == 0))
					send(STEP_DONE);
				else {
					send(STEP_DONE_PUSH);
					processGET();
				}
				this.notify();
			}
		}

	}

}
