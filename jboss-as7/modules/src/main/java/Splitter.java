import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.apache.maven.plugin.assembly.model.Component;
import org.apache.maven.plugin.assembly.model.DependencySet;
import org.apache.maven.plugin.assembly.model.FileItem;
import org.apache.maven.plugin.assembly.model.FileSet;
import org.apache.maven.plugin.assembly.model.io.xpp3.ComponentXpp3Reader;
import org.apache.maven.plugin.assembly.model.io.xpp3.ComponentXpp3Writer;
import org.drools.lang.dsl.DSLMapParser.entry_return;

public class Splitter {

	static class Pointer {
		Component component;
		File location;
	}

	public static void main(String[] args) throws Exception {
		Map<String, Pointer> components = new HashMap<String, Pointer>();
		Component read = new ComponentXpp3Reader().read(new FileReader("src/main/resources/external/assembly-component.xml"));

		for (FileItem item : read.getFiles()) {
			getComponent(components, item).addFile(item);
		}

		for (FileSet item : read.getFileSets()) {
			getComponent(components, item).addFileSet(item);
		}

		for (DependencySet item : read.getDependencySets()) {
			getComponent(components, item).addDependencySet(item);
		}

		TreeSet<File> items = new TreeSet<File>();
		for (Entry<String, Pointer> comp : components.entrySet()) {
			File moduleDescr = new File(comp.getValue().location, "assembly-component.xml");
			new ComponentXpp3Writer().write(new FileOutputStream(moduleDescr), comp.getValue().component);
		}
	}

	private static Pointer getPointer(Map<String, Pointer> components, String outputDirectory) {
		if (!components.containsKey(outputDirectory)) {
			System.err.println("Unknown output directory " + outputDirectory);
		}
		return components.get(outputDirectory);
	}

	private static Component getComponent(Map<String, Pointer> components, DependencySet item) {
		return getPointer(components, item.getOutputDirectory()).component;
	}

	private static Component getComponent(Map<String, Pointer> components, FileSet item) {
		return getPointer(components, item.getOutputDirectory()).component;
	}


	private static Component getComponent(Map<String, Pointer> components, FileItem item) {
		if (!components.containsKey(item.getOutputDirectory())) {
			components.put(item.getOutputDirectory(), createPointer(item));
		}
		return getPointer(components, item.getOutputDirectory()).component;
	}


	static Pointer createPointer(FileItem item) {
		Pointer pointer = new Pointer();
		pointer.location = new File(item.getSource()).getParentFile();
		pointer.component = new Component();
		return pointer;
	}

}
