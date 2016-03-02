import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class IntermediateParser {

	// An S-Expression parser. Based on the parser
	// written in python found on wikipedia
	public Object parser(String s) {
		List<List<Object>> sexp = new ArrayList<List<Object>>();
		sexp.add(new ArrayList<Object>());
		String word = "";
		boolean inString = false;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '(' && !inString) {
				sexp.add(new ArrayList<Object>());
			} else if (s.charAt(i) == ')' && !inString) {
				if (word.length() > 0) {
					sexp.get(sexp.size() - 1).add(word);
					word = "";
				}
				sexp.get(sexp.size() - 2).add(sexp.remove(sexp.size() - 1));
			} else if ((s.charAt(i) == ' ' || s.charAt(i) == '\n' || s
					.charAt(i) == '\t') && !inString) {
				sexp.get(sexp.size() - 1).add(word);
				word = "";
			} else if (s.charAt(i) == '\"') {
				inString = !inString;
			} else {
				word += s.charAt(i);
			}
		}

		return sexp.remove(sexp.size() - 1).get(0);
	}

	// Warning created as we are using generics
	@SuppressWarnings("unchecked")
	public Object formatBlocks(Object sexp) {
		// Formatted parsed list
		List<Object> formatted = new ArrayList<Object>();

		// Separate the function name and arguments and
		// create a separate list for them
		for (int i = 0; i < ((List<List<Object>>) sexp).size(); i++) {
			List<Object> temp = new ArrayList<Object>();
			temp.add((((List<List<Object>>) sexp).get(i)).get(0));
			temp.add((((List<List<Object>>) sexp).get(i)).get(1));
			formatted.add(temp);
		}

		// Group all the blocks together into a single list
		// and add them to the formatted list
		for (int i = 0; i < ((List<List<Object>>) sexp).size(); i++) {
			List<Object> blocks = new ArrayList<Object>();
			for (int j = 2; j < (((List<List<Object>>) sexp).get(i)).size(); j++) {
				blocks.add((((List<List<Object>>) sexp).get(i)).get(j));
			}
			// System.out.println(blocks);
			((List<List<Object>>) formatted.get(i)).add(blocks);
		}

		return formatted;
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		try{
			FileReader fileReader = new FileReader(args[0]);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = null;
			String exp = "";
			
			while((line = bufferedReader.readLine()) != null){
				//line.replace("    ", "");
				exp += line.replace("    ", "").replace("   ", "");
			}
			
			IntermediateParser parser = new IntermediateParser();
			List<Object> list = (List<Object>) parser.parser(exp.replace("( ", "(")
					.replace(" )", ")"));
			List<Object> formatted = (List<Object>) parser.formatBlocks(list);
		    System.out.println(formatted);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
