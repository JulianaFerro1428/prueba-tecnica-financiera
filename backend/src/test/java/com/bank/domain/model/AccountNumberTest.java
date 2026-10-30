package com.bank.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountNumberTest {

    @Test
    void generate_savingsAccount_startsWith53AndHasTenDigits() {
        AccountNumber number = AccountNumber.generate(AccountType.SAVINGS, 1);

        assertThat(number.value()).isEqualTo("5300000001");
    }

    @Test
    void generate_checkingAccount_startsWith33AndHasTenDigits() {
        AccountNumber number = AccountNumber.generate(AccountType.CHECKING, 12345);

        assertThat(number.value()).isEqualTo("3300012345");
    }

    @Test
    void generate_largestAllowedSequence_stillHasTenDigits() {
        AccountNumber number = AccountNumber.generate(AccountType.SAVINGS, 99_999_999);

        assertThat(number.value()).isEqualTo("5399999999").hasSize(10);
    }

    @Test
    void generate_sequenceOutOfRange_isRejected() {
        assertThatThrownBy(() -> AccountNumber.generate(AccountType.SAVINGS, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AccountNumber.generate(AccountType.SAVINGS, 100_000_000))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_invalidFormat_isRejected() {
        assertThatThrownBy(() -> new AccountNumber("12345"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AccountNumber("5512345678"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AccountNumber("53ABC45678"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void belongsTo_comparesPrefixWithAccountType() {
        AccountNumber savings = new AccountNumber("5300000001");

        assertThat(savings.belongsTo(AccountType.SAVINGS)).isTrue();
        assertThat(savings.belongsTo(AccountType.CHECKING)).isFalse();
    }
}
