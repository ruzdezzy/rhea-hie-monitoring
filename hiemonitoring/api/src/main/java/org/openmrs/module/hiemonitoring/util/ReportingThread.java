package org.openmrs.module.hiemonitoring.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.hiemonitoring.HIEMonitoringConstants;
import org.openmrs.module.rheapocadapter.RHEAConstants;
import org.openmrs.module.rheapocadapter.handler.ConnectionHandler;
import org.openmrs.module.rheapocadapter.handler.ResponseHandler;
import org.openmrs.module.rheapocadapter.transaction.ArchiveTransaction;
import org.openmrs.module.rheapocadapter.transaction.ErrorTransaction;
import org.openmrs.module.rheapocadapter.transaction.ProcessingTransaction;
import org.openmrs.module.rheapocadapter.transaction.Transaction;
//import org.openmrs.module.rheapocadapter.util.PatientNotInCRError;
import org.openmrs.scheduler.SchedulerConstants;

public class ReportingThread implements Runnable {

	private Log log = LogFactory.getLog(this.getClass());
	private ResponseHandler response = new ResponseHandler();
	private String[] methd;
	private String message;
	private String result = "";
	private String url = "";

	HttpURLConnection connection = null;
	// private Patient patient;
	private String transactionType;

	public ReportingThread(String[] methd, String message, String result,
			String transactionType, TreeMap<String, String> parameters) {
		super();
		this.methd = methd;
		this.message = message;
		this.result = result;
		this.transactionType = transactionType;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Context.openSession();
		if (!Context.isAuthenticated()) {
			log.info("Authenticating ...");
			authenticate();
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date d = new Date();

		User creator = Context.getUserService().getUserByUsername(
				Context.getAuthenticatedUser().getUsername());
		int sender = creator.getUserId();
		try {
			ConnectionHandler conn = new ConnectionHandler();
			String hostname = Context.getAdministrationService().getGlobalProperty(RHEAConstants.SERVER_HOSTNAME);
			
			url = hostname+Context.getAdministrationService().getGlobalProperty(HIEMonitoringConstants.REPORT_URL_NAME);
			
			log.error("URL >>> "+url);
			
            Date sendDateTime = new Date();
			String[] results = conn.callPostAndPut(url, message, methd[0]);
			Date receiveDateTime = new Date();

			Transaction transaction = new Transaction(sendDateTime, results[1],
					methd[0] + " " + url, sender);

			Transaction item = response.generateMessage(transaction,
					Integer.parseInt(results[0]), methd[0], receiveDateTime);

			if (item instanceof ArchiveTransaction) {
				item.setMessage(transactionType + " : " + sdf.format(d));
				result = "Sending Report succeded";
				log.error(result);
			} else if (item instanceof ProcessingTransaction) {
				item.setMessage(transactionType + " : " + sdf.format(d));
				result = "Sending Report failed, try again later";
				log.error(result);
			} else if (item instanceof ErrorTransaction) {
				item.setMessage(transactionType + " : " + sdf.format(d));
				result = "Sending Report failed, Contact Administrator";
				log.error(result);

			}
			response.handleResponse(item);

		} catch (KeyManagementException e) {
			Date sendDateTime = new Date();
			Date receiveDateTime = new Date();
			Transaction transaction = new Transaction(sendDateTime,
					e.getMessage(), methd[0] + " " + url, sender);
			Transaction item = response.generateMessage(transaction, 400,
					methd[0], receiveDateTime);
			log.error("KeyManagementException generated" + e.getMessage());
			response.handleResponse(item);
			;
		} catch (KeyStoreException e) {
			Date sendDateTime = new Date();
			Date receiveDateTime = new Date();
			Transaction transaction = new Transaction(sendDateTime,
					e.getMessage(), methd[0] + " " + url, sender);
			Transaction item = response.generateMessage(transaction, 400,
					methd[0], receiveDateTime);
			log.error("KeyStoreException generated" + e.getMessage());
			response.handleResponse(item);
			;
		} catch (NoSuchAlgorithmException e) {
			Date sendDateTime = new Date();
			Date receiveDateTime = new Date();
			Transaction transaction = new Transaction(sendDateTime,
					e.getMessage(), methd[0] + " " + url, sender);
			Transaction item = response.generateMessage(transaction, 400,
					methd[0], receiveDateTime);
			log.error("NoSuchAlgorithmException generated" + e.getMessage());
			response.handleResponse(item);
			;
		} catch (CertificateException e) {
			Date sendDateTime = new Date();
			Date receiveDateTime = new Date();
			Transaction transaction = new Transaction(sendDateTime,
					e.getMessage(), methd[0] + " " + url, sender);
			Transaction item = response.generateMessage(transaction, 400,
					methd[0], receiveDateTime);
			log.error("CertificateException generated" + e.getMessage());
			response.handleResponse(item);
			;
		} catch (TransformerFactoryConfigurationError e) {
			Date sendDateTime = new Date();
			Date receiveDateTime = new Date();
			Transaction transaction = new Transaction(sendDateTime,
					e.getMessage(), methd[0] + " " + url, sender);
			Transaction item = response.generateMessage(transaction, 400,
					methd[0], receiveDateTime);
			log.error("TransformerFactoryConfigurationError generated"
					+ e.getMessage());
			response.handleResponse(item);
			;
		} catch (SocketTimeoutException e) {
			Date sendDateTime = new Date();
			Date receiveDateTime = new Date();
			Transaction transaction = new Transaction(sendDateTime,
					e.getMessage(), methd[0] + " " + url, sender);
			Transaction item = response.generateMessage(transaction, 600,
					methd[0], receiveDateTime);
			log.error("SocketTimeoutException generated " + e.getMessage());
			response.handleResponse(item);
			;
		} catch (IOException e) {
			Date sendDateTime = new Date();
			Date receiveDateTime = new Date();
			Transaction transaction = new Transaction(sendDateTime,
					e.getMessage(), methd[0] + " " + url, sender);
			Transaction item = response.generateMessage(transaction, 600,
					methd[0], receiveDateTime);
			log.error("IOException generated " + e.getMessage());
			e.printStackTrace();
			response.handleResponse(item);
			;
		}
	}

	@SuppressWarnings("deprecation")
	protected void authenticate() {
		try {
			AdministrationService adminService = Context
					.getAdministrationService();
			Context.authenticate(
					adminService
							.getGlobalProperty(SchedulerConstants.SCHEDULER_USERNAME_PROPERTY),
					adminService
							.getGlobalProperty(SchedulerConstants.SCHEDULER_PASSWORD_PROPERTY));

		} catch (ContextAuthenticationException e) {
			log.error("Error authenticating user");
		}
	}

}
