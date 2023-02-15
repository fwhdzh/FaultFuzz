package edu.iscas.CCrashFuzzer;

import java.io.*;
import java.util.Arrays;

import org.w3c.dom.*;

import javax.xml.parsers.*;

public class MyXMLReader {//read jacoco results
	public static void read_trace_map(String covTrace) {
		CoverageCollector.trace_bits = new byte[Conf.MAP_SIZE / 8 + (Conf.MAP_SIZE % 8 == 0 ? 0 : 1)];
		Arrays.fill(CoverageCollector.trace_bits, (byte)0);
		File f = new File(covTrace);
		if(!f.getName().endsWith(".xml")) {
			return;
		}

		try {
			DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
			DocumentBuilder builder=factory.newDocumentBuilder();
			Document doc = builder.parse(f);
			
			int location = 0;
			NodeList packages = doc.getElementsByTagName("package");
			for (int i=0;i<packages.getLength();i++) {
				Node packageNode = packages.item(i);
				if (packageNode.getNodeType() == Node.ELEMENT_NODE) {
					Element pElement = (Element) packageNode;
					NodeList classes = pElement.getElementsByTagName("class");
					for (int j=0;j<classes.getLength();j++) {
						Node classNode = classes.item(j);
						if (classNode.getNodeType() == Node.ELEMENT_NODE) {
							Element cElement = (Element) classNode;
							String className = cElement.getAttribute("name");
							NodeList methods = cElement.getElementsByTagName("method");
							for (int k=0;k<methods.getLength();k++) {
								Node methodNode = methods.item(k);
								if (methodNode.getNodeType() == Node.ELEMENT_NODE) {
									Element mElement = (Element) methodNode;
									String methodName = mElement.getAttribute("name");
									String methodDesc = mElement.getAttribute("desc");
									boolean coveredMethod = false;
									int coveredComplexity = 0;
									NodeList counters = mElement.getElementsByTagName("counter");
									for (int c=0;c<counters.getLength();c++) {
										Node counterNode = counters.item(c);
										if (counterNode.getNodeType() == Node.ELEMENT_NODE) {
											Element ctElement = (Element) counterNode;
											String counterType = ctElement.getAttribute("type");
											if(counterType.equals("METHOD")) {
												int tmp = Integer.parseInt(ctElement.getAttribute("covered"));
												if(tmp == 1) {
													coveredMethod = true;
													break;
												}
											}
										}
									}
									if(coveredMethod) {
										CoverageCollector.trace_bits[location] = (byte) 1;
									}
									location++;
								}
							}
						}
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
