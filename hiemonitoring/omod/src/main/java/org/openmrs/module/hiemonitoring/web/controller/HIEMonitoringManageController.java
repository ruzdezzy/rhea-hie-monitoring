/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.hiemonitoring.web.controller;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.rheapocadapter.handler.EnteredHandler;
import org.openmrs.module.rheapocadapter.transaction.Transaction;
import org.openmrs.module.hiemonitoring.util.GenerateStats;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * The main controller.
 */
@Controller
public class HIEMonitoringManageController {

	protected final Log log = LogFactory.getLog(getClass());
	private GenerateStats gs = new GenerateStats();
	EnteredHandler enteredHandler = new EnteredHandler();

	

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/module/hiemonitoring/transactionStats", method = RequestMethod.GET)
	public void manage(ModelMap model) throws ParseException {

		

		

		/*List<Transaction> archiveTransactions = new ArrayList<Transaction>();
		archiveTransactions = (List<Transaction>) enteredHandler.getArchiveQueue();

		List<Transaction> processingTransactions = new ArrayList<Transaction>();
		processingTransactions = (List<Transaction>) enteredHandler
				.getProcessingQueue();

		List<Transaction> errorTransactions = new ArrayList<Transaction>();
		errorTransactions = (List<Transaction>) enteredHandler.getErrorQueue();

		String impID = Context.getAdministrationService().getImplementationId()
				.getImplementationId();
		impID = impID.substring(impID.indexOf("rwanda") + 6);
		
		
		Map<String, Long> responseTimes = gs.getResponseTimes(archiveTransactions,errorTransactions);


		gs.getEncounterStats();
		long max = responseTimes.get("maximum");
		long min = responseTimes.get("minimum");
		long avg = responseTimes.get("average");

		int archiveTotal = archiveTransactions.size();
		int processingTotal = processingTransactions.size();
		int errorTotal = errorTransactions.size();

		
		Map<String, Integer> processingMap = gs.getQueueCounts(processingTransactions);
		Map<String, Integer> archiveMap = gs.getQueueCounts(archiveTransactions);
		Map<String, Integer> errorMap = gs.getQueueCounts(errorTransactions);
		

		String xml = gs.WriteXML(impID, min, max, avg, processingMap, archiveMap,
				errorMap);

		model.addAttribute("user", Context.getAuthenticatedUser());
		model.addAttribute("archiveTotal", archiveTotal);
		model.addAttribute("processingTotal", processingTotal);
		model.addAttribute("errorTotal", errorTotal);
		model.addAttribute("max", max);
		model.addAttribute("avg", avg);
		model.addAttribute("min", min);
		model.addAttribute("pq", processingMap);
		model.addAttribute("site", impID);
		model.addAttribute("xml", xml);*/
	}

	
}
