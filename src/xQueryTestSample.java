import static org.junit.Assert.*;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

import org.junit.Test;

public class xQueryTestSample {

	@Test
	public void test() throws Exception {
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		File sampleXQueryFile = getFile("sampleXQuery.xq");
		parameters.put("books", this.getFile("book.xml"));
		parameters.put("myString", "Test String");
		parameters.put("myInteger", 150);
		parameters.put("myDouble", 355.69);
		parameters.put("myBool", true);
		parameters.put("myDate", new Date(6048000000L));
		
		XQueryTransformer transformer = new XQueryTransformer(sampleXQueryFile.getPath(), parameters);
		transformer.setPrintTransformedXmlToConsoleBoolean(true);
		String currentXML = transformer.transform();
		String expected = FileHelper.readFile(getFile("expected.xml").getPath(), "UTF-8");
		
		assertNotNull(currentXML);
		assertEquals(expected, currentXML);
	}
	
	private File getFile(String fileName){
		return new File(getClass().getResource(fileName).getFile());
	}

}
