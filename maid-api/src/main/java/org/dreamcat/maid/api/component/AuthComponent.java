package org.dreamcat.maid.api.component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.io.ImageUtil;
import org.dreamcat.common.util.RandomUtil;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.common.web.util.BeanCopierUtil;
import org.dreamcat.maid.api.config.AuthProperties;
import org.dreamcat.maid.api.controller.auth.ImageCodeQuery;
import org.dreamcat.maid.api.controller.auth.LoginQuery;
import org.dreamcat.maid.api.controller.auth.NonceQuery;
import org.dreamcat.maid.api.controller.auth.PasswordResetConfirmQuery;
import org.dreamcat.maid.api.controller.auth.PasswordResetQuery;
import org.dreamcat.maid.api.controller.auth.RegisterConfirmQuery;
import org.dreamcat.maid.api.controller.auth.RegisterQuery;
import org.dreamcat.maid.api.core.ImageCodeView;
import org.dreamcat.maid.api.core.PasswordResetEntity;
import org.dreamcat.maid.api.core.RegisterEntity;
import org.dreamcat.maid.api.service.AuthUserService;
import org.dreamcat.maid.api.service.AuthVerificationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

/**
 * Create by tuke on 2020/3/21
 */
@Slf4j
@AllArgsConstructor
@Service
public class AuthComponent<T> {
    private AuthUserService<T> authUserService;
    private MailComponent mailComponent;
    private AccessTokenComponent accessTokenComponent;
    private AuthProperties authProperties;
    private AuthVerificationService authVerificationService;
    private PasswordEncoder passwordEncoder;


    public RestBody<?> register(RegisterQuery query) {
        String username = query.getUsername();
        String email = query.getEmail();
        String imageCode = query.getImageCode();

        String cachedImageCode = authVerificationService.releaseImageCode(concatProof(query));
        if (!imageCode.equalsIgnoreCase(cachedImageCode)) {
            return RestBody.error("Incorrect image code");
        }
        if (authUserService.findByName(username) != null) {
            return RestBody.error("User %s alraedy exists", username);
        }

        RegisterEntity register = BeanCopierUtil.copy(query, RegisterEntity.class);
        register.setPassword(passwordEncoder.encode(query.getPassword()));
        register.setTimestamp(System.currentTimeMillis());

        String accessToken = accessTokenComponent.encode(register);
        try {
            mailComponent.sendActiveRegisterMail(email,
                    query.getRedirect() + "?accessToken=" + accessToken);
        } catch (Exception e) {
            log.error(e.getMessage());
            return RestBody.error("Failed to send a active register mail to %s", email);
        }
        try {
            authVerificationService.sealRegister(username, register);
        } catch (Exception e) {
            log.error(e.getMessage());
            return RestBody.error("Failed to register '%s' with mail %s", username, email);
        }
        return RestBody.ok();
    }

    public RestBody<?> registerConfirm(RegisterConfirmQuery query) {
        String username = query.getUsername();
        String accessToken = query.getAccessToken();
        RegisterEntity register;
        try {
            register = authVerificationService.releaseRegister(username);
        } catch (Exception e) {
            log.error(e.getMessage());
            return RestBody.error("Failed to active account '%s'", username);
        }
        if (register == null) {
            return RestBody.error("Unavailable access token for '%s'", username);
        }
        if (System.currentTimeMillis() - register.getTimestamp() > authProperties
                .getMaxAge().getRegisterAccessToken()) {
            return RestBody.error("Expired access token for '%s'", username);
        }
        if (accessTokenComponent.isInvalid(register, accessToken)) {
            return RestBody.error("Wrong access token for '%s'", username);
        }

        authUserService.createUser(username, register);
        return RestBody.ok();
    }

    public RestBody<?> login(LoginQuery query, Consumer<String> tokenSetter) {
        String username = query.getUsername();
        String password = query.getPassword();
        String imageCode = query.getImageCode();

        String cachedImageCode = authVerificationService.releaseImageCode(concatProof(query));
        if (!imageCode.equalsIgnoreCase(cachedImageCode)) {
            return RestBody.error("Incorrect image code");
        }

        T user = authUserService.findByName(username);
        if (user == null) {
            return RestBody.error("Wrong username or password");
        }
        String encodedPassword = authUserService.destructureAsEncodedPassword(user);

        if (!passwordEncoder.matches(password, encodedPassword)) {
            return RestBody.error("Wrong username or password");
        }

        String token = authUserService.generateToken(user);
        tokenSetter.accept(token);
        return RestBody.ok();
    }

    public RestBody<?> passwordReset(PasswordResetQuery query) {
        String username = query.getUsername();
        String email = query.getEmail();
        String imageCode = query.getImageCode();
        String cachedImageCode = authVerificationService.releaseImageCode(email);
        if (!imageCode.equalsIgnoreCase(cachedImageCode)) {
            return RestBody.error("Incorrect image code");
        }
        if (authUserService.findByName(username) == null) {
            return RestBody.error("User %s doesn't exist", username);
        }

        PasswordResetEntity passwordReset = new PasswordResetEntity();
        passwordReset.setPassword(passwordEncoder.encode(query.getNewPassword()));
        passwordReset.setTimestamp(System.currentTimeMillis());
        String accessToken = accessTokenComponent.encode(passwordReset);
        try {
            mailComponent.sendPasswordResetMail(email,
                    query.getRedirect() + "?accessToken=" + accessToken);
        } catch (Exception e) {
            log.error(e.getMessage());
            return RestBody.error("Failed to send a password reset mail to %s", email);
        }
        try {
            authVerificationService.sealPasswordReset(username, passwordReset);
        } catch (Exception e) {
            log.error(e.getMessage());
            return RestBody.error("Failed to reset the password of '%s' with mail %s", username, email);
        }
        return RestBody.ok();
    }

    public RestBody<?> passwordResetConfirm(PasswordResetConfirmQuery query) {
        String username = query.getUsername();
        String accessToken = query.getAccessToken();
        PasswordResetEntity passwordReset;
        try {
            passwordReset = authVerificationService.releasePasswordReset(username);
        } catch (Exception e) {
            log.error(e.getMessage());
            return RestBody.error("Failed to reset the password of '%s'", username);
        }
        if (passwordReset == null) {
            return RestBody.error("Unavailable access token for '%s'", username);
        }
        if (System.currentTimeMillis() - passwordReset.getTimestamp() > authProperties
                .getMaxAge().getPasswordResetAccessToken()) {
            return RestBody.error("Expired access token for '%s'", username);
        }
        String password = passwordReset.getPassword();
        if (!passwordEncoder.matches(query.getNewPassword(), password)) {
            return RestBody.error("Wrong access token or password for '%s'", username);
        }
        if (accessTokenComponent.isInvalid(passwordReset, accessToken)) {
            return RestBody.error("Wrong access token or password for '%s'", username);
        }

        T user = authUserService.findByName(username);
        if (user == null) {
            return RestBody.error("User %s doesn't exist", username);
        }
        authUserService.updateUserPassword(user, password);
        return RestBody.ok();
    }

    public RestBody<ImageCodeView> fetchImageCode(ImageCodeQuery query) {
        String imageCode = RandomUtil.choose72(6);
        long timestamp = System.currentTimeMillis();
        int nonce = RandomUtil.randi(1 << 30);
        String proof = concatProof(timestamp, query.getProof(), nonce);
        authVerificationService.sealImageCode(proof, imageCode);
        String source = ImageUtil.generateBase64ImageSource(
                imageCode, query.getWidth(), query.getHeight());

        ImageCodeView view = new ImageCodeView();
        view.setSource(source);
        view.setTimestamp(timestamp);
        view.setNonce(nonce);
        return RestBody.ok(view);
    }

    public String concatProof(long timestamp, String proof, int nonce) {
        return String.format("%d:%s:%d", timestamp, proof, nonce);
    }

    public String concatProof(NonceQuery query) {
        return String.format("%d:%s:%d", query.getTimestamp(), query.getProof(), query.getNonce());
    }
}
