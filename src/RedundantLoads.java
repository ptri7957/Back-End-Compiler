import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RedundantLoads {
	CFG G;

	public RedundantLoads(CFG G) {
		this.G = G;
	}

	// A helper function that UNIONs two lists.
	// Used for Gen/Kill framework
	public <T> List<T> union(List<T> list1, List<T> list2) {
		Set<T> set = new HashSet<T>();

		set.addAll(list1);
		set.addAll(list2);

		return new ArrayList<T>(set);
	}

	// Function to create a GEN set
	public List<Instruction> GEN(Node node) {
		// Create a GEN set.
		Set<Instruction> gen = new HashSet<Instruction>();

		// For every instruction in the node, we check for any load instructions
		// and add them. If a ret, br, or st instruction is encountered, we
		// check the GEN set for any instructions containing the same register
		// as the encountered instruction and remove them from the set
		for (Instruction instr : node.getInstructions()) {
			String op = instr.getOp();
			if (!op.equals("ret") || !op.equals("br") || !op.equals("st")) {
				Iterator<Instruction> itr = gen.iterator();
				while (itr.hasNext()) {
					Instruction inst = itr.next();
					if (inst.getRegisters()[0].equals(instr.getRegisters()[0])) {
						itr.remove();
					}
				}
				if (op.equals("ld")) {
					gen.add(instr);
				}
			}
			// If a store instruction is encountered, check the GEN set for
			// any instruction containing the same variables as the encountered
			// instruction and remove them from the set and add the encountered
			// instruction
			if (op.equals("st")) {
				Iterator<Instruction> itr = gen.iterator();
				while (itr.hasNext()) {
					Instruction inst = itr.next();
					if (inst.getVars()[0].equals(instr.getVars()[0])) {
						itr.remove();
					}
				}
				gen.add(instr);
			}
		}
		return new ArrayList<Instruction>(gen);
	}

	// Function to create a KILL set
	public List<Instruction> KILL(Node node) {
		Set<Instruction> kill = new HashSet<Instruction>();
		for (Instruction instr : node.getInstructions()) {
			String op = instr.getOp();
			if (!op.equals("ret") || !op.equals("br")) {
				List<Node> node2 = new ArrayList<Node>();
				// Create a new list of nodes that exclude the 
				// current node to ensure that it is not comparing
				// with itself
				for (Node n : G.getNodes()) {
					if (n != node) {
						node2.add(n);
					}
				}

				// Iterate the graph excluding the current node
				for (Node n : node2) {
					List<Instruction> insts = new ArrayList<Instruction>();
					for (Instruction instr2 : n.getInstructions()) {
						if (instr2.getOp().equals("ld")) {
							insts.add(instr2);
						}
					}

					// Iterate through the set of ld instructions in the
					// graph.
					for (Instruction instr2 : insts) {
						if (!op.equals("ret") || !op.equals("br")
								|| !op.equals("st")) {
							// Excluding the ret, br and st instructions,
							// if the instruction in G/{n} is equal
							// to a register in an instruction in n,
							// add it to the kill list
							if (instr2.getRegisters()[0].equals(instr
									.getRegisters()[0])) {
								kill.add(instr2);
							}
						}

						// The instruction is an st operation.
						if (op.equals("st")) {
							// If the instruction in G\{n} has a variable that is stored
							// in a register in n's instruction, add it to the kill
							// list.
							if (instr2.getVars()[0].equals(instr.getVars()[0])) {
								kill.add(instr2);
							}
						}
					}
				}
			}
		}
		return new ArrayList<Instruction>(kill);
	}

	// GEN/KILL framework transfer function
	public List<Instruction> setOut(Node node,
			Map<Node, List<List<Instruction>>> set) {

		// Grab the IN, GEN, and KILL sets from the map
		List<Instruction> in = set.get(node).get(0);
		List<Instruction> gen = set.get(node).get(2);
		List<Instruction> kill = set.get(node).get(3);

		Iterator<Instruction> inItr = in.iterator();

		// Iterate through the IN and KILL sets
		// and remove any instruction from the IN
		// set that is equal to an instruction in the KILL
		// set
		while (inItr.hasNext()) {
			// in[n] - kill[n]
			Instruction instr = inItr.next();
			for (Instruction instr2 : kill) {
				if (instr == instr2) {
					inItr.remove();
				}
			}
		}

		// Return gen[n] UNION (in[n] - kill[n])
		return union(gen, in);
	}

	// Prepares the graph for redundant load removal
	public void setup() {
		int IN = 0;
		int OUT = 1;
		int GEN = 2;
		int KILL = 3;
		List<Node> nodes = G.getNodes();
		Map<Node, List<List<Instruction>>> status = 
				new HashMap<Node, List<List<Instruction>>>();

		// Create a map of states for a given node.
		// Each node will contain an IN, OUT, GEN, and KILL set
		for (Node node : nodes) {
			List<List<Instruction>> states = new ArrayList<List<Instruction>>();
			states.add(new ArrayList<Instruction>());
			states.add(new ArrayList<Instruction>());
			states.add(new ArrayList<Instruction>());
			states.add(new ArrayList<Instruction>());
			status.put(node, states);
		}

		// Set up the GEN and KILL sets for the node
		for (Node node : nodes) {
			status.get(node).set(GEN, GEN(node));
			status.get(node).set(KILL, KILL(node));
		}
		
		// Execute the Gen/Kill framework
		/*
		 * For all nodes n
		 * - set     in[n] = UNION {out[p] | p <- parent node}
		 * - iterate out[n] = gen[n] UNION (in[n] - kill[n])
		 * 
		 * */
		for(Node node: nodes){
			status.get(node).set(OUT, setOut(node, status));
			for(Node children : node.getOutNodes()){
				status.get(children).set(IN, status.get(node).get(OUT));
			}
		}

		// Iterate through the IN and OUT sets of each node
		// and check for any instruction that has different
		// registers holding the same variables and replace them
		for (Node node : nodes) {
			for (Instruction instr : status.get(node).get(IN)) {
				for (Instruction instr2 : status.get(node).get(OUT)) {
					String reg1 = instr.getRegisters()[0];
					String reg2 = instr2.getRegisters()[0];
					String var1 = instr.getVars()[0];
					String var2 = instr2.getVars()[0];
					if (var1.equals(var2) && !reg1.equals(reg2)) {
						// Iterate through all the instructions from the graph.
						// Check if each instruction has a different register that
						// holds the same variable. If so, replace the last register
						// to hold the variable with the first.
						for(Instruction instr3 : node.getInstructions()){
							if (instr3 != instr2) {
								for (int i = 0; i < instr3.getInstrArgs().length; i++) {
									if (instr3.getInstrArgs()[i].equals(reg2)) {
										instr3.getInstrArgs()[i] = reg1;
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
