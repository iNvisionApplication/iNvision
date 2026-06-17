package com.invision.web.Invision.event;

import com.invision.web.Invision.enums.NotificationReason;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class LoanRequestEvent {
    private final long requesterId;
    private final String managerOneEmail;
    private final String managerTwoEmail;
    private final String assetTitle;
    private final String requesterEmail;
}
