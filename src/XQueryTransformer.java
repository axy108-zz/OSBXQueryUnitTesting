
import java.io.File;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlBoolean;
import org.apache.xmlbeans.XmlDateTime;
import org.apache.xmlbeans.XmlDouble;
import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlString;
import org.w3c.dom.Node;

/**
 * @author By Alberto Alvarado: 
 * 
 * Execute an specific XQyery transformation using the same engine used by Oracle OSB.
 */
public class XQueryTransformer {
	private Map<String, Object> xqueryParametersMap;
	private String xQueryFilePath;

	private Boolean printTransformedXmlToConsoleBoolean;

	/**
	 * 
	 * @param logTransformedXmlToConsoleBoolean Set if the value that indicates if the transform method should 
	 * 								print to the console the transformed XML.<br>
	 * 								Default is value is false.
	 */
	public void setPrintTransformedXmlToConsoleBoolean(
			Boolean logTransformedXmlToConsoleBoolean) {
		this.printTransformedXmlToConsoleBoolean = logTransformedXmlToConsoleBoolean;
	}

	/**
	 * 
	 * @return Get the current value that indicates if the 
	 * 		transform method should print to the console the transformed XML.
	 */
	public Boolean getPrintTransformedXmlToConsoleBoolean() {
		return printTransformedXmlToConsoleBoolean;
	}
	
	private String encoding;

	/**
	 * @return Get the current encoding used to read the files passed as parameters.
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * @param encoding Set the current encoding used to read the files passed as parameters.
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	/**
	 * Initialize a new XQueryTransformer
	 * @param xqueryFilePath
	 * 						Path of the XQuery to execute.
	 * @param xqueryParameters
	 * 						Map that contains the parameters that will be sent to the XQuery.<br>
	 * 						The key of each parameter must be the name of the variable declared on the XQuery file.<br>
	 * 						The type of the value of the parameter must follow this conventions:<br>
	 * 							File -> XML file reference where the content for the variable will be extracted.<br>
	 * 							StringBuffer - > StringBuffer that contains an XML string that will be sent to the XQuery.<br>
	 * 							String -> regular string parameter, must be declare as external only in the XQuery transformer<br>
	 * 									  sample: "declare variable $var1 external;"<br>
	 * 									  if the variable is declare "as xs:string" it will throw an error at the transformation time.<br>
	 */
	public XQueryTransformer(String xqueryFilePath, Map<String, Object> xqueryParameters) {
		this.xqueryParametersMap = xqueryParameters;
		this.xQueryFilePath = xqueryFilePath;
		this.printTransformedXmlToConsoleBoolean = false;
		this.encoding = "UTF-8";
	}
	
	/**
	 * Execute the XQuery transformation indicated when the object was created.
	 * @return
	 * 		String that contains the result of the XQuery transformation.
	 * @throws Exception
	 */
	public String transform() throws Exception{
		XmlObject xmlObject = XmlObject.Factory.newInstance();
		XmlOptions options = new XmlOptions();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		Iterator<Entry<String, Object>> it = xqueryParametersMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Object> pairs = it.next();
			Object value = pairs.getValue();
			String key = pairs.getKey();
			if (value instanceof Date) {
				XmlDateTime dt = XmlDateTime.Factory.newInstance();
				dt.setDateValue((Date) value);
				paramMap.put(key, dt);
			}
			else if (value instanceof Boolean) {
				XmlBoolean b = XmlBoolean.Factory.newInstance();
				b.setBooleanValue((Boolean) value);
				paramMap.put(key, b);
			}
			else if (value instanceof Float || value instanceof Double) {
				XmlDouble xd = XmlDouble.Factory.newInstance();
				xd.setDoubleValue((Double) value);
				paramMap.put(key, xd);
			}
			else if (value instanceof String) {
				XmlString string = XmlString.Factory.newInstance();
				string.setStringValue(value.toString());
				paramMap.put(key, string);
			}
			else if (value instanceof Integer){
				XmlInteger i = XmlInteger.Factory.newInstance();
				i.setBigIntegerValue(BigInteger.valueOf(((Integer) value).intValue()));
				paramMap.put(key, i);
			}
			else if (value instanceof StringBuffer) {
				XmlObject inputXml = XmlObject.Factory.parse(value.toString());
				paramMap.put(key, this.getXmlObject(inputXml));
			}
			else if (value instanceof File) {
				String xmlFileContent = FileHelper.readFile(((File) value).getPath(), encoding);
				XmlObject inputXml = XmlObject.Factory.parse(xmlFileContent);
				paramMap.put(key, this.getXmlObject(inputXml));
			}			
		}
		String xqueryFileContent = FileHelper.readFile(this.xQueryFilePath, encoding);		
		options.setXqueryVariables(paramMap);
		
		XmlObject[] resultsObjects = xmlObject.execQuery(xqueryFileContent, options);
		
		String resultXMLString = resultsObjects[0].xmlText(new XmlOptions().setSavePrettyPrint().setSavePrettyPrintIndent(2));
		
		if (this.printTransformedXmlToConsoleBoolean) {
			System.out.println(resultXMLString);
		}
		
		return resultXMLString;
	}
	
	private XmlObject getXmlObject(XmlObject inputXml) throws Exception {
		Node firstNode = inputXml.getDomNode().getFirstChild();
		String firstNodeName = firstNode.getNodeName();
		String nsUri = firstNode.getNamespaceURI(); 
		XmlObject paramXml;
		XmlObject[] nodesSelected;
		//Check if the first node is using namespace
		if (firstNodeName.indexOf(":") >= 0) {
			String[] vals = firstNodeName.split(":");
			nodesSelected = inputXml.selectChildren(nsUri, vals[1]);
		}
		else {
			//if the XML has a namespace use it to retrieve the first node because 
			//that is the type of the element that needs to be sent to the XQuery transformation.
			if (nsUri != null && !nsUri.isEmpty()) {
				nodesSelected = inputXml.selectChildren(nsUri, firstNodeName);
			}
			else {
				nodesSelected = inputXml.selectChildren(new QName(firstNodeName));
			}					
		}
		if (nodesSelected.length > 0) {
			paramXml = nodesSelected[0];
		}
		else {
			throw new Exception("Could not load the first node, please verify the namespaces of the input XMLs");
		}
		return paramXml;
	}
}
