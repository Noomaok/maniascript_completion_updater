package noomaok;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Generator extends JPanel {
	String JsonFile;
	String ClassNames = "";
	String EnumNames = "";
	
	ArrayList<String> _classes;
	ArrayList<String> _primitives;
	ArrayList<String> _namespaces;
	
	JProgressBar pBar;
	JLabel currentAdding;
	int compteur = 0;
	
	/**
	 * Constructor of the Generator class
	 */
	public Generator() {
		try {
			Document docClasses = Jsoup.connect("https://maniaplanet.github.io/maniascript-reference/annotated.html").get();
			Document docNamespaces = Jsoup.connect("https://maniaplanet.github.io/maniascript-reference/namespaces.html").get();
			
			Elements linksClasses = docClasses.select("a");
			_classes = new ArrayList<String>();
			_primitives = new ArrayList<String>();
			
			for(Element link : linksClasses) {
				if(!link.attr("class").equals("")) {
					String linkString = link.attr("abs:href");
					if(linkString.contains("class"))
						_primitives.add(linkString);
					else
						_classes.add(linkString);
				}
			}

			Elements linksNamespaces = docNamespaces.select("a");
			_namespaces = new ArrayList<String>();

			for(Element link : linksNamespaces) {
				if(!link.attr("class").equals("")) {
					_namespaces.add(link.attr("abs:href"));
				}
			}
			
			this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			
			currentAdding = new JLabel("Adding :");
			add(currentAdding);
			
			pBar = new JProgressBar();
			pBar.setMinimum(0);
			pBar.setMaximum(_primitives.size()+_classes.size()+_namespaces.size());
			add(pBar);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Update the progress bar and the label
	 * @param name String to put in the label
	 */
	public void updateBar(String name) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				currentAdding.setText(name);
				pBar.setValue(compteur);
			}
		});
	}
	
	/**
	 * Do all the steps to add info to json file
	 * @return Return true if the file has correctly been created
	 */
	public Boolean generate() {
		addNamespacesInFile();
		addPrimitivesInFile();
		addCLassesInFile();
		return finishFile();
	}

	/**
	 * Function that add different namespaces to completion file
	 */
	public void addNamespacesInFile() {
		JsonFile = "{\n\t\"namespaces\": {";

		for(String url : _namespaces) {
			try {
				Document doc = Jsoup.connect(url).get();
				String name = (doc.title().split(" "))[2];
				updateBar("Adding : " + name);

				JsonFile += "\n\t\t\"" + name + "\": {";
				compteur++;

				Elements info = doc.select("tr");
				ArrayList<String> methods = new ArrayList<String>();
				ArrayList<String> enums = new ArrayList<String>();

				for(Element e : info) {
					if(e.attr("class").contains("memitem"))
					{
						if(!e.text().contains("enum")) {
							methods.add(e.text());
						}
						else {
							enums.add(e.text());
						}
					}
				}
				addNamespaceEnums(enums);
				addNamespaceMethods(methods);
				
				JsonFile += "\n\t\t},";
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		JsonFile = JsonFile.substring(0, JsonFile.length()-1);
		JsonFile += "\n\t},";
	}
	
	/**
	 * Function that add all the enums of a specific namespace
	 * @param enums
	 */
	public void addNamespaceEnums(ArrayList<String> enums) {
		JsonFile += "\n\t\t\t\"enums\": {";

		for(String currentEnum : enums) {
			String enumName = currentEnum.split(" ")[1];
			String[] values = currentEnum.substring(currentEnum.indexOf("{")+2, currentEnum.indexOf("}")-1).split(", ");

			JsonFile += "\n\t\t\t\t\"" + enumName + "\": [";
			for(String value : values) {
				JsonFile += "\n\t\t\t\t\t\"" + value + "\",";
			}

			JsonFile = JsonFile.substring(0, JsonFile.length()-1);
			JsonFile += "\n\t\t\t\t],";
		}

		if(enums.isEmpty()) {
			JsonFile += "},";
		}
		else {
			JsonFile = JsonFile.substring(0, JsonFile.length()-1);
			JsonFile += "\n\t\t\t},";
		}
	}

	/**
	 * Function that add all the methods of a specific namespace
	 * @param methods
	 */
	public void addNamespaceMethods(ArrayList<String> methods) {

		JsonFile += "\n\t\t\t\"methods\": [";

		for(String meth : methods) {
			String returnType = (meth.split(" "))[0];
			String methName = (meth.split(" "))[1];
			if(methName.contains("[Void]")) {
				returnType += "[Void]";
				methName = (meth.split(" "))[2];
			}
			String[] parameters = meth.substring(meth.indexOf('(')+1, meth.length()-1).split(", ");
			
			JsonFile += "\n\t\t\t\t{\n\t\t\t\t\t\"name\": \"" + methName + "\",\n\t\t\t\t\t\"returns\": \"" + returnType + "\",\n\t\t\t\t\t\"params\": [";
			
			if(!parameters[0].equals("")) {
				for (String t : parameters) {
					String ident = (t.split(" "))[0];
					String arg = (t.split(" "))[1];
					JsonFile += "\n\t\t\t\t\t\t{\n\t\t\t\t\t\t\t\"identifier\": \"" + ident + "\",\n\t\t\t\t\t\t\t\"argument\": \"" + arg + "\"\n\t\t\t\t\t\t},";
				}
				JsonFile = JsonFile.substring(0, JsonFile.length()-1);
				JsonFile += "\n\t\t\t\t\t";
			}
			JsonFile += "]\n\t\t\t\t},";
		}
		JsonFile = JsonFile.substring(0, JsonFile.length()-1);
		JsonFile += "\n\t\t\t]";
	}

	/**
	 * Function that add different types to completion file
	 */
	public void addPrimitivesInFile() {
		
		JsonFile += "\n\t\"primitives\": [";
		
		for(String url : _primitives) {
			try {
				Document doc = Jsoup.connect(url).get();
				String name = (doc.title().split(" "))[2];
				updateBar("Adding : " + name);
				
				JsonFile += "\n\t\t\"" + name + "\",";
				compteur++;
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		JsonFile = JsonFile.substring(0, JsonFile.length()-1);
		JsonFile += "\n\t],\n";
	}
	
	/**
	 * Function that add all the classes in the completion file
	 */
	public void addCLassesInFile() {
		
		JsonFile += "\t\"classes\": {";
		
		for(String url : _classes) {
			try {
				Document doc = Jsoup.connect(url).get();
				String nameClass = (doc.title().split(" "))[2];
				ClassNames += nameClass + "|";
				updateBar("Adding : " + nameClass);
				
				Elements info = doc.select("tr");
				
				ArrayList<String> metho = new ArrayList<String>();
				ArrayList<String> attri = new ArrayList<String>();
				ArrayList<String> enums = new ArrayList<String>();
				
				for(Element e : info) {
					if(e.attr("class").contains("memitem") && !(e.attr("class").contains("inherit"))) {
						String text = e.text();
						if (text.contains("(") && !text.contains("=")) {
							metho.add(text);
						} else if (text.contains("{")) {
							enums.add(text);
						} else {
							if(text.contains("const")) {
								text = text.substring(6, text.length());
							}
							attri.add(text);
						}
					}
				}
				
				
				String inherit;
				try {
					inherit = doc.select("area").first().attr("alt");
					if(nameClass.equals("CNod"))
						inherit = "";
				} catch(NullPointerException e) {
					inherit = "";
				}
				
				JsonFile += "\n\t\t\"" + nameClass + "\": {\n\t\t\t\"inherit\": \"" + inherit + "\",\n\t\t\t\"enums\": {";
				
				if(!enums.isEmpty()) {
					addEnums(enums);
					JsonFile = JsonFile.substring(0, JsonFile.length()-1);
					JsonFile += "\n\t\t\t";
				}
				JsonFile += "},\n\t\t\t\"props\": {";
				
				if(!attri.isEmpty()) {
					addProps(attri);
					JsonFile = JsonFile.substring(0, JsonFile.length()-1);
					JsonFile += "\n\t\t\t\t]\n\t\t\t";
				}
				JsonFile += "},\n\t\t\t\"methods\": [";
				
				if(!metho.isEmpty()) {
					addMethods(metho);
					JsonFile = JsonFile.substring(0, JsonFile.length()-1);
					JsonFile += "\n\t\t\t";
				}
				JsonFile += "]\n\t\t},";

				compteur++;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		JsonFile = JsonFile.substring(0, JsonFile.length()-1);
		JsonFile += "\n\t}\n}";
		
	}
	
	/**
	 * Function that add all the enum of a specific class
	 * @param enums ArrayList of all the strings needed
	 */
	public void addEnums(ArrayList<String> enums) {
		for(String enumString : enums) {
			String nameCurrent = enumString.substring(5, enumString.indexOf("{")-1);
			EnumNames += nameCurrent + "|";
			String[] values = enumString.substring(enumString.indexOf("{")+2, enumString.indexOf("}")-1).split(", ");
			
			JsonFile += "\n\t\t\t\t\"" + nameCurrent + "\": [";
			
			for(String val : values) {
				JsonFile += "\n\t\t\t\t\t\"" + val + "\",";
				EnumNames += val + "|";
			}
			
			JsonFile = JsonFile.substring(0, JsonFile.length()-1);
			JsonFile += "\n\t\t\t\t],";
		}
	}
	
	/**
	 * Function that add all the attributes of a specific class
	 * @param props ArrayList of all the strings needed
	 */
	public void addProps(ArrayList<String> props) {
		props.sort(null);
		String currentProp = (props.get(0).split(" "))[0];
		if(props.get(0).contains("[")) {
			JsonFile += "\n\t\t\t\t\"" + currentProp + "[]\": [";
			currentProp = currentProp + " []";
		} else {
			JsonFile += "\n\t\t\t\t\"" + currentProp + "\": [";
		}
		
		for(String processProp : props) {
			Boolean needSwitch = false;
			
			if(!processProp.contains(currentProp) || (processProp.contains("[") && !currentProp.contains("["))) needSwitch = true;
			
			if(needSwitch) {
				JsonFile = JsonFile.substring(0, JsonFile.length()-1);
				JsonFile += "\n\t\t\t\t],";
				currentProp = (processProp.split(" "))[0];
				if(processProp.contains("[")) {
					JsonFile += "\n\t\t\t\t\"" + currentProp + "[]\": [";
					currentProp = currentProp + " []";
				} else {
					JsonFile += "\n\t\t\t\t\"" + currentProp + "\": [";
				}
			}
			if(processProp.contains("[")) {
				JsonFile += "\n\t\t\t\t\t\"" + (processProp.split(" "))[2] + "\",";
			} else {
				JsonFile += "\n\t\t\t\t\t\"" + (processProp.split(" "))[1] + "\",";
			}
		}
	}
	
	/**
	 * Function that add all the methods of a specific class
	 * @param methods ArrayList of all the strings needed
	 */
	public void addMethods(ArrayList<String> methods) {
		for(String meth : methods) {
			String returnType = (meth.split(" "))[0];
			String methName = (meth.split(" "))[1];
			String[] parameters = meth.substring(meth.indexOf('(')+1, meth.length()-1).split(", ");
			
			JsonFile += "\n\t\t\t\t{\n\t\t\t\t\t\"name\": \"" + methName + "\",\n\t\t\t\t\t\"returns\": \"" + returnType + "\",\n\t\t\t\t\t\"params\": [";
			
			if(!parameters[0].equals("")) {
				for (String t : parameters) {
					String ident = (t.split(" "))[0];
					String arg = (t.split(" "))[1];
					JsonFile += "\n\t\t\t\t\t\t{\n\t\t\t\t\t\t\t\"identifier\": \"" + ident + "\",\n\t\t\t\t\t\t\t\"argument\": \"" + arg + "\"\n\t\t\t\t\t\t},";
				}
				JsonFile = JsonFile.substring(0, JsonFile.length()-1);
				JsonFile += "\n\t\t\t\t\t";
			}
			JsonFile += "]\n\t\t\t\t},";
		}
	}
	
	/**
	 * Function that output the final file
	 */
	public Boolean finishFile() {
		updateBar("Finishing");
		
		File outputFile = new File("completions.json");
		outputFile.delete();
		
		// File outputFile2 = new File("tmlanguage.txt");
		// outputFile2.delete();
		
		try {
			PrintWriter out = new PrintWriter("completions.json");
			out.println(JsonFile);
			out.close();
			// PrintWriter out2 = new PrintWriter("tmlanguage.txt");
			// out2.println(ClassNames);
			// out2.println(EnumNames);
			// out2.close();
			return true;
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}
