package com.brenthepburn.cs799.ca;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Iterator;

import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import methods.cryptography.RSA;
import methods.hash.MD5;

import com.brenthepburn.cs799.db.Cert;
import com.brenthepburn.cs799.db.CertException;
import com.brenthepburn.cs799.db.DBHelper;
import com.brenthepburn.cs799.mail.MailHelper;

import functions.FastExponentiation;

public class index extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private String error = null;
	private String success = null;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		DBHelper helper = DBHelper.getInstance();

		try {
			error = null;
			success = null;
			parseInput(request, helper);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (CertException e) {
			error = e.getMessage();
		} catch (NumberFormatException e) {
			error = "Invalid input please try your action again.";
		} catch (Exception e) {
			e.printStackTrace();
		}

		PrintWriter w = response.getWriter();

		w.println("<html>");
		w.println("<head></head>");
		w.println("<body>");

		if (error != null)
			w.print("<p style=\"color:red;font-weight: bold;\">" + error
					+ "</p>");

		if (success != null)
			w.print("<p style=\"color:green;font-weight: bold;\">" + success
					+ "</p>");

		w.println("<p><strong>CA's public modulus:" + Cert.rsa.getN()
				+ " and key:" + Cert.rsa.getEncryptionKey() + "</strong></p>");
		w.println("<p>Note: RSA is used to sign the first 32bits of the MD5 digest of your cert.");
		w.println("<br />");
		w.println("Certs will come in the following format: email,fName,lName,key,modulus,timestamp,signature");
		w.println("</p>");

		w.println("<form id=\"add\" method=\"POST\">");
		w.println("<strong>Request your own public cert:</strong><br/>");
		w.println("First Name: <input type=\"text\" name=\"fName\"/><br/>");
		w.println("Last Name: <input type=\"text\" name=\"lName\"/><br/>");
		w.println("Email: <input type=\"text\" name=\"email\"/><br/>");
		w.println("Modulus (n): <input type=\"text\" name=\"modulus\"/><br/>");
		w.println("Public encryption key (e): <input type=\"text\" name=\"encryptionKey\"/><br/>");
		w.println("<input type=\"submit\" value=\"Submit\"/><br/>");
		w.println("</form>");
		w.println("</body>");

		w.println("<form id=\"deleteform\" method=\"POST\">");
		try {
			Iterator<Cert> i = helper.getAllCerts().iterator();

			if (i.hasNext()) {
				w.println("<script type=\"text/javascript\">");
				w.println("function submitform(email)");
				w.println("{");
				w.println("var elem = document.getElementById(\"delete\");");
				w.println("elem.value = email;");
				w.println("var form = document.getElementById(\"deleteform\");");
				w.println("form.submit();");
				w.println("}");
				w.println("</script>");

				w.println("<p><strong>To delete your existing cert enter in your private values and click \"Remove\" next to your email.</strong>");
				w.println("<input type=\"hidden\" id=\"delete\" name=\"delete\"/><br/>");
				w.println("p: <input type=\"text\" name=\"p\"/><br/>");
				w.println("q: <input type=\"text\" name=\"q\"/><br/>");
			}

			if (i.hasNext()) {
				w.println("<strong>Existing certs:</strong><br/>");
			}

			while (i.hasNext()) {
				Cert cert = i.next();
				w.println(cert.getEmail()
						+ " <a href=\"#\" onclick=\"submitform('"
						+ cert.getEmail() + "');\">Remove</a><br/>");
			}

			w.println("</p>");
			w.println("</form>");

			w.println("<form id=\"sendemail\" method=\"POST\">");
			w.println("<strong>Send message to user:</strong><br/>");
			w.println("To (email): <input type=\"text\" name=\"toemail\"/><br/>");
			w.println("Your cert: <input type=\"text\" name=\"fromcert\" size=\"70\"/><br/>");
			w.println("Message: <input type=\"text\" name=\"message\" size=\"100\"/><br/>");
			w.println("Message Signature: <input type=\"text\" name=\"messageSig\" size=\"100\"/><br/>");
			w.println("<input type=\"submit\" value=\"Send\"/><br/>");
			w.println("</form>");

			w.println("<form id=\"verifyCert\" method=\"POST\">");
			w.println("<strong>Verify Signature on Cert:</strong><br/>");
			w.println("Cert: <input type=\"text\" name=\"cert\" size=\"70\"/><br/>");
			w.println("<input type=\"submit\" value=\"Verify\"/><br/>");
			w.println("</form>");

			w.println("<form id=\"signMessge\" method=\"POST\">");
			w.println("<strong>Sign a message:</strong><br/>");
			w.println("Message: <input type=\"text\" name=\"signMsg\" size=\"70\"/><br/>");
			w.println("Private p: <input type=\"text\" name=\"signP\"/><br/>");
			w.println("Pricate q: <input type=\"text\" name=\"signQ\"/><br/>");
			w.println("Encryption Key: <input type=\"text\" name=\"signE\"/><br/>");
			w.println("<input type=\"submit\" value=\"Sign\"/><br/>");
			w.println("</form>");

			w.println("<form id=\"verifyMessge\" method=\"POST\">");
			w.println("<strong>Verify Signature on message:</strong><br/>");
			w.println("Sender cert: <input type=\"text\" name=\"sentCert\" size=\"70\"/><br/>");
			w.println("Message: <input type=\"text\" name=\"sentMsg\" size=\"70\"/><br/>");
			w.println("Message signature: <input type=\"text\" name=\"sentSig\" size=\"70\"/><br/>");
			w.println("<input type=\"submit\" value=\"Verify\"/><br/>");
			w.println("</form>");

			w.println("</body>");

		} catch (SQLException e) {
			e.printStackTrace();
		}

		w.println("</html>");
	}

	private void parseInput(HttpServletRequest request, DBHelper dbHelper)
			throws Exception, CertException, NumberFormatException {

		//
		// ADD Cert section
		//

		Cert cert = new Cert();
		cert.setfName(request.getParameter("fName"));
		cert.setlName(request.getParameter("lName"));
		cert.setEmail(request.getParameter("email"));
		cert.setModulus(request.getParameter("modulus"));
		cert.setKey(request.getParameter("encryptionKey"));
		if (cert.check()) {
			try {
				new InternetAddress(cert.getEmail());
				new BigInteger(cert.getModulus());
				new BigInteger(cert.getKey());
				dbHelper.saveCert(cert);
				cert.sign();
				MailHelper.getInstance().mailMessage(cert.getEmail(),
						"You cert for CS799", cert.toString(true));
				success = "Cert generated and emailed to " + cert.getEmail()
						+ ".";
			} catch (Exception e) {
				throw new CertException("Invalid information for new cert.");
			}
		}

		//
		// DELETE Cert section
		//

		String email = request.getParameter("delete");
		if (email != null) {
			email = email.trim();
			BigInteger p = new BigInteger(request.getParameter("p").trim());
			BigInteger q = new BigInteger(request.getParameter("q").trim());
			BigInteger n = p.multiply(q);
			cert = dbHelper.getCert(email);
			if (cert != null) {
				if (cert.getModulus().equals(n.toString())) {
					dbHelper.deleteCert(email);
					success = "Cert successfully deleted.";
				} else {
					throw new CertException(
							"Invalid p and q values.  Cert not removed.");
				}
			}
		}

		//
		// SEND message section
		//
		String message = request.getParameter("message");
		if (message != null) {
			message = message.trim();
			String toemail = request.getParameter("toemail").trim();
			String fromcert = request.getParameter("fromcert").trim();
			String messageSig = request.getParameter("messageSig").trim();
			if (message.length() < 0 || toemail.length() < 0
					|| fromcert.length() < 0 || messageSig.length() < 0) {
				throw new CertException(
						"Missing information can't send message.");
			} else {
				String[] values = fromcert.split(",");
				if (values.length == 7) {
					MailHelper.getInstance().mailMessage(
							toemail,
							"Message from CS799 teammate.",
							"Cert of sender:\n" + fromcert + "\n\nMessage:\n"
									+ message + "\n\nMessage Signature:\n"
									+ messageSig);
					success = "Message sent to email " + toemail;
				} else {
					throw new CertException(
							"Invlid cert format. Please use information below");
				}
			}
		}

		//
		// VERIFY Cert section
		//

		String certString = request.getParameter("cert");
		if (certString != null) {
			certString = certString.trim();
			String[] values = certString.split(",");
			if (values.length == 7) {
				MD5 md5 = new MD5();
				md5.setMessage(certString.replace("," + values[6], ""));
				byte[] digest = md5.calculate32BitDigest();
				BigInteger bigInt = new BigInteger(1, digest);

				BigInteger b = new BigInteger(values[6]);
				BigInteger e = Cert.rsa.getEncryptionKey();
				BigInteger n = Cert.rsa.getN();
				BigInteger value = FastExponentiation.fastExponentiation(b, e,
						n);
				if (!bigInt.equals(value)) {
					throw new CertException("Invalid signature");
				} else {
					success = "The following cert is valid: " + certString;
				}
			} else {
				throw new CertException(
						"Invlid cert format. Please use information below");
			}
		}

		//
		// VERIFY Message Signature section
		//

		String sentCert = request.getParameter("sentCert");
		if (sentCert != null) {
			sentCert = sentCert.trim();
			String sentMsg = request.getParameter("sentMsg").trim();
			String sentSig = request.getParameter("sentSig").trim();
			String[] values = sentCert.split(",");
			if (values.length == 7) {
				String keyString = values[3];
				String modulusString = values[4];

				MD5 md5 = new MD5();
				md5.setMessage(sentMsg);
				byte[] bytes = md5.calculate32BitDigest();
				BigInteger hash = new BigInteger(bytes);

				BigInteger signature = new BigInteger(sentSig);
				BigInteger key = new BigInteger(keyString);
				BigInteger modulus = new BigInteger(modulusString);
				BigInteger sigHash = FastExponentiation.fastExponentiation(
						signature, key, modulus);
				if (!hash.equals(sigHash)) {
					throw new CertException("Invalid signature on message");
				} else {
					success = "The following signature on the message is valid.";
				}
			} else {
				throw new CertException(
						"Invlid cert format. Please use information below");
			}
		}

		//
		// VERIFY Sign Message section
		//

		String signMsg = request.getParameter("signMsg");
		if (signMsg != null) {
			String signPStr = request.getParameter("signP").trim();
			String signQStr = request.getParameter("signQ").trim();
			String signEStr = request.getParameter("signE").trim();
			
			BigInteger p = new BigInteger(signPStr);
			BigInteger q = new BigInteger(signQStr);
			BigInteger e = new BigInteger(signEStr);
			
			MD5 md5 = new MD5();
			md5.setMessage(signMsg);
			byte[] digest = md5.calculate32BitDigest();
			BigInteger hash = new BigInteger(1, digest);
			
			RSA rsa = new RSA();
			rsa.setPrivateInfo(p, q);
			rsa.setPublicInfo(e, p.multiply(q));
			BigInteger sig = rsa.decrypt(hash);
			
			success = "The signature for the message is: " + sig;
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		doGet(request, response);
	}
}
