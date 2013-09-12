package org.openmrs.module.hiemonitoring.handler;

import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.hiemonitoring.util.ReportingThread;



public class ReportSendingService {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	public void sendReport(String xml){
		
		String[] methd = new String[] { "POST", "SendReport" };
		
		TreeMap<String, String> parameters = new TreeMap<String, String>();
		
		log.error("Send report " + xml);
		
		Thread thread = new Thread(new ReportingThread(methd, xml,"","HIE Daily Report",parameters));
	    thread.setDaemon(true);
	    thread.start();
	}

}
