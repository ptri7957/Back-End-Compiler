import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Instruction {

	// A grammar for parsing instructions
	private Map<String, String[]> instrMap;

	// The operation within the instruction
	private String instrOp;

	// The arguments the instruction operator
	// takes in
	private String[] instrArgs;

	// Instruction Constructor
	public Instruction(String instrOp, String[] instrArgs) {
		this.instrOp = instrOp;
		this.instrArgs = instrArgs;

		// Create the grammar for the instructions
		instrMap = new HashMap<String, String[]>();
		instrMap.put("lc", new String[] { "Reg", "Num" });
		instrMap.put("ld", new String[] { "Reg", "Id" });
		instrMap.put("st", new String[] { "Id", "Reg" });
		instrMap.put("add", new String[] { "Reg", "Reg", "Reg" });
		instrMap.put("sub", new String[] { "Reg", "Reg", "Reg" });
		instrMap.put("mul", new String[] { "Reg", "Reg", "Reg" });
		instrMap.put("div", new String[] { "Reg", "Reg", "Reg" });
		instrMap.put("lt", new String[] { "Reg", "Reg", "Reg" });
		instrMap.put("gt", new String[] { "Reg", "Reg", "Reg" });
		instrMap.put("eq", new String[] { "Reg", "Reg", "Reg" });
		instrMap.put("br", new String[] { "Reg", "Num", "Num" });
		instrMap.put("ret", new String[] { "Reg" });
		instrMap.put("call", new String[] { "Reg", "Id", "Reg_List" });
	}

	public String[] getRegisters() {

		List<String> temp = new ArrayList<String>();
		// Get the arguments from the corresponding
		// instruction operator
		String[] gInstrArgs = instrMap.get(this.instrOp);
		// Loop through the instruction arguments and
		// check for registers
		for (int i = 0; i < gInstrArgs.length; i++) {
			String arg = this.instrArgs[i];
			if (gInstrArgs[i].equals("Reg")) {
				// System.out.println(arg);
				// Check if the register is not in the list
				// of registers. This is to ensure that
				// we do not get repeating registers
				if (!temp.contains(arg)) {
					temp.add(arg);
				}

				// If the instruction contains a register list
			} else if (gInstrArgs[i].equals("Reg_List")) {
				// If the registers in the reg_list are not in the list
				// of instruction registers, then add them to the list.
				// Again, this ensures that there are no duplicate
				// registers in the list
				for (int j = i; j < instrArgs.length; j++) {
					if (!temp.contains(instrArgs[j])) {
						temp.add(instrArgs[j]);
					}
				}
			}
		}
		String[] registers = new String[temp.size()];

		for (int i = 0; i < temp.size(); i++) {
			registers[i] = temp.get(i);
		}

		return registers;
	}

	public String[] getVars() {
		List<String> temp = new ArrayList<String>();
		// Get the arguments from the corresponding
		// instruction operator
		String[] gInstrArgs = instrMap.get(this.instrOp);
		// Loop through the instruction arguments and
		// check for variables
		for (int i = 0; i < gInstrArgs.length; i++) {
			String arg = this.instrArgs[i];
			if (gInstrArgs[i].equals("Id")) {
				if (!temp.contains(arg)) {
					temp.add(arg);
				}
			}
		}

		String[] vars = new String[temp.size()];

		for (int i = 0; i < temp.size(); i++) {
			vars[i] = temp.get(i);
		}

		return vars;
	}

	public int[] getNums() {
		List<String> temp = new ArrayList<String>();
		// Get the arguments from the corresponding
		// instruction operator
		String[] gInstrArgs = instrMap.get(this.instrOp);
		// Loop through the instruction arguments and
		// check for nums
		for (int i = 0; i < gInstrArgs.length; i++) {
			String arg = this.instrArgs[i];
			if (gInstrArgs[i].equals("Num")) {
				if (!temp.contains(arg)) {
					temp.add(arg);
				}
			}
		}

		int[] nums = new int[temp.size()];

		for (int i = 0; i < temp.size(); i++) {
			nums[i] = Integer.parseInt(temp.get(i));
		}

		return nums;
	}

	// Return the grammar
	public Map<String, String[]> getInstrGrammar() {
		return instrMap;
	}

	// Return the operator arguments
	public String[] getInstrArgs() {
		return instrArgs;
	}

	// Return the instruction operator
	public String getOp() {
		return instrOp;
	}

	// Format the instruction
	public String formatInstr() {
		String instr = "(" + instrOp + " ";
		for (int i = 0; i < instrArgs.length - 1; i++) {
			instr += instrArgs[i] + " ";
		}
		instr += instrArgs[instrArgs.length - 1] + ")";

		return instr;
	}
	
	public void setRegister(int index, String reg){
		this.getInstrArgs()[index] = reg;
	}

}
