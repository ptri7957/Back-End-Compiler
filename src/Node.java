import java.util.ArrayList;
import java.util.List;

public class Node {

	private String startEnd;
	
	// Node id
	private int id;
	// List of instructions within a block
	private List<Instruction> instrs;
	// List of nodes pointing towards this node
	private List<Node> inNodes = new ArrayList<Node>();
	// List of nodes this node is pointing to
	private List<Node> outNodes = new ArrayList<Node>();

	public Node(String startEnd, List<Instruction> instrs) {
		this.startEnd = startEnd;
		this.instrs = instrs;
	}
	
	// Constructor
	public Node(int id) {
		this.id = id;
	}

	// Constructor
	public Node(int id, List<Instruction> instrs) {
		this.id = id;
		this.instrs = instrs;
		startEnd = "";
	}

	// Add instructions to a node
	public void addInstr(Instruction instr) {
		instrs.add(instr);
	}

	// Remove instructions from a node
	public void removeInstr(Instruction instr) {
		instrs.remove(instr);
	}

	public void addChildNode(Node n) {
		outNodes.add(n);
		n.getParentNodes().add(this);
	}

	// Return the block id
	public int getId() {
		return id;
	}
	
	public String getSE() {
		return startEnd;
	}

	// Return the instructions in the node
	public List<Instruction> getInstructions() {
		return instrs;
	}
	
	public List<String[]> getRegisters(){
		List<String[]> registers = new ArrayList<String[]>();
		for(int i = 0; i < instrs.size(); i++){
			registers.add(instrs.get(i).getRegisters());
		}
		return registers;
	}
	
	public void setInstructions(List<Instruction> inst){
		this.instrs = inst;
	}

	// Return the list of in-nodes
	public List<Node> getParentNodes() {
		return inNodes;
	}

	// Return the list of out-nodes
	public List<Node> getOutNodes() {
		return outNodes;
	}
	
	public String getStartEnd(){
		return startEnd;
	}
}
