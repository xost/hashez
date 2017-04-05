package org.host43.gibloc;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.Set;

public class Check implements UAction{

  private Client cli;
  private DbDialog dbd;
  private boolean save;
  private boolean email;
  private String mailFrom;
  private String mailTo;
  private String smtpHost;
  private String smtpPort;
  private String mailSubject;

  private final Logger log=LogManager.getLogger(this.getClass());

  Check(CommandLine cl) throws BadParametersException, ClientNotFoundException {
    Config cfg=Config.getInstance(cl);
    save=cfg.save();
    email=cfg.email();
    dbd=DbDialog.getInstance(cfg.connection(),
        cfg.jdbcDriver(),
        cfg.username(),
        cfg.password());
    cli=new Client(cfg.cliName(),dbd);
    mailFrom=cfg.mailFrom();
    mailTo=cfg.mailTo();
    smtpHost=cfg.smtpHost();
    smtpPort=cfg.smtpPort();
    mailSubject=cfg.mailSubject();
    if(email&&
        (mailFrom==null||
            mailTo==null||
            smtpHost==null||
            smtpPort==null||
            mailSubject==null))
    {
      String errMsg="\"email\" flag is set, but email options are not configured.";
      log.error(errMsg);
      throw new RuntimeException(errMsg);
    }
  }

  @Override
  public void perform() {
    cli.recalculate();
    if(save)
      cli.updateFileSet();
    File.outFileSet(cli.getBadFiles(),System.out);
    if(email){
      sendMail();
    }
  }

  private void sendMail() {
    Properties props=new Properties();
    props.setProperty("mail.smtp.host",smtpHost);
    props.setProperty("mail.smtp.port",smtpPort);
    Session session=Session.getDefaultInstance(props);
    try{
      MimeMessage mMsg=new MimeMessage(session);
      mMsg.setFrom(new InternetAddress(mailFrom));
      mMsg.addRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
      mMsg.setSubject(mailSubject);

      String msg;
      Set<File> badFiles=cli.getBadFiles();
      if(badFiles.isEmpty()) {
        msg="Control is PASSED";
      }else{
        msg="Control is FAILED\n";
        msg+="bad files:\n";
        for(File file:badFiles){{
          msg+="\t"+file.getFileName()+"\n";
        }}
      }
      mMsg.setText(msg);
      Transport.send(mMsg);
    }catch(MessagingException e){
      log.error(e);
      log.info(e);
    }
  }
}
