package com.brenthepburn.cs799.ca;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Iterator;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.brenthepburn.cs799.db.Cert;
import com.brenthepburn.cs799.db.CertException;
import com.brenthepburn.cs799.db.DBHelper;
import com.brenthepburn.cs799.mail.MailHelper;

public class index extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		DBHelper helper = DBHelper.getInstance();
		String error = null;

		try {
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
			w.println("<input type=\"submit\" value=\"Send\"/><br/>");
			w.println("</form>");

			w.println("</body>");

		} catch (SQLException e) {
			e.printStackTrace();
		}

		w.println("</html>");
	}

	private void parseInput(HttpServletRequest request, DBHelper dbHelper)
			throws Exception, CertException, NumberFormatException {

		Cert cert = new Cert();
		cert.setfName(request.getParameter("fName"));
		cert.setlName(request.getParameter("lName"));
		cert.setEmail(request.getParameter("email"));
		cert.setModulus(request.getParameter("modulus"));
		cert.setKey(request.getParameter("encryptionKey"));

		if (cert.check()) {
			dbHelper.saveCert(cert);
			cert.sign();
			MailHelper.getInstance().mailMessage(cert.getEmail(),
					"You cert for CS799", cert.toString(true));
		}

		String email = request.getParameter("delete");
		if (email != null) {
			BigInteger p = new BigInteger(request.getParameter("p"));
			BigInteger q = new BigInteger(request.getParameter("q"));
			BigInteger n = p.multiply(q);
			cert = dbHelper.getCert(email);
			if (cert != null) {
				if (cert.getModulus().equals(n.toString()))
					dbHelper.deleteCert(email);
				else
					throw new CertException(
							"Invalid p and q values.  Cert not removed.");
			}
		}

		String message = request.getParameter("message");
		if (message != null) {
			String toemail = request.getParameter("toemail");
			String fromcert = request.getParameter("fromcert");

			String[] values = fromcert.split(",");
			if (values.length == 7) {
				MailHelper.getInstance().mailMessage(
						toemail,
						"Message from CS799 teammate.",
						"Cert of sender:\n" + fromcert + "\n\nMessage:\n"
								+ message);
			} else {
				throw new CertException(
						"Invlid cert format. Please use information below");
			}
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		doGet(request, response);
	}
}
