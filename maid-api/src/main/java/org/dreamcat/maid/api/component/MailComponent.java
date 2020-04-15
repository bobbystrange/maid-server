package org.dreamcat.maid.api.component;

import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.web.mail.MailTransmitter;
import org.dreamcat.common.web.template.ThymeleafProcesser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;

/**
 * Create by tuke on 2020/3/11
 */
@Slf4j
@Component
public class MailComponent {
    @Value("${spring.mail.username}")
    private String from;
    @Resource
    private MailTransmitter mailTransmitter;
    @Resource
    private ThymeleafProcesser thymeleafProcesser;

    public void sendActiveRegisterMail(String email, String url) {
        Context context = new Context();
        context.setVariable("url", url);
        String content = thymeleafProcesser.process("templates/register-confirm", context);

        log.info("Prepare to send a active register mail to {}", email);
        long ts = System.currentTimeMillis();
        mailTransmitter.newOp()
                .from(from)
                .to(email)
                .subject("Active your account on Lovely Rita")
                .content(content)
                .send();
        ts = System.currentTimeMillis() - ts;
        log.info("Sent a active register mail to {}, total cost {} ms", email, ts);
    }

    public void sendPasswordResetMail(String email, String url) {
        Context context = new Context();
        context.setVariable("url", url);
        String content = thymeleafProcesser.process("templates/password-reset-confirm", context);

        log.info("Prepare to send a active register mail to {}", email);
        long ts = System.currentTimeMillis();
        mailTransmitter.newOp()
                .from(from)
                .to(email)
                .subject("Reset your password on Lovely Rita")
                .content(content)
                .send();
        ts = System.currentTimeMillis() - ts;
        log.info("Sent a active register mail to {}, total cost {} ms", email, ts);
    }
}
