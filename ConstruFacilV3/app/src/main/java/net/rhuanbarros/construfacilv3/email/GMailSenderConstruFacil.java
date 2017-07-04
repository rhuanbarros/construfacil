package net.rhuanbarros.construfacilv3.email;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import net.rhuanbarros.construfacilv3.R;
import net.rhuanbarros.construfacilv3.models.ItemLista;

import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by rhuanbarros on 02/07/2017.
 * Tutorial credits: https://medium.com/@ssaurel/how-to-send-an-email-with-javamail-api-in-android-2fc405441079
 * //PRECISA AUTORIZAR NO GMAIL ANTES: https://www.google.com/settings/security/lesssecureapps
 */

public class GMailSenderConstruFacil extends javax.mail.Authenticator {
    private static final String TAG = "GMailSenderConstruFacil";
    private String mailhost = "smtp.gmail.com";
    private String user = ""; //colocar email
    private String password = ""; //colocar senha do email
    private Session session;

    static {
        Security.addProvider(new JSSEProvider());
    }

    private List<ItemLista> lista;
    private Activity activity;

    public GMailSenderConstruFacil(List<ItemLista> lista, Activity activity) {
        this.lista = lista;
        this.activity = activity;

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", mailhost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        session = Session.getDefaultInstance(props, this);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    public synchronized void sendMail() throws Exception {
        String sender = user; //alterar isso quando alugar um servidor
        String recipients = user; //alterar isso quando alugar um servidor
        String subject = "Solicitação de orçamento";

        String textoExtraido = extrairTextoDoArquivo(R.raw.htmlcimacomaspasduplas);
        StringBuilder email = new StringBuilder(textoExtraido);

        Iterator<ItemLista> iterator = lista.iterator();
        while (iterator.hasNext()) {
            ItemLista item = iterator.next();
            StringBuilder htmlItem = new StringBuilder("<tr><td class=\"tg-dzk6\">");
            htmlItem.append(item.getDescricao());
            htmlItem.append("</td><td class=\"tg-dzk6\">");
            htmlItem.append(item.getQuantidade());
            htmlItem.append("</td></tr>");
            email.append(htmlItem);
        }
        textoExtraido = extrairTextoDoArquivo(R.raw.htmlbaixocomaspasduplas);
        email.append(textoExtraido);
        Utils.splitAndLog(TAG, email.toString());

        String body = email.toString();
        MimeMessage message = new MimeMessage(session);
        DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/html"));
        message.setSender(new InternetAddress(sender));
        message.setSubject(subject);
        message.setDataHandler(handler);

        if (recipients.indexOf(',') > 0)
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
        else
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));

        Transport.send(message);
    }


    private String extrairTextoDoArquivo(int arquivo) {
        byte[] buffer = new byte[0];
        try {
            InputStream is = activity.getResources().openRawResource(arquivo);
            buffer = new byte[is.available()];
            while (is.read(buffer) != -1) ;
        } catch (Exception e) {
            Log.e(TAG, "" + e.toString());
        }
        return new String(buffer);
    }
}
