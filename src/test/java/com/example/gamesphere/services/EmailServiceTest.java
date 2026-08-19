package com.example.gamesphere.services;

import com.example.gamesphere.config.EmailProperties;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock ObjectProvider<JavaMailSender> mailSenderProvider;
    @Mock JavaMailSender mailSender;

    @Test
    void disabledEmailDoesNotResolveOrUseMailSender() {
        EmailProperties properties = new EmailProperties();
        properties.setEnabled(false);
        EmailService service = new EmailService(mailSenderProvider, properties);

        service.sendWelcomeEmail(User.builder().email("user@mail.com").username("user").build());

        verify(mailSenderProvider, never()).getIfAvailable();
        verify(mailSender, never()).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
    }

    @Test
    void welcomeEmailUsesConfiguredSenderAndRecipient() {
        EmailProperties properties = new EmailProperties();
        properties.setEnabled(true);
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        EmailService service = new EmailService(mailSenderProvider, properties);
        ReflectionTestUtils.setField(service, "from", "noreply@gamesphere.test");

        service.sendWelcomeEmail(User.builder()
                .email("user@mail.com")
                .username("user")
                .firstName("Sabir")
                .build());

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getFrom()).isEqualTo("noreply@gamesphere.test");
        assertThat(captor.getValue().getTo()).containsExactly("user@mail.com");
        assertThat(captor.getValue().getSubject()).isEqualTo("Welcome to Gamesphere");
        assertThat(captor.getValue().getText()).contains("Hi Sabir");
    }

    @Test
    void testEmailFailsClearlyWhenEmailIsDisabled() {
        EmailProperties properties = new EmailProperties();
        properties.setEnabled(false);
        EmailService service = new EmailService(mailSenderProvider, properties);

        assertThatThrownBy(() -> service.sendTestEmail("recipient@mail.com"))
                .hasMessageContaining("MAIL_ENABLED=true");
    }
}
