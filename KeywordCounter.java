import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

public class KeywordCounter {

	FiboHeap heap = new FiboHeap();
	Hashtable<String, Node> ht = new Hashtable<String, Node>();		// To store <keyword, node> pairs
	StringBuffer output = new StringBuffer("");
	char startLineChar;		//	To handle start character other than $
	int lineNumber = 0;
	String outputFilePath = null;
	
	public static void main(String[] args) {
		String inputFile = args[0];
		//File file = new File("C:/Users/Shashank M/Desktop/million.txt");
		File file = new File(inputFile);
		KeywordCounter prog = new KeywordCounter();
		prog.readFile(file);
	}
	
	public void readFile(File file) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			outputFilePath = file.getParent()+"/output_file.txt";		//	Output file path directory same as input file's
			String line = null;
			int returned = 3;	// Before processing lines
			while((line = br.readLine()) != null){
				if(returned == 3){
					startLineChar = line.charAt(0);		//	Processed the first line. Read the first character.
					if(startLineChar != '$')
						System.out.println("Expected $. But working with "+startLineChar);	//	 Continue with different character.
				}
				returned = processLine(line);
			}
			if(returned != 0){		// File did not end with 'stop'
				System.out.println("Missing 'stop' input terminator.");
				writeOutputFile();
			}
		} catch (FileNotFoundException e) {
			System.out.println("File '"+file.getAbsolutePath()+"' doesn't exist");
			System.out.println("Please provide the correct absolute path.");
		} catch (IOException e) {
			System.out.println("I/O Error");
		}
	}
	
	private int processLine(String line){
		++lineNumber;		//	Maintained a lineCounter to specify in errors
		if(line.length()>0 && line.charAt(0) == startLineChar){
			String[] twoParts = line.split(" ");
			String keyword = twoParts[0].substring(1);
			int freq = Integer.parseInt(twoParts[1]);
			if(ht.containsKey(keyword))		//	Check if keyword has already been encountered
				heap.increaseKey(ht.get(keyword), freq);	
			else {
				Node newNode = new Node(freq);
				ht.put(keyword, newNode);
				heap.insert(newNode);		//	Insert node (keyword) as it is new
			}
			return 1;						//	'stop' not encountered, but continue
		}
		else if(line.toLowerCase().equals("stop")) {
			writeOutputFile();
			return 0;						// 'stop' encountered
		}
		else{
			int count;
			try {
				count = Integer.parseInt(line);
				if(count<0){
					output.append("Line "+lineNumber+" -> Input = "+count+" ----ERROR: Cannot generate result. Negative input."+System.lineSeparator());
					return 1;
				}
				if(count>20)
					output.append("Line "+lineNumber+" -> Input = "+count+" ----WARNING: Input > 20"+System.lineSeparator());
				ArrayList<Node> tempRemNodes = new ArrayList<Node>();	// To temporarily store removed nodes
				int totalCount = count;
				int validCount = totalCount;
				boolean invalidCount = false;		// Set to true if query > # keywords
				while(count!=0){
					Node node2BInserted = heap.removeMax();
					if(node2BInserted != null)		//	Check null (null when no nodes left in heap)
						tempRemNodes.add(node2BInserted);
					else{
						invalidCount = true;		// Query > # keywords
						--validCount;
					}
					--count;
				}
				if(invalidCount == true)
					output.append("Line "+lineNumber+" -> Input = "+totalCount+" ----WARNING: Only "+validCount+" keywords exist!"+System.lineSeparator());
				for(Node node: tempRemNodes)
					heap.insert(node);			// Insert removed nodes
				for(int i=0; i<tempRemNodes.size(); i++){
					for(Map.Entry<String, Node> entry : ht.entrySet()){		//	For each of the removed node
						if(entry.getValue() == tempRemNodes.get(i)){		//	Scan the hash table to get its value(Keyword)
							output.append(entry.getKey()+",");
						}
					}
				}
				output.deleteCharAt(output.length()-1);						// Remove the trailing comma
				output.append(System.lineSeparator());
				return 1;
			} catch (NumberFormatException e) {
				if(line.length()>0)				// If the line was invalid, we will reach here.
					output.append("Line "+lineNumber+" -> Input = "+line+" ----ERROR: Invalid input"+System.lineSeparator());
				return -1;						// Unable to process the current line, but continue
			}
		}
	}
	
	private void writeOutputFile(){
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(outputFilePath));
			bw.write(output.toString());
			bw.close();
			System.out.println("Output > "+outputFilePath);
		} catch (IOException e) {
			System.out.println("I/O Error");
		}
	}
	
}
