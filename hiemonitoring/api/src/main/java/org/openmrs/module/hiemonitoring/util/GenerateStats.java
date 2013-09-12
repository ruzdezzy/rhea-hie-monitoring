package org.openmrs.module.hiemonitoring.util;

import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.module.rheapocadapter.RHEAConstants;
import org.openmrs.module.rheapocadapter.handler.EnteredHandler;
import org.openmrs.module.rheapocadapter.transaction.ArchiveTransaction;
import org.openmrs.module.rheapocadapter.transaction.ErrorTransaction;
import org.openmrs.module.rheapocadapter.transaction.ProcessingTransaction;
import org.openmrs.module.rheapocadapter.transaction.Transaction;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class GenerateStats {

	EnteredHandler enteredHandler = new EnteredHandler();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	Date d = new Date();
	protected final Log log = LogFactory.getLog(getClass());

	public GenerateStats() {

	}

	public Map<String, Integer> getQueueCounts(List<Transaction> trans) {

		Map<String, Integer> map = new TreeMap<String, Integer>();
		int savePatient = 0;
		int updatePatient = 0;
		int saveEncounter = 0;

		List<Transaction> transactions = new ArrayList<Transaction>();
		transactions = getDaysTransactions(trans, "", "");

		for (Transaction tran : transactions) {

			if (tran instanceof ProcessingTransaction) {
				if (((ProcessingTransaction) tran).getMethod().equals("PUT")) {
					updatePatient++;
				}

				if (((ProcessingTransaction) tran).getMethod().equals("POST")
						&& tran.getUrl().contains("encounter")) {
					saveEncounter++;
				}
				if (((ProcessingTransaction) tran).getMethod().equals("POST")
						&& tran.getUrl().contains("patients")) {
					savePatient++;
				}
			} else {
				if (tran.getUrl().contains("PUT")) {
					updatePatient++;
				}

				if (tran.getUrl().contains("POST")
						&& tran.getUrl().contains("encounter")) {
					saveEncounter++;
				}
				if (tran.getUrl().contains("POST")
						&& tran.getUrl().contains("patients")) {
					savePatient++;
				}

			}

		}

		map.put("queueTotal", transactions.size());
		map.put("savePatient", savePatient);
		map.put("updatePatient", updatePatient);
		map.put("saveEncounter", saveEncounter);

		return map;
	}

	public String WriteXML(String fosaid, long minRes, long maxRes,
			long avgRes, Map<String, Integer> processingMap,
			Map<String, Integer> archiveMap, Map<String, Integer> errorMap) {

		try {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

			// Date d = sdf.parse("2013-08-26");

			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root element
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("report");
			doc.appendChild(rootElement);

			// date element
			Element date = (Element) doc.importNode(
					createNode("date", "", "", ""), true);
			date.appendChild(doc.createTextNode(sdf.format(d)));

			// site id element
			Element fosa = (Element) doc.importNode(
					createNode("siteId", "", "", ""), true);
			fosa.appendChild(doc.createTextNode(fosaid));

			// response Time indicator element
			Element resTimes = (Element) doc.importNode(
					createNode("indicator", "responseTimes", "", ""), true);
			Element min = (Element) doc.importNode(
					createNode("dataElement", "minResponseTimeMS", "int",
							"msec"), true);
			min.appendChild(doc.createTextNode(Long.toString(minRes)));
			Element max = (Element) doc.importNode(
					createNode("dataElement", "maxResponseTimeMS", "int",
							"msec"), true);
			max.appendChild(doc.createTextNode(Long.toString(maxRes)));
			Element avg = (Element) doc.importNode(
					createNode("dataElement", "avgResponseTimeMS", "int",
							"msec"), true);
			avg.appendChild(doc.createTextNode(Long.toString(avgRes)));

			resTimes.appendChild(min);
			resTimes.appendChild(max);
			resTimes.appendChild(avg);

			// end

			// archive queue indicator element
			Element archiveQueue = (Element) doc.importNode(
					createNode("indicator", "archiveQueueStats", "", ""), true);
			Element aTotal = (Element) doc.importNode(
					createNode("dataElement", "totalTransactions", "int", ""),
					true);
			aTotal.appendChild(doc.createTextNode(Integer.toString(archiveMap
					.get("queueTotal"))));
			Element aTotalSaveEncounter = (Element) doc
					.importNode(
							createNode("dataElement", "totalEncountersSaved",
									"int", ""), true);
			aTotalSaveEncounter.appendChild(doc.createTextNode(Integer
					.toString(archiveMap.get("saveEncounter"))));
			Element aTotalSavePatient = (Element) doc.importNode(
					createNode("dataElement", "totalPatientsSaved", "int", ""),
					true);
			aTotalSavePatient.appendChild(doc.createTextNode(Integer
					.toString(archiveMap.get("savePatient"))));
			Element aTotalUpdatePatient = (Element) doc
					.importNode(
							createNode("dataElement", "totalPatientsUpdated",
									"int", ""), true);
			aTotalUpdatePatient.appendChild(doc.createTextNode(Integer
					.toString(archiveMap.get("updatePatient"))));

			archiveQueue.appendChild(aTotal);
			archiveQueue.appendChild(aTotalSaveEncounter);
			archiveQueue.appendChild(aTotalSavePatient);
			archiveQueue.appendChild(aTotalUpdatePatient);
			// end

			// error queue indicator element
			Element errorQueue = (Element) doc.importNode(
					createNode("indicator", "errorQueueStats", "", ""), true);
			Element eTotal = (Element) doc.importNode(
					createNode("dataElement", "totalTransactions", "int", ""),
					true);
			eTotal.appendChild(doc.createTextNode(Integer.toString(errorMap
					.get("queueTotal"))));
			Element eTotalSaveEncounter = (Element) doc
					.importNode(
							createNode("dataElement", "totalEncountersSaved",
									"int", ""), true);
			eTotalSaveEncounter.appendChild(doc.createTextNode(Integer
					.toString(errorMap.get("saveEncounter"))));
			Element eTotalSavePatient = (Element) doc.importNode(
					createNode("dataElement", "totalPatientsSaved", "int", ""),
					true);
			eTotalSavePatient.appendChild(doc.createTextNode(Integer
					.toString(errorMap.get("savePatient"))));
			Element eTotalUpdatePatient = (Element) doc
					.importNode(
							createNode("dataElement", "totalPatientsUpdated",
									"int", ""), true);
			eTotalUpdatePatient.appendChild(doc.createTextNode(Integer
					.toString(errorMap.get("updatePatient"))));

			errorQueue.appendChild(eTotal);
			errorQueue.appendChild(eTotalSaveEncounter);
			errorQueue.appendChild(eTotalSavePatient);
			errorQueue.appendChild(eTotalUpdatePatient);

			// end

			// processing queue indicator element
			Element processingQueue = (Element) doc.importNode(
					createNode("indicator", "processingQueueStats", "", ""),
					true);
			Element pTotal = (Element) doc.importNode(
					createNode("dataElement", "totalTransactions", "int", ""),
					true);
			pTotal.appendChild(doc.createTextNode(Integer
					.toString(processingMap.get("queueTotal"))));
			Element pTotalSaveEncounter = (Element) doc
					.importNode(
							createNode("dataElement", "totalEncountersSaved",
									"int", ""), true);
			pTotalSaveEncounter.appendChild(doc.createTextNode(Integer
					.toString(processingMap.get("saveEncounter"))));
			Element pTotalSavePatient = (Element) doc.importNode(
					createNode("dataElement", "totalPatientsSaved", "int", ""),
					true);
			pTotalSavePatient.appendChild(doc.createTextNode(Integer
					.toString(processingMap.get("savePatient"))));
			Element pTotalUpdatePatient = (Element) doc
					.importNode(
							createNode("dataElement", "totalPatientsUpdated",
									"int", ""), true);
			pTotalUpdatePatient.appendChild(doc.createTextNode(Integer
					.toString(processingMap.get("updatePatient"))));

			processingQueue.appendChild(pTotal);
			processingQueue.appendChild(pTotalSaveEncounter);
			processingQueue.appendChild(pTotalSavePatient);
			processingQueue.appendChild(pTotalUpdatePatient);

			// end
			rootElement.appendChild(date);
			rootElement.appendChild(fosa);
			rootElement.appendChild(resTimes);
			rootElement.appendChild(archiveQueue);
			rootElement.appendChild(errorQueue);
			rootElement.appendChild(processingQueue);

			// Encounter Type indicators
			Map<String, Integer[]> encounterStats = getEncounterStat();
			for (Map.Entry<String, Integer[]> e : encounterStats.entrySet()) {
				int ttl = e.getValue()[0];
				int ob = e.getValue()[1];
				int av = ob / ttl;
				int mx = e.getValue()[2];
				int mn = e.getValue()[3];

				Element parent = (Element) doc.importNode(
						createNode("indicator", e.getKey() + "-EncounterStats",
								"", ""), true);
				Element type = (Element) doc
						.importNode(
								createNode("dataElement", "encounterType",
										"string", ""), true);
				type.appendChild(doc.createTextNode(e.getKey()));
				Element total = (Element) doc
						.importNode(
								createNode("dataElement", "totalEncounters",
										"int", ""), true);
				total.appendChild(doc.createTextNode(Integer.toString(ttl)));
				Element avgobs = (Element) doc.importNode(
						createNode("dataElement", "avgObsCount", "int", ""),
						true);
				avgobs.appendChild(doc.createTextNode(Integer.toString(av)));

				Element maxobs = (Element) doc.importNode(
						createNode("dataElement", "maxObsCount", "int", ""),
						true);
				maxobs.appendChild(doc.createTextNode(Integer.toString(mx)));

				Element minobs = (Element) doc.importNode(
						createNode("dataElement", "minObsCount", "int", ""),
						true);
				minobs.appendChild(doc.createTextNode(Integer.toString(mn)));

				parent.appendChild(type);
				parent.appendChild(total);
				parent.appendChild(minobs);
				parent.appendChild(maxobs);
				parent.appendChild(avgobs);
				rootElement.appendChild(parent);

			}

			// end
			// populate root element

			// write out xml
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);
			StringWriter stw = new StringWriter();

			transformer.transform(source, new StreamResult(stw));

			return stw.toString();

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
			return null;
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
			return null;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public List<Transaction> getDaysTransactions(
			List<Transaction> transactions, String dateFrom, String dateTo) {
		try {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

			dateTo = "";
			dateFrom = "2013-08-28";
			Date today = new Date();
			dateFrom = df.format(today);

			if (!dateFrom.trim().isEmpty() && dateTo.trim().isEmpty()) {
				Iterator<Transaction> iterator = transactions.iterator();
				while (iterator.hasNext()) {
					if (iterator.next().getTimeRequestSent()
							.after(df.parse(dateFrom))) {

					} else {
						iterator.remove();
					}
				}

			} else if (dateFrom.trim().isEmpty() && !dateTo.trim().isEmpty()) {
				Iterator<Transaction> iterator = transactions.iterator();
				Date dt = increaseDate(df.parse(dateTo));
				while (iterator.hasNext()) {
					if (iterator.next().getTimeRequestSent().before(dt)) {

					} else {
						iterator.remove();
					}
				}

			} else if (!dateFrom.trim().isEmpty() && !dateTo.trim().isEmpty()) {
				Iterator<Transaction> iterator = transactions.iterator();
				Date dt = increaseDate(df.parse(dateTo));
				while (iterator.hasNext()) {
					Date date = iterator.next().getTimeRequestSent();
					if (date.after(df.parse(dateFrom)) && date.before(dt)) {

					} else {
						iterator.remove();
					}
				}

			}
			return transactions;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Map<String, Long> getResponseTimes(
			List<Transaction> archiveTransactions,
			List<Transaction> errorTransactions) {
		Map<String, Long> map = new TreeMap<String, Long>();
		// Long minimum = (long) 0;
		// Long maximum = (long) 0;
		Long average = (long) 0;

		Long sum = (long) 0;
		int totalTransactions = archiveTransactions.size()
				+ errorTransactions.size();

		Iterator<Transaction> arch = archiveTransactions.iterator();
		Iterator<Transaction> error = errorTransactions.iterator();
		List<Long> resTimes = new ArrayList<Long>();
		while (arch.hasNext()) {
			ArchiveTransaction at = (ArchiveTransaction) arch.next();
			long response = at.getTimeResponseReceived().getTime();
			long request = at.getTimeRequestSent().getTime();
			long diff = response - request;
			resTimes.add(diff);
			// maximum = Math.max(maximum, diff);
			sum += diff;

		}

		while (error.hasNext()) {
			ErrorTransaction et = (ErrorTransaction) error.next();
			long response = et.getResponseTimeReceived().getTime();
			long request = et.getTimeRequestSent().getTime();
			long diff = response - request;
			resTimes.add(diff);
			// maximum = Math.max(maximum, diff);
			sum += diff;

		}
		average = (sum / totalTransactions);

		map.put("minimum", Collections.min(resTimes));
		map.put("maximum", Collections.max(resTimes));
		map.put("average", average);

		return map;

	}

	public static Element createNode(String elementType, String elementName,
			String elementDataType, String elementUnits) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder;

			docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			Element node = doc.createElement(elementType);

			if (!elementName.equals("")) {
				Attr name = doc.createAttribute("name");
				name.setValue(elementName);
				node.setAttributeNode(name);
			}
			if (!elementDataType.equals("")) {
				Attr dataType = doc.createAttribute("dataType");
				dataType.setValue(elementDataType);
				node.setAttributeNode(dataType);
			}
			if (!elementUnits.equals("")) {
				Attr units = doc.createAttribute("units");
				units.setValue(elementUnits);
				node.setAttributeNode(units);
			}

			return node;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		// root elements

	}

	private Date increaseDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, 1); // minus number would decrement the days
		return cal.getTime();
	}

/*	public Map<String, Integer> getEncounterStats() {
		try {
			int average = 0;
			Map<String, Integer> encTypeCounts = new TreeMap<String, Integer>();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String[] allowedEncounterType = Context.getAdministrationService()
					.getGlobalProperty(RHEAConstants.ENCOUNTER_TYPE).split(",");

			List<EncounterType> encTypes = new ArrayList<EncounterType>();
			log.error("Global props >> " + allowedEncounterType.length);
			for (int i = 0; i < allowedEncounterType.length; i++) {
				EncounterType et = Context.getEncounterService()
						.getEncounterType(allowedEncounterType[i].trim());
				if (et != null)
					encTypes.add(et);

			}
			if (encTypes.size() != 0) {
				for (EncounterType t : encTypes)
					log.error(t.getName() + " <<<<<Name ID>>>> " + t.getId());
			}

			//Date d1 = sdf.parse("2013-08-28");
			// Date d2 = sdf.parse("2013-08-14");
			Date d1 = new Date();

			List<Encounter> enc = Context.getEncounterService().getEncounters(
					null, null, d1, null, null, encTypes, null, false);
			enc.get(0).getDateCreated();
			log.error("encounters created >>>>> " + enc.size());

			for (Encounter e : enc) {

				log.error(e.getEncounterType().getName()
						+ " <<< ENC-TYPE OBS>>> " + e.getAllObs().size());
				if (encTypeCounts.containsKey(e.getEncounterType().getName())) {
					int temp = encTypeCounts
							.get(e.getEncounterType().getName()) + 1;
					encTypeCounts.put(e.getEncounterType().getName(), temp);
				} else {
					encTypeCounts.put(e.getEncounterType().getName(), 1);
				}
			}

			return encTypeCounts;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}
*/
	public Map<String, Integer[]> getEncounterStat() {
		try {
			// int average = 0;
			// Map<String, Integer> encTypeCounts = new TreeMap<String,
			// Integer>();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String[] allowedEncounterType = Context.getAdministrationService()
					.getGlobalProperty(RHEAConstants.ENCOUNTER_TYPE).split(",");

			List<EncounterType> encTypes = new ArrayList<EncounterType>();
			log.error("Global props >> " + allowedEncounterType.length);
			for (int i = 0; i < allowedEncounterType.length; i++) {
				EncounterType et = Context.getEncounterService()
						.getEncounterType(allowedEncounterType[i].trim());
				if (et != null)
					encTypes.add(et);

			}
			if (encTypes.size() != 0) {
				for (EncounterType t : encTypes)
					log.error(t.getName() + " <<<<<Name ID>>>> " + t.getId());
			}

			//Date d1 = sdf.parse("2013-08-28");
			// Date d2 = sdf.parse("2013-08-14");
			Date d1 = new Date();

			List<Encounter> enc = Context.getEncounterService().getEncounters(
					null, null, d1, null, null, encTypes, null, false);
			enc.get(0).getDateCreated();
			log.error("encounters created >>>>> " + enc.size());

			Map<String, Integer[]> mp = new TreeMap<String, Integer[]>();

			for (Encounter e : enc) {

				int obs = e.getAllObs().size();
				if (mp.containsKey(e.getEncounterType().getName())) {
					Integer[] temp = mp.get(e.getEncounterType().getName());
					temp[0]++;
					temp[1] = temp[1] + obs;
					temp[2] = Math.max(obs, temp[2]);
					temp[3] = Math.min(obs, temp[3]);
					mp.put(e.getEncounterType().getName(), temp);
				} else {
					Integer[] temp = { 1, obs, obs, obs };
					mp.put(e.getEncounterType().getName(), temp);
				}
			}

			return mp;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

}
