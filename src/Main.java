import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Main {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		try {
			if (args.length > 0) {
				FileReader fileReader = new FileReader(args[0]);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				PrintWriter writer = new PrintWriter(args[1]);

				String line = null;
				String exp = "";

				while ((line = bufferedReader.readLine()) != null) {
					exp += line.replace("    ", "").replace("   ", "")
							.replace("  ", "");
				}
				bufferedReader.close();
				IntermediateParser parser = new IntermediateParser();
				List<Object> list = (List<Object>) parser.parser(exp.replace(
						"( ", "(").replace(" )", ")"));
				List<Object> formatted = (List<Object>) parser
						.formatBlocks(list);
				// System.out.println(formatted);
				List<Object> blocks = new ArrayList<Object>();
				List<CFG> Gs = new ArrayList<CFG>();

				for (int i = 0; i < formatted.size(); i++) {
					blocks.add(((List<List<Object>>) formatted.get(i)).get(2));
					Object tempName = ((List<List<Object>>) formatted.get(i))
							.get(0);
					String name = (String) tempName;
					List<Object> funcArgs = ((List<List<Object>>) formatted
							.get(i)).get(1);

					CFG graph = new CFG(name, funcArgs);
					graph.setup(blocks);
					// Phase 1: perform DFS and create a list of reachable nodes
					List<Node> visited = graph.DFS(graph,
							graph.getNodes().get(0));

					// Phase 2: Remove unreachable code
					for (int j = 0; j < graph.getNodes().size(); j++) {
						if (!visited.contains(graph.getNodes().get(j))) {
							graph.removeNode(graph.getNodes().get(j));
							graph.getNodes().remove(j);

						}
					}

					// System.out.println(graph.getNodes().size());
					Gs.add(graph);

					// Reset the blocks list for the next graph
					blocks = new ArrayList<Object>();
				}

				for (CFG graph : Gs) {
					RedundantLoads r = new RedundantLoads(graph);
					r.setup();

					// Removes dead code
					DeadCodeRemover dr = new DeadCodeRemover(graph);
					dr.removeDeadCode();
				}

				// Write output to file
				if (Gs.size() > 1) {
					writer.print("( ");
					for (int i = 0; i < Gs.size() - 1; i++) {
						writer.print(Gs.get(i).formatInstructions() + '\n');
					}
					writer.print("  "
							+ Gs.get(Gs.size() - 1).formatInstructions());
					writer.print(" )");
					writer.close();
				} else {
					writer.print("( "
							+ Gs.get(Gs.size() - 1).formatInstructions());
					writer.print(" )");
					writer.close();
				}
			} else {
				System.out
						.println("Available optimisations: unreachable block removal, " +
								"dead code removal, load redundancy elimination");
			}

			/*
			 * // Print to console for testing if(Gs.size() > 1){
			 * System.out.print("( "); for (int i = 0; i < Gs.size() - 1; i++) {
			 * System.out.print(Gs.get(i).formatInstructions() + '\n'); }
			 * System.out.print("  " + Gs.get(Gs.size() -
			 * 1).formatInstructions()); System.out.print(" )"); }else{
			 * System.out.print("( " + Gs.get(Gs.size() -
			 * 1).formatInstructions()); System.out.print(" )"); }
			 * System.out.println();
			 */

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
