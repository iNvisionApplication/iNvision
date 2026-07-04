package com.invision.web.Invision.event;

import com.invision.web.Invision.config.CustomUserDetails;
import com.invision.web.Invision.model.User;
import com.invision.web.Invision.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginEventListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final LoanService loanService;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        CustomUserDetails userDetails = (CustomUserDetails) event.getAuthentication().getPrincipal();
        User user = userDetails.getUser();
        loanService.checkAssetCollected(user);

    }


}