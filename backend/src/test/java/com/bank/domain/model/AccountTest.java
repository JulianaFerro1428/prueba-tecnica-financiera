package com.bank.domain.model;

import com.bank.domain.exception.AccountBalanceNotZeroException;
import com.bank.domain.exception.CancelledAccountException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 30, 10, 0);
    private static final AccountNumber SAVINGS_NUMBER = new AccountNumber("5300000001");
    private static final AccountNumber CHECKING_NUMBER = new AccountNumber("3300000001");

    @Test
    void open_savingsAccount_startsActiveWithZeroBalance() {
        Account account = Account.open(AccountType.SAVINGS, SAVINGS_NUMBER, 7L, false, NOW);

        assertThat(account.getId()).isNull();
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.getBalance()).isEqualByComparingTo("0");
        assertThat(account.getAvailableBalance()).isEqualByComparingTo("0");
        assertThat(account.getClientId()).isEqualTo(7L);
        assertThat(account.getCreatedAt()).isEqualTo(NOW);
        assertThat(account.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void open_checkingAccount_alsoStartsActive() {
        Account account = Account.open(AccountType.CHECKING, CHECKING_NUMBER, 7L, true, NOW);

        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.isGmfExempt()).isTrue();
    }

    @Test
    void open_numberThatDoesNotMatchType_isRejected() {
        assertThatThrownBy(() -> Account.open(AccountType.SAVINGS, CHECKING_NUMBER, 7L, false, NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void open_withoutClient_isRejected() {
        assertThatThrownBy(() -> Account.open(AccountType.SAVINGS, SAVINGS_NUMBER, null, false, NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deactivateAndActivate_changeStatusAndModificationDate() {
        Account account = Account.open(AccountType.SAVINGS, SAVINGS_NUMBER, 7L, false, NOW);

        account.deactivate(NOW.plusDays(1));
        assertThat(account.getStatus()).isEqualTo(AccountStatus.INACTIVE);
        assertThat(account.getUpdatedAt()).isEqualTo(NOW.plusDays(1));

        account.activate(NOW.plusDays(2));
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.getUpdatedAt()).isEqualTo(NOW.plusDays(2));
        assertThat(account.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void cancel_withZeroBalance_cancelsAccount() {
        Account account = Account.open(AccountType.SAVINGS, SAVINGS_NUMBER, 7L, false, NOW);

        account.cancel(NOW.plusDays(1));

        assertThat(account.getStatus()).isEqualTo(AccountStatus.CANCELLED);
    }

    @Test
    void cancel_withBalance_isRejectedAndKeepsStatus() {
        Account account = accountWithBalance("50000.00");

        assertThatThrownBy(() -> account.cancel(NOW.plusDays(1)))
                .isInstanceOf(AccountBalanceNotZeroException.class);
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void cancelledAccount_cannotBeActivatedOrDeactivated() {
        Account account = Account.open(AccountType.SAVINGS, SAVINGS_NUMBER, 7L, false, NOW);
        account.cancel(NOW.plusDays(1));

        assertThatThrownBy(() -> account.activate(NOW.plusDays(2)))
                .isInstanceOf(CancelledAccountException.class);
        assertThatThrownBy(() -> account.deactivate(NOW.plusDays(2)))
                .isInstanceOf(CancelledAccountException.class);
        assertThat(account.getStatus()).isEqualTo(AccountStatus.CANCELLED);
    }

    private static Account accountWithBalance(String amount) {
        BigDecimal balance = new BigDecimal(amount);
        return Account.restore(1L, AccountType.SAVINGS, SAVINGS_NUMBER, AccountStatus.ACTIVE,
                balance, balance, false, 7L, NOW, NOW);
    }
}
