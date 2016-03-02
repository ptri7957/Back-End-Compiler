import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CFG {

	private List<Node> nodes = new ArrayList<Node>();
	List<Node> visited = new ArrayList<Node>();
	private String funcName;
	private List<Object> args;
	Node start;
	Node end;
	public int blockNum;

	public CFG(String funcName, List<Object> args) {
		this.funcName = funcName;
		this.args = args;
		this.blockNum = 0;
		start = new Node("Start", new ArrayList<Instruction>());
		end = new Node("End", new ArrayList<Instruction>());
		nodes.add(start);
		nodes.add(end);
	}

	@SuppressWarnings("unchecked")
	// Graph construction
	// Loop through each instruction in each block and check for
	// a return or branch instructions while at the same time
	// adding the instructions to an instruction holder.
	// If a 'ret' or 'br' instruction has been reached, stop adding
	// instructions to the instruction holder. Create a new node
	// and add the list of instructions from the instruction holder
	// to the newly created node. Reset the container and repeat.
	// until all the instructions have been read.
	public void setup(List<Object> blocks) {

		// A container to hold the instructions that have been visited
		// Once a 'ret' or 'br' instruction
		// has been seen, the current visited instructions will
		// be assigned to a node with the container reseting.
		List<Instruction> instruction = new ArrayList<Instruction>();
		// Keeps track of which node to branch to if a 'br'
		// instruction has been encountered. Nodes are added to
		// this map on creation
		Map<Integer, Node> nodeBlocks = new HashMap<Integer, Node>();

		// Loop through a function block and add
		// each block of instructions to a new list
		for (int i = 0; i < blocks.size(); i++) {
			// Loop through each individual block
			for (int j = 0; j < ((List<List<Object>>) blocks.get(i)).size(); j++) {
				Object temp = ((List<List<Object>>) blocks.get(i)).get(j)
						.get(0);
				String num = (String) temp;
				int blkNum = Integer.parseInt(num);

				// Loop through instructions in each block
				for (int k = 1; k < ((List<List<Object>>) blocks.get(i)).get(j)
						.size(); k++) {
					// Grab the instruction operation from the instruction
					Object op = ((List<List<Object>>) ((List<List<Object>>) blocks
							.get(i)).get(j).get(k)).get(0);
					String operation = (String) op;

					// A temporary instruction argument container
					List<Object> arg = new ArrayList<Object>();
					for (int l = 1; l < ((List<List<Object>>) ((List<List<Object>>) blocks
							.get(i)).get(j).get(k)).size(); l++) {
						arg.add(((List<List<Object>>) ((List<List<Object>>) blocks
								.get(i)).get(j).get(k)).get(l));
					}

					// Grab the instruction arguments and
					// add them to the array
					String[] arr = new String[arg.size()];
					for (int l = 0; l < arr.length; l++) {
						arr[l] = (String) arg.get(l);
					}

					// Create a new instruction object and add it
					// to the list of instructions visited.
					instruction.add(new Instruction(operation, arr));

					// Check if the operation of the instruction is
					// a 'ret' or 'br'. If 'ret' or 'br' is encountered, create
					// a new node and
					// add the appended instructions to the newly created
					// node. Dealing with branching nodes come later
					if (operation.equals("ret") || operation.equals("br")) {
						Node node = addNode(instruction);
						instruction = new ArrayList<Instruction>();
						// If the node has a 'ret' instruction,
						// link the node the the 'end' node.
						if (operation.equals("ret")) {
							node.addChildNode(end);
							if (!nodeBlocks.containsKey(blkNum)) {
								nodeBlocks.put(blkNum, node);
							}
						}

					}

				}
				if(!instruction.isEmpty()){
					Node node = addNode(instruction);
					instruction = new ArrayList<Instruction>();
					if (!nodeBlocks.containsKey(blkNum)) {
						nodeBlocks.put(blkNum, node);
					}
				}
			}
		}

		// Dealing with br instructions:
		// If the last instruction of a node is a 'br'
		// operation, add the two nodes to branch to
		// as the node's children.
		for (Node n : getNodes()) {
			for (Instruction inst : n.getInstructions()) {
				if (inst != null) {
					if (inst.getOp().equals("br")) {
						// First block to branch
						int fstBlkNum = Integer
								.parseInt(inst.getInstrArgs()[1]);
						// Second block to branch
						int sndBlkNum = Integer
								.parseInt(inst.getInstrArgs()[2]);

						// Link the two nodes to branch to the
						// current node
						if (fstBlkNum != sndBlkNum) {
							n.addChildNode(nodeBlocks.get(fstBlkNum));
							n.addChildNode(nodeBlocks.get(sndBlkNum));
						}else{
							n.addChildNode(nodeBlocks.get(fstBlkNum));
						}

					}
				}
			}
		}

		// Connect the start node with the first
		// node added to the graph after the start
		// and end nodes
		this.start.addChildNode(nodes.get(2));
	}

	public Node addNode(List<Instruction> instruction) {
		Node node = new Node(blockNum, instruction);
		addNodeToGraph(node);
		blockNum++;
		return node;
	}

	public void addNodeToGraph(Node n) {
		nodes.add(n);
	}

	// Phase 1:
	// Perform a depth first search on the CFG
	public List<Node> DFS(CFG G, Node n) {
		visited.add(n);
		for (Node node : G.getNodes()) {
			for (Node child : node.getOutNodes()) {
				if (!visited.contains(child)) {
					return DFS(G, child);
				}
			}
		}
		return visited;
	}

	public void removeNode(Node n) {
		for (Node node : n.getParentNodes()) {
			node.getOutNodes().remove(n);
		}

		for (Node node : n.getOutNodes()) {
			node.getParentNodes().remove(n);
		}
	}

	public void setNodes(List<Node> list) {
		this.nodes = list;
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public List<Object> getFuncArgs() {
		return args;
	}

	public String getFuncName() {
		return funcName;
	}

	public List<Node> getVisitedNodes() {
		return visited;
	}

	public Node getStartNode() {
		return start;
	}

	public Node getEndNode() {
		return end;
	}

	public String formatInstructions() {
		String args = "";
		for (int i = 0; i < this.args.size() - 1; i++) {
			args += this.args + ", ";
		}
		args += this.args.get(this.args.size() - 1);
		String str = "(" + this.getFuncName() + " (" + args + ")\n";

		for (int i = 0; i < getNodes().size() - 1; i++) {
			if (getNodes().get(i).getStartEnd().equals("")) {
				str += "    (" + getNodes().get(i).getId() + " ";
				str += getNodes().get(i).getInstructions().get(0).formatInstr()
						+ "\n";

				for (int j = 1; j < getNodes().get(i).getInstructions().size() - 1; j++) {
					str += "       "
							+ getNodes().get(i).getInstructions().get(j)
									.formatInstr() + "\n";
				}
				str += "       "
						+ getNodes()
								.get(i)
								.getInstructions()
								.get(getNodes().get(i).getInstructions().size() - 1)
								.formatInstr() + " )\n";
			}

		}
		if (getNodes().get(getNodes().size() - 1).getStartEnd().equals("")) {
			str += "    (" + getNodes().get(getNodes().size() - 1).getId()
					+ " ";
			str += getNodes().get(getNodes().size() - 1).getInstructions()
					.get(0).formatInstr()
					+ "\n";

			for (int j = 1; j < getNodes().get(getNodes().size() - 1)
					.getInstructions().size() - 1; j++) {
				str += "       "
						+ getNodes().get(getNodes().size() - 1)
								.getInstructions().get(j).formatInstr() + "\n";
			}
			str += "       "
					+ getNodes()
							.get(getNodes().size() - 1)
							.getInstructions()
							.get(getNodes().get(getNodes().size() - 1)
									.getInstructions().size() - 1)
							.formatInstr() + " ) )";
		}

		return str;
	}
}
