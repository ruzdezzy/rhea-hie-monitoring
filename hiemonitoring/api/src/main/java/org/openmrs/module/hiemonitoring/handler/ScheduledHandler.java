package org.openmrs.module.hiemonitoring.handler;

import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.hiemonitoring.util.GenerateStats;
import org.openmrs.module.rheapocadapter.handler.EnteredHandler;
import org.openmrs.module.rheapocadapter.transaction.Transaction;
import org.openmrs.scheduler.tasks.AbstractTask;

public class ScheduledHandler extends AbstractTask {

	Log log = LogFactory.getLog(this.getClass());

	
	
/*	List<Transaction> archiveTransactions;
	

	List<Transaction> processingTransactions;

	List<Transaction> errorTransactions; 

	String impID ;
	
	@SuppressWarnings("unchecked")
	public ScheduledHandler() {
		super();
		archiveTransactions = (List<Transaction>) enteredHandler.getArchiveQueue();
		processingTransactions = (List<Transaction>) enteredHandler
				.getProcessingQueue();
		errorTransactions = (List<Transaction>) enteredHandler.getErrorQueue();
		impID = Context.getAdministrationService().getImplementationId()
				.getImplementationId().substring(impID.indexOf("rwanda") + 6);
		
	}*/
	@SuppressWarnings({ "deprecation", "unchecked" })
	@Override
	public void execute() {
		Context.openSession();
		if (!Context.isAuthenticated()) {
			log.info("Authenticating ...");
			authenticate();
		}
		
		EnteredHandler enteredHandler = new EnteredHandler();
		
		GenerateStats gs = new GenerateStats();
		
		List<Transaction> archiveTransactions = (List<Transaction>) enteredHandler.getArchiveQueue();
		List<Transaction> processingTransactions = (List<Transaction>) enteredHandler
				.getProcessingQueue();
		List<Transaction> errorTransactions = (List<Transaction>) enteredHandler.getErrorQueue();
		String impID = Context.getAdministrationService().getImplementationId()
				.getImplementationId();
				impID = impID.substring(impID.indexOf("rwanda") + 6);
		
		
		log.info("HIE Reporting Scheduled Service Started");
		
		Map<String, Long> responseTimes = gs.getResponseTimes(archiveTransactions,errorTransactions);
		long max = responseTimes.get("maximum");
		long min = responseTimes.get("minimum");
		long avg = responseTimes.get("average");
		
		Map<String, Integer> processingMap = gs.getQueueCounts(processingTransactions);
		Map<String, Integer> archiveMap = gs.getQueueCounts(archiveTransactions);
		Map<String, Integer> errorMap = gs.getQueueCounts(errorTransactions);
		
		String xml = gs.WriteXML(impID, min, max, avg, processingMap, archiveMap,
				errorMap);
		
		log.error("Message to be Sent >>> "+xml);
		
		ReportSendingService rps = new ReportSendingService();
		rps.sendReport(xml);
		
		
		Context.closeSession();

	}



}
