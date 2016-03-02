import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class DeadCodeRemover {
	private CFG G;

	public DeadCodeRemover(CFG G) {
		this.G = G;
	}

	// Helper function to determine whether a register is
	// used by an instruction whose results are used.
	public boolean isInExecuted(Map<String, Boolean> map, Instruction inst) {
		for (String register : inst.getRegisters())
			if (map.containsKey(register))
				return true;
		return false;
	}

	// Helper function to determine if all registers are used
	public boolean isExecuted(List<String> list, Instruction inst) {
		for (String register : inst.getRegisters())
			if (list.contains(register))
				return false;
		return true;
	}

	// Read the list of instructions from the bottom up
	// Register is considered used if its value is in a "ret" instruction,
	// a "br" instruction, or an "st" instruction, or used by any
	// instruction whose results are used
	// The in_state maps the state of the registers during the
	// start of execution
	public Map<String, Boolean> isRegisterExecuted(Node node,
			Map<String, Boolean> in_state) {
		// Maps the state of the registers after code execution
		// Instructions that contain registers whose state has
		// not changed will be removed
		Map<String, Boolean> isExecuted = in_state;

		// Reverse the order of the instructions in the
		// given node. This allows for bottom up
		// analysis of instructions
		ListIterator<Instruction> itr = node.getInstructions().listIterator(
				node.getInstructions().size());
		while (itr.hasPrevious()) {
			Instruction inst = itr.previous();
			// If a register is used in a br, st, or ret instruction, or
			// it is used in another instruction whose results are used
			// map the register as "used" i.e. true
			if (inst.getOp().equals("br") || inst.getOp().equals("ret")
					|| inst.getOp().equals("st")
					|| isInExecuted(isExecuted, inst)) {
				for (String register : inst.getRegisters()) {
					// Register is used, change the state
					isExecuted.put(register, true);
				}
			}
		}

		return isExecuted;
	}

	// Function to remove dead code
	public void removeDeadCode() {
		// Get all reachable nodes from the CFG
		List<Node> nodes = G.getNodes();

		// The status of each register from before and after
		// execution of the intermediate code
		Map<Node, List<Map<String, Boolean>>> status = 
				new HashMap<Node, List<Map<String, Boolean>>>();

		for (Node node : nodes) {
			List<Map<String, Boolean>> states = new ArrayList<Map<String, Boolean>>();
			// Each node will contain two states - in and out, which
			// describes the state of the registers in the node before
			// and after execution
			states.add(new HashMap<String, Boolean>());
			states.add(new HashMap<String, Boolean>());
			status.put(node, states);
		}

		// A temporary list of nodes that contain only nodes
		// ending with a "ret" instruction
		List<Node> temp = nodes.get(1).getParentNodes();
		//System.out.println(temp.size());

		// Go through each node in the graph and
		// change the state of each register as we
		// trace through each instruction.
		while (!temp.isEmpty()) {
			// Grab a node from the temp list
			Node node = temp.get(0);
			// Transfer function - calls isRegisterExecuted
			// to check for used registers
			status.get(node).set(1,
					isRegisterExecuted(node, status.get(node).get(0)));

			// Since we are performing a backwards analysis,
			// pass the out state of the child node as the
			// in state of its parent nodes
			for (Node parent : node.getParentNodes()) {
				status.get(parent).set(0, status.get(node).get(0));
				temp.add(parent);
			}

			temp.remove(node);
		}

		// Final step: Iterate each node in the graph and check
		// each instruction to see whether it contains unused
		// registers. If so, remove that instruction from the
		// node
		for (Node node : nodes) {
			List<String[]> registers = node.getRegisters();
			// Get the execution state of the node
			Map<String, Boolean> out_state = status.get(node).get(1);
			List<String> unusedRegisters = new ArrayList<String>();
			for (int i = 0; i < registers.size(); i++) {
				for (String register : registers.get(i)) {
					if (!out_state.containsKey(register)) {
						unusedRegisters.add(register);
					}
				}
			}

			List<Instruction> newInstructions = new ArrayList<Instruction>();
			Iterator<Instruction> itr = node.getInstructions().iterator();
			// Iterate through all of the instructions and
			// check if the registers are used in some way.
			// Remove them and add them to a new list of instructions
			while (itr.hasNext()) {
				Instruction inst = itr.next();
				if (isExecuted(unusedRegisters, inst)) {
					newInstructions.add(inst);
					itr.remove();
				}
			}
			// Add them back into the node
			node.setInstructions(newInstructions);
		}
	}

	public CFG getCFG() {
		return G;
	}

	
}
